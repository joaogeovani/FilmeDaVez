package com.example.filmedavez

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.filmedavez.model.generosDisponiveis
import com.example.filmedavez.ui.screens.TelaSelecaoGeneros
import com.example.filmedavez.ui.theme.FilmeDaVezTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.filmedavez.data.remote.RetrofitClient
import com.example.filmedavez.data.remote.dto.FilmeDto
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FilmeDaVezTheme {

                // Controla qual tela está aparecendo
                var telaAtual by rememberSaveable {
                    mutableStateOf("inicio")
                }

                // Armazena os IDs dos gêneros selecionados
                var generosSelecionados by rememberSaveable {
                    mutableStateOf(emptyList<Int>())
                }

                // Controla o botão Voltar do Android
                BackHandler(enabled = telaAtual != "inicio") {
                    telaAtual = if (telaAtual == "resumo") {
                        "generos"
                    } else {
                        "inicio"
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    when (telaAtual) {

                        "inicio" -> TelaInicial(
                            modifier = Modifier.padding(innerPadding),
                            onComecar = {
                                telaAtual = "generos"
                            }
                        )

                        "generos" -> TelaSelecaoGeneros(
                            modifier = Modifier.padding(innerPadding),
                            idsSelecionados = generosSelecionados,

                            onGeneroClick = { id ->
                                generosSelecionados =
                                    if (id in generosSelecionados) {
                                        generosSelecionados - id
                                    } else {
                                        generosSelecionados + id
                                    }
                            },

                            onContinuar = {
                                telaAtual = "resumo"
                            },

                            onVoltar = {
                                telaAtual = "inicio"
                            }
                        )

                        "resumo" -> TelaResumo(
                            modifier = Modifier.padding(innerPadding),

                            idsGeneros = generosSelecionados,

                            nomesGeneros = generosDisponiveis
                                .filter { it.id in generosSelecionados }
                                .map { it.nome },

                            onVoltar = {
                                telaAtual = "generos"
                            }
                        )

                    }
                }
            }
        }
    }
}

// Tela inicial do aplicativo
@Composable
fun TelaInicial(
    onComecar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎬 FilmeDaVez",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Encontre o filme ideal para assistir com seus amigos.",
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onComecar
        ) {
            Text("Começar")
        }
    }
}

// Tela de confirmação dos gêneros
@Composable
fun TelaResumo(
    idsGeneros: List<Int>,
    nomesGeneros: List<String>,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filmes by remember(idsGeneros) {
        mutableStateOf<List<FilmeDto>>(emptyList())
    }

    var carregando by remember(idsGeneros) {
        mutableStateOf(true)
    }

    var erro by remember(idsGeneros) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(idsGeneros) {
        carregando = true
        erro = null

        try {
            val filtro = idsGeneros
                .distinct()
                .joinToString("|")

            val resposta = RetrofitClient.api.descobrirFilmes(
                generos = filtro
            )

            filmes = resposta.results.take(5)

        } catch (e: CancellationException) {
            throw e

        } catch (e: HttpException) {
            erro = "Erro HTTP ${e.code()} ao consultar o TMDB."

        } catch (e: Exception) {
            erro = "Não foi possível buscar os filmes. " +
                    "Verifique sua conexão com a internet."

        } finally {
            carregando = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Filmes encontrados",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Gêneros: ${nomesGeneros.joinToString(", ")}"
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            carregando -> {
                CircularProgressIndicator()
                Text("Buscando filmes no TMDB...")
            }

            erro != null -> {
                Text(erro ?: "Erro desconhecido.")
            }

            filmes.isEmpty() -> {
                Text("Nenhum filme encontrado.")
            }

            else -> {
                filmes.forEachIndexed { index, filme ->
                    Text(
                        text = "${index + 1}. ${filme.title}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Nota TMDB: ${
                            String.format("%.1f", filme.voteAverage)
                        }/10"
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onVoltar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Alterar gêneros")
        }
    }
}

