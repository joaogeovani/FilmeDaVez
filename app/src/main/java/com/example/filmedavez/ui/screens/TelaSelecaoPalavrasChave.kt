package com.example.filmedavez.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
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
    var textoDigitado by rememberSaveable {
        mutableStateOf("")
    }

    val novaPalavra = textoDigitado.trim()

    val podeAdicionar = novaPalavra.isNotEmpty() &&
            palavrasChave.size < 5 &&
            palavrasChave.none {
                it.equals(novaPalavra, ignoreCase = true)
            }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Escolha os temas",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Adicione até 5 palavras-chave ou temas " +
                    "que vocês gostariam de encontrar nos filmes."
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = textoDigitado,
            onValueChange = {
                textoDigitado = it
            },
            label = {
                Text("Palavra-chave")
            },
            placeholder = {
                Text("Ex.: Viagem no tempo")
            },
            singleLine = true,
            enabled = palavrasChave.size < 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (podeAdicionar) {
                    onAdicionarPalavra(novaPalavra)
                    textoDigitado = ""
                }
            },
            enabled = podeAdicionar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Adicionar tema")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${palavrasChave.size} de 5 temas adicionados"
        )

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            palavrasChave.forEach { palavra ->
                AssistChip(
                    onClick = {
                        onRemoverPalavra(palavra)
                    },
                    label = {
                        Text("✕ $palavra")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onContinuar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar")
        }

        OutlinedButton(
            onClick = onVoltar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar aos gêneros")
        }
    }
}
