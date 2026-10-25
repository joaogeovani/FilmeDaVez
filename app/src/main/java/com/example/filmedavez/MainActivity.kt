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
import com.example.filmedavez.ui.screens.TelaSelecaoPalavrasChave
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.filmedavez.ui.viewmodel.FilmesViewModel
import com.example.filmedavez.ui.viewmodel.EstadoBuscaFilmes


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

                var palavrasChaveSelecionadas by rememberSaveable {
                    mutableStateOf(emptyList<String>())
                }

                // Controla o botão Voltar do Android
                BackHandler(enabled = telaAtual != "inicio") {
                    telaAtual = when (telaAtual) {
                        "resumo" -> "palavras"
                        "palavras" -> "generos"
                        "generos" -> "inicio"
                        else -> "inicio"
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
                                telaAtual = "palavras"
                            },

                            onVoltar = {
                                telaAtual = "inicio"
                            }
                        )

                        "palavras" -> TelaSelecaoPalavrasChave(
                            modifier = Modifier.padding(innerPadding),

                            palavrasChave = palavrasChaveSelecionadas,

                            onAdicionarPalavra = { novaPalavra ->
                                val palavra = novaPalavra.trim()

                                if (
                                    palavra.isNotEmpty() &&
                                    palavrasChaveSelecionadas.size < 5 &&
                                    palavrasChaveSelecionadas.none {
                                        it.equals(palavra, ignoreCase = true)
                                    }
                                ) {
                                    palavrasChaveSelecionadas =
                                        palavrasChaveSelecionadas + palavra
                                }
                            },

                            onRemoverPalavra = { palavra ->
                                palavrasChaveSelecionadas =
                                    palavrasChaveSelecionadas - palavra
                            },

                            onContinuar = {
                                telaAtual = "resumo"
                            },

                            onVoltar = {
                                telaAtual = "generos"
                            }
                        )


                        "resumo" -> TelaResumo(
                            modifier = Modifier.padding(innerPadding),

                            idsGeneros = generosSelecionados,
                            palavrasChave = palavrasChaveSelecionadas,

                            nomesGeneros = generosDisponiveis
                                .filter { it.id in generosSelecionados }
                                .map { it.nome },

                            onVoltar = {
                                telaAtual = "palavras"
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
    palavrasChave: List<String>,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Obtém nossa ViewModel
    val filmesViewModel: FilmesViewModel = viewModel()

    // Observa o estado da busca
    val estado by filmesViewModel.estado
        .collectAsStateWithLifecycle()

    // Solicita os filmes ao entrar na tela
    LaunchedEffect(idsGeneros, palavrasChave) {
        filmesViewModel.buscarFilmes(
            idsGeneros = idsGeneros,
            palavrasChave = palavrasChave
        )
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

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (palavrasChave.isEmpty()) {
                "Nenhuma palavra-chave adicionada."
            } else {
                "Temas escolhidos: ${palavrasChave.joinToString(", ")}"
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Exibe o conteúdo conforme o estado da ViewModel
        when (val resultado = estado) {

            EstadoBuscaFilmes.Inicial -> {
                Text("Preparando busca...")
            }

            EstadoBuscaFilmes.Carregando -> {
                CircularProgressIndicator()

                Spacer(modifier = Modifier.height(12.dp))

                Text("Buscando filmes no TMDB...")
            }

            is EstadoBuscaFilmes.Sucesso -> {
                resultado.filmes.forEachIndexed { index, filme ->

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

            EstadoBuscaFilmes.SemResultados -> {
                Text("Nenhum filme encontrado.")
            }

            is EstadoBuscaFilmes.Erro -> {
                Text(resultado.mensagem)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onVoltar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Alterar preferências")
        }
    }
}