package com.diegoguerrero.futtracker.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class Partido(
    val id: Long = 0,
    val fecha: Long = System.currentTimeMillis(),
    val modoJuego: TipoFutbol = TipoFutbol.FUTSAL,
    val golesAFavor: Int = 0,
    val golesEnContra: Int = 0,
    val posicionJugada: Posicion = Posicion.DC,
    val posicionesJugadas: Set<Posicion> = setOf(posicionJugada),
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
    val duracionMinutos: Int = 60
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
