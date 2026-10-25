package com.example.filmedavez.model

data class GeneroFilme(
    val id: Int,
    val nome: String
)

val generosDisponiveis = listOf(
    GeneroFilme(28, "Ação"),
    GeneroFilme(12, "Aventura"),
    GeneroFilme(16, "Animação"),
    GeneroFilme(35, "Comédia"),
    GeneroFilme(27, "Terror"),
    GeneroFilme(53, "Suspense"),
    GeneroFilme(878, "Ficção científica"),
    GeneroFilme(10749, "Romance")
)
