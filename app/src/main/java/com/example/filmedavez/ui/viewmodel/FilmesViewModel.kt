package com.example.filmedavez.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmedavez.data.remote.dto.FilmeDto
import com.example.filmedavez.data.repository.FilmesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Representa os estados possíveis da busca
sealed interface EstadoBuscaFilmes {

    data object Inicial : EstadoBuscaFilmes

    data object Carregando : EstadoBuscaFilmes

    data class Sucesso(
        val filmes: List<FilmeDto>
    ) : EstadoBuscaFilmes

    data object SemResultados : EstadoBuscaFilmes

    data class Erro(
        val mensagem: String
    ) : EstadoBuscaFilmes
}

class FilmesViewModel(
    private val repository: FilmesRepository = FilmesRepository()
) : ViewModel() {

    private val _estado =
        MutableStateFlow<EstadoBuscaFilmes>(
            EstadoBuscaFilmes.Inicial
        )

    val estado: StateFlow<EstadoBuscaFilmes> =
        _estado.asStateFlow()

    fun buscarFilmes(
        idsGeneros: List<Int>,
        palavrasChave: List<String>
    ) {
        viewModelScope.launch {

            _estado.value = EstadoBuscaFilmes.Carregando

            try {
                val filmes =
                    repository.buscarFilmesPorPreferencias(
                        idsGeneros = idsGeneros,
                        palavrasChave = palavrasChave
                    )

                _estado.value = if (filmes.isEmpty()) {
                    EstadoBuscaFilmes.SemResultados
                } else {
                    EstadoBuscaFilmes.Sucesso(filmes)
                }

            } catch (e: CancellationException) {
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
}
