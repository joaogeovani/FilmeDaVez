package com.example.filmedavez.data.remote

import com.example.filmedavez.data.remote.dto.RespostaFilmesDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApiService {

    @GET("discover/movie")
    suspend fun descobrirFilmes(
        @Query("with_genres")
        generos: String,

        @Query("language")
        idioma: String = "pt-BR",

        @Query("include_adult")
        incluirAdultos: Boolean = false,

        @Query("sort_by")
        ordenarPor: String = "popularity.desc",

        @Query("page")
        pagina: Int = 1

    ): RespostaFilmesDto
}


