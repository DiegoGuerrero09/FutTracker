package com.diegoguerrero.futtracker.ui.screens.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.domain.model.TipoFutbol
import com.diegoguerrero.futtracker.domain.repository.JugadorRepository
import com.diegoguerrero.futtracker.domain.repository.PartidoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

enum class TipoFiltroEstadisticas { TOTAL, ULTIMAS_4_SEMANAS, ULTIMOS_3_MESES, TEMPORADA, ANIO_NATURAL, FECHA_PERSONALIZADA }

data class ResumenEstadisticas(
    val totalPartidos: Int = 0,
    val victorias: Int = 0,
    val empates: Int = 0,
    val derrotas: Int = 0,
    val porcentajeVictorias: Int = 0,
    val totalGoles: Int = 0,
    val promedioGoles: Float = 0f,
    val totalAsistencias: Int = 0,
    val promedioAsistencias: Float = 0f,
    val totalPalos: Int = 0,
    val golesAFavor: Int = 0,
    val golesEnContra: Int = 0,
    val diferenciaGoles: Int = 0
)

data class EstadisticasJugadorGeneral(
    val jugador: Jugador,
    val partidosJugados: Int = 0,
    val victorias: Int = 0,
    val empates: Int = 0,
    val derrotas: Int = 0,
    val porcentajeVictorias: Int = 0,
    val minutosJugados: Int = 0
)

enum class CriterioOrdenGeneral {
    VICTORIAS,
    DERROTAS,
    EMPATES,
    PARTIDOS,
    PORCENTAJE,
    MINUTOS,
    NOMBRE
}

private data class FiltrosGeneralData(
    val busqueda: String,
    val soloFavoritos: Boolean,
    val posicion: Posicion?,
    val soloPosicionPrincipal: Boolean
)

