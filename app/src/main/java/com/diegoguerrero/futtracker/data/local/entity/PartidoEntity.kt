package com.diegoguerrero.futtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diegoguerrero.futtracker.domain.model.Clima
import com.diegoguerrero.futtracker.domain.model.EquipoColor
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
    val posicionesSecundarias: String = "",
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
    val jugadoPorMi: Boolean = true,
    val esFavorito: Boolean = false,
    val clima: String = "",
    val fotoUri: String? = null,
    val equipoJugado: String? = null,
    val estadioId: Long? = null
) {
    fun toDomain(): Partido {
        val posJugada = runCatching { Posicion.valueOf(posicionJugada) }.getOrDefault(Posicion.DC)
        val posSecundarias = if (posicionesSecundarias.isBlank()) {
            emptySet()
        } else {
            posicionesSecundarias.split(",")
                .mapNotNull { name -> runCatching { Posicion.valueOf(name.trim()) }.getOrNull() }
                .toSet()
        }
        val posJugadas = if (posicionesJugadas.isBlank()) {
            setOf(posJugada) + posSecundarias
        } else {
            posicionesJugadas.split(",")
                .mapNotNull { name -> runCatching { Posicion.valueOf(name.trim()) }.getOrNull() }
                .toSet().ifEmpty { setOf(posJugada) }
        }

        val idsGeneral = if (jugadoresIds.isBlank()) emptyList() else jugadoresIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val idsMiEquipo = if (jugadoresMiEquipo.isBlank()) idsGeneral else jugadoresMiEquipo.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val idsRival = if (jugadoresEquipoRival.isBlank()) emptyList() else jugadoresEquipoRival.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val climaEnum = if (clima.isBlank() || clima == "NINGUNO") null else Clima.fromString(clima)
        val equipoEnum = equipoJugado?.let { runCatching { EquipoColor.valueOf(it) }.getOrNull() }

        return Partido(
            id = id,
            fecha = fecha,
            modoJuego = runCatching { TipoFutbol.valueOf(modoJuego) }.getOrDefault(TipoFutbol.FUTSAL),
            golesAFavor = golesAFavor,
            golesEnContra = golesEnContra,
            posicionJugada = posJugada,
            posicionesJugadas = posJugadas,
            posicionesSecundarias = posSecundarias,
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
            jugadoPorMi = jugadoPorMi,
            esFavorito = esFavorito,
            clima = climaEnum,
            fotoUri = fotoUri,
            equipoJugado = equipoEnum,
            estadioId = estadioId
        )
    }
}

fun Partido.toEntity(): PartidoEntity {
    val allPos = (setOf(posicionJugada) + posicionesJugadas + posicionesSecundarias)
    val posJugadasStr = allPos.joinToString(",") { it.name }
    val posSecundariasStr = posicionesSecundarias.joinToString(",") { it.name }

    return PartidoEntity(
        id = id,
        fecha = fecha,
        modoJuego = modoJuego.name,
        golesAFavor = golesAFavor,
        golesEnContra = golesEnContra,
        posicionJugada = posicionJugada.name,
        posicionesJugadas = posJugadasStr,
        posicionesSecundarias = posSecundariasStr,
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
        jugadoPorMi = jugadoPorMi,
        esFavorito = esFavorito,
        clima = clima?.name ?: "",
        fotoUri = fotoUri,
        equipoJugado = equipoJugado?.name,
        estadioId = estadioId
    )
}
