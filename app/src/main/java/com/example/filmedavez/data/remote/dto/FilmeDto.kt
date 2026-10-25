package com.example.filmedavez.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FilmeDto(
    val id: Int,
    val title: String,
    val overview: String,

    @SerializedName("poster_path")
    val posterPath: String?,

    @SerializedName("vote_average")
    val voteAverage: Double,

    @SerializedName("vote_count")
    val voteCount: Int,

    @SerializedName("genre_ids")
    val genreIds: List<Int>,

    @SerializedName("release_date")
    val releaseDate: String?
)