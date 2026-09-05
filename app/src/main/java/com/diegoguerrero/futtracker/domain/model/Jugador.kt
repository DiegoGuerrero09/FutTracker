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
    val fotoUri: String? = null,
    val posicionesPrimarias: Set<Posicion> = emptySet(),
    val posicionesSecundarias: Set<Posicion> = emptySet(),
    val esFavorito: Boolean = false,
    val nivel: Int = 3,
    val esUsuarioPropio: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    val inicialesPosiciones: String
        get() = if (posicionesPrimarias.isEmpty()) {
            "-"
        } else {
            posicionesPrimarias.joinToString("/") { it.name }
        }
}

fun Jugador.nombreConTu(): String {
    val esYo = this.esUsuarioPropio || this.id == "usuario_propio_id"
    return if (esYo) "${this.nombre.removeSuffix(" (Tú)")} (Tú)" else this.nombre
}