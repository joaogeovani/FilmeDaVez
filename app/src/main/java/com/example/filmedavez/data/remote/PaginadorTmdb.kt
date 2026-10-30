package com.example.filmedavez.data.remote

import com.example.filmedavez.data.remote.dto.FilmeDto
import com.example.filmedavez.data.remote.dto.RespostaFilmesDto
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import retrofit2.HttpException

data class ResultadoPaginacaoTmdb(
    val paginasConsultadas: Int,
    val totalPaginasInformadas: Int,
    val limiteDaApiAtingido: Boolean
)

object PaginadorTmdb {

    private const val LIMITE_PAGINAS_API = 500
    private const val INTERVALO_MS = 300L
    private const val MAX_TENTATIVAS = 3

    suspend fun percorrerPaginas(
        consultarPagina: suspend (Int) -> RespostaFilmesDto,
        aoReceberFilmes: (List<FilmeDto>) -> Unit,
        aoAtualizarProgresso: (Int, Int) -> Unit = { _, _ -> },
        limitePaginas: Int = 3

    ): ResultadoPaginacaoTmdb {

        // Consulta a primeira página
        val primeiraResposta = consultarComRetry(
            pagina = 1,
            consultarPagina = consultarPagina
        )

        val totalPaginas = primeiraResposta.totalPages
            .coerceAtLeast(0)

        // A API permite consultar até a página 500
        val ultimaPagina = minOf(
            totalPaginas.coerceAtLeast(1),
            limitePaginas.coerceIn(1, LIMITE_PAGINAS_API)
        )

        aoReceberFilmes(primeiraResposta.results)

        aoAtualizarProgresso(
            1,
            ultimaPagina
        )

        // Percorre todas as demais páginas disponíveis
        for (pagina in 2..ultimaPagina) {

            // Pequeno intervalo entre as requisições
            delay(INTERVALO_MS)

            // Permite cancelar a operação
            currentCoroutineContext().ensureActive()

            val resposta = consultarComRetry(
                pagina = pagina,
                consultarPagina = consultarPagina
            )

            aoReceberFilmes(resposta.results)

            aoAtualizarProgresso(
                pagina,
                ultimaPagina
            )
        }

        return ResultadoPaginacaoTmdb(
            paginasConsultadas = ultimaPagina,
            totalPaginasInformadas = totalPaginas,
            limiteDaApiAtingido =
                totalPaginas > LIMITE_PAGINAS_API
        )
    }

    private suspend fun consultarComRetry(
        pagina: Int,
        consultarPagina: suspend (Int) -> RespostaFilmesDto
    ): RespostaFilmesDto {

        for (tentativa in 1..MAX_TENTATIVAS) {

            currentCoroutineContext().ensureActive()

            try {
                return consultarPagina(pagina)

            } catch (erro: HttpException) {

                // Somente tenta novamente em caso de HTTP 429
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
            }
        }

        error("Não foi possível consultar a página $pagina")
    }
}
