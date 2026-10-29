package com.example.filmedavez.model

import java.text.Normalizer
import java.util.Locale

object AnalisadorSinopse {

    // Expressões relacionadas aos temas pesquisados
    private val temasRelacionados = mapOf(
        "assassino" to listOf(
            "assassino", "assassina", "assassinato",
            "assassinar", "homicidio", "matador",
            "serial killer", "killer", "murderer", "murder"
        ),

        "viagem no tempo" to listOf(
            "viagem no tempo", "viajar no tempo",
            "viaja no tempo", "voltar no tempo",
            "maquina do tempo", "time travel",
            "time machine"
        ),

        "inteligencia artificial" to listOf(
            "inteligencia artificial",
            "inteligencias artificiais",
            "artificial intelligence"
        ),

        "alienigenas" to listOf(
            "alienigena", "alienigenas",
            "extraterrestre", "extraterrestres",
            "alien", "aliens"
        ),

        "zumbis" to listOf(
            "zumbi", "zumbis", "zombie", "zombies"
        )
    )

    fun contarTemasEncontrados(
        sinopse: String,
        temas: List<String>
    ): Int {

        if (sinopse.isBlank() || temas.isEmpty()) {
            return 0
        }

        val textoNormalizado = " ${normalizar(sinopse)} "

        return temas
            .map { normalizar(it) }
            .filter { it.isNotBlank() }
            .distinct()
            .count { tema ->

                val variacoes = when (tema) {
                    "assassino", "assassina", "assassinato" ->
                        temasRelacionados["assassino"]

                    "time travel", "viagem no tempo" ->
                        temasRelacionados["viagem no tempo"]

                    "inteligencia artificial" ->
                        temasRelacionados["inteligencia artificial"]

                    "alienigena", "alienigenas" ->
                        temasRelacionados["alienigenas"]

                    "zumbi", "zumbis" ->
                        temasRelacionados["zumbis"]

                    else -> null
                } ?: listOf(tema)

                variacoes.any { variacao ->
                    val termo = normalizar(variacao)
                    textoNormalizado.contains(" $termo ")
                }
            }
    }

    private fun normalizar(texto: String): String {

        val minusculo = texto.lowercase(Locale.ROOT)

        val semAcentos = Normalizer.normalize(
            minusculo,
            Normalizer.Form.NFD
        ).replace(Regex("\\p{M}+"), "")

        return semAcentos
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
    }
}
