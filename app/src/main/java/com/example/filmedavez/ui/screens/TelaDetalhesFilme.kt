package com.example.filmedavez.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.filmedavez.data.remote.dto.FilmeDto
import com.example.filmedavez.ui.viewmodel.EstadoDetalhesFilme
import com.example.filmedavez.ui.viewmodel.EstadoOndeAssistir
import java.util.Locale

@Composable
fun TelaDetalhesFilme(
    filme: FilmeDto,
    estadoDetalhes: EstadoDetalhesFilme,
    estadoOndeAssistir: EstadoOndeAssistir,
    onTentarNovamente: () -> Unit,
    onTentarNovamenteOndeAssistir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cores = MaterialTheme.colorScheme

    Column(
        modifier = modifier.fillMaxWidth(),

        verticalArrangement = Arrangement.spacedBy(16.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        when (val resultado = estadoDetalhes) {

            EstadoDetalhesFilme.Inicial,
            EstadoDetalhesFilme.Carregando -> {

                CircularProgressIndicator(
                    color = cores.primary
                )

                Text(
                    text = "Preparando seu filme...",
                    color = cores.onSurfaceVariant
                )
            }

            is EstadoDetalhesFilme.Sucesso -> {

                val detalhes = resultado.detalhes

                // Garante que os detalhes pertencem
                // ao filme sorteado atualmente.
                if (detalhes.id != filme.id) {

                    CircularProgressIndicator(
                        color = cores.primary
                    )

                    Text("Carregando novo filme...")

                } else {

                    Surface(
                        modifier = Modifier.fillMaxWidth(),

                        color = cores.surfaceVariant,

                        shape = RoundedCornerShape(24.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),

                            verticalArrangement =
                                Arrangement.spacedBy(16.dp)
                        ) {

                            Text(
                                text = "SEU FILME DA VEZ",
                                color = cores.secondary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Pôster do filme
                            val urlPoster = detalhes.posterPath
                                ?.takeIf { it.isNotBlank() }
                                ?.let {
                                    "https://image.tmdb.org/t/p/w500$it"
                                }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(cores.background),

                                contentAlignment = Alignment.Center
                            ) {

                                if (urlPoster != null) {

                                    AsyncImage(
                                        model = urlPoster,

                                        contentDescription =
                                            "Pôster de ${detalhes.title}",

                                        modifier = Modifier.fillMaxSize(),

                                        contentScale = ContentScale.Fit
                                    )

                                } else {

                                    Text(
                                        text = "Pôster não disponível",
                                        color = cores.onSurfaceVariant
                                    )
                                }
                            }

                            // Título do filme
                            Text(
                                text = detalhes.title.orEmpty()
                                    .ifBlank {
                                        "Título não informado"
                                    },

                                color = cores.onBackground,

                                style =
                                    MaterialTheme.typography.headlineSmall,

                                fontWeight = FontWeight.Bold
                            )

                            // Avaliação TMDB
                            val nota = String.format(
                                Locale.forLanguageTag("pt-BR"),
                                "%.1f",
                                detalhes.voteAverage
                            )

                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,

                                horizontalArrangement =
                                    Arrangement.spacedBy(12.dp)
                            ) {

                                Surface(
                                    color = cores.tertiary,
                                    contentColor = cores.onTertiary,
                                    shape = RoundedCornerShape(16.dp)
                                ) {

                                    Text(
                                        text = "★ $nota",
                                        modifier = Modifier.padding(
                                            horizontal = 14.dp,
                                            vertical = 10.dp
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                Text(
                                    text = "Avaliação TMDB / 10",
                                    color = cores.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            // Formatação da data
                            val partesData = detalhes.releaseDate
                                ?.split("-")
                                .orEmpty()

                            val lancamento =
                                if (partesData.size == 3) {
                                    "${partesData[2]}/${partesData[1]}/${partesData[0]}"
                                } else {
                                    "Não informado"
                                }

                            // Formatação da duração
                            val duracao = detalhes.runtime
                                ?.takeIf { it > 0 }
                                ?.let {
                                    "${it / 60}h ${it % 60}min"
                                } ?: "Não informada"

                            val generos = detalhes.genres
                                .orEmpty()
                                .joinToString(", ") { it.name }
                                .ifBlank {
                                    "Não informados"
                                }

                            // Informações complementares
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = cores.background,
                                shape = RoundedCornerShape(16.dp)
                            ) {

                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement =
                                        Arrangement.spacedBy(12.dp)
                                ) {

                                    InformacaoFilmeCinema(
                                        titulo = "Lançamento",
                                        valor = lancamento
                                    )

                                    InformacaoFilmeCinema(
                                        titulo = "Duração",
                                        valor = duracao
                                    )

                                    InformacaoFilmeCinema(
                                        titulo = "Gêneros",
                                        valor = generos
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = cores.outline.copy(alpha = 0.4f)
                            )

                            // Sinopse
                            Text(
                                text = "Sinopse",
                                color = cores.onBackground,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = detalhes.overview
                                    .orEmpty()
                                    .ifBlank {
                                        "Sinopse não disponível."
                                    },

                                color = cores.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            is EstadoDetalhesFilme.Erro -> {

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = cores.surfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            text = resultado.mensagem,
                            color = cores.error
                        )

                        Text(
                            text = filme.title,
                            color = cores.onBackground,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = filme.overview
                                .orEmpty()
                                .ifBlank {
                                    "Sinopse não disponível."
                                },
                            color = cores.onSurfaceVariant
                        )

                        OutlinedButton(
                            onClick = onTentarNovamente,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tentar carregar detalhes novamente")
                        }
                    }
                }
            }
        }

        // Mantém a seção de streaming que já funciona.
        SecaoOndeAssistir(
            filmeId = filme.id,
            estado = estadoOndeAssistir,
            onTentarNovamente = onTentarNovamenteOndeAssistir
        )
    }
}

// Componente reutilizável para exibir informações
@Composable
private fun InformacaoFilmeCinema(
    titulo: String,
    valor: String
) {
    val cores = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {

        Text(
            text = titulo,
            color = cores.primary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(100.dp)
        )

        Text(
            text = valor,
            color = cores.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}
