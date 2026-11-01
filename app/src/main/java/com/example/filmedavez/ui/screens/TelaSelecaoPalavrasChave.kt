package com.example.filmedavez.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun TelaSelecaoPalavrasChave(
    palavrasChave: List<String>,
    onAdicionarPalavra: (String) -> Unit,
    onRemoverPalavra: (String) -> Unit,
    onContinuar: () -> Unit,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {

    val cores = MaterialTheme.colorScheme

    var textoDigitado by rememberSaveable {
        mutableStateOf("")
    }

    val palavraLimpa = textoDigitado.trim()

    // Limite de cinco palavras-chave
    val limiteAtingido = palavrasChave.size >= 5

    // Impede palavras repetidas
    val palavraDuplicada = palavrasChave.any {
        it.equals(palavraLimpa, ignoreCase = true)
    }

    val podeAdicionar =
        palavraLimpa.isNotEmpty() &&
                !limiteAtingido &&
                !palavraDuplicada

    fun adicionarPalavra() {
        if (podeAdicionar) {
            onAdicionarPalavra(palavraLimpa)
            textoDigitado = ""
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(cores.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Spacer(modifier = Modifier.height(8.dp))

        // Identificação da etapa
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "ETAPA 02 DE 03",
                color = cores.secondary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = "Palavras-chave",
                color = cores.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Indicador visual de progresso
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
                    .fillMaxWidth(0.667f)
                    .fillMaxHeight()
                    .background(
                        cores.primary,
                        RoundedCornerShape(50)
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "O que você quer assistir?",
            color = cores.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Adicione temas ou assuntos que gostaria " +
                    "de encontrar nos filmes. Essa etapa é opcional.",
            color = cores.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )

        // Card principal
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = cores.surfaceVariant,
            shape = RoundedCornerShape(20.dp)
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Seus temas",
                        color = cores.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "${palavrasChave.size}/5",
                        color = cores.secondary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedTextField(
                    value = textoDigitado,

                    onValueChange = {
                        textoDigitado = it
                    },

                    modifier = Modifier.fillMaxWidth(),

                    enabled = !limiteAtingido,

                    label = {
                        Text("Adicionar palavra-chave")
                    },

                    placeholder = {
                        Text("Ex.: assassino, espaço...")
                    },

                    singleLine = true,

                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),

                    keyboardActions = KeyboardActions(
                        onDone = {
                            adicionarPalavra()
                        }
                    ),

                    shape = RoundedCornerShape(14.dp),

                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cores.surface,
                        unfocusedContainerColor = cores.surface,
                        disabledContainerColor = cores.surface,

                        focusedBorderColor = cores.primary,
                        unfocusedBorderColor = cores.outline,

                        focusedTextColor = cores.onSurface,
                        unfocusedTextColor = cores.onSurface,

                        cursorColor = cores.primary
                    )
                )

                if (palavraDuplicada && palavraLimpa.isNotEmpty()) {

                    Text(
                        text = "Essa palavra-chave já foi adicionada.",
                        color = cores.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (limiteAtingido) {

                    Text(
                        text = "Limite de cinco palavras-chave atingido.",
                        color = cores.secondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedButton(
                    onClick = {
                        adicionarPalavra()
                    },

                    enabled = podeAdicionar,

                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Adicionar tema")
                }

                if (palavrasChave.isNotEmpty()) {

                    HorizontalDivider(
                        color = cores.outline.copy(alpha = 0.4f)
                    )

                    FlowRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(4.dp)
                    ) {

                        palavrasChave.forEach { palavra ->

                            AssistChip(
                                onClick = {
                                    onRemoverPalavra(palavra)
                                },

                                label = {
                                    Text(palavra)
                                },

                                trailingIcon = {
                                    Text("×")
                                },

                                shape = RoundedCornerShape(16.dp),

                                colors =
                                    AssistChipDefaults.assistChipColors(
                                        containerColor = cores.surface,
                                        labelColor = cores.onSurface,
                                        trailingIconContentColor =
                                            cores.onSurface
                                    )
                            )
                        }
                    }

                    Text(
                        text = "Toque em um tema para removê-lo.",
                        color = cores.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Text(
            text = "Dica: experimente temas como viagem no tempo, " +
                    "mistério, assassinato ou sobrevivência.",
            color = cores.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Avança com ou sem palavras-chave
        Button(
            onClick = onContinuar,

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
                text = if (palavrasChave.isEmpty()) {
                    "Continuar sem palavras-chave"
                } else {
                    "Continuar"
                },
                fontWeight = FontWeight.Bold
            )
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