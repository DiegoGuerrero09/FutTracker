package com.diegoguerrero.futtracker.ui.screens.enfrentamientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegoguerrero.futtracker.domain.model.DestacadosEnfrentamientos
import com.diegoguerrero.futtracker.domain.model.DuoEstadisticas
import com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorCruzadas
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.domain.repository.EnfrentamientosRepository
import com.diegoguerrero.futtracker.domain.repository.JugadorRepository
import com.diegoguerrero.futtracker.domain.repository.PartidoRepository
import com.diegoguerrero.futtracker.ui.screens.partidos.PeriodoPartidos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private fun calcularTemporadaActual(): String {
    val cal = Calendar.getInstance()
    val anio = cal.get(Calendar.YEAR)
    val mes = cal.get(Calendar.MONTH)
    return if (mes >= Calendar.SEPTEMBER) "$anio/${anio + 1}" else "${anio - 1}/$anio"
}

private fun obtenerTemporada(fecha: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = fecha }
    val anio = cal.get(Calendar.YEAR)
    val mes = cal.get(Calendar.MONTH)
    return if (mes >= Calendar.SEPTEMBER) "$anio/${anio + 1}" else "${anio - 1}/$anio"
}

private fun calcularRangoPeriodo(
    periodo: PeriodoPartidos,
    anio: Int,
    temporada: String = "",
    fechaInicio: Long = 0L,
    fechaFin: Long = 0L
): Pair<Long?, Long?> {
    return when (periodo) {
        PeriodoPartidos.TOTAL -> Pair(null, null)
        PeriodoPartidos.TEMPORADA -> {
            val anioInicio = runCatching { temporada.split("/")[0].toInt() }.getOrElse {
                val now = Calendar.getInstance()
                if (now.get(Calendar.MONTH) >= Calendar.SEPTEMBER) now.get(Calendar.YEAR) else now.get(Calendar.YEAR) - 1
            }
            val calInicio = Calendar.getInstance().apply {
                set(anioInicio, Calendar.SEPTEMBER, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val calFin = Calendar.getInstance().apply {
                set(anioInicio + 1, Calendar.AUGUST, 31, 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            Pair(calInicio, calFin)
        }
        PeriodoPartidos.ANIO_NATURAL -> {
            val calInicio = Calendar.getInstance().apply {
                set(anio, Calendar.JANUARY, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val calFin = Calendar.getInstance().apply {
                set(anio, Calendar.DECEMBER, 31, 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            Pair(calInicio, calFin)
        }
        PeriodoPartidos.RANGO_FECHAS -> {
            val calInicio = Calendar.getInstance().apply {
                timeInMillis = if (fechaInicio > 0) fechaInicio else System.currentTimeMillis() - 30L * 86400000L
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val calFin = Calendar.getInstance().apply {
                timeInMillis = if (fechaFin > 0) fechaFin else System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            Pair(calInicio, calFin)
        }
        PeriodoPartidos.ULTIMOS_MESES -> {
            val calInicio = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
            Pair(calInicio, null)
        }
        PeriodoPartidos.ULTIMAS_SEMANAS -> {
            val calInicio = System.currentTimeMillis() - 28L * 24 * 60 * 60 * 1000
            Pair(calInicio, null)
        }
    }
}

private data class FiltrosInspeccionado(
    val id: String?,
    val busqueda: String,
    val fav: Boolean,
    val pos: Posicion?,
    val soloPrin: Boolean
)

private data class FiltrosPeriodo(
    val periodo: PeriodoPartidos,
    val anio: Int,
    val temp: String,
    val fechaInicio: Long,
    val fechaFin: Long
)

private data class FiltrosCruzado(
    val texto: String,
    val filtro: FiltroHistorial,
    val fav: Boolean,
    val pos: Posicion?,
    val soloPrin: Boolean
)

@HiltViewModel
class EnfrentamientosViewModel @Inject constructor(
    private val enfrentamientosRepository: EnfrentamientosRepository,
    private val jugadorRepository: JugadorRepository,
    private val partidoRepository: PartidoRepository
) : ViewModel() {

    private val _seccionActual = MutableStateFlow(SeccionEnfrentamientos.INDIVIDUAL)

    // Selector del jugador a inspeccionar (arriba, default "Tú")
    private val _jugadorSeleccionadoId = MutableStateFlow<String?>(null)
    private val _busquedaJugadorInspeccionado = MutableStateFlow("")
    private val _soloFavoritosInspeccionado = MutableStateFlow(false)
    private val _posicionInspeccionado = MutableStateFlow<Posicion?>(null)
    private val _soloPosicionPrincipalInspeccionado = MutableStateFlow(false)

    // Período
    private val _filtroPeriodo = MutableStateFlow(PeriodoPartidos.TOTAL)
    private val _anioSeleccionado = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _temporadaSeleccionada = MutableStateFlow(calcularTemporadaActual())
    private val _fechaInicio = MutableStateFlow(System.currentTimeMillis() - 30L * 86400000L)
    private val _fechaFin = MutableStateFlow(System.currentTimeMillis())

    // Filtros de la lista cruzada (otros jugadores vs el inspeccionado)
    private val _filtroTexto = MutableStateFlow("")
    private val _filtroHistorial = MutableStateFlow(FiltroHistorial.TODOS)
    private val _filtroSoloFavoritos = MutableStateFlow(false)
    private val _filtroPosicion = MutableStateFlow<Posicion?>(null)
    private val _filtroSoloPosicionPrincipal = MutableStateFlow(false)
    private val _jugadorDetalle = MutableStateFlow<EstadisticasJugadorCruzadas?>(null)

    val aniosDisponibles: StateFlow<List<Int>> = partidoRepository.obtenerPartidos()
        .map { lista ->
            val cal = Calendar.getInstance()
            val currentYear = cal.get(Calendar.YEAR)
            val years = lista.map {
                cal.timeInMillis = it.fecha
                cal.get(Calendar.YEAR)
            }.toSet() + currentYear
            years.sortedDescending()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(Calendar.getInstance().get(Calendar.YEAR)))

    val temporadasDisponibles: StateFlow<List<String>> = partidoRepository.obtenerPartidos()
        .map { lista ->
            val actual = calcularTemporadaActual()
            val temporadas = lista.map { obtenerTemporada(it.fecha) }
            (listOf(actual) + temporadas).distinct().sortedDescending()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(calcularTemporadaActual()))

    val todosJugadores: StateFlow<List<Jugador>> = jugadorRepository.obtenerJugadores()
        .map { lista ->
            lista.sortedWith(
                compareByDescending<Jugador> { it.esUsuarioPropio || it.id == "usuario_propio_id" }
                    .thenByDescending { it.esFavorito }
                    .thenBy { it.nombre.lowercase() }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val duos = enfrentamientosRepository.obtenerDuos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val rangoPeriodo = combine(
        _filtroPeriodo,
        _anioSeleccionado,
        _temporadaSeleccionada,
        _fechaInicio,
        _fechaFin
    ) { periodo, anio, temp, fIni, fFin ->
        calcularRangoPeriodo(periodo, anio, temp, fIni, fFin)
    }

    private val targetJugadorIdFlow = combine(
        _jugadorSeleccionadoId,
        todosJugadores
    ) { selId, jugadores ->
        selId ?: jugadores.firstOrNull { it.esUsuarioPropio || it.id == "usuario_propio_id" }?.id ?: jugadores.firstOrNull()?.id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val destacados: StateFlow<DestacadosEnfrentamientos> = combine(
        targetJugadorIdFlow,
        rangoPeriodo
    ) { id, (inicio, fin) ->
        if (id != null) {
            enfrentamientosRepository.obtenerDestacadosParaJugador(id, inicio, fin)
        } else {
            flowOf(DestacadosEnfrentamientos())
        }
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DestacadosEnfrentamientos())

    @OptIn(ExperimentalCoroutinesApi::class)
    val historial: StateFlow<List<EstadisticasJugadorCruzadas>> = combine(
        targetJugadorIdFlow,
        rangoPeriodo
    ) { id, (inicio, fin) ->
        if (id != null) {
            enfrentamientosRepository.obtenerHistorialCruzadoParaJugador(id, inicio, fin)
        } else {
            flowOf(emptyList())
        }
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val filtrosInspeccionadoFlow = combine(
        targetJugadorIdFlow,
        _busquedaJugadorInspeccionado,
        _soloFavoritosInspeccionado,
        _posicionInspeccionado,
        _soloPosicionPrincipalInspeccionado
    ) { id, busq, fav, pos, soloPrin ->
        FiltrosInspeccionado(id, busq, fav, pos, soloPrin)
    }

    private val filtrosPeriodoFlow = combine(
        _filtroPeriodo,
        _anioSeleccionado,
        _temporadaSeleccionada,
        _fechaInicio,
        _fechaFin
    ) { periodo, anio, temp, fIni, fFin ->
        FiltrosPeriodo(periodo, anio, temp, fIni, fFin)
    }

    private val filtrosCruzadoFlow = combine(
        _filtroTexto,
        _filtroHistorial,
        _filtroSoloFavoritos,
        _filtroPosicion,
        _filtroSoloPosicionPrincipal
    ) { texto, hist, fav, pos, soloPrin ->
        FiltrosCruzado(texto, hist, fav, pos, soloPrin)
    }

    val uiState: StateFlow<EnfrentamientosUiState> = combine(
        _seccionActual,
        filtrosInspeccionadoFlow,
        filtrosPeriodoFlow,
        filtrosCruzadoFlow,
        combine(
            destacados,
            historial,
            todosJugadores,
            duos,
            _jugadorDetalle
        ) { dest, hist, todos, duosList, det ->
            listOf(dest, hist, todos, duosList, det)
        }
    ) { sec, fInsp, fPer, fCruz, combinedData ->
        val dest = combinedData[0] as DestacadosEnfrentamientos
        val hist = combinedData[1] as List<EstadisticasJugadorCruzadas>
        val todos = combinedData[2] as List<Jugador>
        val duosList = combinedData[3] as List<DuoEstadisticas>
        val det = combinedData[4] as EstadisticasJugadorCruzadas?

        val jugadoresFiltradosInsp = todos.filter { item ->
            val coincideBusq = fInsp.busqueda.isBlank() || item.nombre.contains(fInsp.busqueda, ignoreCase = true)
            val coincideFav = !fInsp.fav || item.esFavorito
            val coincidePos = when {
                fInsp.pos == null -> true
                fInsp.soloPrin -> item.posicionesPrimarias.contains(fInsp.pos)
                else -> item.posicionesPrimarias.contains(fInsp.pos) || item.posicionesSecundarias.contains(fInsp.pos)
            }
            coincideBusq && coincideFav && coincidePos
        }

        val historialFiltrado = hist.filter { item ->
            val coincideTexto = fCruz.texto.isBlank() || item.jugador.nombre.contains(fCruz.texto, ignoreCase = true)
            val coincideFiltro = when (fCruz.filtro) {
                FiltroHistorial.TODOS -> true
                FiltroHistorial.COMPANEROS -> item.partidosComoCompanero > 0
                FiltroHistorial.RIVALES -> item.partidosComoRival > 0
            }
            val coincideFav = !fCruz.fav || item.jugador.esFavorito
            val coincidePos = when {
                fCruz.pos == null -> true
                fCruz.soloPrin -> item.jugador.posicionesPrimarias.contains(fCruz.pos)
                else -> item.jugador.posicionesPrimarias.contains(fCruz.pos) || item.jugador.posicionesSecundarias.contains(fCruz.pos)
            }
            coincideTexto && coincideFiltro && coincideFav && coincidePos
        }

        EnfrentamientosUiState(
            seccionActual = sec,
            jugadorSeleccionadoId = fInsp.id,
            busquedaJugadorInspeccionado = fInsp.busqueda,
            soloFavoritosInspeccionado = fInsp.fav,
            posicionInspeccionado = fInsp.pos,
            soloPosicionPrincipalInspeccionado = fInsp.soloPrin,
            jugadoresFiltradosInspeccionados = jugadoresFiltradosInsp,
            filtroPeriodo = fPer.periodo,
            anioSeleccionado = fPer.anio,
            temporadaSeleccionada = fPer.temp,
            fechaInicio = fPer.fechaInicio,
            fechaFin = fPer.fechaFin,
            aniosDisponibles = aniosDisponibles.value,
            temporadasDisponibles = temporadasDisponibles.value,
            destacados = dest,
            historial = historialFiltrado,
            filtroTexto = fCruz.texto,
            filtroHistorial = fCruz.filtro,
            filtroSoloFavoritos = fCruz.fav,
            filtroPosicion = fCruz.pos,
            filtroSoloPosicionPrincipal = fCruz.soloPrin,
            jugadorDetalle = det,
            duos = duosList,
            todosLosJugadores = todos
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EnfrentamientosUiState())

    init {
        viewModelScope.launch {
            todosJugadores.collect { jugadores ->
                if (jugadores.isNotEmpty() && _jugadorSeleccionadoId.value == null) {
                    val user = jugadores.firstOrNull { it.esUsuarioPropio || it.id == "usuario_propio_id" }
                    _jugadorSeleccionadoId.value = user?.id ?: jugadores[0].id
                }
            }
        }
    }

    fun setSeccion(seccion: SeccionEnfrentamientos) {
        _seccionActual.value = seccion
    }

    // Métodos para el jugador inspeccionado
    fun seleccionarJugadorInspeccionado(id: String) {
        _jugadorSeleccionadoId.value = id
    }

    fun setBusquedaJugadorInspeccionado(texto: String) {
        _busquedaJugadorInspeccionado.value = texto
    }

    fun toggleSoloFavoritosInspeccionado() {
        _soloFavoritosInspeccionado.value = !_soloFavoritosInspeccionado.value
    }

    fun setPosicionInspeccionado(pos: Posicion?) {
        _posicionInspeccionado.value = pos
    }

    fun setSoloPosicionPrincipalInspeccionado(soloPrincipal: Boolean) {
        _soloPosicionPrincipalInspeccionado.value = soloPrincipal
    }

    // Métodos de período
    fun setFiltroPeriodo(periodo: PeriodoPartidos) {
        _filtroPeriodo.value = periodo
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

    // Métodos para el historial cruzado (otros jugadores)
    fun setFiltroTexto(texto: String) {
        _filtroTexto.value = texto
    }

    fun setFiltroHistorial(filtro: FiltroHistorial) {
        _filtroHistorial.value = filtro
    }

    fun toggleFiltroSoloFavoritos() {
        _filtroSoloFavoritos.value = !_filtroSoloFavoritos.value
    }

    fun setFiltroPosicion(pos: Posicion?) {
        _filtroPosicion.value = pos
    }

    fun setFiltroSoloPosicionPrincipal(soloPrincipal: Boolean) {
        _filtroSoloPosicionPrincipal.value = soloPrincipal
    }

    fun seleccionarJugadorDetalle(item: EstadisticasJugadorCruzadas?) {
        _jugadorDetalle.value = item
    }
}

