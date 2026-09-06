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

fun Clima.obtenerEmoji(hora: Int? = null, fechaMillis: Long? = null): String {
    return when (this) {
        Clima.DESPEJADO -> {
            if (hora == null) return "☀️"
            val cal = java.util.Calendar.getInstance()
            if (fechaMillis != null) {
                cal.timeInMillis = fechaMillis
            }
            val mes = cal.get(java.util.Calendar.MONTH) + 1 // 1..12
            val dia = cal.get(java.util.Calendar.DAY_OF_MONTH)
            // Verano: 21 de junio al 21 de septiembre (Sol hasta las 21h)
            val esVerano = (mes == 6 && dia >= 21) || mes == 7 || mes == 8 || (mes == 9 && dia <= 21)
            // Invierno: 21 de diciembre al 20 de marzo (Sol hasta las 19h)
            val esInvierno = (mes == 12 && dia >= 21) || mes == 1 || mes == 2 || (mes == 3 && dia <= 20)
            val limiteLuna = when {
                esVerano -> 21
                esInvierno -> 19
                else -> 20
            }
            if (hora >= limiteLuna || hora < 7) "🌙" else "☀️"
        }
        Clima.NUBLADO -> "⛅"
        Clima.LLUVIOSO -> "🌧️"
    }
}

fun Clima.obtenerEmojiParaFecha(fechaMillis: Long): String {
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = fechaMillis }
    val hora = cal.get(java.util.Calendar.HOUR_OF_DAY)
    return obtenerEmoji(hora, fechaMillis)
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
    val paradas: Int = 0,
    val clima: Clima? = null,
    val fotoUri: String? = null,
    val equipoJugado: EquipoColor? = null,
    val estadioId: Long? = null,
    val formacionMiEquipo: String? = null,
    val formacionRival: String? = null,
    val jugadoresDetalle: List<EstadisticasJugadorPartido> = emptyList()
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

data class EstadisticasJugadorPartido(
    val jugadorId: String,
    val esMiEquipo: Boolean = true,
    val posicionPrincipal: Posicion = Posicion.MC,
    val posicionesSecundarias: Set<Posicion> = emptySet(),
    val posX: Float = 0.5f,
    val posY: Float = 0.5f,
    val statsRegistradas: Boolean = false,
    val goles: Int = 0,
    val asistencias: Int = 0,
    val tirosAlPalo: Int = 0,
    val golesZurda: Int = 0,
    val golesDiestra: Int = 0,
    val golesCabeza: Int = 0,
    val golesOtro: Int = 0,
    val golesChilena: Int = 0,
    val golesTacon: Int = 0,
    val golesFueraArea: Int = 0,
    val paradas: Int = 0
)
