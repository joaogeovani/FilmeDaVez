package com.example.filmedavez.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.filmedavez.data.remote.dto.FilmeDto
import com.example.filmedavez.ui.viewmodel.EstadoDetalhesFilme
import java.util.Locale
import com.example.filmedavez.ui.viewmodel.EstadoOndeAssistir

@Composable
fun TelaDetalhesFilme(
    filme: FilmeDto,
    estadoDetalhes: EstadoDetalhesFilme,
    estadoOndeAssistir: EstadoOndeAssistir,
    onTentarNovamente: () -> Unit,
    onTentarNovamenteOndeAssistir: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        when (val resultado = estadoDetalhes) {

            EstadoDetalhesFilme.Inicial,
            EstadoDetalhesFilme.Carregando -> {

                CircularProgressIndicator()

                Text("Carregando detalhes do filme...")
            }

            is EstadoDetalhesFilme.Sucesso -> {

                val detalhes = resultado.detalhes

                // Evita mostrar os dados do sorteio anterior
                if (detalhes.id != filme.id) {

                    CircularProgressIndicator()

                    Text("Carregando novo filme...")

                } else {

                    // Monta a URL do pôster
                    val urlPoster = detalhes.posterPath
                        ?.takeIf { it.isNotBlank() }
                        ?.let {
                            "https://image.tmdb.org/t/p/w500$it"
                        }

                    if (urlPoster != null) {

                        AsyncImage(
                            model = urlPoster,
                            contentDescription =
                                "Pôster do filme ${detalhes.title}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            contentScale = ContentScale.Fit
                        )

                    } else {

                        Text("Pôster não disponível.")
                    }

                    Text(
                        text = detalhes.title,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    val nota = String.format(
                        Locale.forLanguageTag("pt-BR"),
                        "%.1f",
                        detalhes.voteAverage
                    )

                    Text(
                        text = "Avaliação TMDB: $nota/10"
                    )

                    // Formata a data de lançamento
                    val partesData = detalhes.releaseDate
                        ?.split("-")
                        .orEmpty()

                    val lancamento = if (partesData.size == 3) {
                        "${partesData[2]}/${partesData[1]}/${partesData[0]}"
                    } else {
                        "Não informado"
                    }

                    Text(
                        text = "Lançamento: $lancamento"
                    )

                    // Formata a duração em horas e minutos
                    val duracao = detalhes.runtime
                        ?.takeIf { it > 0 }
                        ?.let {
                            "${it / 60}h ${it % 60}min"
                        } ?: "Não informada"

                    Text(
                        text = "Duração: $duracao"
                    )

                    val generos = detalhes.genres
                        .joinToString(", ") { it.name }
                        .ifBlank { "Não informados" }

                    Text(
                        text = "Gêneros: $generos"
                    )

                    HorizontalDivider()

                    Text(
                        text = "Sinopse",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = detalhes.overview.ifBlank {
                            "Sinopse não disponível."
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            is EstadoDetalhesFilme.Erro -> {

                Text(
                    text = resultado.mensagem,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    text = filme.title,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = filme.overview.ifBlank {
                        "Sinopse não disponível."
                    }
                )

                OutlinedButton(
                    onClick = onTentarNovamente
                ) {
                    Text("Tentar carregar detalhes novamente")
                }
            }
        }

        SecaoOndeAssistir(
            filmeId = filme.id,
            estado = estadoOndeAssistir,
            onTentarNovamente = onTentarNovamenteOndeAssistir
        )

    }
}
