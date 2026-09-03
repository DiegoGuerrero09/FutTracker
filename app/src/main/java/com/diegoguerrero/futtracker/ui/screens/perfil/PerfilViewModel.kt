package com.diegoguerrero.futtracker.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.domain.model.Perfil
import com.diegoguerrero.futtracker.domain.repository.JugadorRepository
import com.diegoguerrero.futtracker.domain.repository.PartidoRepository
import com.diegoguerrero.futtracker.domain.repository.PerfilRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.inject.Inject

enum class TipoFiltroPerfil { TEMPORADA, ANIO_NATURAL, FECHA_PERSONALIZADA }

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

    val todosPartidos: StateFlow<List<Partido>> = partidoRepository.obtenerPartidos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _filtroTipo = MutableStateFlow(TipoFiltroPerfil.TEMPORADA)
    val filtroTipo: StateFlow<TipoFiltroPerfil> = _filtroTipo.asStateFlow()

    private val _anioSeleccionado = MutableStateFlow(LocalDate.now().year)
    val anioSeleccionado: StateFlow<Int> = _anioSeleccionado.asStateFlow()

    // Temporada formateada como p.ej. "2024/2025"
    private val _temporadaSeleccionada = MutableStateFlow(calcularTemporadaActual())
    val temporadaSeleccionada: StateFlow<String> = _temporadaSeleccionada.asStateFlow()

    private val _fechaInicio = MutableStateFlow(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
    val fechaInicio: StateFlow<Long> = _fechaInicio.asStateFlow()

    private val _fechaFin = MutableStateFlow(System.currentTimeMillis())
    val fechaFin: StateFlow<Long> = _fechaFin.asStateFlow()

    private data class FiltroConfig(
        val tipo: TipoFiltroPerfil,
        val temporada: String,
        val anio: Int,
        val inicio: Long,
        val fin: Long
    )

    private val filtroConfig: Flow<FiltroConfig> = combine(
        filtroTipo,
        temporadaSeleccionada,
        anioSeleccionado,
        fechaInicio,
        fechaFin
    ) { tipo, temporada, anio, inicio, fin ->
        FiltroConfig(tipo, temporada, anio, inicio, fin)
    }

    // Partidos filtrados según el modo actual
    val partidosFiltrados: StateFlow<List<Partido>> = combine(
        todosPartidos,
        filtroConfig
    ) { partidos, config ->
        val (desde, hasta) = when (config.tipo) {
            TipoFiltroPerfil.TEMPORADA -> {
                val anioInicio = runCatching { config.temporada.split("/")[0].toInt() }.getOrDefault(2024)
                val calInicio = Calendar.getInstance().apply {
                    set(anioInicio, Calendar.SEPTEMBER, 1, 0, 0, 0)
                }.timeInMillis
                val calFin = Calendar.getInstance().apply {
                    set(anioInicio + 1, Calendar.AUGUST, 31, 23, 59, 59)
                }.timeInMillis
                calInicio to calFin
            }
            TipoFiltroPerfil.ANIO_NATURAL -> {
                val calInicio = Calendar.getInstance().apply {
                    set(config.anio, Calendar.JANUARY, 1, 0, 0, 0)
                }.timeInMillis
                val calFin = Calendar.getInstance().apply {
                    set(config.anio, Calendar.DECEMBER, 31, 23, 59, 59)
                }.timeInMillis
                calInicio to calFin
            }
            TipoFiltroPerfil.FECHA_PERSONALIZADA -> {
                config.inicio to config.fin
            }
        }

        partidos.filter { it.fecha in desde..hasta }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFiltroTipo(tipo: TipoFiltroPerfil) {
        _filtroTipo.value = tipo
    }

    fun setAnio(anio: Int) {
        _anioSeleccionado.value = anio
    }

    fun setTemporada(temporada: String) {
        _temporadaSeleccionada.value = temporada
    }

    fun setRangoFechas(inicio: Long, fin: Long) {
        _fechaInicio.value = inicio
        _fechaFin.value = fin
    }

    fun guardarPerfil(nuevoPerfil: Perfil) {
        viewModelScope.launch {
            perfilRepository.guardarPerfil(nuevoPerfil)

            if (nuevoPerfil.sincronizadoConJugadores) {
                // Sincronizar jugador propio en la tabla de jugadores
                val jugadoresActuales = jugadorRepository.obtenerJugadores().first()
                val usuarioExistente = jugadoresActuales.find { it.esUsuarioPropio }

                val jugadorActualizado = usuarioExistente?.copy(
                    nombre = nuevoPerfil.nombre,
                    nivel = nuevoPerfil.nivel,
                    posicionesPrimarias = setOf(nuevoPerfil.posicionFavorita),
                    esUsuarioPropio = true
                ) ?: Jugador(
                    id = "usuario_propio_id",
                    nombre = nuevoPerfil.nombre,
                    nivel = nuevoPerfil.nivel,
                    posicionesPrimarias = setOf(nuevoPerfil.posicionFavorita),
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

    private fun calcularTemporadaActual(): String {
        val now = LocalDate.now()
        val year = now.year
        return if (now.monthValue >= 9) {
            "$year/${year + 1}"
        } else {
            "${year - 1}/$year"
        }
    }
}
