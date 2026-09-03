package com.diegoguerrero.futtracker.domain.model

enum class TipoPosicion(val label: String, val shortLabel: String) {
    POR("Portero", "POR"),
    DEF("Defensa", "DEF"),
    MED("Centrocampista", "MED"),
    DEL("Delantero", "DEL")
}

data class Jugador(
    val id: String = java.util.UUID.randomUUID().toString(),
    val nombre: String,
    val posicionesPrimarias: Set<Posicion> = emptySet(),
    val posicionesSecundarias: Set<Posicion> = emptySet()
)