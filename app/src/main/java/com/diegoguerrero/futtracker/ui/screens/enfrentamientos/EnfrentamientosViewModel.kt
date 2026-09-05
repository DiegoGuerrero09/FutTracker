package com.diegoguerrero.futtracker.ui.screens.enfrentamientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegoguerrero.futtracker.domain.model.DestacadosEnfrentamientos
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

private data class FiltrosIndividualData(
    val sec: SeccionEnfrentamientos,
    val texto: String,
    val filtro: FiltroHistorial,
    val soloFav: Boolean,
    val pos: Posicion?,
    val soloPrin: Boolean,
    val periodo: PeriodoPartidos,
    val anio: Int,
    val temporada: String,
    val fechaInicio: Long,
    val fechaFin: Long
)

private data class FiltrosGeneralData(
    val busq: String,
    val fav: Boolean,
    val pos: Posicion?,
    val soloPrin: Boolean,
    val idSel: String?,
    val periodo: PeriodoPartidos,
    val anio: Int,
    val temporada: String,
    val fechaInicio: Long,
    val fechaFin: Long
)

private data class H2HAndGenData(
    val idA: String?,
    val idB: String?,
    val destGen: DestacadosEnfrentamientos,
    val anios: List<Int>,
    val temps: List<String>
)

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

