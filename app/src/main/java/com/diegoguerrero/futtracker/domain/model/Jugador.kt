package com.diegoguerrero.futtracker.domain.model

enum class TipoPosicion(val label: String, val shortLabel: String) {
    POR("Portero", "POR"),
    DEF("Defensa", "DEF"),
    MED("Centrocampista", "MED"),
    DEL("Delantero", "DEL")
}

data class Jugador(
    val id: Long = 0,
    val nombre: String,
    val posPrincipal: Posicion,
    val posSecundaria: Posicion? = null,
    val posTercera: Posicion? = null
)