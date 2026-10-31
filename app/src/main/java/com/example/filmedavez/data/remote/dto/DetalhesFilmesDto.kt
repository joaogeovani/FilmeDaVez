package com.example.filmedavez.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DetalhesFilmeDto(

    val id: Int,

    val title: String,

    val overview: String,

    @SerializedName("poster_path")
    val posterPath: String?,

    @SerializedName("release_date")
    val releaseDate: String?,

    @SerializedName("vote_average")
    val voteAverage: Double,

    val runtime: Int?,

    val genres: List<GeneroDetalheDto>
)

data class GeneroDetalheDto(
    val id: Int,
    val name: String
)