@HiltViewModel
class EnfrentamientosViewModel @Inject constructor(
    private val enfrentamientosRepository: EnfrentamientosRepository,
    private val jugadorRepository: JugadorRepository,
    private val partidoRepository: PartidoRepository
) : ViewModel() {

    private val _seccionActual = MutableStateFlow(SeccionEnfrentamientos.INDIVIDUAL)
    private val _filtroTexto = MutableStateFlow("")
    private val _filtroHistorial = MutableStateFlow(FiltroHistorial.TODOS)
    private val _filtroSoloFavoritos = MutableStateFlow(false)
    private val _filtroPosicion = MutableStateFlow<Posicion?>(null)
    private val _filtroSoloPosicionPrincipal = MutableStateFlow(false)
    private val _filtroPeriodoIndividual = MutableStateFlow(PeriodoPartidos.TOTAL)
    private val _anioSeleccionadoIndividual = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _temporadaSeleccionadaIndividual = MutableStateFlow(calcularTemporadaActual())
    private val _fechaInicioIndividual = MutableStateFlow(System.currentTimeMillis() - 30L * 86400000L)
    private val _fechaFinIndividual = MutableStateFlow(System.currentTimeMillis())

    private val _jugadorDetalle = MutableStateFlow<EstadisticasJugadorCruzadas?>(null)
    private val _jugadorAId = MutableStateFlow<String?>(null)
    private val _jugadorBId = MutableStateFlow<String?>(null)

    // Estado para la pestaña General (ver cabra, lacra, caramelito, bestia de cualquier jugador)
    private val _jugadorSeleccionadoGeneralId = MutableStateFlow<String?>(null)
    private val _busquedaGeneral = MutableStateFlow("")
    private val _soloFavoritosGeneral = MutableStateFlow(false)
    private val _posicionGeneral = MutableStateFlow<Posicion?>(null)
    private val _soloPosicionPrincipalGeneral = MutableStateFlow(false)
    private val _filtroPeriodoGeneral = MutableStateFlow(PeriodoPartidos.TOTAL)
    private val _anioSeleccionadoGeneral = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _temporadaSeleccionadaGeneral = MutableStateFlow(calcularTemporadaActual())
    private val _fechaInicioGeneral = MutableStateFlow(System.currentTimeMillis() - 30L * 86400000L)
    private val _fechaFinGeneral = MutableStateFlow(System.currentTimeMillis())

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

    private val rangoIndividual = combine(
        _filtroPeriodoIndividual,
        _anioSeleccionadoIndividual,
        _temporadaSeleccionadaIndividual,
        _fechaInicioIndividual,
        _fechaFinIndividual
    ) { periodo, anio, temp, fIni, fFin ->
        calcularRangoPeriodo(periodo, anio, temp, fIni, fFin)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val historial: StateFlow<List<EstadisticasJugadorCruzadas>> = rangoIndividual.flatMapLatest { (inicio, fin) ->
        enfrentamientosRepository.obtenerHistorialCruzado(inicio, fin)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val destacados: StateFlow<DestacadosEnfrentamientos> = rangoIndividual.flatMapLatest { (inicio, fin) ->
        enfrentamientosRepository.obtenerDestacados(inicio, fin)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DestacadosEnfrentamientos())

    val duos = enfrentamientosRepository.obtenerDuos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todosJugadores = jugadorRepository.obtenerJugadores()
        .map { lista ->
            lista.sortedWith(
                compareByDescending<Jugador> { it.esUsuarioPropio || it.id == "usuario_propio_id" }
                    .thenByDescending { it.esFavorito }
                    .thenBy { it.nombre.lowercase() }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val rangoGeneral = combine(
        _filtroPeriodoGeneral,
        _anioSeleccionadoGeneral,
        _temporadaSeleccionadaGeneral,
        _fechaInicioGeneral,
        _fechaFinGeneral
    ) { periodo, anio, temp, fIni, fFin ->
        calcularRangoPeriodo(periodo, anio, temp, fIni, fFin)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val destacadosGeneral: StateFlow<DestacadosEnfrentamientos> = combine(
        _jugadorSeleccionadoGeneralId,
        rangoGeneral
    ) { id, (inicio, fin) ->
        if (id != null) {
            enfrentamientosRepository.obtenerDestacadosParaJugador(id, inicio, fin)
        } else {
            flowOf(DestacadosEnfrentamientos())
        }
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DestacadosEnfrentamientos())

    @OptIn(ExperimentalCoroutinesApi::class)
    val comparativaCaraACara = combine(_jugadorAId, _jugadorBId) { idA, idB ->
        Pair(idA, idB)
    }.flatMapLatest { (idA, idB) ->
        if (idA != null && idB != null && idA != idB) {
            enfrentamientosRepository.obtenerComparativa(idA, idB)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val filtrosIndividualFlow = combine(
        combine(_seccionActual, _filtroTexto, _filtroHistorial) { sec, texto, filtro ->
            Triple(sec, texto, filtro)
        },
        combine(_filtroSoloFavoritos, _filtroPosicion, _filtroSoloPosicionPrincipal) { fav, pos, soloPrin ->
            Triple(fav, pos, soloPrin)
        },
        combine(
            _filtroPeriodoIndividual,
            _anioSeleccionadoIndividual,
            _temporadaSeleccionadaIndividual,
            _fechaInicioIndividual,
            _fechaFinIndividual
        ) { periodo, anio, temp, fIni, fFin ->
            listOf(periodo, anio, temp, fIni, fFin)
        }
    ) { (sec, texto, filtro), (fav, pos, soloPrin), periodoArgs ->
        FiltrosIndividualData(
            sec, texto, filtro, fav, pos, soloPrin,
            periodoArgs[0] as PeriodoPartidos,
            periodoArgs[1] as Int,
            periodoArgs[2] as String,
            periodoArgs[3] as Long,
            periodoArgs[4] as Long
        )
    }

    private val filtrosGeneralFlow = combine(
        combine(_busquedaGeneral, _soloFavoritosGeneral, _posicionGeneral) { busq, fav, pos ->
            Triple(busq, fav, pos)
        },
        combine(_soloPosicionPrincipalGeneral, _jugadorSeleccionadoGeneralId) { soloPrin, idSel ->
            Pair(soloPrin, idSel)
        },
        combine(
            _filtroPeriodoGeneral,
            _anioSeleccionadoGeneral,
            _temporadaSeleccionadaGeneral,
            _fechaInicioGeneral,
            _fechaFinGeneral
        ) { periodo, anio, temp, fIni, fFin ->
            listOf(periodo, anio, temp, fIni, fFin)
        }
    ) { (busq, fav, pos), (soloPrin, idSel), periodoArgs ->
        FiltrosGeneralData(
            busq, fav, pos, soloPrin, idSel,
            periodoArgs[0] as PeriodoPartidos,
            periodoArgs[1] as Int,
            periodoArgs[2] as String,
            periodoArgs[3] as Long,
            periodoArgs[4] as Long
        )
    }

    private val h2hAndGenFlow = combine(
        combine(_jugadorAId, _jugadorBId) { a, b -> Pair(a, b) },
        destacadosGeneral,
        aniosDisponibles,
        temporadasDisponibles
    ) { (a, b), destGen, anios, temps ->
        H2HAndGenData(a, b, destGen, anios, temps)
    }

    val uiState: StateFlow<EnfrentamientosUiState> = combine(
        filtrosIndividualFlow,
        combine(historial, destacados, duos) { hist, dest, d ->
            Triple(hist, dest, d)
        },
        combine(todosJugadores, _jugadorDetalle, comparativaCaraACara) { jug, det, comp ->
            Triple(jug, det, comp)
        },
        filtrosGeneralFlow,
        h2hAndGenFlow
    ) { fIndiv, (hist, dest, d), (jug, det, comp), fGen, h2hGen ->
        val (idA, idB, destGen, anios, temps) = h2hGen

        val historialFiltrado = hist.filter { item ->
            val coincideTexto = fIndiv.texto.isBlank() || item.jugador.nombre.contains(fIndiv.texto, ignoreCase = true)
            val coincideFiltro = when (fIndiv.filtro) {
                FiltroHistorial.TODOS -> true
                FiltroHistorial.COMPANEROS -> item.partidosComoCompanero > 0
                FiltroHistorial.RIVALES -> item.partidosComoRival > 0
            }
            val coincideFav = !fIndiv.soloFav || item.jugador.esFavorito
            val coincidePos = when {
                fIndiv.pos == null -> true
                fIndiv.soloPrin -> item.jugador.posicionesPrimarias.contains(fIndiv.pos)
                else -> item.jugador.posicionesPrimarias.contains(fIndiv.pos) || item.jugador.posicionesSecundarias.contains(fIndiv.pos)
            }
            coincideTexto && coincideFiltro && coincideFav && coincidePos
        }

        val jugadoresFiltradosGen = jug.filter { item ->
            val coincideBusq = fGen.busq.isBlank() || item.nombre.contains(fGen.busq, ignoreCase = true)
            val coincideFav = !fGen.fav || item.esFavorito
            val coincidePos = when {
                fGen.pos == null -> true
                fGen.soloPrin -> item.posicionesPrimarias.contains(fGen.pos)
                else -> item.posicionesPrimarias.contains(fGen.pos) || item.posicionesSecundarias.contains(fGen.pos)
            }
            coincideBusq && coincideFav && coincidePos
        }

        val finalGenId = fGen.idSel ?: jug.firstOrNull()?.id

        EnfrentamientosUiState(
            seccionActual = fIndiv.sec,
            historial = historialFiltrado,
            destacados = dest,
            duos = d,
            todosLosJugadores = jug,
            filtroTexto = fIndiv.texto,
            filtroHistorial = fIndiv.filtro,
            filtroSoloFavoritos = fIndiv.soloFav,
            filtroPosicion = fIndiv.pos,
            filtroSoloPosicionPrincipal = fIndiv.soloPrin,
            filtroPeriodoIndividual = fIndiv.periodo,
            anioSeleccionadoIndividual = fIndiv.anio,
            temporadaSeleccionadaIndividual = fIndiv.temporada,
            fechaInicioIndividual = fIndiv.fechaInicio,
            fechaFinIndividual = fIndiv.fechaFin,
            jugadorDetalle = det,
            jugadorAId = idA,
            jugadorBId = idB,
            comparativaCaraACara = comp,
            jugadorSeleccionadoGeneralId = finalGenId,
            destacadosGeneral = destGen,
            busquedaGeneral = fGen.busq,
            soloFavoritosGeneral = fGen.fav,
            posicionGeneral = fGen.pos,
            soloPosicionPrincipalGeneral = fGen.soloPrin,
            filtroPeriodoGeneral = fGen.periodo,
            anioSeleccionadoGeneral = fGen.anio,
            temporadaSeleccionadaGeneral = fGen.temporada,
            fechaInicioGeneral = fGen.fechaInicio,
            fechaFinGeneral = fGen.fechaFin,
            aniosDisponibles = anios,
            temporadasDisponibles = temps,
            jugadoresFiltradosGeneral = jugadoresFiltradosGen
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EnfrentamientosUiState())

    init {
        viewModelScope.launch {
            todosJugadores.collect { jugadores ->
                if (jugadores.isNotEmpty()) {
                    if (_jugadorSeleccionadoGeneralId.value == null) {
                        _jugadorSeleccionadoGeneralId.value = jugadores[0].id
                    }
                }
            }
        }
    }

    fun setSeccion(seccion: SeccionEnfrentamientos) {
        _seccionActual.value = seccion
    }

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

    fun setPeriodoIndividual(periodo: PeriodoPartidos, anio: Int = _anioSeleccionadoIndividual.value) {
        _filtroPeriodoIndividual.value = periodo
        _anioSeleccionadoIndividual.value = anio
    }

    fun setFiltroPeriodoIndividual(periodo: PeriodoPartidos) {
        _filtroPeriodoIndividual.value = periodo
    }

    fun setAnioIndividual(anio: Int) {
        _anioSeleccionadoIndividual.value = anio
    }

    fun setTemporadaIndividual(temporada: String) {
        _temporadaSeleccionadaIndividual.value = temporada
    }

    fun setRangoFechasIndividual(inicio: Long, fin: Long) {
        _fechaInicioIndividual.value = inicio
        _fechaFinIndividual.value = fin
    }

    fun seleccionarJugadorDetalle(item: EstadisticasJugadorCruzadas?) {
        _jugadorDetalle.value = item
    }

    fun seleccionarJugadorA(id: String) {
        _jugadorAId.value = id
    }

    fun seleccionarJugadorB(id: String) {
        _jugadorBId.value = id
    }

    fun seleccionarJugadorGeneral(id: String) {
        _jugadorSeleccionadoGeneralId.value = id
    }

    fun setBusquedaGeneral(texto: String) {
        _busquedaGeneral.value = texto
    }

    fun toggleSoloFavoritosGeneral() {
        _soloFavoritosGeneral.value = !_soloFavoritosGeneral.value
    }

    fun setPosicionGeneral(pos: Posicion?) {
        _posicionGeneral.value = pos
    }

    fun setSoloPosicionPrincipalGeneral(soloPrincipal: Boolean) {
        _soloPosicionPrincipalGeneral.value = soloPrincipal
    }

    fun setPeriodoGeneral(periodo: PeriodoPartidos, anio: Int = _anioSeleccionadoGeneral.value) {
        _filtroPeriodoGeneral.value = periodo
        _anioSeleccionadoGeneral.value = anio
    }

    fun setFiltroPeriodoGeneral(periodo: PeriodoPartidos) {
        _filtroPeriodoGeneral.value = periodo
    }

    fun setAnioGeneral(anio: Int) {
        _anioSeleccionadoGeneral.value = anio
    }

    fun setTemporadaGeneral(temporada: String) {
        _temporadaSeleccionadaGeneral.value = temporada
    }

    fun setRangoFechasGeneral(inicio: Long, fin: Long) {
        _fechaInicioGeneral.value = inicio
        _fechaFinGeneral.value = fin
    }
}
