package com.example.filmedavez.data.remote.dto

import com.google.gson.annotations.SerializedName

// Resposta principal do endpoint
data class DisponibilidadeFilmeDto(

    val id: Int,

    // Cada país possui suas próprias plataformas
    val results: Map<String, DisponibilidadePorPaisDto>?
)

// Disponibilidade de um filme em determinado país
data class DisponibilidadePorPaisDto(

    // Link para consultar as opções no TMDB
    val link: String?,

    // Streaming por assinatura
    val flatrate: List<ProvedorFilmeDto>?,

    // Aluguel digital
    val rent: List<ProvedorFilmeDto>?,

    // Compra digital
    val buy: List<ProvedorFilmeDto>?,

    // Disponível gratuitamente
    val free: List<ProvedorFilmeDto>?,

    // Gratuito com anúncios
    val ads: List<ProvedorFilmeDto>?
)

// Representa uma plataforma
data class ProvedorFilmeDto(

    @SerializedName("provider_id")
    val providerId: Int,

    @SerializedName("provider_name")
    val providerName: String,

    @SerializedName("logo_path")
    val logoPath: String?,

    @SerializedName("display_priority")
    val displayPriority: Int?
)
