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
    val paradas: Int = 0,
    val clima: String = "",
    val fotoUri: String? = null,
    val equipoJugado: String? = null,
    val estadioId: Long? = null,
    val jugadoresDetalleJson: String = ""
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
            paradas = paradas,
            clima = climaEnum,
            fotoUri = fotoUri,
            equipoJugado = equipoEnum,
            estadioId = estadioId,
            jugadoresDetalle = deserializarJugadoresDetalle(jugadoresDetalleJson)
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
        paradas = paradas,
        clima = clima?.name ?: "",
        fotoUri = fotoUri,
        equipoJugado = equipoJugado?.name,
        estadioId = estadioId,
        jugadoresDetalleJson = serializarJugadoresDetalle(jugadoresDetalle)
    )
}

fun serializarJugadoresDetalle(lista: List<com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorPartido>): String {
    if (lista.isEmpty()) return ""
    return runCatching {
        val array = org.json.JSONArray()
        for (item in lista) {
            val obj = org.json.JSONObject().apply {
                put("id", item.jugadorId)
                put("esMiEquipo", item.esMiEquipo)
                put("posPrin", item.posicionPrincipal.name)
                put("posSec", item.posicionesSecundarias.joinToString(",") { it.name })
                put("x", item.posX.toDouble())
                put("y", item.posY.toDouble())
                put("statsReg", item.statsRegistradas)
                put("goles", item.goles)
                put("asistencias", item.asistencias)
                put("palos", item.tirosAlPalo)
                put("gZurda", item.golesZurda)
                put("gDiestra", item.golesDiestra)
                put("gCabeza", item.golesCabeza)
                put("gOtro", item.golesOtro)
                put("gChilena", item.golesChilena)
                put("gTacon", item.golesTacon)
                put("gFuera", item.golesFueraArea)
                put("paradas", item.paradas)
            }
            array.put(obj)
        }
        array.toString()
    }.getOrDefault("")
}

fun deserializarJugadoresDetalle(json: String?): List<com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorPartido> {
    if (json.isNullOrBlank()) return emptyList()
    return runCatching {
        val array = org.json.JSONArray(json)
        val result = mutableListOf<com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorPartido>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val posPrin = runCatching { Posicion.valueOf(obj.optString("posPrin", Posicion.MC.name)) }.getOrDefault(Posicion.MC)
            val posSecStr = obj.optString("posSec", "")
            val posSec = if (posSecStr.isBlank()) emptySet() else {
                posSecStr.split(",").mapNotNull { s -> runCatching { Posicion.valueOf(s.trim()) }.getOrNull() }.toSet()
            }
            result.add(
                com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorPartido(
                    jugadorId = obj.optString("id", ""),
                    esMiEquipo = obj.optBoolean("esMiEquipo", true),
                    posicionPrincipal = posPrin,
                    posicionesSecundarias = posSec,
                    posX = obj.optDouble("x", 0.5).toFloat(),
                    posY = obj.optDouble("y", 0.5).toFloat(),
                    statsRegistradas = obj.optBoolean("statsReg", false),
                    goles = obj.optInt("goles", 0),
                    asistencias = obj.optInt("asistencias", 0),
                    tirosAlPalo = obj.optInt("palos", 0),
                    golesZurda = obj.optInt("gZurda", 0),
                    golesDiestra = obj.optInt("gDiestra", 0),
                    golesCabeza = obj.optInt("gCabeza", 0),
                    golesOtro = obj.optInt("gOtro", 0),
                    golesChilena = obj.optInt("gChilena", 0),
                    golesTacon = obj.optInt("gTacon", 0),
                    golesFueraArea = obj.optInt("gFuera", 0),
                    paradas = obj.optInt("paradas", 0)
                )
            )
        }
        result
    }.getOrDefault(emptyList())
}
