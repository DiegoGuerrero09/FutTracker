package com.diegoguerrero.futtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.domain.model.TipoFutbol

@Entity(tableName = "partidos")
data class PartidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: Long,
    val modoJuego: String,
    val golesAFavor: Int,
    val golesEnContra: Int,
    val posicionJugada: String,
    val goles: Int,
    val asistencias: Int,
    val notas: String,
    val jugadoresIds: String
) {
    fun toDomain(): Partido {
        return Partido(
            id = id,
            fecha = fecha,
            modoJuego = runCatching { TipoFutbol.valueOf(modoJuego) }.getOrDefault(TipoFutbol.FUTSAL),
            golesAFavor = golesAFavor,
            golesEnContra = golesEnContra,
            posicionJugada = runCatching { Posicion.valueOf(posicionJugada) }.getOrDefault(Posicion.DC),
            goles = goles,
            asistencias = asistencias,
            notas = notas,
            jugadoresIds = if (jugadoresIds.isBlank()) emptyList() else jugadoresIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        )
    }
}

fun Partido.toEntity(): PartidoEntity {
    return PartidoEntity(
        id = id,
        fecha = fecha,
        modoJuego = modoJuego.name,
        golesAFavor = golesAFavor,
        golesEnContra = golesEnContra,
        posicionJugada = posicionJugada.name,
        goles = goles,
        asistencias = asistencias,
        notas = notas,
        jugadoresIds = jugadoresIds.joinToString(",")
    )
}
