package com.example.filmedavez.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun BarraProgressoFilme(
    progresso: Float,
    modifier: Modifier = Modifier
) {
    // Mantém o progresso entre 0% e 100%
    val progressoLimitado = progresso.coerceIn(0f, 1f)

    // Suaviza o avanço da película
    val progressoAnimado by animateFloatAsState(
        targetValue = progressoLimitado,
        animationSpec = tween(durationMillis = 400),
        label = "AnimacaoPelícula"
    )

    val corPreenchimento = MaterialTheme.colorScheme.primary
    val corFundo = Color(0xFF242424)
    val corDetalhes = Color.White

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    progressoLimitado,
                    0f..1f
                )
            }
    ) {
        val largura = size.width
        val altura = size.height

        val raio = 8.dp.toPx()

        // Fundo da película
        drawRoundRect(
            color = corFundo,
            cornerRadius = CornerRadius(raio)
        )

        // Preenchimento de acordo com o progresso
        clipRect(
            left = 0f,
            top = 0f,
            right = largura * progressoAnimado,
            bottom = altura
        ) {
            drawRoundRect(
                color = corPreenchimento,
                cornerRadius = CornerRadius(raio)
            )
        }

        // Borda externa da película
        drawRoundRect(
            color = corDetalhes.copy(alpha = 0.6f),
            cornerRadius = CornerRadius(raio),
            style = Stroke(width = 1.dp.toPx())
        )

        // Perfurações características da película
        val larguraFuro = 10.dp.toPx()
        val alturaFuro = 5.dp.toPx()
        val margem = 8.dp.toPx()
        val distanciaFuros = 20.dp.toPx()

        var posicaoX = margem

        while (posicaoX + larguraFuro < largura - margem) {

            // Perfurações superiores
            drawRoundRect(
                color = corDetalhes.copy(alpha = 0.85f),
                topLeft = Offset(
                    posicaoX,
                    5.dp.toPx()
                ),
                size = Size(larguraFuro, alturaFuro),
                cornerRadius = CornerRadius(1.dp.toPx())
            )

            // Perfurações inferiores
            drawRoundRect(
                color = corDetalhes.copy(alpha = 0.85f),
                topLeft = Offset(
                    posicaoX,
                    altura - 10.dp.toPx()
                ),
                size = Size(larguraFuro, alturaFuro),
                cornerRadius = CornerRadius(1.dp.toPx())
            )

            posicaoX += distanciaFuros
        }

        // Quadros internos da película
        val larguraQuadro = 30.dp.toPx()
        val alturaQuadro = 28.dp.toPx()
        val distanciaQuadros = 6.dp.toPx()

        var quadroX = margem

        while (quadroX + larguraQuadro < largura - margem) {

            val centroQuadro = quadroX + larguraQuadro / 2f

            val opacidade = if (
                centroQuadro <= largura * progressoAnimado
            ) {
                0.95f
            } else {
                0.35f
            }

            drawRoundRect(
                color = corDetalhes.copy(alpha = opacidade),
                topLeft = Offset(
                    quadroX,
                    (altura - alturaQuadro) / 2f
                ),
                size = Size(larguraQuadro, alturaQuadro),
                cornerRadius = CornerRadius(2.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            quadroX += larguraQuadro + distanciaQuadros
        }
    }
}
