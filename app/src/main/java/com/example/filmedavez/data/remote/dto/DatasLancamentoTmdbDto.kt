package com.example.filmedavez.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DatasLancamentoTmdbDto(
    val id: Int,

    val results: List<LancamentosPaisDto>? = null
)

data class LancamentosPaisDto(

    @SerializedName("iso_3166_1")
    val pais: String? = null,

    @SerializedName("release_dates")
    val lancamentos: List<LancamentoFilmeDto>? = null
)

data class LancamentoFilmeDto(

    @SerializedName("release_date")
    val data: String? = null,

    @SerializedName("type")
    val tipo: Int? = null
)
