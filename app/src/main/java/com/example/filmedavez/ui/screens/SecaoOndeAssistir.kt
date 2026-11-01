package com.example.filmedavez.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.filmedavez.data.remote.dto.ProvedorFilmeDto
import com.example.filmedavez.ui.viewmodel.EstadoOndeAssistir

@Composable
fun SecaoOndeAssistir(
    filmeId: Int,
    estado: EstadoOndeAssistir,
    onTentarNovamente: () -> Unit,
    modifier: Modifier = Modifier
) {

    val cores = MaterialTheme.colorScheme
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = cores.surfaceVariant,
            shape = RoundedCornerShape(24.dp)
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Cabeçalho
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "Onde assistir",
                        color = cores.onBackground,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        color = cores.background,
                        shape = RoundedCornerShape(50)
                    ) {

                        Text(
                            text = "Brasil",
                            color = cores.secondary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 8.dp
                            )
                        )
                    }
                }

                Text(
                    text = "Confira as plataformas disponíveis " +
                            "para assistir ao filme sorteado.",
                    color = cores.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )

                HorizontalDivider(
                    color = cores.outline.copy(alpha = 0.4f)
                )

                when (val resultado = estado) {

                    EstadoOndeAssistir.Inicial,
                    EstadoOndeAssistir.Carregando -> {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = cores.primary,
                                strokeWidth = 3.dp
                            )

                            Text(
                                text = "Consultando plataformas...",
                                color = cores.onSurfaceVariant
                            )
                        }
                    }

                    EstadoOndeAssistir.SemInformacoes -> {

                        Surface(
                            color = cores.background,
                            shape = RoundedCornerShape(16.dp)
                        ) {

                            Text(
                                text = "Não encontramos informações " +
                                        "de disponibilidade para este " +
                                        "filme no Brasil.",
                                color = cores.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    is EstadoOndeAssistir.Erro -> {

                        Text(
                            text = resultado.mensagem,
                            color = cores.error
                        )

                        OutlinedButton(
                            onClick = onTentarNovamente,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tentar novamente")
                        }
                    }

                    is EstadoOndeAssistir.Sucesso -> {

                        // Evita mostrar dados do sorteio anterior
                        if (resultado.filmeId != filmeId) {

                            Text(
                                text = "Atualizando disponibilidade...",
                                color = cores.onSurfaceVariant
                            )

                        } else {

                            val dados = resultado.disponibilidade

                            GrupoPlataformasCinema(
                                titulo = "Streaming por assinatura",
                                provedores = dados.flatrate.orEmpty()
                            )

                            GrupoPlataformasCinema(
                                titulo = "Aluguel digital",
                                provedores = dados.rent.orEmpty()
                            )

                            GrupoPlataformasCinema(
                                titulo = "Compra digital",
                                provedores = dados.buy.orEmpty()
                            )

                            GrupoPlataformasCinema(
                                titulo = "Gratuito",
                                provedores = dados.free.orEmpty()
                            )

                            GrupoPlataformasCinema(
                                titulo = "Gratuito com anúncios",
                                provedores = dados.ads.orEmpty()
                            )

                            // Link fornecido pelo TMDB
                            val link = dados.link?.takeIf {
                                it.startsWith(
                                    "https://www.themoviedb.org/"
                                )
                            }

                            if (link != null) {

                                OutlinedButton(
                                    onClick = {
                                        uriHandler.openUri(link)
                                    },

                                    modifier = Modifier.fillMaxWidth(),

                                    shape = RoundedCornerShape(16.dp),

                                    border = BorderStroke(
                                        1.dp,
                                        cores.secondary
                                    ),

                                    colors =
                                        ButtonDefaults.outlinedButtonColors(
                                            containerColor =
                                                cores.background,
                                            contentColor =
                                                cores.secondary
                                        )
                                ) {

                                    Text(
                                        text = "Consultar opções no TMDB",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = cores.outline.copy(alpha = 0.4f)
                )

                // Atribuição dos dados de disponibilidade
                Text(
                    text = "Dados de disponibilidade: " +
                            "JustWatch, via TMDB. " +
                            "As opções podem mudar com o tempo.",
                    color = cores.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Componente reutilizável para cada categoria
@Composable
private fun GrupoPlataformasCinema(
    titulo: String,
    provedores: List<ProvedorFilmeDto>
) {

    if (provedores.isEmpty()) {
        return
    }

    val cores = MaterialTheme.colorScheme

    val provedoresOrdenados = provedores
        .distinctBy { it.providerId }
        .sortedWith(
            compareBy<ProvedorFilmeDto> {
                it.displayPriority ?: Int.MAX_VALUE
            }.thenBy {
                it.providerName
            }
        )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // Título e quantidade de plataformas
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = titulo,
                color = cores.onBackground,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Surface(
                color = cores.background,
                shape = RoundedCornerShape(50)
            ) {

                Text(
                    text = "${provedoresOrdenados.size}",
                    color = cores.secondary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 6.dp
                    )
                )
            }
        }

        // Cartão de cada plataforma
        provedoresOrdenados.forEach { provedor ->

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = cores.surface,
                shape = RoundedCornerShape(14.dp)
            ) {

                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    val urlLogo = provedor.logoPath
                        ?.takeIf { it.isNotBlank() }
                        ?.let {
                            "https://image.tmdb.org/t/p/w92$it"
                        }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(cores.background),

                        contentAlignment = Alignment.Center
                    ) {

                        if (urlLogo != null) {

                            AsyncImage(
                                model = urlLogo,
                                contentDescription =
                                    "Logo de ${provedor.providerName}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )

                        } else {

                            Text(
                                text = "TV",
                                color = cores.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = provedor.providerName,
                        color = cores.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
