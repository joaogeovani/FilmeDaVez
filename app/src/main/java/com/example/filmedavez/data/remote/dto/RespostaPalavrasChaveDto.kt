package com.example.filmedavez.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RespostaPalavrasChaveDto(
    val page: Int,
    val results: List<PalavraChaveDto>,

    @SerializedName("total_pages")
    val totalPages: Int,

    @SerializedName("total_results")
    val totalResults: Int
)