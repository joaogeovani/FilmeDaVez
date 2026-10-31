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
import com.example.filmedavez.data.remote.dto.DetalhesFilmeDto
import com.example.filmedavez.data.remote.dto.DisponibilidadePorPaisDto

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

// Estados da consulta de um filme específico
sealed interface EstadoDetalhesFilme {

    data object Inicial : EstadoDetalhesFilme

    data object Carregando : EstadoDetalhesFilme

    data class Sucesso(
        val detalhes: DetalhesFilmeDto
    ) : EstadoDetalhesFilme

    data class Erro(
        val mensagem: String
    ) : EstadoDetalhesFilme
}

// Estados da consulta de disponibilidade
sealed interface EstadoOndeAssistir {

    data object Inicial : EstadoOndeAssistir

    data object Carregando : EstadoOndeAssistir

    data class Sucesso(
        val filmeId: Int,
        val disponibilidade: DisponibilidadePorPaisDto
    ) : EstadoOndeAssistir

    data object SemInformacoes : EstadoOndeAssistir

    data class Erro(
        val mensagem: String
    ) : EstadoOndeAssistir
}

class FilmesViewModel(
    private val repository: FilmesRepository = FilmesRepository()
) : ViewModel() {

    // Estado da consulta dos detalhes
    private val _estadoDetalhes =
        MutableStateFlow<EstadoDetalhesFilme>(
            EstadoDetalhesFilme.Inicial
        )

    val estadoDetalhes: StateFlow<EstadoDetalhesFilme> =
        _estadoDetalhes.asStateFlow()

    // Estado da disponibilidade do filme
    private val _estadoOndeAssistir =
        MutableStateFlow<EstadoOndeAssistir>(
            EstadoOndeAssistir.Inicial
        )

    val estadoOndeAssistir: StateFlow<EstadoOndeAssistir> =
        _estadoOndeAssistir.asStateFlow()

    // Controla a consulta de disponibilidade
    private var ondeAssistirJob: Job? = null

    // Controla a consulta dos detalhes
    private var detalhesJob: Job? = null

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

    fun buscarDetalhesFilme(filmeId: Int) {

        // Cancela a consulta de detalhes anterior
        detalhesJob?.cancel()

        _estadoDetalhes.value = EstadoDetalhesFilme.Carregando

        detalhesJob = viewModelScope.launch {

            try {

                val detalhes = withContext(Dispatchers.IO) {
                    repository.buscarDetalhesFilme(
                        filmeId = filmeId
                    )
                }

                _estadoDetalhes.value =
                    EstadoDetalhesFilme.Sucesso(detalhes)

            } catch (e: CancellationException) {
                throw e

            } catch (e: HttpException) {

                _estadoDetalhes.value =
                    EstadoDetalhesFilme.Erro(
                        "Erro HTTP ${e.code()} ao carregar o filme."
                    )

            } catch (e: IOException) {

                _estadoDetalhes.value =
                    EstadoDetalhesFilme.Erro(
                        "Verifique sua conexão com a internet."
                    )

            } catch (e: Exception) {

                _estadoDetalhes.value =
                    EstadoDetalhesFilme.Erro(
                        "Não foi possível carregar os detalhes."
                    )
            }
        }
    }

    fun buscarOndeAssistir(filmeId: Int) {

        // Cancela a consulta anterior, se existir
        ondeAssistirJob?.cancel()

        _estadoOndeAssistir.value =
            EstadoOndeAssistir.Carregando

        ondeAssistirJob = viewModelScope.launch {

            try {

                val disponibilidade = withContext(Dispatchers.IO) {
                    repository.buscarOndeAssistir(
                        filmeId = filmeId
                    )
                }

                // Verifica se existem plataformas cadastradas
                val possuiOpcoes = disponibilidade?.let { dados ->
                    dados.flatrate.orEmpty().isNotEmpty() ||
                            dados.rent.orEmpty().isNotEmpty() ||
                            dados.buy.orEmpty().isNotEmpty() ||
                            dados.free.orEmpty().isNotEmpty() ||
                            dados.ads.orEmpty().isNotEmpty()
                } == true

                _estadoOndeAssistir.value =
                    if (disponibilidade != null && possuiOpcoes) {

                        EstadoOndeAssistir.Sucesso(
                            filmeId = filmeId,
                            disponibilidade = disponibilidade
                        )

                    } else {

                        EstadoOndeAssistir.SemInformacoes
                    }

            } catch (e: CancellationException) {

                // Não trata cancelamento como erro
                throw e

            } catch (e: HttpException) {

                _estadoOndeAssistir.value =
                    EstadoOndeAssistir.Erro(
                        "Erro HTTP ${e.code()} ao consultar plataformas."
                    )

            } catch (e: IOException) {

                _estadoOndeAssistir.value =
                    EstadoOndeAssistir.Erro(
                        "Verifique sua conexão com a internet."
                    )

            } catch (e: Exception) {

                _estadoOndeAssistir.value =
                    EstadoOndeAssistir.Erro(
                        "Não foi possível consultar onde assistir."
                    )
            }
        }
    }

    // Cancela e limpa a consulta de disponibilidade
    fun limparOndeAssistir() {

        ondeAssistirJob?.cancel()

        _estadoOndeAssistir.value =
            EstadoOndeAssistir.Inicial
    }

    // Cancela e limpa os detalhes anteriores
    fun limparDetalhesFilme() {

        detalhesJob?.cancel()

        _estadoDetalhes.value =
            EstadoDetalhesFilme.Inicial
    }

    fun cancelarBusca() {

        if (buscaAtual?.isActive == true) {

            buscaAtual?.cancel()

            _estado.value = EstadoBuscaFilmes.Cancelada
        }
    }
}
