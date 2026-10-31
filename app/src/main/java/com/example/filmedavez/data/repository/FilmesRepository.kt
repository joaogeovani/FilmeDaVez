package com.example.filmedavez.data.repository

import com.example.filmedavez.data.remote.RetrofitClient
import com.example.filmedavez.data.remote.TmdbApiService
import com.example.filmedavez.data.remote.dto.FilmeDto
import com.example.filmedavez.data.remote.dto.RespostaFilmesDto
import com.example.filmedavez.model.AnalisadorSinopse
import com.example.filmedavez.model.FiltrosFilmes
import com.example.filmedavez.model.NormalizadorTemas
import com.example.filmedavez.data.remote.PaginadorTmdb
import com.example.filmedavez.data.remote.dto.DetalhesFilmeDto
import com.example.filmedavez.data.remote.dto.DisponibilidadePorPaisDto

class FilmesRepository(
    private val apiService: TmdbApiService = RetrofitClient.api
) {

    suspend fun buscarFilmesPorPreferencias(
        idsGeneros: List<Int>,
        palavrasChave: List<String>,
        filtros: FiltrosFilmes = FiltrosFilmes(),
        aoAtualizarProgresso: (Int, Int, String) -> Unit = { _, _, _ -> }
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

        // Armazena os filmes encontrados pelas palavras-chave
        val filmesFiltrados = mutableListOf<FilmeDto>()

        if (idsPalavrasChave.isNotEmpty()) {

            val filtroTemas = idsPalavrasChave.joinToString("|")

            PaginadorTmdb.percorrerPaginas(

                limitePaginas = 500,

                consultarPagina = { pagina ->
                    consultarPagina(
                        pagina = pagina,
                        idsTemas = filtroTemas
                    )
                },

                aoReceberFilmes = { filmesPagina ->
                    filmesFiltrados.addAll(filmesPagina)
                },

                aoAtualizarProgresso = { atual, total ->
                    aoAtualizarProgresso(
                        atual,
                        total,
                        "Consultando filmes por palavras-chave"
                    )
                }
            )
        }



        // Lista que armazenará os filmes encontrados
        val filmesPorGeneros = mutableListOf<FilmeDto>()

        // Consulta as páginas utilizando nosso paginador
        PaginadorTmdb.percorrerPaginas(

            // Mantemos três páginas apenas durante a integração
            limitePaginas = 500,

            // Realiza a consulta ao TMDB
            consultarPagina = { pagina ->
                consultarPagina(pagina = pagina)
            },

            // Adiciona os filmes de cada página à lista
            aoReceberFilmes = { filmesPagina ->
                filmesPorGeneros.addAll(filmesPagina)
            },

            // Comunica quantas páginas foram consultadas
            aoAtualizarProgresso = { atual, total ->
                aoAtualizarProgresso(
                    atual,
                    total,
                    "Consultando filmes por gêneros"
                )
            }
        )


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

        // Temporariamente mantemos dez resultados
        return filmesOrdenados.take(10)
    }

    // Busca as informações completas de um filme específico
    suspend fun buscarDetalhesFilme(
        filmeId: Int
    ): DetalhesFilmeDto {

        return apiService.buscarDetalhesFilme(
            filmeId = filmeId
        )
    }

    // Busca onde o filme está disponível no Brasil
    suspend fun buscarOndeAssistir(
        filmeId: Int
    ): DisponibilidadePorPaisDto? {

        val resposta = apiService.buscarOndeAssistir(
            filmeId = filmeId
        )

        // Seleciona apenas as plataformas do Brasil
        return resposta.results?.get("BR")
    }
}
