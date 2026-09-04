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
                    nivel = nuevoPerfil.nivel,
                    posicionesPrimarias = posicionesAsignar,
                    esUsuarioPropio = true
                ) ?: Jugador(
                    id = "usuario_propio_id",
                    nombre = nuevoPerfil.nombre,
                    fotoUri = nuevoPerfil.fotoUri,
                    nivel = nuevoPerfil.nivel,
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
            put("posicionFavorita", p.posicionFavorita.name)
            put("posiciones", JSONArray(p.posiciones.map { it.name }))
            put("nivel", p.nivel)
            put("sincronizadoConJugadores", p.sincronizadoConJugadores)
        }
        root.put("perfil", perfilObj)

        val jugadoresArr = JSONArray()
        jugadores.forEach { j ->
            val jObj = JSONObject().apply {
                put("id", j.id)
                put("nombre", j.nombre)
                put("nivel", j.nivel)
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
}
