package com.example.filmedavez.model

import java.text.Normalizer
import java.util.Locale

object NormalizadorTemas {

    private val equivalencias = mapOf(
        "viagem no tempo" to "time travel",
        "inteligencia artificial" to "artificial intelligence",
        "espaco" to "space",
        "robo" to "robot",
        "robos" to "robot",
        "alienigenas" to "alien",
        "zumbi" to "zombie",
        "zumbis" to "zombie",
        "amizade" to "friendship"
    )

    fun converterParaBusca(tema: String): String {

        val temaNormalizado = normalizarTexto(tema)

        return equivalencias[temaNormalizado]
            ?: tema.trim()
    }

    private fun normalizarTexto(texto: String): String {

        val textoMinusculo = texto
            .trim()
            .lowercase(Locale.ROOT)

        val semAcentos = Normalizer.normalize(
            textoMinusculo,
            Normalizer.Form.NFD
        ).replace(Regex("\\p{M}+"), "")

        return semAcentos.replace(Regex("\\s+"), " ")
    }
}
