package com.example.filmedavez.data.remote

import com.example.filmedavez.data.remote.dto.RespostaFilmesDto
import com.example.filmedavez.data.remote.dto.RespostaPalavrasChaveDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApiService {

    @GET("discover/movie")
    suspend fun descobrirFilmes(
        @Query("with_genres")
        generos: String,

        @Query("with_keywords")
        palavrasChave: String? = null,

        @Query("language")
        idioma: String = "pt-BR",

        @Query("include_adult")
        incluirAdultos: Boolean = false,

        @Query("sort_by")
        ordenarPor: String = "popularity.desc",

        @Query("page")
        pagina: Int = 1

    ): RespostaFilmesDto

    @GET("search/keyword")
    suspend fun pesquisarPalavrasChave(
        @Query("query") texto: String,
        @Query("page") pagina: Int = 1
    ): RespostaPalavrasChaveDto

}


