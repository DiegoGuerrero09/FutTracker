package com.diegoguerrero.futtracker.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Perfil
import com.diegoguerrero.futtracker.domain.repository.JugadorRepository
import com.diegoguerrero.futtracker.domain.repository.PartidoRepository
import com.diegoguerrero.futtracker.domain.repository.PerfilRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val perfilRepository: PerfilRepository,
    private val partidoRepository: PartidoRepository,
    private val jugadorRepository: JugadorRepository
) : ViewModel() {

    val perfil: StateFlow<Perfil> = perfilRepository.obtenerPerfil()
        .map { it ?: Perfil() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Perfil()
        )

    fun guardarPerfil(nuevoPerfil: Perfil) {
        viewModelScope.launch {
            perfilRepository.guardarPerfil(nuevoPerfil)

            if (nuevoPerfil.sincronizadoConJugadores) {
                val jugadoresActuales = jugadorRepository.obtenerJugadores().first()
                val usuarioExistente = jugadoresActuales.find { it.esUsuarioPropio }

                val posicionesAsignar = nuevoPerfil.posiciones.ifEmpty { setOf(nuevoPerfil.posicionFavorita) }

                val jugadorActualizado = usuarioExistente?.copy(
                    nombre = nuevoPerfil.nombre,
                    fotoUri = nuevoPerfil.fotoUri,
                    posicionesPrimarias = posicionesAsignar,
                    esUsuarioPropio = true
                ) ?: Jugador(
                    id = "usuario_propio_id",
                    nombre = nuevoPerfil.nombre,
                    fotoUri = nuevoPerfil.fotoUri,
                    posicionesPrimarias = posicionesAsignar,
                    esUsuarioPropio = true
                )

                if (usuarioExistente != null) {
                    jugadorRepository.actualizarJugador(jugadorActualizado)
                } else {
                    jugadorRepository.insertarJugador(jugadorActualizado)
                }
            }
        }
    }

    suspend fun exportarDatosJson(): String {
        val p = perfil.first()
        val jugadores = jugadorRepository.obtenerJugadores().first()
        val partidos = partidoRepository.obtenerPartidos().first()

        val root = JSONObject()
        root.put("version", 1)
        root.put("app", "FutTracker")
        root.put("fechaExportacion", System.currentTimeMillis())

        val perfilObj = JSONObject().apply {
            put("nombre", p.nombre)
            p.fotoUri?.let { put("fotoUri", it) }
            put("posicionFavorita", p.posicionFavorita.name)
            put("posiciones", JSONArray(p.posiciones.map { it.name }))
            put("sincronizadoConJugadores", p.sincronizadoConJugadores)
        }
        root.put("perfil", perfilObj)

        val jugadoresArr = JSONArray()
        jugadores.forEach { j ->
            val jObj = JSONObject().apply {
                put("id", j.id)
                put("nombre", j.nombre)
                j.fotoUri?.let { put("fotoUri", it) }
                put("esFavorito", j.esFavorito)
                put("esUsuarioPropio", j.esUsuarioPropio)
                put("posicionesPrimarias", JSONArray(j.posicionesPrimarias.map { it.name }))
                put("posicionesSecundarias", JSONArray(j.posicionesSecundarias.map { it.name }))
            }
            jugadoresArr.put(jObj)
        }
        root.put("jugadores", jugadoresArr)

        val partidosArr = JSONArray()
        partidos.forEach { part ->
            val pObj = JSONObject().apply {
                put("id", part.id)
                put("fecha", part.fecha)
                put("modoJuego", part.modoJuego.name)
                put("duracionMinutos", part.duracionMinutos)
                put("golesAFavor", part.golesAFavor)
                put("golesEnContra", part.golesEnContra)
                put("posicionJugada", part.posicionJugada.name)
                put("posicionesJugadas", JSONArray(part.posicionesJugadas.map { it.name }))
                put("goles", part.goles)
                put("asistencias", part.asistencias)
                put("tirosAlPalo", part.tirosAlPalo)
                put("golesFueraArea", part.golesFueraArea)
                put("notas", part.notas)
                put("golesZurda", part.golesZurda)
                put("golesDiestra", part.golesDiestra)
                put("golesCabeza", part.golesCabeza)
                put("golesOtro", part.golesOtro)
                put("golesChilena", part.golesChilena)
                put("golesTacon", part.golesTacon)
                put("jugadoresMiEquipo", JSONArray(part.jugadoresMiEquipo))
                put("jugadoresEquipoRival", JSONArray(part.jugadoresEquipoRival))
                put("jugadoresIds", JSONArray(part.jugadoresIds))
            }
            partidosArr.put(pObj)
        }
        root.put("partidos", partidosArr)

        return root.toString(2)
    }

    suspend fun restaurarCopiaSeguridad(jsonStr: String): Result<Pair<Int, Int>> = runCatching {
        val root = JSONObject(jsonStr)

        // 1. Restaurar perfil si está presente
        if (root.has("perfil")) {
            val pObj = root.getJSONObject("perfil")
            val nombre = pObj.optString("nombre", "Mi Jugador")
            val fotoUri = if (pObj.has("fotoUri") && !pObj.isNull("fotoUri")) pObj.getString("fotoUri") else null
            val posFavStr = pObj.optString("posicionFavorita", com.diegoguerrero.futtracker.domain.model.Posicion.DC.name)
            val posFav = runCatching { com.diegoguerrero.futtracker.domain.model.Posicion.valueOf(posFavStr) }
                .getOrDefault(com.diegoguerrero.futtracker.domain.model.Posicion.DC)
            val posiciones = mutableSetOf<com.diegoguerrero.futtracker.domain.model.Posicion>()
            val posArr = pObj.optJSONArray("posiciones")
            if (posArr != null) {
                for (i in 0 until posArr.length()) {
                    runCatching { com.diegoguerrero.futtracker.domain.model.Posicion.valueOf(posArr.getString(i)) }
                        .getOrNull()?.let { posiciones.add(it) }
                }
            }
            val sinc = pObj.optBoolean("sincronizadoConJugadores", true)
            val perfilRestaurado = Perfil(
                nombre = nombre,
                fotoUri = fotoUri,
                posicionFavorita = posFav,
                posiciones = posiciones.ifEmpty { setOf(posFav) },
                sincronizadoConJugadores = sinc
            )
            guardarPerfil(perfilRestaurado)
        }

        // 2. Restaurar jugadores
        var countJugadores = 0
        if (root.has("jugadores")) {
            val jArr = root.getJSONArray("jugadores")
            for (i in 0 until jArr.length()) {
                val jObj = jArr.getJSONObject(i)
                val id = jObj.optString("id", java.util.UUID.randomUUID().toString())
                val nombre = jObj.optString("nombre", "")
                if (nombre.isNotBlank()) {
                    val fotoUri = if (jObj.has("fotoUri") && !jObj.isNull("fotoUri")) jObj.getString("fotoUri") else null
                    val esFavorito = jObj.optBoolean("esFavorito", false)
                    val esUsuarioPropio = jObj.optBoolean("esUsuarioPropio", false)

                    val primarias = mutableSetOf<com.diegoguerrero.futtracker.domain.model.Posicion>()
                    val primArr = jObj.optJSONArray("posicionesPrimarias")
                    if (primArr != null) {
                        for (k in 0 until primArr.length()) {
                            runCatching { com.diegoguerrero.futtracker.domain.model.Posicion.valueOf(primArr.getString(k)) }
                                .getOrNull()?.let { primarias.add(it) }
                        }
                    }
                    val secundarias = mutableSetOf<com.diegoguerrero.futtracker.domain.model.Posicion>()
                    val secArr = jObj.optJSONArray("posicionesSecundarias")
                    if (secArr != null) {
                        for (k in 0 until secArr.length()) {
                            runCatching { com.diegoguerrero.futtracker.domain.model.Posicion.valueOf(secArr.getString(k)) }
                                .getOrNull()?.let { secundarias.add(it) }
                        }
                    }

                    val jugador = Jugador(
                        id = id,
                        nombre = nombre,
                        fotoUri = fotoUri,
                        posicionesPrimarias = primarias,
                        posicionesSecundarias = secundarias,
                        esFavorito = esFavorito,
                        esUsuarioPropio = esUsuarioPropio
                    )
                    jugadorRepository.insertarJugador(jugador)
                    countJugadores++
                }
            }
        }

        // 3. Restaurar partidos
        var countPartidos = 0
        if (root.has("partidos")) {
            val pArr = root.getJSONArray("partidos")
            for (i in 0 until pArr.length()) {
                val pObj = pArr.getJSONObject(i)
                val fecha = pObj.optLong("fecha", System.currentTimeMillis())
                val modoStr = pObj.optString("modoJuego", com.diegoguerrero.futtracker.domain.model.TipoFutbol.FUTSAL.name)
                val modo = runCatching { com.diegoguerrero.futtracker.domain.model.TipoFutbol.valueOf(modoStr) }
                    .getOrDefault(com.diegoguerrero.futtracker.domain.model.TipoFutbol.FUTSAL)
                val duracionMinutos = pObj.optInt("duracionMinutos", 60)
                val gf = pObj.optInt("golesAFavor", 0)
                val gc = pObj.optInt("golesEnContra", 0)
                val posJugadaStr = pObj.optString("posicionJugada", com.diegoguerrero.futtracker.domain.model.Posicion.DC.name)
                val posJugada = runCatching { com.diegoguerrero.futtracker.domain.model.Posicion.valueOf(posJugadaStr) }
                    .getOrDefault(com.diegoguerrero.futtracker.domain.model.Posicion.DC)
                val posJugadas = mutableSetOf<com.diegoguerrero.futtracker.domain.model.Posicion>()
                val pjArr = pObj.optJSONArray("posicionesJugadas")
                if (pjArr != null) {
                    for (k in 0 until pjArr.length()) {
                        runCatching { com.diegoguerrero.futtracker.domain.model.Posicion.valueOf(pjArr.getString(k)) }
                            .getOrNull()?.let { posJugadas.add(it) }
                    }
                }
                val goles = pObj.optInt("goles", 0)
                val asist = pObj.optInt("asistencias", 0)
                val palos = pObj.optInt("tirosAlPalo", 0)
                val fuera = pObj.optInt("golesFueraArea", 0)
                val notas = pObj.optString("notas", "")
                val zurda = pObj.optInt("golesZurda", 0)
                val diestra = pObj.optInt("golesDiestra", 0)
                val cabeza = pObj.optInt("golesCabeza", 0)
                val otro = pObj.optInt("golesOtro", 0)
                val chilena = pObj.optInt("golesChilena", 0)
                val tacon = pObj.optInt("golesTacon", 0)

                val miEq = mutableListOf<String>()
                val miEqArr = pObj.optJSONArray("jugadoresMiEquipo")
                if (miEqArr != null) {
                    for (k in 0 until miEqArr.length()) {
                        miEq.add(miEqArr.getString(k))
                    }
                }
                val rivEq = mutableListOf<String>()
                val rivArr = pObj.optJSONArray("jugadoresEquipoRival")
                if (rivArr != null) {
                    for (k in 0 until rivArr.length()) {
                        rivEq.add(rivArr.getString(k))
                    }
                }
                val jIds = mutableListOf<String>()
                val jIdsArr = pObj.optJSONArray("jugadoresIds")
                if (jIdsArr != null) {
                    for (k in 0 until jIdsArr.length()) {
                        jIds.add(jIdsArr.getString(k))
                    }
                }

                val partido = com.diegoguerrero.futtracker.domain.model.Partido(
                    fecha = fecha,
                    modoJuego = modo,
                    duracionMinutos = duracionMinutos,
                    golesAFavor = gf,
                    golesEnContra = gc,
                    posicionJugada = posJugada,
                    posicionesJugadas = posJugadas.ifEmpty { setOf(posJugada) },
                    goles = goles,
                    asistencias = asist,
                    tirosAlPalo = palos,
                    golesFueraArea = fuera,
                    notas = notas,
                    golesZurda = zurda,
                    golesDiestra = diestra,
                    golesCabeza = cabeza,
                    golesOtro = otro,
                    golesChilena = chilena,
                    golesTacon = tacon,
                    jugadoresMiEquipo = miEq,
                    jugadoresEquipoRival = rivEq,
                    jugadoresIds = jIds.ifEmpty { (miEq + rivEq).distinct() }
                )
                partidoRepository.insertarPartido(partido)
                countPartidos++
            }
        }

        Pair(countJugadores, countPartidos)
    }
}
