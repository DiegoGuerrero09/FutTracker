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
    val posicionesJugadas: String = "",
    val goles: Int,
    val asistencias: Int,
    val tirosAlPalo: Int = 0,
    val notas: String,
    val jugadoresIds: String = "",
    val jugadoresMiEquipo: String = "",
    val jugadoresEquipoRival: String = "",
    val golesZurda: Int = 0,
    val golesDiestra: Int = 0,
    val golesCabeza: Int = 0,
    val golesOtro: Int = 0,
    val golesChilena: Int = 0,
    val golesTacon: Int = 0,
    val golesFueraArea: Int = 0,
    val duracionMinutos: Int = 60,
    val jugadoPorMi: Boolean = true
) {
    fun toDomain(): Partido {
        val posJugada = runCatching { Posicion.valueOf(posicionJugada) }.getOrDefault(Posicion.DC)
        val posJugadas = if (posicionesJugadas.isBlank()) {
            setOf(posJugada)
        } else {
            posicionesJugadas.split(",")
                .mapNotNull { name -> runCatching { Posicion.valueOf(name.trim()) }.getOrNull() }
                .toSet().ifEmpty { setOf(posJugada) }
        }

        val idsGeneral = if (jugadoresIds.isBlank()) emptyList() else jugadoresIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val idsMiEquipo = if (jugadoresMiEquipo.isBlank()) idsGeneral else jugadoresMiEquipo.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val idsRival = if (jugadoresEquipoRival.isBlank()) emptyList() else jugadoresEquipoRival.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        return Partido(
            id = id,
            fecha = fecha,
            modoJuego = runCatching { TipoFutbol.valueOf(modoJuego) }.getOrDefault(TipoFutbol.FUTSAL),
            golesAFavor = golesAFavor,
            golesEnContra = golesEnContra,
            posicionJugada = posJugada,
            posicionesJugadas = posJugadas,
            goles = goles,
            asistencias = asistencias,
            tirosAlPalo = tirosAlPalo,
            notas = notas,
            jugadoresIds = idsGeneral,
            jugadoresMiEquipo = idsMiEquipo,
            jugadoresEquipoRival = idsRival,
            golesZurda = golesZurda,
            golesDiestra = golesDiestra,
            golesCabeza = golesCabeza,
            golesOtro = golesOtro,
            golesChilena = golesChilena,
            golesTacon = golesTacon,
            golesFueraArea = golesFueraArea,
            duracionMinutos = duracionMinutos,
            jugadoPorMi = jugadoPorMi
        )
    }
}

fun Partido.toEntity(): PartidoEntity {
    val posJugadasStr = posicionesJugadas.joinToString(",") { it.name }
    val primaryPos = posicionesJugadas.firstOrNull() ?: posicionJugada

    return PartidoEntity(
        id = id,
        fecha = fecha,
        modoJuego = modoJuego.name,
        golesAFavor = golesAFavor,
        golesEnContra = golesEnContra,
        posicionJugada = primaryPos.name,
        posicionesJugadas = posJugadasStr,
        goles = goles,
        asistencias = asistencias,
        tirosAlPalo = tirosAlPalo,
        notas = notas,
        jugadoresIds = (jugadoresMiEquipo + jugadoresEquipoRival).distinct().joinToString(","),
        jugadoresMiEquipo = jugadoresMiEquipo.joinToString(","),
        jugadoresEquipoRival = jugadoresEquipoRival.joinToString(","),
        golesZurda = golesZurda,
        golesDiestra = golesDiestra,
        golesCabeza = golesCabeza,
        golesOtro = golesOtro,
        golesChilena = golesChilena,
        golesTacon = golesTacon,
        golesFueraArea = golesFueraArea,
        duracionMinutos = duracionMinutos,
        jugadoPorMi = jugadoPorMi
    )
}
