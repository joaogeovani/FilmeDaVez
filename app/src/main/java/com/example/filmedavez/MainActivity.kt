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
import com.example.filmedavez.model.FiltrosFilmes
import com.example.filmedavez.ui.screens.TelaFiltrosFilmes
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.LinearProgressIndicator
import com.example.filmedavez.ui.components.BarraProgressoFilme
import com.example.filmedavez.ui.screens.TelaDetalhesFilme
import com.example.filmedavez.ui.viewmodel.EstadoDetalhesFilme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke

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

                // Armazena os filtros escolhidos pelo usuário
                var filtrosSelecionados by remember {
                    mutableStateOf(FiltrosFilmes())
                }

                // Controla o botão Voltar do Android
                BackHandler(enabled = telaAtual != "inicio") {
                    telaAtual = when (telaAtual) {
                        "resumo" -> "filtros"
                        "filtros" -> "palavras"
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
                                telaAtual = "filtros"
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

                            filtros = filtrosSelecionados,

                            onVoltar = {
                                telaAtual = "filtros"
                            }
                        )

                        "filtros" -> {
                            TelaFiltrosFilmes(
                                filtros = filtrosSelecionados,

                                onContinuar = { novosFiltros ->
                                    filtrosSelecionados = novosFiltros
                                    telaAtual = "resumo"
                                },

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
}

// Tela inicial do aplicativo

@Composable
fun TelaInicial(
    onComecar: () -> Unit,
    modifier: Modifier = Modifier
) {

    val cores = MaterialTheme.colorScheme

    val etapas = listOf(
        Triple(
            "01",
            "Escolha seus gêneros",
            "Selecione os estilos de filmes que você gosta."
        ),
        Triple(
            "02",
            "Defina suas preferências",
            "Personalize temas e filtros opcionais."
        ),
        Triple(
            "03",
            "Descubra o filme da vez",
            "Sorteie um filme e descubra onde assistir."
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        cores.background,
                        Color(0xFF202C35),
                        cores.background
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Spacer(modifier = Modifier.height(12.dp))

        // Pequena identificação cinematográfica
        Text(
            text = "●  SUA PRÓXIMA SESSÃO COMEÇA AQUI",
            color = cores.secondary,
            style = MaterialTheme.typography.labelMedium
        )

        // Nome do aplicativo com destaque laranja
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Filme",
                color = cores.onBackground,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "DaVez",
                color = cores.primary,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Descubra o filme ideal para assistir hoje.",
            color = cores.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium
        )

        // Destaque visual com tema de cinema
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF344A5A),
                            Color(0xFF1B2832),
                            Color(0xFF10161C)
                        )
                    )
                )
        ) {

            Text(
                text = "🎬",
                fontSize = 76.sp,
                modifier = Modifier.align(Alignment.Center)
            )

            Text(
                text = "Escolha. Sorteie. Assista.",
                color = cores.onBackground,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            )
        }

        // Apresenta as três etapas do aplicativo
        etapas.forEach { (numero, titulo, descricao) ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cores.surfaceVariant)
                    .padding(16.dp),

                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = numero,
                    color = cores.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(4.dp)
                ) {

                    Text(
                        text = titulo,
                        color = cores.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = descricao,
                        color = cores.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onComecar,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),

            shape = RoundedCornerShape(18.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = cores.primary,
                contentColor = cores.onPrimary
            )
        ) {

            Text(
                text = "Começar",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "→",
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}


// Tela de confirmação dos gêneros
@Composable
fun TelaResumo(
    idsGeneros: List<Int>,
    nomesGeneros: List<String>,
    palavrasChave: List<String>,
    filtros: FiltrosFilmes,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Obtém nossa ViewModel
    val filmesViewModel: FilmesViewModel = viewModel()

    val cores = MaterialTheme.colorScheme

    // Observa o estado da busca
    val estado by filmesViewModel.estado
        .collectAsStateWithLifecycle()

    val estadoDetalhes by filmesViewModel.estadoDetalhes
        .collectAsStateWithLifecycle()

    // Observa o andamento da paginação
    val progresso by filmesViewModel.progresso
        .collectAsStateWithLifecycle()

    val estadoOndeAssistir by filmesViewModel.estadoOndeAssistir
        .collectAsStateWithLifecycle()

    // Solicita os filmes ao entrar na tela
    LaunchedEffect(idsGeneros, palavrasChave,filtros) {
        filmesViewModel.buscarFilmes(
            idsGeneros = idsGeneros,
            palavrasChave = palavrasChave,
            filtros = filtros
        )
    }

    DisposableEffect(filmesViewModel) {

        onDispose {
            filmesViewModel.cancelarBusca()
            filmesViewModel.limparDetalhesFilme()
            filmesViewModel.limparOndeAssistir()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(cores.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        CabecalhoResumoCinema(
            nomesGeneros = nomesGeneros,
            palavrasChave = palavrasChave
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Exibe o conteúdo conforme o estado da ViewModel
        when (val resultado = estado) {

            EstadoBuscaFilmes.Inicial -> {
                CartaoEstadoBuscaCinema(
                    titulo = "Preparando sua sessão",
                    descricao = "Organizando as preferências " +
                            "para iniciar a pesquisa.",
                    destaque = cores.secondary
                ) {
                    CircularProgressIndicator(
                        color = cores.primary,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                }
            }

            EstadoBuscaFilmes.Carregando -> {

                CartaoEstadoBuscaCinema(
                    titulo = "Procurando seu próximo filme",
                    descricao = "Estamos analisando o catálogo " +
                            "para encontrar filmes compatíveis " +
                            "com suas preferências.",
                    destaque = cores.primary
                ) {

                    CircularProgressIndicator(
                        color = cores.primary,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )

                    if (progresso.totalPaginas > 0) {

                        Text(
                            text = progresso.etapa,
                            color = cores.secondary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "Página ${progresso.paginaAtual} " +
                                    "de ${progresso.totalPaginas}",
                            color = cores.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Mantemos nossa animação cinematográfica
                        BarraProgressoFilme(
                            progresso = (
                                    progresso.paginaAtual.toFloat() /
                                            progresso.totalPaginas.toFloat()
                                    ).coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth()
                        )

                    } else {

                        Text(
                            text = "Preparando a pesquisa...",
                            color = cores.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            filmesViewModel.cancelarBusca()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = cores.onBackground
                        )
                    ) {
                        Text("Cancelar busca")
                    }
                }
            }

            is EstadoBuscaFilmes.Sucesso -> {

                val filmes = resultado.filmes

                // Um único filme é selecionado automaticamente
                var indiceSorteado by remember(filmes) {
                    mutableStateOf<Int?>(
                        if (filmes.size == 1) 0 else null
                    )
                }

                Text(
                    text = "${filmes.size} filme(s) compatível(is)",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sorteio disponível somente com dois ou mais filmes
                if (filmes.size > 1) {

                    Button(
                        onClick = {

                            // Mantém a regra de não repetir
                            // imediatamente o último filme sorteado.
                            val opcoes = filmes.indices.filter {
                                it != indiceSorteado
                            }

                            indiceSorteado = opcoes.random()
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),

                        shape = RoundedCornerShape(18.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = cores.primary,
                            contentColor = cores.onPrimary
                        )
                    ) {
                        Text(
                            text = if (indiceSorteado == null) {
                                "🎲 Sortear filme"
                            } else {
                                "🎲 Sortear outro filme"
                            },
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Recupera o filme escolhido no sorteio
                val filmeEscolhido = indiceSorteado?.let {
                    filmes[it]
                }

                LaunchedEffect(filmeEscolhido?.id) {

                    if (filmeEscolhido != null) {

                        // Busca informações completas
                        filmesViewModel.buscarDetalhesFilme(
                            filmeId = filmeEscolhido.id
                        )

                        // Busca as plataformas disponíveis no Brasil
                        filmesViewModel.buscarOndeAssistir(
                            filmeId = filmeEscolhido.id
                        )

                    } else {

                        filmesViewModel.limparDetalhesFilme()
                        filmesViewModel.limparOndeAssistir()
                    }
                }


                if (filmeEscolhido != null) {

                    Text(
                        text = if (filmes.size == 1) {
                            "Seu filme recomendado!"
                        } else {
                            "Filme sorteado!"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TelaDetalhesFilme(
                        filme = filmeEscolhido,
                        estadoDetalhes = estadoDetalhes,
                        estadoOndeAssistir = estadoOndeAssistir,

                        onTentarNovamente = {
                            filmesViewModel.buscarDetalhesFilme(
                                filmeId = filmeEscolhido.id
                            )
                        },

                        onTentarNovamenteOndeAssistir = {
                            filmesViewModel.buscarOndeAssistir(
                                filmeId = filmeEscolhido.id
                            )
                        }
                    )

                } else {

                    Text(
                        text = "Tudo pronto! Clique em Sortear filme " +
                                "para descobrir o que assistir."
                    )
                }

            }

            EstadoBuscaFilmes.SemResultados -> {
                CartaoEstadoBuscaCinema(
                    titulo = "Nenhum filme encontrado",
                    descricao = "Não encontramos filmes que " +
                            "atendam às preferências selecionadas. " +
                            "Experimente reduzir os filtros " +
                            "ou escolher outros gêneros.",
                    destaque = cores.secondary
                )
            }

            EstadoBuscaFilmes.Cancelada -> {
                CartaoEstadoBuscaCinema(
                    titulo = "Busca cancelada",
                    descricao = "A pesquisa foi interrompida. " +
                            "Você pode voltar aos filtros e " +
                            "iniciar uma nova busca.",
                    destaque = cores.primary
                )
            }

            is EstadoBuscaFilmes.Erro -> {
                CartaoEstadoBuscaCinema(
                    titulo = "Não foi possível concluir a busca",
                    descricao = resultado.mensagem,
                    destaque = cores.error
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onVoltar,

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),

            shape = RoundedCornerShape(18.dp),

            border = BorderStroke(
                width = 1.dp,
                color = cores.outline
            ),

            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = cores.onBackground
            )
        ) {
            Text(
                text = "Alterar preferências",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CabecalhoResumoCinema(
    nomesGeneros: List<String>,
    palavrasChave: List<String>
) {
    val cores = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cores.surfaceVariant,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🎬 SUA SESSÃO DE CINEMA",
                color = cores.secondary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Vamos encontrar seu FilmeDaVez",
                color = cores.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Suas preferências estão prontas. " +
                        "Agora é hora de descobrir o próximo filme.",
                color = cores.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider(
                color = cores.outline.copy(alpha = 0.4f)
            )

            Text(
                text = "GÊNEROS ESCOLHIDOS",
                color = cores.primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = nomesGeneros.joinToString(", ")
                    .ifBlank { "Nenhum gênero selecionado" },
                color = cores.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "PALAVRAS-CHAVE",
                color = cores.primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = palavrasChave.joinToString(", ")
                    .ifBlank {
                        "Nenhuma palavra-chave adicionada"
                    },
                color = cores.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CartaoEstadoBuscaCinema(
    titulo: String,
    descricao: String,
    destaque: Color,
    conteudo: @Composable ColumnScope.() -> Unit = {}
) {
    val cores = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cores.surfaceVariant,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            destaque,
                            RoundedCornerShape(50)
                        )
                )

                Text(
                    text = titulo,
                    color = cores.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = descricao,
                color = cores.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            conteudo()
        }
    }
}
