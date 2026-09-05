package com.diegoguerrero.futtracker.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegoguerrero.futtracker.domain.model.Clima
import com.diegoguerrero.futtracker.domain.model.EquipoColor
import com.diegoguerrero.futtracker.domain.model.Estadio
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Perfil
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.domain.model.TipoFutbol
import com.diegoguerrero.futtracker.domain.repository.EstadioRepository
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
    private val jugadorRepository: JugadorRepository,
    private val estadioRepository: EstadioRepository
) : ViewModel() {

    val perfil: StateFlow<Perfil> = perfilRepository.obtenerPerfil()
        .map { it ?: Perfil() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Perfil()
        )

    init {
        viewModelScope.launch {
            val p = perfilRepository.obtenerPerfil().first() ?: Perfil()
            val jugadoresActuales = jugadorRepository.obtenerJugadores().first()
            val usuarioExistente = jugadoresActuales.find { it.esUsuarioPropio || it.id == "usuario_propio_id" || it.nombre.equals(p.nombre, ignoreCase = true) }
            val posicionesAsignar = p.posiciones.ifEmpty { setOf(p.posicionFavorita) }

            if (usuarioExistente != null) {
                if (!usuarioExistente.esUsuarioPropio) {
                    jugadorRepository.actualizarJugador(usuarioExistente.copy(esUsuarioPropio = true))
                }
            } else {
                jugadorRepository.insertarJugador(
                    Jugador(
                        id = "usuario_propio_id",
                        nombre = p.nombre,
                        fotoUri = p.fotoUri,
                        posicionesPrimarias = posicionesAsignar,
                        posicionesSecundarias = emptySet(),
                        esFavorito = true,
                        esUsuarioPropio = true
                    )
                )
            }
        }
    }

    fun guardarPerfil(perfilActualizado: Perfil) {
        viewModelScope.launch {
            perfilRepository.guardarPerfil(perfilActualizado)
            if (perfilActualizado.sincronizadoConJugadores) {
                val jugadores = jugadorRepository.obtenerJugadores().first()
                val usuarioJugador = jugadores.find { it.esUsuarioPropio || it.id == "usuario_propio_id" || it.nombre.equals(perfilActualizado.nombre, ignoreCase = true) }
                val posicionesAsignar = perfilActualizado.posiciones.ifEmpty { setOf(perfilActualizado.posicionFavorita) }

                if (usuarioJugador != null) {
                    val actualizado = usuarioJugador.copy(
                        nombre = perfilActualizado.nombre,
                        fotoUri = perfilActualizado.fotoUri,
                        posicionesPrimarias = posicionesAsignar,
                        esUsuarioPropio = true
                    )
                    jugadorRepository.actualizarJugador(actualizado)
                } else {
                    val nuevoUsuario = Jugador(
                        id = "usuario_propio_id",
                        nombre = perfilActualizado.nombre,
                        fotoUri = perfilActualizado.fotoUri,
                        posicionesPrimarias = posicionesAsignar,
                        posicionesSecundarias = emptySet(),
                        esFavorito = true,
                        esUsuarioPropio = true
                    )
                    jugadorRepository.insertarJugador(nuevoUsuario)
                }
            }
        }
    }

    suspend fun exportarDatosJson(): String {
        val p = perfil.first()
        val jugadores = jugadorRepository.obtenerJugadores().first()
        val partidos = partidoRepository.obtenerPartidos().first()
        val estadios = estadioRepository.obtenerEstadios().first()

        val root = JSONObject()
        root.put("version", 2)
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
                put("fechaCreacion", j.fechaCreacion)
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
                put("jugadoPorMi", part.jugadoPorMi)
                put("esFavorito", part.esFavorito)
                put("clima", part.clima.name)
                part.fotoUri?.let { put("fotoUri", it) }
                part.equipoJugado?.let { put("equipoJugado", it.name) }
                part.estadioId?.let { put("estadioId", it) }
                put("golesAFavor", part.golesAFavor)
                put("golesEnContra", part.golesEnContra)
                put("posicionJugada", part.posicionJugada.name)
                put("posicionesJugadas", JSONArray(part.posicionesJugadas.map { it.name }))
                put("posicionesSecundarias", JSONArray(part.posicionesSecundarias.map { it.name }))
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

        val estadiosArr = JSONArray()
        estadios.forEach { est ->
            val eObj = JSONObject().apply {
                put("id", est.id)
                put("nombre", est.nombre)
                put("modalidades", JSONArray(est.modalidades.map { it.name }))
                est.fotoUri?.let { put("fotoUri", it) }
            }
            estadiosArr.put(eObj)
        }
        root.put("estadios", estadiosArr)

        return root.toString(2)
    }

    suspend fun restaurarCopiaSeguridad(jsonStr: String): Result<Pair<Int, Int>> = runCatching {
        val root = JSONObject(jsonStr)

        // 1. Restaurar perfil si está presente
        if (root.has("perfil")) {
            val pObj = root.getJSONObject("perfil")
            val nombre = pObj.optString("nombre", "Mi Jugador")
            val fotoUri = if (pObj.has("fotoUri") && !pObj.isNull("fotoUri")) pObj.getString("fotoUri") else null
            val posFavStr = pObj.optString("posicionFavorita", Posicion.DC.name)
            val posFav = runCatching { Posicion.valueOf(posFavStr) }.getOrDefault(Posicion.DC)
            val posiciones = mutableSetOf<Posicion>()
            val posArr = pObj.optJSONArray("posiciones")
            if (posArr != null) {
                for (i in 0 until posArr.length()) {
                    runCatching { Posicion.valueOf(posArr.getString(i)) }.getOrNull()?.let { posiciones.add(it) }
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
                    val fechaCreacion = jObj.optLong("fechaCreacion", System.currentTimeMillis())

                    val primarias = mutableSetOf<Posicion>()
                    val primArr = jObj.optJSONArray("posicionesPrimarias")
                    if (primArr != null) {
                        for (k in 0 until primArr.length()) {
                            runCatching { Posicion.valueOf(primArr.getString(k)) }.getOrNull()?.let { primarias.add(it) }
                        }
                    }
                    val secundarias = mutableSetOf<Posicion>()
                    val secArr = jObj.optJSONArray("posicionesSecundarias")
                    if (secArr != null) {
                        for (k in 0 until secArr.length()) {
                            runCatching { Posicion.valueOf(secArr.getString(k)) }.getOrNull()?.let { secundarias.add(it) }
                        }
                    }

                    val jugador = Jugador(
                        id = id,
                        nombre = nombre,
                        fotoUri = fotoUri,
                        posicionesPrimarias = primarias,
                        posicionesSecundarias = secundarias,
                        esFavorito = esFavorito,
                        esUsuarioPropio = esUsuarioPropio,
                        fechaCreacion = fechaCreacion
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
                val modoStr = pObj.optString("modoJuego", TipoFutbol.FUTSAL.name)
                val modo = runCatching { TipoFutbol.valueOf(modoStr) }.getOrDefault(TipoFutbol.FUTSAL)
                val duracionMinutos = pObj.optInt("duracionMinutos", 60)
                val gf = pObj.optInt("golesAFavor", 0)
                val gc = pObj.optInt("golesEnContra", 0)
                val posJugadaStr = pObj.optString("posicionJugada", Posicion.DC.name)
                val posJugada = runCatching { Posicion.valueOf(posJugadaStr) }.getOrDefault(Posicion.DC)
                val posJugadas = mutableSetOf<Posicion>()
                val pjArr = pObj.optJSONArray("posicionesJugadas")
                if (pjArr != null) {
                    for (k in 0 until pjArr.length()) {
                        runCatching { Posicion.valueOf(pjArr.getString(k)) }.getOrNull()?.let { posJugadas.add(it) }
                    }
                }
                val posSecundarias = mutableSetOf<Posicion>()
                val psArr = pObj.optJSONArray("posicionesSecundarias")
                if (psArr != null) {
                    for (k in 0 until psArr.length()) {
                        runCatching { Posicion.valueOf(psArr.getString(k)) }.getOrNull()?.let { posSecundarias.add(it) }
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

                val esFav = pObj.optBoolean("esFavorito", false)
                val climaStr = pObj.optString("clima", Clima.SOLEADO.name)
                val clima = runCatching { Clima.valueOf(climaStr) }.getOrDefault(Clima.SOLEADO)
                val fotoUri = if (pObj.has("fotoUri") && !pObj.isNull("fotoUri")) pObj.getString("fotoUri") else null
                val eqColorStr = if (pObj.has("equipoJugado") && !pObj.isNull("equipoJugado")) pObj.getString("equipoJugado") else null
                val eqColor = eqColorStr?.let { runCatching { EquipoColor.valueOf(it) }.getOrNull() }
                val estadioId = if (pObj.has("estadioId") && !pObj.isNull("estadioId")) pObj.getLong("estadioId") else null

                val partido = com.diegoguerrero.futtracker.domain.model.Partido(
                    fecha = fecha,
                    modoJuego = modo,
                    duracionMinutos = duracionMinutos,
                    golesAFavor = gf,
                    golesEnContra = gc,
                    posicionJugada = posJugada,
                    posicionesJugadas = posJugadas.ifEmpty { setOf(posJugada) },
                    posicionesSecundarias = posSecundarias,
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
                    jugadoresIds = jIds.ifEmpty { (miEq + rivEq).distinct() },
                    jugadoPorMi = pObj.optBoolean("jugadoPorMi", true),
                    esFavorito = esFav,
                    clima = clima,
                    fotoUri = fotoUri,
                    equipoJugado = eqColor,
                    estadioId = estadioId
                )
                partidoRepository.insertarPartido(partido)
                countPartidos++
            }
        }

        // 4. Restaurar estadios
        if (root.has("estadios")) {
            val eArr = root.getJSONArray("estadios")
            for (i in 0 until eArr.length()) {
                val eObj = eArr.getJSONObject(i)
                val nombre = eObj.optString("nombre", "")
                if (nombre.isNotBlank()) {
                    val modalidades = mutableSetOf<TipoFutbol>()
                    val mArr = eObj.optJSONArray("modalidades")
                    if (mArr != null) {
                        for (k in 0 until mArr.length()) {
                            runCatching { TipoFutbol.valueOf(mArr.getString(k)) }.getOrNull()?.let { modalidades.add(it) }
                        }
                    }
                    val fotoUri = if (eObj.has("fotoUri") && !eObj.isNull("fotoUri")) eObj.getString("fotoUri") else null
                    val estadio = Estadio(
                        nombre = nombre,
                        modalidades = modalidades.ifEmpty { setOf(TipoFutbol.FUTSAL) },
                        fotoUri = fotoUri
                    )
                    estadioRepository.insertarEstadio(estadio)
                }
            }
        }

        Pair(countJugadores, countPartidos)
    }
}
