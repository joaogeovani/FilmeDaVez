package com.example.filmedavez.model

data class FiltrosFilmes(

    // Avaliação do TMDB: 0.0 a 10.0
    val notaMinima: Double? = null,
    val notaMaxima: Double? = null,

    // Ano de lançamento
    val anoInicial: Int? = null,
    val anoFinal: Int? = null,

    // Duração em minutos
    val duracaoMinima: Int? = null,
    val duracaoMaxima: Int? = null,

    // Código do país de origem: BR, US, JP...
    val paisOrigem: String? = null

) {

    val possuiFiltros: Boolean
        get() =
            notaMinima != null ||
                    notaMaxima != null ||
                    anoInicial != null ||
                    anoFinal != null ||
                    duracaoMinima != null ||
                    duracaoMaxima != null ||
                    paisOrigem != null
}
