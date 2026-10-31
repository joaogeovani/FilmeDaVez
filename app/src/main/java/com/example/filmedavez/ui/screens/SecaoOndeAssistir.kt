package com.example.filmedavez.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
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
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        HorizontalDivider()

        Text(
            text = "Onde assistir no Brasil",
            style = MaterialTheme.typography.titleLarge
        )

        when (val resultado = estado) {

            EstadoOndeAssistir.Inicial,
            EstadoOndeAssistir.Carregando -> {

                CircularProgressIndicator()

                Text("Consultando plataformas disponíveis...")
            }

            EstadoOndeAssistir.SemInformacoes -> {

                Text(
                    "Não encontramos informações de " +
                            "disponibilidade para este filme no Brasil."
                )
            }

            is EstadoOndeAssistir.Erro -> {

                Text(
                    text = resultado.mensagem,
                    color = MaterialTheme.colorScheme.error
                )

                OutlinedButton(
                    onClick = onTentarNovamente
                ) {
                    Text("Tentar novamente")
                }
            }

            is EstadoOndeAssistir.Sucesso -> {

                // Impede exibir plataformas do sorteio anterior
                if (resultado.filmeId != filmeId) {

                    Text("Atualizando disponibilidade...")

                } else {

                    val dados = resultado.disponibilidade

                    GrupoPlataformas(
                        titulo = "Streaming por assinatura",
                        provedores = dados.flatrate.orEmpty()
                    )

                    GrupoPlataformas(
                        titulo = "Aluguel digital",
                        provedores = dados.rent.orEmpty()
                    )

                    GrupoPlataformas(
                        titulo = "Compra digital",
                        provedores = dados.buy.orEmpty()
                    )

                    GrupoPlataformas(
                        titulo = "Gratuito",
                        provedores = dados.free.orEmpty()
                    )

                    GrupoPlataformas(
                        titulo = "Gratuito com anúncios",
                        provedores = dados.ads.orEmpty()
                    )

                    val link = dados.link

                    if (
                        link != null &&
                        link.startsWith(
                            "https://www.themoviedb.org/"
                        )
                    ) {

                        OutlinedButton(
                            onClick = {
                                uriHandler.openUri(link)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Consultar opções no TMDB")
                        }
                    }
                }
            }
        }

        Text(
            text = "Dados de disponibilidade: JustWatch, via TMDB.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun GrupoPlataformas(
    titulo: String,
    provedores: List<ProvedorFilmeDto>
) {

    if (provedores.isEmpty()) {
        return
    }

    Text(
        text = titulo,
        style = MaterialTheme.typography.titleMedium
    )

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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        provedoresOrdenados.forEach { provedor ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                val urlLogo = provedor.logoPath
                    ?.takeIf { it.isNotBlank() }
                    ?.let {
                        "https://image.tmdb.org/t/p/w92$it"
                    }

                if (urlLogo != null) {

                    AsyncImage(
                        model = urlLogo,
                        contentDescription =
                            "Logo de ${provedor.providerName}",
                        modifier = Modifier.size(44.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Text(
                    text = provedor.providerName,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
