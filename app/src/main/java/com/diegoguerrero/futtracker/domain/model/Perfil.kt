package com.diegoguerrero.futtracker.domain.model

data class Perfil(
    val id: Long = 1,
    val nombre: String = "Mi Jugador",
    val fotoUri: String? = null,
    val posicionFavorita: Posicion = Posicion.DC,
    val nivel: Int = 4,
    val sincronizadoConJugadores: Boolean = true
)
