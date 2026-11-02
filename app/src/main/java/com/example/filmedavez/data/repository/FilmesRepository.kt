package com.example.filmedavez.data.repository

import android.util.Log
import com.example.filmedavez.data.remote.PaginadorTmdb
import com.example.filmedavez.data.remote.RetrofitClient
import com.example.filmedavez.data.remote.TmdbApiService
import com.example.filmedavez.data.remote.dto.DetalhesFilmeDto
import com.example.filmedavez.data.remote.dto.DisponibilidadePorPaisDto
import com.example.filmedavez.data.remote.dto.FilmeDto
import com.example.filmedavez.data.remote.dto.RespostaFilmesDto
import com.example.filmedavez.model.AnalisadorSinopse
import com.example.filmedavez.model.FiltrosFilmes
import com.example.filmedavez.model.NormalizadorTemas
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.filmedavez.data.remote.dto.DatasLancamentoTmdbDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

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

        // Data atual no formato utilizado pela API do TMDB.
        val hoje = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(Date())

        // Respeita o ano final escolhido pelo usuário,
        // mas nunca permite lançamentos após hoje.
        val dataMaximaPermitida = minOf(
            filtros.anoFinal?.let { "$it-12-31" } ?: hoje,
            hoje
        )

        // Centraliza os filtros de todas as consultas.
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

                dataFinal = dataMaximaPermitida,

                duracaoMinima = filtros.duracaoMinima,
                duracaoMaxima = filtros.duracaoMaxima,

                paisOrigem = filtros.paisOrigem
            )
        }

        // Armazena os candidatos encontrados.
        // Essas listas ficam fora do timeout para
        // preservar os filmes já coletados.
        val filmesPorGeneros = mutableListOf<FilmeDto>()
        val filmesFiltrados = mutableListOf<FilmeDto>()

        // Reservamos 25 segundos para a coleta.
        // O restante será utilizado para a classificação
        // e verificação dos lançamentos recentes.
        val tempoMaximoBuscaMs = 25_000L

        val pesquisaConcluida = withTimeoutOrNull(
            tempoMaximoBuscaMs.milliseconds
        ) {

            // ETAPA 1:
            // Pesquisa primeiro pelos gêneros.
            // Assim já teremos uma base de candidatos.
            PaginadorTmdb.percorrerPaginas(
                limitePaginas = 500,

                consultarPagina = { pagina ->
                    consultarPagina(
                        pagina = pagina
                    )
                },

                aoReceberFilmes = { filmesPagina ->
                    filmesPorGeneros.addAll(filmesPagina)
                },

                aoAtualizarProgresso = { atual, total ->
                    aoAtualizarProgresso(
                        atual,
                        total,
                        "Consultando filmes por gêneros"
                    )
                }
            )

            // ETAPA 2:
            // Identifica os IDs das palavras-chave no TMDB.
            val idsPalavrasChave = palavrasChave
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinctBy { it.lowercase() }
                .mapNotNull { palavra ->

                    val termoBusca =
                        NormalizadorTemas.converterParaBusca(
                            palavra
                        )

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

            // ETAPA 3:
            // Complementa os candidatos com filmes
            // associados às palavras-chave.
            if (idsPalavrasChave.isNotEmpty()) {

                val filtroTemas =
                    idsPalavrasChave.joinToString("|")

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

            // Retorna true quando a coleta
            // termina antes dos 30 segundos.
            true

        } ?: false

        // ETAPA 4:
        // Registra informações para diagnóstico.
        Log.d(
            "FilmeDaVez",
            "Pesquisa concluída: $pesquisaConcluida. " +
                    "Candidatos por gêneros: ${filmesPorGeneros.size}. " +
                    "Candidatos por temas: ${filmesFiltrados.size}."
        )

        if (!pesquisaConcluida) {

            Log.w(
                "FilmeDaVez",
                "Limite de 30 segundos atingido. " +
                        "Aproveitando candidatos já encontrados."
            )
        }

        // ETAPA 5:
        // Identifica os filmes encontrados pelos temas.
        val idsFilmesPorTemas = filmesFiltrados
            .map { it.id }
            .toSet()

        val generosEscolhidos = idsGeneros.toSet()

        val temasSolicitados = palavrasChave
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }

        // Remove filmes duplicados.
        val candidatos = (
                filmesFiltrados + filmesPorGeneros
                )
            .distinctBy { it.id }
            .filter { filme ->

                val dataEstreia = filme.releaseDate.orEmpty()

                // Mantém filmes sem data cadastrada para
                // não excluir clássicos com dados incompletos.
                dataEstreia.isBlank() ||
                        dataEstreia <= hoje
            }

        // Mantém candidatos com relação temática
        // identificada na sinopse ou no TMDB.
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

        // ETAPA 6:
        // Ordena pela pontuação de compatibilidade.
        val filmesOrdenados =
            candidatosRelevantes.sortedWith(

                compareByDescending<FilmeDto> { filme ->

                    val generosCorrespondentes =
                        filme.genreIds.orEmpty().count {
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

        // Seleciona até 40 candidatos bem classificados
        // para verificar os lançamentos recentes.
        val candidatosFinais = filmesOrdenados.take(40)

        // Calcula o início da janela de 90 dias.
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_YEAR, -90)

        val inicioJanela = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(calendario.time)

        // Usa a variável "hoje" que já criamos na etapa anterior.
        val filmesRecentes = candidatosFinais.filter { filme ->
            val data = filme.releaseDate.orEmpty().take(10)

            data.length == 10 && data >= inicioJanela
        }

        // Armazena os resultados das verificações concluídas.
        val verificacoes = ConcurrentHashMap<Int, Boolean>()

        // No máximo três verificações simultâneas.
        val semaforo = Semaphore(3)

        // Limite adicional de quatro segundos.
        withTimeoutOrNull(4_000L.milliseconds) {

            coroutineScope {

                filmesRecentes.map { filme ->

                    async {

                        semaforo.withPermit {

                            val permitir = try {

                                val dados =
                                    apiService.buscarDatasLancamento(
                                        filmeId = filme.id
                                    )

                                permitirFilmeRecente(
                                    dados = dados,
                                    inicioJanela = inicioJanela,
                                    hoje = hoje
                                )

                            } catch (erro: CancellationException) {

                                // Preserva o cancelamento correto
                                // das corrotinas.
                                throw erro

                            } catch (erro: Exception) {

                                // Falhas pontuais na verificação
                                // não interrompem toda a busca.
                                Log.w(
                                    "FilmeDaVez",
                                    "Falha ao verificar lançamento " +
                                            "do filme ${filme.id}",
                                    erro
                                )

                                true
                            }

                            verificacoes[filme.id] = permitir
                        }
                    }

                }.awaitAll()
            }
        }

        // Filtra apenas títulos comprovadamente
        // identificados pela nossa regra.
        // Filmes não verificados são preservados.
        val filmesDisponiveis = candidatosFinais.filter { filme ->
            verificacoes[filme.id] != false
        }

        Log.d(
            "FilmeDaVez",
            "Verificação de lançamentos: " +
                    "${filmesRecentes.size} recentes, " +
                    "${verificacoes.size} verificados."
        )

        return filmesDisponiveis.take(10)
    }

    private fun permitirFilmeRecente(
        dados: DatasLancamentoTmdbDto,
        inicioJanela: String,
        hoje: String
    ): Boolean {

        // Analisa os lançamentos cadastrados no Brasil.
        val lancamentosBrasil = dados.results
            .orEmpty()
            .firstOrNull { it.pais == "BR" }
            ?.lancamentos
            .orEmpty()

        // Se não houver registros brasileiros,
        // não excluímos o filme por falta de dados.
        if (lancamentosBrasil.isEmpty()) {
            return true
        }

        // Verifica se já houve lançamento doméstico.
        // 4 = Digital
        // 5 = Físico
        // 6 = Televisão
        val jaLancadoParaCasa = lancamentosBrasil.any {
            val data = it.data.orEmpty().take(10)

            it.tipo in listOf(4, 5, 6) &&
                    data.length == 10 &&
                    data <= hoje
        }

        // Se já chegou ao formato doméstico,
        // o filme pode participar do sorteio.
        if (jaLancadoParaCasa) {
            return true
        }

        // 2 = Cinema limitado
        // 3 = Cinema comercial
        val cinemaRecenteOuFuturo = lancamentosBrasil.any {
            val data = it.data.orEmpty().take(10)

            it.tipo in listOf(2, 3) &&
                    data.length == 10 &&
                    data >= inicioJanela
        }

        // Exclui somente quando há indicação
        // de lançamento cinematográfico recente
        // ou futuro, sem lançamento doméstico
        // já registrado no Brasil.
        return !cinemaRecenteOuFuturo
    }

    // Busca as informações completas
    // de um filme específico.
    suspend fun buscarDetalhesFilme(
        filmeId: Int
    ): DetalhesFilmeDto {

        return apiService.buscarDetalhesFilme(
            filmeId = filmeId
        )
    }

    // Busca onde o filme está disponível no Brasil.
    suspend fun buscarOndeAssistir(
        filmeId: Int
    ): DisponibilidadePorPaisDto? {

        val resposta = apiService.buscarOndeAssistir(
            filmeId = filmeId
        )

        // Seleciona apenas as plataformas do Brasil.
        return resposta.results?.get("BR")
    }
}
