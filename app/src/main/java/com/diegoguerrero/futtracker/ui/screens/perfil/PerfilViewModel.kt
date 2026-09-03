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

enum class TipoFiltroPerfil { TOTAL, TEMPORADA, ANIO_NATURAL, FECHA_PERSONALIZADA }

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

    val temporadasConDatos: StateFlow<List<String>> = todosPartidos.map { partidos ->
        val actual = calcularTemporadaActual()
        val temporadasDePartidos = partidos.map { obtenerTemporada(it.fecha) }
        (listOf(actual) + temporadasDePartidos).distinct().sortedDescending()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(calcularTemporadaActual())
    )

    val aniosConDatos: StateFlow<List<Int>> = todosPartidos.map { partidos ->
        val actual = LocalDate.now().year
        val aniosDePartidos = partidos.map {
            Calendar.getInstance().apply { timeInMillis = it.fecha }.get(Calendar.YEAR)
        }
        (listOf(actual) + aniosDePartidos).distinct().sortedDescending()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(LocalDate.now().year)
    )

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
        when (config.tipo) {
            TipoFiltroPerfil.TOTAL -> partidos
            TipoFiltroPerfil.TEMPORADA -> {
                val anioInicio = runCatching { config.temporada.split("/")[0].toInt() }.getOrDefault(2024)
                val calInicio = Calendar.getInstance().apply {
                    set(anioInicio, Calendar.SEPTEMBER, 1, 0, 0, 0)
                }.timeInMillis
                val calFin = Calendar.getInstance().apply {
                    set(anioInicio + 1, Calendar.AUGUST, 31, 23, 59, 59)
                }.timeInMillis
                partidos.filter { it.fecha in calInicio..calFin }
            }
            TipoFiltroPerfil.ANIO_NATURAL -> {
                val calInicio = Calendar.getInstance().apply {
                    set(config.anio, Calendar.JANUARY, 1, 0, 0, 0)
                }.timeInMillis
                val calFin = Calendar.getInstance().apply {
                    set(config.anio, Calendar.DECEMBER, 31, 23, 59, 59)
                }.timeInMillis
                partidos.filter { it.fecha in calInicio..calFin }
            }
            TipoFiltroPerfil.FECHA_PERSONALIZADA -> {
                partidos.filter { it.fecha in config.inicio..config.fin }
            }
        }
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

    private fun calcularTemporadaActual(): String {
        val now = LocalDate.now()
        val year = now.year
        return if (now.monthValue >= 9) {
            "$year/${year + 1}"
        } else {
            "${year - 1}/$year"
        }
    }

    private fun obtenerTemporada(fechaMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = fechaMillis }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        return if (month >= Calendar.SEPTEMBER) {
            "$year/${year + 1}"
        } else {
            "${year - 1}/$year"
        }
    }
}
