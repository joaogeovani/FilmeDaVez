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
        pagina: Int = 1,

        // Filtros de avaliação
        @Query("vote_average.gte")
        notaMinima: Double? = null,

        @Query("vote_average.lte")
        notaMaxima: Double? = null,

        // Filtros de lançamento
        @Query("primary_release_date.gte")
        dataInicial: String? = null,

        @Query("primary_release_date.lte")
        dataFinal: String? = null,

        // Filtros de duração
        @Query("with_runtime.gte")
        duracaoMinima: Int? = null,

        @Query("with_runtime.lte")
        duracaoMaxima: Int? = null,

        // País de origem
        @Query("with_origin_country")
        paisOrigem: String? = null

    ): RespostaFilmesDto

    @GET("search/keyword")
    suspend fun pesquisarPalavrasChave(
        @Query("query") texto: String,
        @Query("page") pagina: Int = 1
    ): RespostaPalavrasChaveDto

}


