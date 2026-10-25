package com.example.filmedavez.data.repository

import com.example.filmedavez.data.remote.RetrofitClient
import com.example.filmedavez.data.remote.TmdbApiService
import com.example.filmedavez.data.remote.dto.FilmeDto

class FilmesRepository(
    private val apiService: TmdbApiService = RetrofitClient.api
) {

    suspend fun buscarFilmesPorPreferencias(
        idsGeneros: List<Int>,
        palavrasChave: List<String>
    ): List<FilmeDto> {

        require(idsGeneros.isNotEmpty()) {
            "Selecione pelo menos um gênero."
        }

        // Monta o filtro de gêneros
        val filtroGeneros = idsGeneros
            .distinct()
            .joinToString("|")

        // Busca os IDs das palavras-chave no TMDB
        val idsPalavrasChave = palavrasChave
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .mapNotNull { palavra ->

                val resposta = apiService.pesquisarPalavrasChave(
                    texto = palavra
                )

                resposta.results
                    .firstOrNull {
                        it.name.equals(
                            palavra,
                            ignoreCase = true
                        )
                    }
                    ?.id
            }
            .distinct()

        // Busca filmes com gêneros e temas encontrados
        val filmesFiltrados = if (idsPalavrasChave.isNotEmpty()) {

            apiService.descobrirFilmes(
                generos = filtroGeneros,
                palavrasChave = idsPalavrasChave.joinToString("|")
            ).results

        } else {
            emptyList()
        }

        // Busca também filmes pelos gêneros escolhidos
        val filmesPorGeneros = apiService.descobrirFilmes(
            generos = filtroGeneros
        ).results

        // Guarda os IDs dos filmes encontrados pelos temas
        val idsFilmesPorTemas = filmesFiltrados
            .map { it.id }
            .toSet()

        val generosEscolhidos = idsGeneros.toSet()

        // Combina os resultados e calcula a compatibilidade
        val filmesOrdenados = (filmesFiltrados + filmesPorGeneros)
            .distinctBy { it.id }
            .sortedWith(
                compareByDescending<FilmeDto> { filme ->

                    val generosCorrespondentes =
                        filme.genreIds.count {
                            it in generosEscolhidos
                        }

                    val pontosGeneros = generosCorrespondentes * 2

                    val pontosTema = if (
                        filme.id in idsFilmesPorTemas
                    ) {
                        3
                    } else {
                        0
                    }

                    pontosGeneros + pontosTema
                }
                    .thenByDescending { filme ->
                        if (filme.voteCount >= 50) {
                            filme.voteAverage
                        } else {
                            0.0
                        }
                    }
                    .thenByDescending { filme ->
                        filme.voteCount
                    }
            )

        return filmesOrdenados.take(5)

    }
}

