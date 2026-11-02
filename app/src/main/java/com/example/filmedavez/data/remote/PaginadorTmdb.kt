package com.example.filmedavez.data.remote

import com.example.filmedavez.data.remote.dto.FilmeDto
import com.example.filmedavez.data.remote.dto.RespostaFilmesDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import retrofit2.HttpException
import java.io.IOException

data class ResultadoPaginacaoTmdb(
    val paginasConsultadas: Int,
    val totalPaginasInformadas: Int,
    val limiteDaApiAtingido: Boolean
)

object PaginadorTmdb {

    private const val LIMITE_PAGINAS_API = 500

    // Quantidade de páginas distribuídas pelo catálogo
    private const val PAGINAS_AMOSTRADAS = 30

    // Prioriza também as primeiras páginas
    private const val PAGINAS_INICIAIS = 6

    // Evita sobrecarregar a API
    private const val REQUISICOES_SIMULTANEAS = 3

    private const val MAX_TENTATIVAS = 3

    suspend fun percorrerPaginas(
        consultarPagina: suspend (Int) -> RespostaFilmesDto,
        aoReceberFilmes: (List<FilmeDto>) -> Unit,
        aoAtualizarProgresso: (Int, Int) -> Unit = { _, _ -> },
        limitePaginas: Int = 500
    ): ResultadoPaginacaoTmdb {

        // Primeiro descobrimos o tamanho do catálogo
        // correspondente aos filtros informados.
        val primeiraResposta = consultarComRetry(
            pagina = 1,
            consultarPagina = consultarPagina
        )

        val totalPaginas = primeiraResposta.totalPages
            .coerceAtLeast(0)

        val ultimaPagina = minOf(
            totalPaginas.coerceAtLeast(1),
            limitePaginas.coerceIn(1, LIMITE_PAGINAS_API)
        )

        val paginasSelecionadas =
            selecionarPaginas(ultimaPagina)

        var paginasConcluidas = 1

        aoReceberFilmes(
            primeiraResposta.results.orEmpty()
        )

        aoAtualizarProgresso(
            paginasConcluidas,
            paginasSelecionadas.size
        )

        // Trabalha em pequenos lotes paralelos.
        // Os resultados são adicionados de maneira
        // sequencial, evitando alterações simultâneas
        // nas listas do Repository.
        val paginasRestantes = paginasSelecionadas.drop(1)

        for (
        lote in paginasRestantes.chunked(
            REQUISICOES_SIMULTANEAS
        )
        ) {

            currentCoroutineContext().ensureActive()

            val respostas = coroutineScope {

                lote.map { pagina ->

                    async {
                        consultarComRetry(
                            pagina = pagina,
                            consultarPagina = consultarPagina
                        )
                    }

                }.awaitAll()
            }

            respostas.forEach { resposta ->

                aoReceberFilmes(
                    resposta.results.orEmpty()
                )

                paginasConcluidas++

                aoAtualizarProgresso(
                    paginasConcluidas,
                    paginasSelecionadas.size
                )
            }
        }

        return ResultadoPaginacaoTmdb(
            paginasConsultadas = paginasConcluidas,
            totalPaginasInformadas = totalPaginas,
            limiteDaApiAtingido =
                totalPaginas > LIMITE_PAGINAS_API
        )
    }

    // Distribui as consultas por diferentes
    // regiões do catálogo, sem percorrer
    // todas as páginas sequencialmente.
    private fun selecionarPaginas(
        totalPaginas: Int
    ): List<Int> {

        if (totalPaginas <= PAGINAS_AMOSTRADAS) {
            return (1..totalPaginas).toList()
        }

        val primeiras = (
                1..minOf(PAGINAS_INICIAIS, totalPaginas)
                ).toList()

        val distribuidas = (
                0 until PAGINAS_AMOSTRADAS
                ).map { indice ->

                1 + (
                        (totalPaginas - 1) * indice /
                                (PAGINAS_AMOSTRADAS - 1)
                        )
            }

        return (primeiras + distribuidas)
            .distinct()
    }

    // Repete consultas temporariamente limitadas
    // pela API ou afetadas por falhas de rede.
    private suspend fun consultarComRetry(
        pagina: Int,
        consultarPagina: suspend (Int) -> RespostaFilmesDto
    ): RespostaFilmesDto {

        for (tentativa in 1..MAX_TENTATIVAS) {

            currentCoroutineContext().ensureActive()

            try {

                return consultarPagina(pagina)

            } catch (erro: HttpException) {

                if (
                    erro.code() != 429 ||
                    tentativa == MAX_TENTATIVAS
                ) {
                    throw erro
                }

                val segundosEspera = erro.response()
                    ?.headers()
                    ?.get("Retry-After")
                    ?.toLongOrNull()
                    ?.coerceAtLeast(1L)
                    ?: 5L

                delay(segundosEspera * 1000L)

            } catch (erro: IOException) {

                if (tentativa == MAX_TENTATIVAS) {
                    throw erro
                }

                delay(500L * tentativa)
            }
        }

        error(
            "Não foi possível consultar a página $pagina"
        )
    }
}
