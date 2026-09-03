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
    val goles: Int = 0,
    val asistencias: Int = 0,
    val notas: String = "",
    val jugadoresIds: List<String> = emptyList()
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
}
