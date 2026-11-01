package com.example.filmedavez.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FilmeDaVezDarkColors = darkColorScheme(

    // Ações principais
    primary = LaranjaCinema,
    onPrimary = FundoCinema,

    // Elementos de apoio
    secondary = AzulCinema,
    onSecondary = FundoCinema,

    // Feedback e destaques positivos
    tertiary = VerdeCinema,
    onTertiary = FundoCinema,

    // Fundo do aplicativo
    background = FundoCinema,
    onBackground = TextoPrincipal,

    // Cards e superfícies
    surface = SuperficieCinema,
    onSurface = TextoPrincipal,

    // Superfícies alternativas
    surfaceVariant = SuperficieEscura,
    onSurfaceVariant = TextoSecundario,

    // Bordas
    outline = BordaCinema
)

@Composable
fun FilmeDaVezTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    // Inicialmente utilizamos sempre a identidade escura
    // do FilmeDaVez, independentemente do tema do celular.

    MaterialTheme(
        colorScheme = FilmeDaVezDarkColors,
        typography = Typography,
        content = content
    )
}