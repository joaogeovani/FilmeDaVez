package com.example.filmedavez.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.filmedavez.model.generosDisponiveis

@Composable
fun TelaSelecaoGeneros(
    idsSelecionados: List<Int>,
    onGeneroClick: (Int) -> Unit,
    onContinuar: () -> Unit,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cores = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(cores.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Spacer(modifier = Modifier.height(8.dp))

        // Indicador da etapa atual
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ETAPA 01 DE 03",
                color = cores.secondary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Gêneros",
                color = cores.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Barra ilustrativa de progresso
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(
                    cores.surfaceVariant,
                    RoundedCornerShape(50)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.33f)
                    .fillMaxHeight()
                    .background(
                        cores.primary,
                        RoundedCornerShape(50)
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Quais filmes você gosta?",
            color = cores.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Escolha um ou mais gêneros. " +
                    "Vamos usar suas preferências para " +
                    "encontrar os filmes ideais.",
            color = cores.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card principal dos gêneros
        Surface(
            color = cores.surfaceVariant,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Selecione seus gêneros",
                    color = cores.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${idsSelecionados.size} selecionado(s)",
                    color = cores.secondary,
                    style = MaterialTheme.typography.bodySmall
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    generosDisponiveis.forEach { genero ->

                        val selecionado =
                            genero.id in idsSelecionados

                        FilterChip(
                            selected = selecionado,

                            onClick = {
                                onGeneroClick(genero.id)
                            },

                            label = {
                                Text(
                                    text = genero.nome,
                                    fontWeight =
                                        FontWeight.Medium
                                )
                            },

                            leadingIcon = if (selecionado) {
                                {
                                    Text(
                                        text = "✓",
                                        color = cores.onPrimary
                                    )
                                }
                            } else {
                                null
                            },

                            shape = RoundedCornerShape(16.dp),

                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = cores.surface,
                                labelColor = cores.onSurface,

                                selectedContainerColor = cores.primary,
                                selectedLabelColor = cores.onPrimary,
                                selectedLeadingIconColor = cores.onPrimary
                            )
                        )
                    }
                }
            }
        }

        // Orientação para o usuário
        Text(
            text = if (idsSelecionados.isEmpty()) {
                "Selecione pelo menos um gênero para continuar."
            } else {
                "Ótimo! Você pode escolher quantos gêneros quiser."
            },
            color = cores.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Avança somente com pelo menos um gênero
        Button(
            onClick = onContinuar,
            enabled = idsSelecionados.isNotEmpty(),

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
                text = "Continuar",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text("→")
        }

        TextButton(
            onClick = onVoltar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Voltar",
                color = cores.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}