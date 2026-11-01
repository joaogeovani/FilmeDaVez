package com.example.filmedavez.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    val cores = MaterialTheme.colorScheme

    // Mantém as preferências recebidas da MainActivity
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

    // Mantém os países disponíveis na versão anterior
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

    // Valida e aplica os filtros selecionados
    fun aplicarFiltros() {

        val notaMin = notaMinTexto.trim()
            .replace(",", ".").toDoubleOrNull()

        val notaMax = notaMaxTexto.trim()
            .replace(",", ".").toDoubleOrNull()

        val anoMin = anoMinTexto.trim().toIntOrNull()
        val anoMax = anoMaxTexto.trim().toIntOrNull()

        val durMin = duracaoMinTexto.trim().toIntOrNull()
        val durMax = duracaoMaxTexto.trim().toIntOrNull()

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
                        .takeIf { it.isNotBlank() }
                )
            )
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
                text = "ETAPA 03 DE 03",
                color = cores.secondary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = "Filtros",
                color = cores.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Indicador visual da etapa final
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
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(
                        cores.primary,
                        RoundedCornerShape(50)
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Personalize sua busca",
            color = cores.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Refine os resultados conforme suas " +
                    "preferências. Todos os filtros são opcionais.",
            color = cores.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )

        // Card de avaliação
        CardFiltroCinema(
            titulo = "Avaliação TMDB",
            descricao = "Escolha as notas desejadas, de 0 a 10."
        ) {

            CampoIntervaloCinema(
                minimo = notaMinTexto,
                maximo = notaMaxTexto,
                onMinimoChange = { notaMinTexto = it },
                onMaximoChange = { notaMaxTexto = it },
                teclado = KeyboardType.Decimal
            )
        }

        // Card de lançamento
        CardFiltroCinema(
            titulo = "Ano de lançamento",
            descricao = "Defina um ano específico ou intervalo."
        ) {

            CampoIntervaloCinema(
                minimo = anoMinTexto,
                maximo = anoMaxTexto,
                onMinimoChange = { anoMinTexto = it },
                onMaximoChange = { anoMaxTexto = it },
                teclado = KeyboardType.Number
            )
        }

        // Card de duração
        CardFiltroCinema(
            titulo = "Duração do filme",
            descricao = "Informe a duração em minutos."
        ) {

            CampoIntervaloCinema(
                minimo = duracaoMinTexto,
                maximo = duracaoMaxTexto,
                onMinimoChange = { duracaoMinTexto = it },
                onMaximoChange = { duracaoMaxTexto = it },
                teclado = KeyboardType.Number
            )
        }

        // Card de país de origem
        CardFiltroCinema(
            titulo = "País de origem",
            descricao = "Escolha onde o filme foi produzido."
        ) {

            val nomePais = paises
                .firstOrNull { it.first == paisSelecionado }
                ?.second ?: "Qualquer país"

            Box(modifier = Modifier.fillMaxWidth()) {

                OutlinedButton(
                    onClick = {
                        menuPaisesAberto = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
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
        }

        Text(
            text = "Dica: para buscar uma nota, ano ou " +
                    "duração exata, preencha o mesmo valor " +
                    "nos campos mínimo e máximo.",
            color = cores.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )

        // Exibe possíveis erros de validação
        if (mensagemErro != null) {

            Text(
                text = mensagemErro.orEmpty(),
                color = cores.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                aplicarFiltros()
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
                text = "Aplicar filtros e continuar",
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedButton(
            onClick = {
                onContinuar(FiltrosFilmes())
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Continuar sem filtros")
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

// Card reutilizável para organizar os filtros
@Composable
private fun CardFiltroCinema(
    titulo: String,
    descricao: String,
    conteudo: @Composable ColumnScope.() -> Unit
) {
    val cores = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cores.surfaceVariant,
        shape = RoundedCornerShape(20.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = titulo,
                color = cores.onBackground,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = descricao,
                color = cores.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            conteudo()
        }
    }
}

// Campos reutilizáveis de valor mínimo e máximo
@Composable
private fun CampoIntervaloCinema(
    minimo: String,
    maximo: String,
    onMinimoChange: (String) -> Unit,
    onMaximoChange: (String) -> Unit,
    teclado: KeyboardType
) {
    val cores = MaterialTheme.colorScheme

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = cores.surface,
        unfocusedContainerColor = cores.surface,
        focusedBorderColor = cores.primary,
        unfocusedBorderColor = cores.outline,
        focusedTextColor = cores.onSurface,
        unfocusedTextColor = cores.onSurface,
        cursorColor = cores.primary
    )

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
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = coresCampo
        )

        OutlinedTextField(
            value = maximo,
            onValueChange = onMaximoChange,
            label = { Text("Máximo") },
            keyboardOptions = KeyboardOptions(
                keyboardType = teclado
            ),
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = coresCampo
        )
    }
}
