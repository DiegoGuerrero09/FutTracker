package com.diegoguerrero.futtracker.domain.model

data class Estadio(
    val id: Long = 0,
    val nombre: String,
    val modalidades: Set<TipoFutbol> = setOf(TipoFutbol.FUTSAL),
    val fotoUri: String? = null,
    val esFavorito: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis()
)
