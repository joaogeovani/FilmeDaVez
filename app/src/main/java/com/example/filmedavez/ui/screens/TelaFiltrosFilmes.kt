package com.example.filmedavez.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.filmedavez.model.FiltrosFilmes

@Composable
fun TelaFiltrosFilmes(
    filtros: FiltrosFilmes,
    onContinuar: (FiltrosFilmes) -> Unit,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Valores digitados pelo usuário
    var notaMinTexto by rememberSaveable {
        mutableStateOf(filtros.notaMinima?.toString() ?: "")
    }
    var notaMaxTexto by rememberSaveable {
        mutableStateOf(filtros.notaMaxima?.toString() ?: "")
    }

    var anoMinTexto by rememberSaveable {
        mutableStateOf(filtros.anoInicial?.toString() ?: "")
    }
    var anoMaxTexto by rememberSaveable {
        mutableStateOf(filtros.anoFinal?.toString() ?: "")
    }

    var duracaoMinTexto by rememberSaveable {
        mutableStateOf(filtros.duracaoMinima?.toString() ?: "")
    }
    var duracaoMaxTexto by rememberSaveable {
        mutableStateOf(filtros.duracaoMaxima?.toString() ?: "")
    }

    var paisSelecionado by rememberSaveable {
        mutableStateOf(filtros.paisOrigem ?: "")
    }

    var menuPaisesAberto by remember {
        mutableStateOf(false)
    }

    var mensagemErro by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    // Lista inicial de países
    val paises = listOf(
        "BR" to "Brasil",
        "US" to "Estados Unidos",
        "JP" to "Japão",
        "KR" to "Coreia do Sul",
        "GB" to "Reino Unido",
        "FR" to "França",
        "DE" to "Alemanha",
        "IT" to "Itália",
        "ES" to "Espanha",
        "CA" to "Canadá",
        "IN" to "Índia",
        "MX" to "México",
        "CN" to "China",
        "AU" to "Austrália"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Filtros de filmes",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Personalize sua busca. Todos os filtros são opcionais."
        )

        Spacer(modifier = Modifier.height(24.dp))

        CampoIntervalo(
            titulo = "Avaliação TMDB (0 a 10)",
            minimo = notaMinTexto,
            maximo = notaMaxTexto,
            onMinimoChange = { notaMinTexto = it },
            onMaximoChange = { notaMaxTexto = it },
            teclado = KeyboardType.Decimal
        )

        CampoIntervalo(
            titulo = "Ano de lançamento",
            minimo = anoMinTexto,
            maximo = anoMaxTexto,
            onMinimoChange = { anoMinTexto = it },
            onMaximoChange = { anoMaxTexto = it },
            teclado = KeyboardType.Number
        )

        CampoIntervalo(
            titulo = "Duração (minutos)",
            minimo = duracaoMinTexto,
            maximo = duracaoMaxTexto,
            onMinimoChange = { duracaoMinTexto = it },
            onMaximoChange = { duracaoMaxTexto = it },
            teclado = KeyboardType.Number
        )

        Text(
            text = "País de origem",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth()) {

            OutlinedButton(
                onClick = { menuPaisesAberto = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                val nomePais = paises
                    .firstOrNull { it.first == paisSelecionado }
                    ?.second ?: "Qualquer país"

                Text(nomePais)
            }

            DropdownMenu(
                expanded = menuPaisesAberto,
                onDismissRequest = {
                    menuPaisesAberto = false
                }
            ) {
                DropdownMenuItem(
                    text = { Text("Qualquer país") },
                    onClick = {
                        paisSelecionado = ""
                        menuPaisesAberto = false
                    }
                )

                paises.forEach { (codigo, nome) ->
                    DropdownMenuItem(
                        text = { Text(nome) },
                        onClick = {
                            paisSelecionado = codigo
                            menuPaisesAberto = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (mensagemErro != null) {
            Text(
                text = mensagemErro!!,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                val notaMin = notaMinTexto.trim()
                    .replace(",", ".").toDoubleOrNull()

                val notaMax = notaMaxTexto.trim()
                    .replace(",", ".").toDoubleOrNull()

                val anoMin = anoMinTexto.trim().toIntOrNull()
                val anoMax = anoMaxTexto.trim().toIntOrNull()

                val durMin = duracaoMinTexto.trim().toIntOrNull()
                val durMax = duracaoMaxTexto.trim().toIntOrNull()

                // Verifica entradas não numéricas
                val formatoInvalido =
                    (notaMinTexto.isNotBlank() && notaMin == null) ||
                            (notaMaxTexto.isNotBlank() && notaMax == null) ||
                            (anoMinTexto.isNotBlank() && anoMin == null) ||
                            (anoMaxTexto.isNotBlank() && anoMax == null) ||
                            (duracaoMinTexto.isNotBlank() && durMin == null) ||
                            (duracaoMaxTexto.isNotBlank() && durMax == null)

                val erro = when {
                    formatoInvalido ->
                        "Preencha os campos com números válidos."

                    (notaMin != null && notaMin !in 0.0..10.0) ||
                            (notaMax != null && notaMax !in 0.0..10.0) ->
                        "A avaliação deve estar entre 0 e 10."

                    (anoMin != null && anoMin !in 1888..2100) ||
                            (anoMax != null && anoMax !in 1888..2100) ->
                        "Informe anos entre 1888 e 2100."

                    (durMin != null && durMin <= 0) ||
                            (durMax != null && durMax <= 0) ->
                        "A duração deve ser maior que zero."

                    notaMin != null && notaMax != null &&
                            notaMin > notaMax ->
                        "A nota mínima não pode superar a máxima."

                    anoMin != null && anoMax != null &&
                            anoMin > anoMax ->
                        "O ano inicial não pode superar o final."

                    durMin != null && durMax != null &&
                            durMin > durMax ->
                        "A duração mínima não pode superar a máxima."

                    else -> null
                }

                if (erro != null) {
                    mensagemErro = erro
                } else {
                    mensagemErro = null

                    onContinuar(
                        FiltrosFilmes(
                            notaMinima = notaMin,
                            notaMaxima = notaMax,
                            anoInicial = anoMin,
                            anoFinal = anoMax,
                            duracaoMinima = durMin,
                            duracaoMaxima = durMax,
                            paisOrigem = paisSelecionado
                                .ifBlank { null }
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aplicar filtros e continuar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                onContinuar(FiltrosFilmes())
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar sem filtros")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onVoltar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar")
        }
    }
}

// Componente reutilizado nos três intervalos
@Composable
private fun CampoIntervalo(
    titulo: String,
    minimo: String,
    maximo: String,
    onMinimoChange: (String) -> Unit,
    onMaximoChange: (String) -> Unit,
    teclado: KeyboardType
) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = minimo,
            onValueChange = onMinimoChange,
            label = { Text("Mínimo") },
            keyboardOptions = KeyboardOptions(
                keyboardType = teclado
            ),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = maximo,
            onValueChange = onMaximoChange,
            label = { Text("Máximo") },
            keyboardOptions = KeyboardOptions(
                keyboardType = teclado
            ),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))
}
