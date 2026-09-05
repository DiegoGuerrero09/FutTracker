package com.diegoguerrero.futtracker.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class Clima(val label: String, val emoji: String) {
    DESPEJADO("Despejado", "☀️"),
    NUBLADO("Nublado", "⛅"),
    LLUVIOSO("Lluvioso", "🌧️");

    companion object {
        val SOLEADO = DESPEJADO

        fun fromString(str: String?): Clima? {
            return when (str?.trim()?.uppercase()) {
                "DESPEJADO", "SOLEADO" -> DESPEJADO
                "NUBLADO" -> NUBLADO
                "LLUVIOSO" -> LLUVIOSO
                else -> null
            }
        }
    }
}

fun Clima.obtenerEmoji(hora: Int? = null): String {
    return when (this) {
        Clima.DESPEJADO -> if (hora != null && hora >= 20) "🌙" else "☀️"
        Clima.NUBLADO -> "⛅"
        Clima.LLUVIOSO -> "🌧️"
    }
}

fun Clima.obtenerEmojiParaFecha(fechaMillis: Long): String {
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = fechaMillis }
    val hora = cal.get(java.util.Calendar.HOUR_OF_DAY)
    return obtenerEmoji(hora)
}

enum class EquipoColor(val label: String, val emoji: String) {
    CLARO("Claro", "⚪"),
    OSCURO("Oscuro", "⚫")
}

data class Partido(
    val id: Long = 0,
    val fecha: Long = System.currentTimeMillis(),
    val modoJuego: TipoFutbol = TipoFutbol.FUTSAL,
    val golesAFavor: Int = 0,
    val golesEnContra: Int = 0,
    val posicionJugada: Posicion = Posicion.DC,
    val posicionesJugadas: Set<Posicion> = setOf(posicionJugada),
    val posicionesSecundarias: Set<Posicion> = emptySet(),
    val goles: Int = 0,
    val asistencias: Int = 0,
    val tirosAlPalo: Int = 0,
    val notas: String = "",
    val jugadoresIds: List<String> = emptyList(),
    val jugadoresMiEquipo: List<String> = emptyList(),
    val jugadoresEquipoRival: List<String> = emptyList(),
    val golesZurda: Int = 0,
    val golesDiestra: Int = 0,
    val golesCabeza: Int = 0,
    val golesOtro: Int = 0,
    val golesChilena: Int = 0,
    val golesTacon: Int = 0,
    val golesFueraArea: Int = 0,
    val duracionMinutos: Int = 60,
    val jugadoPorMi: Boolean = true,
    val esFavorito: Boolean = false,
    val clima: Clima? = null,
    val fotoUri: String? = null,
    val equipoJugado: EquipoColor? = null,
    val estadioId: Long? = null
) {
    val resultado: String
        get() = "$golesAFavor - $golesEnContra"

    val esVictoria: Boolean
        get() = golesAFavor > golesEnContra

    val esEmpate: Boolean
        get() = golesAFavor == golesEnContra

    val esDerrota: Boolean
        get() = golesAFavor < golesEnContra

    val localDate: LocalDate
        get() = Instant.ofEpochMilli(fecha).atZone(ZoneId.systemDefault()).toLocalDate()

    val totalGolesPorParteCuerpo: Int
        get() = golesDiestra + golesZurda + golesCabeza + golesOtro
}