@HiltViewModel
class EstadisticasViewModel @Inject constructor(
    private val partidoRepository: PartidoRepository,
    private val jugadorRepository: JugadorRepository
) : ViewModel() {

    val todosPartidos: StateFlow<List<Partido>> = partidoRepository.obtenerPartidos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _filtroModoJuego = MutableStateFlow<TipoFutbol?>(null) // null = Total
    val filtroModoJuego: StateFlow<TipoFutbol?> = _filtroModoJuego.asStateFlow()

    private val _filtroTiempo = MutableStateFlow(TipoFiltroEstadisticas.TOTAL)
    val filtroTiempo: StateFlow<TipoFiltroEstadisticas> = _filtroTiempo.asStateFlow()

    private val _anioSeleccionado = MutableStateFlow(LocalDate.now().year)
    val anioSeleccionado: StateFlow<Int> = _anioSeleccionado.asStateFlow()

    private val _temporadaSeleccionada = MutableStateFlow(calcularTemporadaActual())
    val temporadaSeleccionada: StateFlow<String> = _temporadaSeleccionada.asStateFlow()

    val temporadasConDatos: StateFlow<List<String>> = todosPartidos.map { partidos ->
        val actual = calcularTemporadaActual()
        val temporadas = partidos.map { obtenerTemporada(it.fecha) }
        (listOf(actual) + temporadas).distinct().sortedDescending()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(calcularTemporadaActual())
    )

    val aniosConDatos: StateFlow<List<Int>> = todosPartidos.map { partidos ->
        val actual = LocalDate.now().year
        val anios = partidos.map {
            Calendar.getInstance().apply { timeInMillis = it.fecha }.get(Calendar.YEAR)
        }
        (listOf(actual) + anios).distinct().sortedDescending()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(LocalDate.now().year)
    )

    private val _fechaInicio = MutableStateFlow(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
    val fechaInicio: StateFlow<Long> = _fechaInicio.asStateFlow()

    private val _fechaFin = MutableStateFlow(System.currentTimeMillis())
    val fechaFin: StateFlow<Long> = _fechaFin.asStateFlow()

    val partidosFiltrados: StateFlow<List<Partido>> = combine(
        todosPartidos,
        filtroModoJuego,
        filtroTiempo,
        temporadaSeleccionada,
        anioSeleccionado,
        fechaInicio,
        fechaFin
    ) { args ->
        val partidos = args[0] as List<Partido>
        val modo = args[1] as TipoFutbol?
        val tipoTiempo = args[2] as TipoFiltroEstadisticas
        val temporada = args[3] as String
        val anio = args[4] as Int
        val fInicio = args[5] as Long
        val fFin = args[6] as Long

        var lista = if (modo != null) partidos.filter { it.modoJuego == modo } else partidos

        lista = when (tipoTiempo) {
            TipoFiltroEstadisticas.TOTAL -> lista
            TipoFiltroEstadisticas.ULTIMAS_4_SEMANAS -> {
                val calInicio = System.currentTimeMillis() - 28L * 24 * 60 * 60 * 1000
                lista.filter { it.fecha >= calInicio }
            }
            TipoFiltroEstadisticas.ULTIMOS_3_MESES -> {
                val calInicio = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
                lista.filter { it.fecha >= calInicio }
            }
            TipoFiltroEstadisticas.TEMPORADA -> {
                val anioInicio = runCatching { temporada.split("/")[0].toInt() }.getOrDefault(2024)
                val calInicio = Calendar.getInstance().apply {
                    set(anioInicio, Calendar.SEPTEMBER, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val calFin = Calendar.getInstance().apply {
                    set(anioInicio + 1, Calendar.AUGUST, 31, 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                lista.filter { it.fecha in calInicio..calFin }
            }
            TipoFiltroEstadisticas.ANIO_NATURAL -> {
                val calInicio = Calendar.getInstance().apply {
                    set(anio, Calendar.JANUARY, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val calFin = Calendar.getInstance().apply {
                    set(anio, Calendar.DECEMBER, 31, 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                lista.filter { it.fecha in calInicio..calFin }
            }
            TipoFiltroEstadisticas.FECHA_PERSONALIZADA -> {
                val calInicio = Calendar.getInstance().apply {
                    timeInMillis = fInicio
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val calFin = Calendar.getInstance().apply {
                    timeInMillis = fFin
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                lista.filter { it.fecha in calInicio..calFin }
            }
        }
        lista
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val resumen: StateFlow<ResumenEstadisticas> = partidosFiltrados.map { partidos ->
        val total = partidos.size
        val victorias = partidos.count { it.esVictoria }
        val empates = partidos.count { it.esEmpate }
        val derrotas = partidos.count { it.esDerrota }
        val porcentajeVic = if (total > 0) (victorias * 100 / total) else 0

        val totalGoles = partidos.sumOf { it.goles }
        val promGoles = if (total > 0) (totalGoles.toFloat() / total) else 0f

        val totalAsist = partidos.sumOf { it.asistencias }
        val promAsist = if (total > 0) (totalAsist.toFloat() / total) else 0f

        val totalPalos = partidos.sumOf { it.tirosAlPalo }
        val gf = partidos.sumOf { it.golesAFavor }
        val gc = partidos.sumOf { it.golesEnContra }

        ResumenEstadisticas(
            totalPartidos = total,
            victorias = victorias,
            empates = empates,
            derrotas = derrotas,
            porcentajeVictorias = porcentajeVic,
            totalGoles = totalGoles,
            promedioGoles = promGoles,
            totalAsistencias = totalAsist,
            promedioAsistencias = promAsist,
            totalPalos = totalPalos,
            golesAFavor = gf,
            golesEnContra = gc,
            diferenciaGoles = gf - gc
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ResumenEstadisticas()
    )

    fun setFiltroModoJuego(modo: TipoFutbol?) {
        _filtroModoJuego.value = modo
    }

    fun setFiltroTiempo(tipo: TipoFiltroEstadisticas) {
        _filtroTiempo.value = tipo
    }

    fun setTemporada(temporada: String) {
        _temporadaSeleccionada.value = temporada
    }

    fun setAnio(anio: Int) {
        _anioSeleccionado.value = anio
    }

    fun setRangoFechas(inicio: Long, fin: Long) {
        _fechaInicio.value = inicio
        _fechaFin.value = fin
    }

    private fun calcularTemporadaActual(): String {
        val now = LocalDate.now()
        val year = now.year
        val startYear = if (now.monthValue >= 9) year else year - 1
        val endTwoDigits = String.format(Locale.getDefault(), "%02d", (startYear + 1) % 100)
        return "$startYear/$endTwoDigits"
    }

    private fun obtenerTemporada(fechaMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = fechaMillis }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val startYear = if (month >= Calendar.SEPTEMBER) year else year - 1
        val endTwoDigits = String.format(Locale.getDefault(), "%02d", (startYear + 1) % 100)
        return "$startYear/$endTwoDigits"
    }

    // --- Pestaña General (Ranking por Jugador) ---

    val todosJugadores: StateFlow<List<Jugador>> = jugadorRepository.obtenerJugadores()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _busquedaGeneral = MutableStateFlow("")
    val busquedaGeneral: StateFlow<String> = _busquedaGeneral.asStateFlow()

    private val _soloFavoritosGeneral = MutableStateFlow(false)
    val soloFavoritosGeneral: StateFlow<Boolean> = _soloFavoritosGeneral.asStateFlow()

    private val _posicionGeneral = MutableStateFlow<Posicion?>(null)
    val posicionGeneral: StateFlow<Posicion?> = _posicionGeneral.asStateFlow()

    private val _soloPosicionPrincipalGeneral = MutableStateFlow(false)
    val soloPosicionPrincipalGeneral: StateFlow<Boolean> = _soloPosicionPrincipalGeneral.asStateFlow()

    private val _criterioOrdenGeneral = MutableStateFlow(CriterioOrdenGeneral.VICTORIAS)
    val criterioOrdenGeneral: StateFlow<CriterioOrdenGeneral> = _criterioOrdenGeneral.asStateFlow()

    private val _ordenAscendenteGeneral = MutableStateFlow(false)
    val ordenAscendenteGeneral: StateFlow<Boolean> = _ordenAscendenteGeneral.asStateFlow()

    val jugadoresEstadisticasGeneral: StateFlow<List<EstadisticasJugadorGeneral>> = combine(
        todosJugadores,
        todosPartidos,
        combine(
            _busquedaGeneral,
            _soloFavoritosGeneral,
            _posicionGeneral,
            _soloPosicionPrincipalGeneral
        ) { q, fav, pos, soloPrin ->
            FiltrosGeneralData(q, fav, pos, soloPrin)
        },
        combine(
            _criterioOrdenGeneral,
            _ordenAscendenteGeneral
        ) { crit, asc ->
            Pair(crit, asc)
        }
    ) { jugadores, partidos, filtros, orden ->
        val calculados = jugadores.map { j ->
            var v = 0
            var e = 0
            var d = 0
            var min = 0
            for (p in partidos) {
                val enMiEquipo = p.jugadoresMiEquipo.contains(j.id)
                val enRival = p.jugadoresEquipoRival.contains(j.id)
                val participo = enMiEquipo || enRival || (p.jugadoresMiEquipo.isEmpty() && p.jugadoresEquipoRival.isEmpty() && p.jugadoresIds.contains(j.id))
                if (participo) {
                    min += p.duracionMinutos
                }
                if (enMiEquipo) {
                    if (p.esVictoria) v++
                    else if (p.esEmpate) e++
                    else if (p.esDerrota) d++
                } else if (enRival) {
                    if (p.esDerrota) v++
                    else if (p.esEmpate) e++
                    else if (p.esVictoria) d++
                } else if (p.jugadoresMiEquipo.isEmpty() && p.jugadoresEquipoRival.isEmpty() && p.jugadoresIds.contains(j.id)) {
                    if (p.esVictoria) v++
                    else if (p.esEmpate) e++
                    else if (p.esDerrota) d++
                }
            }
            val pj = v + e + d
            val pct = if (pj > 0) (v * 100 / pj) else 0
            EstadisticasJugadorGeneral(
                jugador = j,
                partidosJugados = pj,
                victorias = v,
                empates = e,
                derrotas = d,
                porcentajeVictorias = pct,
                minutosJugados = min
            )
        }

        val filtrados = calculados.filter { item ->
            val coincideBusqueda = filtros.busqueda.isBlank() || item.jugador.nombre.contains(filtros.busqueda, ignoreCase = true)
            val coincideFav = !filtros.soloFavoritos || item.jugador.esFavorito
            val coincidePos = when {
                filtros.posicion == null -> true
                filtros.soloPosicionPrincipal -> item.jugador.posicionesPrimarias.contains(filtros.posicion)
                else -> item.jugador.posicionesPrimarias.contains(filtros.posicion) || item.jugador.posicionesSecundarias.contains(filtros.posicion)
            }
            coincideBusqueda && coincideFav && coincidePos
        }

        val (criterio, asc) = orden
        val ordenados = when (criterio) {
            CriterioOrdenGeneral.VICTORIAS -> if (asc) filtrados.sortedWith(compareBy({ it.victorias }, { it.porcentajeVictorias }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.victorias }.thenByDescending { it.porcentajeVictorias }.thenBy { it.jugador.nombre })
            CriterioOrdenGeneral.DERROTAS -> if (asc) filtrados.sortedWith(compareBy({ it.derrotas }, { it.partidosJugados }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.derrotas }.thenByDescending { it.partidosJugados }.thenBy { it.jugador.nombre })
            CriterioOrdenGeneral.EMPATES -> if (asc) filtrados.sortedWith(compareBy({ it.empates }, { it.partidosJugados }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.empates }.thenByDescending { it.partidosJugados }.thenBy { it.jugador.nombre })
            CriterioOrdenGeneral.PARTIDOS -> if (asc) filtrados.sortedWith(compareBy({ it.partidosJugados }, { it.victorias }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.partidosJugados }.thenByDescending { it.victorias }.thenBy { it.jugador.nombre })
            CriterioOrdenGeneral.PORCENTAJE -> if (asc) filtrados.sortedWith(compareBy({ it.porcentajeVictorias }, { it.victorias }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.porcentajeVictorias }.thenByDescending { it.victorias }.thenBy { it.jugador.nombre })
            CriterioOrdenGeneral.MINUTOS -> if (asc) filtrados.sortedWith(compareBy({ it.minutosJugados }, { it.partidosJugados }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.minutosJugados }.thenByDescending { it.partidosJugados }.thenBy { it.jugador.nombre })
            CriterioOrdenGeneral.NOMBRE -> if (asc) filtrados.sortedBy { it.jugador.nombre.lowercase() }
                else filtrados.sortedByDescending { it.jugador.nombre.lowercase() }
        }

        ordenados
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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

    fun setCriterioOrdenGeneral(criterio: CriterioOrdenGeneral) {
        if (_criterioOrdenGeneral.value == criterio) {
            _ordenAscendenteGeneral.value = !_ordenAscendenteGeneral.value
        } else {
            _criterioOrdenGeneral.value = criterio
            _ordenAscendenteGeneral.value = when (criterio) {
                CriterioOrdenGeneral.NOMBRE -> true
                else -> false
            }
        }
    }

    fun toggleOrdenAscendenteGeneral() {
        _ordenAscendenteGeneral.value = !_ordenAscendenteGeneral.value
    }
}
