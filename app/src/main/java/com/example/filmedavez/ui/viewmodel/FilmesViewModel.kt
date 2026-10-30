package com.example.filmedavez.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmedavez.data.remote.dto.FilmeDto
import com.example.filmedavez.data.repository.FilmesRepository
import com.example.filmedavez.model.FiltrosFilmes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Estados possíveis da pesquisa
sealed interface EstadoBuscaFilmes {

    data object Inicial : EstadoBuscaFilmes

    data object Carregando : EstadoBuscaFilmes

    data class Sucesso(
        val filmes: List<FilmeDto>
    ) : EstadoBuscaFilmes

    data object SemResultados : EstadoBuscaFilmes

    data object Cancelada : EstadoBuscaFilmes

    data class Erro(
        val mensagem: String
    ) : EstadoBuscaFilmes
}

// Informações sobre o andamento da paginação
data class ProgressoBusca(
    val paginaAtual: Int = 0,
    val totalPaginas: Int = 0,
    val etapa: String = "Preparando a pesquisa"
)
class FilmesViewModel(
    private val repository: FilmesRepository = FilmesRepository()
) : ViewModel() {

    private val _estado =
        MutableStateFlow<EstadoBuscaFilmes>(
            EstadoBuscaFilmes.Inicial
        )

    val estado: StateFlow<EstadoBuscaFilmes> =
        _estado.asStateFlow()

    // Progresso da pesquisa
    private val _progresso =
        MutableStateFlow(ProgressoBusca())

    val progresso: StateFlow<ProgressoBusca> =
        _progresso.asStateFlow()

    // Guarda a coroutine responsável pela pesquisa atual
    private var buscaAtual: Job? = null

    fun buscarFilmes(
        idsGeneros: List<Int>,
        palavrasChave: List<String>,
        filtros: FiltrosFilmes
    ) {
        // Cancela uma pesquisa anterior, se existir
        buscaAtual?.cancel()

        // Reinicia os indicadores
        _progresso.value = ProgressoBusca()
        _estado.value = EstadoBuscaFilmes.Carregando

        buscaAtual = viewModelScope.launch {

            try {

                val filmes = withContext(Dispatchers.IO) {

                    repository.buscarFilmesPorPreferencias(
                        idsGeneros = idsGeneros,
                        palavrasChave = palavrasChave,
                        filtros = filtros,

                        aoAtualizarProgresso = { atual, total, etapa ->
                            _progresso.value = ProgressoBusca(
                                paginaAtual = atual,
                                totalPaginas = total,
                                etapa = etapa
                            )
                        }
                    )
                }

                _estado.value = if (filmes.isEmpty()) {
                    EstadoBuscaFilmes.SemResultados
                } else {
                    EstadoBuscaFilmes.Sucesso(filmes)
                }

            } catch (e: CancellationException) {
                // Não transforma cancelamento em erro
                throw e

            } catch (e: HttpException) {
                _estado.value = EstadoBuscaFilmes.Erro(
                    "Erro HTTP ${e.code()} ao buscar filmes."
                )

            } catch (e: IOException) {
                _estado.value = EstadoBuscaFilmes.Erro(
                    "Verifique sua conexão com a internet."
                )

            } catch (e: Exception) {
                _estado.value = EstadoBuscaFilmes.Erro(
                    "Não foi possível buscar os filmes."
                )
            }
        }
    }

    fun cancelarBusca() {

        if (buscaAtual?.isActive == true) {

            buscaAtual?.cancel()

            _estado.value = EstadoBuscaFilmes.Cancelada
        }
    }
}
