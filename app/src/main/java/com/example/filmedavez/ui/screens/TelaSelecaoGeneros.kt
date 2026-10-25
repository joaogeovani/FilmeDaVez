package com.example.filmedavez.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Escolha os gêneros",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selecione os tipos de filmes que vocês querem assistir."
        )

        Spacer(modifier = Modifier.height(24.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            generosDisponiveis.forEach { genero ->
                FilterChip(
                    selected = genero.id in idsSelecionados,
                    onClick = {
                        onGeneroClick(genero.id)
                    },
                    label = {
                        Text(genero.nome)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${idsSelecionados.size} gênero(s) selecionado(s)"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onContinuar,
            enabled = idsSelecionados.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar")
        }

        OutlinedButton(
            onClick = onVoltar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar")
        }
    }
}
