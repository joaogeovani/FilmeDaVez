package com.example.filmedavez.data.repository

import com.example.filmedavez.data.remote.RetrofitClient
import com.example.filmedavez.data.remote.TmdbApiService
import com.example.filmedavez.data.remote.dto.FilmeDto
import com.example.filmedavez.data.remote.dto.RespostaFilmesDto
import com.example.filmedavez.model.AnalisadorSinopse
import com.example.filmedavez.model.FiltrosFilmes
import com.example.filmedavez.model.NormalizadorTemas

class FilmesRepository(
    private val apiService: TmdbApiService = RetrofitClient.api
) {

    suspend fun buscarFilmesPorPreferencias(
        idsGeneros: List<Int>,
        palavrasChave: List<String>,
        filtros: FiltrosFilmes = FiltrosFilmes()
    ): List<FilmeDto> {

        require(idsGeneros.isNotEmpty()) {
            "Selecione pelo menos um gênero."
        }

        val filtroGeneros = idsGeneros
            .distinct()
            .joinToString("|")

        // Centraliza os filtros de todas as consultas
        suspend fun consultarPagina(
            pagina: Int,
            idsTemas: String? = null
        ): RespostaFilmesDto {

            return apiService.descobrirFilmes(
                generos = filtroGeneros,
                palavrasChave = idsTemas,
                pagina = pagina,

                notaMinima = filtros.notaMinima,
                notaMaxima = filtros.notaMaxima,

                dataInicial = filtros.anoInicial?.let {
                    "$it-01-01"
                },

                dataFinal = filtros.anoFinal?.let {
                    "$it-12-31"
                },

                duracaoMinima = filtros.duracaoMinima,
                duracaoMaxima = filtros.duracaoMaxima,

                paisOrigem = filtros.paisOrigem
            )
        }

        // Identifica os IDs das palavras-chave no TMDB
        val idsPalavrasChave = palavrasChave
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .mapNotNull { palavra ->

                val termoBusca =
                    NormalizadorTemas.converterParaBusca(palavra)

                val resposta =
                    apiService.pesquisarPalavrasChave(
                        texto = termoBusca
                    )

                resposta.results
                    .firstOrNull {
                        it.name.equals(
                            termoBusca,
                            ignoreCase = true
                        )
                    }
                    ?.id
            }
            .distinct()

        // Filmes encontrados pelas palavras-chave
        val filmesFiltrados =
            if (idsPalavrasChave.isNotEmpty()) {

                consultarPagina(
                    pagina = 1,
                    idsTemas = idsPalavrasChave.joinToString("|")
                ).results

            } else {
                emptyList()
            }

        // Filmes encontrados pelos gêneros
        val primeiraPagina = consultarPagina(pagina = 1)

        val filmesPorGeneros =
            primeiraPagina.results.toMutableList()

        // Limite temporário: ampliaremos a paginação depois
        val ultimaPagina = minOf(
            3,
            primeiraPagina.totalPages
        )

        for (pagina in 2..ultimaPagina) {

            val resposta = consultarPagina(
                pagina = pagina
            )

            filmesPorGeneros.addAll(
                resposta.results
            )
        }

        // Identifica os filmes encontrados pelos temas
        val idsFilmesPorTemas = filmesFiltrados
            .map { it.id }
            .toSet()

        val generosEscolhidos = idsGeneros.toSet()

        val temasSolicitados = palavrasChave
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }

        // Remove filmes duplicados
        val candidatos = (
                filmesFiltrados + filmesPorGeneros
                ).distinctBy { it.id }

        // Mantém candidatos com relação temática identificada
        val candidatosRelevantes =
            if (temasSolicitados.isEmpty()) {

                candidatos

            } else {

                candidatos.filter { filme ->

                    val temasNaSinopse =
                        AnalisadorSinopse.contarTemasEncontrados(
                            sinopse = filme.overview.orEmpty(),
                            temas = temasSolicitados
                        )

                    temasNaSinopse > 0 ||
                            filme.id in idsFilmesPorTemas
                }
            }

        // Ordena pela pontuação de compatibilidade
        val filmesOrdenados =
            candidatosRelevantes.sortedWith(

                compareByDescending<FilmeDto> { filme ->

                    val generosCorrespondentes =
                        filme.genreIds.count {
                            it in generosEscolhidos
                        }

                    val temasNaSinopse =
                        AnalisadorSinopse.contarTemasEncontrados(
                            sinopse = filme.overview.orEmpty(),
                            temas = temasSolicitados
                        )

                    val correspondeAoTmdb =
                        filme.id in idsFilmesPorTemas

                    val pontosGeneros =
                        generosCorrespondentes * 2

                    val pontosSinopse =
                        temasNaSinopse * 4

                    val pontosTmdb =
                        if (correspondeAoTmdb) 3 else 0

                    pontosGeneros +
                            pontosSinopse +
                            pontosTmdb
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

        // Temporariamente mantemos cinco resultados
        return filmesOrdenados.take(10)
    }
}
