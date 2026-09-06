package com.diegoguerrero.futtracker.ui.screens.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegoguerrero.futtracker.domain.model.Clima
import com.diegoguerrero.futtracker.domain.model.EquipoColor
import com.diegoguerrero.futtracker.domain.model.Estadio
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.domain.model.TipoFutbol
import com.diegoguerrero.futtracker.domain.repository.EstadioRepository
import com.diegoguerrero.futtracker.domain.repository.JugadorRepository
import com.diegoguerrero.futtracker.domain.repository.PartidoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlin.math.roundToInt
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
    val minutosJugados: Int = 0,
    val partidosConStats: Int = 0,
    val goles: Int = 0,
    val golesDiestra: Int = 0,
    val golesZurda: Int = 0,
    val golesCabeza: Int = 0,
    val golesOtro: Int = 0,
    val asistencias: Int = 0,
    val tirosAlPalo: Int = 0,
    val golesFueraArea: Int = 0,
    val golesChilena: Int = 0,
    val golesTacon: Int = 0,
    val partidosPortero: Int = 0,
    val golesEncajadosTotal: Int = 0,
    val paradasTotal: Int = 0
) {
    val golesPorPartido: Float
        get() = if (partidosConStats > 0) goles.toFloat() / partidosConStats else 0f
    val golesDiestraPorPartido: Float
        get() = if (partidosConStats > 0) golesDiestra.toFloat() / partidosConStats else 0f
    val golesZurdaPorPartido: Float
        get() = if (partidosConStats > 0) golesZurda.toFloat() / partidosConStats else 0f
    val golesCabezaPorPartido: Float
        get() = if (partidosConStats > 0) golesCabeza.toFloat() / partidosConStats else 0f
    val golesOtroPorPartido: Float
        get() = if (partidosConStats > 0) golesOtro.toFloat() / partidosConStats else 0f
    val asistenciasPorPartido: Float
        get() = if (partidosConStats > 0) asistencias.toFloat() / partidosConStats else 0f
    val tirosAlPaloPorPartido: Float
        get() = if (partidosConStats > 0) tirosAlPalo.toFloat() / partidosConStats else 0f
    val golesFueraAreaPorPartido: Float
        get() = if (partidosConStats > 0) golesFueraArea.toFloat() / partidosConStats else 0f
    val golesChilenaPorPartido: Float
        get() = if (partidosConStats > 0) golesChilena.toFloat() / partidosConStats else 0f
    val golesTaconPorPartido: Float
        get() = if (partidosConStats > 0) golesTacon.toFloat() / partidosConStats else 0f
    val golesEncajadosPorPartido: Float
        get() = if (partidosPortero > 0) golesEncajadosTotal.toFloat() / partidosPortero else 0f
    val paradasPorPartido: Float
        get() = if (partidosConStats > 0) paradasTotal.toFloat() / partidosConStats else 0f
}

enum class FranjaHoraria(val label: String, val emoji: String) {
    MANANA("Mañana (8h a 16h)", "🌅"),
    TARDE("Tarde (17h a 20h)", "🌇"),
    NOCHE("Noche (21h a 7h)", "🌙")
}

data class StatsClima(
    val clima: Clima?,
    val total: Int,
    val porcentaje: Float,
    val victorias: Int = 0,
    val empates: Int = 0,
    val derrotas: Int = 0,
    val golesFavor: Int = 0,
    val golesContra: Int = 0,
    val ratioGoles: Float = 0f,
    val label: String = clima?.label ?: "Techado",
    val emoji: String = clima?.emoji ?: "🏠"
)

data class StatsEstadio(
    val nombre: String,
    val total: Int,
    val porcentaje: Float
)

data class StatsDiaSemana(
    val dia: String,
    val diaNum: Int = 0,
    val total: Int = 0,
    val porcentaje: Float = 0f,
    val nombreDia: String = ""
)

data class StatsFranjaHoraria(
    val franja: FranjaHoraria,
    val total: Int,
    val porcentaje: Float
)

typealias StatsEquipoColor = StatsColorCamiseta

data class StatsColorCamiseta(
    val color: EquipoColor,
    val partidosJugados: Int = 0,
    val victorias: Int = 0,
    val empates: Int = 0,
    val derrotas: Int = 0,
    val porcentajeVictorias: Int = 0
)

data class StatsPosicionFrecuencia(
    val posicion: Posicion,
    val minutos: Int = 0,
    val partidosJugados: Int = 0,
    val victorias: Int = 0,
    val empates: Int = 0,
    val derrotas: Int = 0,
    val porcentajeVictorias: Int = 0,
    val goles: Int = 0,
    val asistencias: Int = 0,
    val tirosAlPalo: Int = 0,
    val total: Float = minutos.toFloat(),
    val porcentaje: Float = 0f
)

data class StatsHoraPartido(
    val hora: Int,
    val total: Int,
    val horaTexto: String
)

enum class CriterioOrdenGeneral {
    PORCENTAJE,
    NOMBRE,
    PARTIDOS,
    MINUTOS,
    VICTORIAS,
    EMPATES,
    DERROTAS,
    GOLES,
    GOLES_DIESTRA,
    GOLES_ZURDA,
    GOLES_CABEZA,
    GOLES_OTRO,
    ASISTENCIAS,
    TIROS_AL_PALO,
    FUERA_AREA,
    CHILENAS,
    TACONES,
    GOLES_ENCAJADOS,
    PARADAS
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
    private val jugadorRepository: JugadorRepository,
    private val estadioRepository: EstadioRepository
) : ViewModel() {

    val todosPartidos: StateFlow<List<Partido>> = partidoRepository.obtenerPartidos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todosEstadios: StateFlow<List<Estadio>> = estadioRepository.obtenerEstadios()
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
        val temporadas = partidos.filter { it.jugadoPorMi }.map { obtenerTemporada(it.fecha) }
        (listOf(actual) + temporadas).distinct().sortedDescending()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(calcularTemporadaActual())
    )

    val aniosConDatos: StateFlow<List<Int>> = todosPartidos.map { partidos ->
        val actual = LocalDate.now().year
        val anios = partidos.filter { it.jugadoPorMi }.map {
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

    private val _jugadorInspeccionadoId = MutableStateFlow<String?>(null) // null = Usuario propio
    val jugadorInspeccionadoId: StateFlow<String?> = _jugadorInspeccionadoId.asStateFlow()

    fun seleccionarJugadorInspeccionado(id: String?) {
        _jugadorInspeccionadoId.value = id
    }

    val todosPartidosFiltroGeneral: StateFlow<List<Partido>> = combine(
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

    val partidosFiltrados: StateFlow<List<Partido>> = combine(
        todosPartidosFiltroGeneral,
        _jugadorInspeccionadoId
    ) { lista, jId ->
        if (jId == null) {
            lista.filter { it.jugadoPorMi }
        } else {
            lista.filter { p ->
                p.jugadoresMiEquipo.contains(jId) ||
                p.jugadoresEquipoRival.contains(jId) ||
                p.jugadoresDetalle.any { it.jugadorId == jId } ||
                p.jugadoresIds.contains(jId)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val resumen: StateFlow<ResumenEstadisticas> = combine(partidosFiltrados, _jugadorInspeccionadoId) { partidos, jId ->
        val total = partidos.size
        var victorias = 0
        var empates = 0
        var derrotas = 0
        var totalGoles = 0
        var totalAsist = 0
        var totalPalos = 0
        var gf = 0
        var gc = 0
        var partidosConStats = 0

        for (p in partidos) {
            val enMiEquipo = if (jId == null) true else p.jugadoresMiEquipo.contains(jId)
            val enRival = if (jId == null) false else p.jugadoresEquipoRival.contains(jId)
            val det = if (jId == null) null else p.jugadoresDetalle.firstOrNull { it.jugadorId == jId }

            if (enMiEquipo) {
                if (p.esVictoria) victorias++
                else if (p.esEmpate) empates++
                else if (p.esDerrota) derrotas++
                gf += p.golesAFavor
                gc += p.golesEnContra
            } else if (enRival) {
                if (p.esDerrota) victorias++
                else if (p.esEmpate) empates++
                else if (p.esVictoria) derrotas++
                gf += p.golesEnContra
                gc += p.golesAFavor
            } else {
                if (p.esVictoria) victorias++
                else if (p.esEmpate) empates++
                else if (p.esDerrota) derrotas++
                gf += p.golesAFavor
                gc += p.golesEnContra
            }

            if (jId == null) {
                if (p.jugadoPorMi) {
                    totalGoles += p.goles
                    totalAsist += p.asistencias
                    totalPalos += p.tirosAlPalo
                    partidosConStats++
                }
            } else {
                if (det != null && det.statsRegistradas) {
                    totalGoles += det.goles
                    totalAsist += det.asistencias
                    totalPalos += det.tirosAlPalo
                    partidosConStats++
                }
            }
        }

        val porcentajeVic = if (total > 0) (victorias * 100 / total) else 0
        val promGoles = if (partidosConStats > 0) (totalGoles.toFloat() / partidosConStats) else 0f
        val promAsist = if (partidosConStats > 0) (totalAsist.toFloat() / partidosConStats) else 0f

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

    // --- Pestaña Partidos ---

    private val _filtroClimas = MutableStateFlow<Set<Clima>>(emptySet())
    val filtroClimas: StateFlow<Set<Clima>> = _filtroClimas.asStateFlow()

    private val _filtroTechado = MutableStateFlow(false)
    val filtroTechado: StateFlow<Boolean> = _filtroTechado.asStateFlow()

    private val _filtroEstadios = MutableStateFlow<Set<String>>(emptySet())
    val filtroEstadios: StateFlow<Set<String>> = _filtroEstadios.asStateFlow()

    private val _filtroDiasSemana = MutableStateFlow<Set<Int>>(emptySet())
    val filtroDiasSemana: StateFlow<Set<Int>> = _filtroDiasSemana.asStateFlow()

    private val _filtroFranjasHorarias = MutableStateFlow<Set<FranjaHoraria>>(emptySet())
    val filtroFranjasHorarias: StateFlow<Set<FranjaHoraria>> = _filtroFranjasHorarias.asStateFlow()

    val hayFiltrosPartidosActivos: StateFlow<Boolean> = combine(
        _filtroClimas, _filtroTechado, _filtroEstadios, _filtroDiasSemana, _filtroFranjasHorarias
    ) { c, t, e, d, fh ->
        c.isNotEmpty() || t || e.isNotEmpty() || d.isNotEmpty() || fh.isNotEmpty()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun toggleFiltroClima(clima: Clima) {
        _filtroClimas.value = if (_filtroClimas.value.contains(clima)) {
            _filtroClimas.value - clima
        } else {
            _filtroClimas.value + clima
        }
    }

    fun toggleFiltroTechado() {
        _filtroTechado.value = !_filtroTechado.value
    }

    fun toggleFiltroEstadio(estadioNombre: String) {
        _filtroEstadios.value = if (_filtroEstadios.value.contains(estadioNombre)) {
            _filtroEstadios.value - estadioNombre
        } else {
            _filtroEstadios.value + estadioNombre
        }
    }

    fun toggleFiltroDiaSemana(diaNum: Int) {
        _filtroDiasSemana.value = if (_filtroDiasSemana.value.contains(diaNum)) {
            _filtroDiasSemana.value - diaNum
        } else {
            _filtroDiasSemana.value + diaNum
        }
    }

    fun toggleFiltroFranjaHoraria(franja: FranjaHoraria) {
        _filtroFranjasHorarias.value = if (_filtroFranjasHorarias.value.contains(franja)) {
            _filtroFranjasHorarias.value - franja
        } else {
            _filtroFranjasHorarias.value + franja
        }
    }

    fun limpiarFiltrosPartidos() {
        _filtroClimas.value = emptySet()
        _filtroTechado.value = false
        _filtroEstadios.value = emptySet()
        _filtroDiasSemana.value = emptySet()
        _filtroFranjasHorarias.value = emptySet()
    }

    val estadiosDisponiblesFiltro: StateFlow<List<String>> = combine(todosPartidosFiltroGeneral, todosEstadios) { partidos, estadios ->
        val map = estadios.associateBy { it.id }
        val names = partidos.map { p -> p.estadioId?.let { map[it]?.nombre } ?: "Sin ubicación" }.distinct().sorted()
        names
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val partidosTabPartidos: StateFlow<List<Partido>> = combine(
        listOf(
            todosPartidosFiltroGeneral,
            todosEstadios,
            _filtroClimas,
            _filtroTechado,
            _filtroEstadios,
            _filtroDiasSemana,
            _filtroFranjasHorarias
        )
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val partidos = flows[0] as List<Partido>
        @Suppress("UNCHECKED_CAST")
        val estadios = flows[1] as List<Estadio>
        @Suppress("UNCHECKED_CAST")
        val climas = flows[2] as Set<Clima>
        @Suppress("UNCHECKED_CAST")
        val techado = flows[3] as Boolean
        @Suppress("UNCHECKED_CAST")
        val estadiosFiltro = flows[4] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val dias = flows[5] as Set<Int>
        @Suppress("UNCHECKED_CAST")
        val franjas = flows[6] as Set<FranjaHoraria>

        val mapEstadios = estadios.associateBy { it.id }
        val cal = Calendar.getInstance()

        partidos.filter { p ->
            val hayFiltroClima = climas.isNotEmpty() || techado
            if (hayFiltroClima) {
                val coincideClima = (p.clima != null && climas.contains(p.clima)) || (p.clima == null && techado)
                if (!coincideClima) return@filter false
            }
            if (estadiosFiltro.isNotEmpty()) {
                val nomEstadio = p.estadioId?.let { mapEstadios[it]?.nombre } ?: "Sin ubicación"
                if (!estadiosFiltro.contains(nomEstadio)) {
                    return@filter false
                }
            }
            cal.timeInMillis = p.fecha
            if (dias.isNotEmpty()) {
                val dow = cal.get(Calendar.DAY_OF_WEEK)
                if (!dias.contains(dow)) {
                    return@filter false
                }
            }
            if (franjas.isNotEmpty()) {
                val h = cal.get(Calendar.HOUR_OF_DAY)
                val coincideFranja = franjas.any { f ->
                    when (f) {
                        FranjaHoraria.MANANA -> h in 8..16
                        FranjaHoraria.TARDE -> h in 17..20
                        FranjaHoraria.NOCHE -> h >= 21 || h <= 7
                    }
                }
                if (!coincideFranja) {
                    return@filter false
                }
            }
            true
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val statsClima: StateFlow<List<StatsClima>> = partidosTabPartidos.map { partidos ->
        val total = partidos.size
        val lista = mutableListOf<StatsClima>()

        Clima.entries.forEach { c ->
            val count = partidos.count { it.clima == c }
            val pct = if (total > 0) (count * 100f / total) else 0f
            lista.add(
                StatsClima(
                    clima = c,
                    label = c.label,
                    emoji = if (c == Clima.DESPEJADO) "☀️/🌙" else c.emoji,
                    total = count,
                    porcentaje = pct
                )
            )
        }

        val countTechado = partidos.count { it.clima == null }
        val pctTechado = if (total > 0) (countTechado * 100f / total) else 0f
        lista.add(
            StatsClima(
                clima = null,
                label = "Techado",
                emoji = "🏟️",
                total = countTechado,
                porcentaje = pctTechado
            )
        )

        lista
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val statsEstadios: StateFlow<List<StatsEstadio>> = combine(partidosTabPartidos, todosEstadios) { partidos, estadios ->
        val total = partidos.size
        val mapEstadios = estadios.associateBy { it.id }
        val counts = mutableMapOf<String, Int>()
        partidos.forEach { p ->
            val nombre = p.estadioId?.let { mapEstadios[it]?.nombre } ?: "Sin ubicación"
            counts[nombre] = (counts[nombre] ?: 0) + 1
        }
        counts.map { (nom, count) ->
            val pct = if (total > 0) (count * 100f / total) else 0f
            StatsEstadio(nombre = nom, total = count, porcentaje = pct)
        }.sortedByDescending { it.total }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val statsDiasSemana: StateFlow<List<StatsDiaSemana>> = partidosTabPartidos.map { partidos ->
        val dias = listOf(
            StatsDiaSemana(dia = "Lun", diaNum = Calendar.MONDAY, total = 0),
            StatsDiaSemana(dia = "Mar", diaNum = Calendar.TUESDAY, total = 0),
            StatsDiaSemana(dia = "Mié", diaNum = Calendar.WEDNESDAY, total = 0),
            StatsDiaSemana(dia = "Jue", diaNum = Calendar.THURSDAY, total = 0),
            StatsDiaSemana(dia = "Vie", diaNum = Calendar.FRIDAY, total = 0),
            StatsDiaSemana(dia = "Sáb", diaNum = Calendar.SATURDAY, total = 0),
            StatsDiaSemana(dia = "Dom", diaNum = Calendar.SUNDAY, total = 0),
        )
        val counts = IntArray(8)
        val cal = Calendar.getInstance()
        partidos.forEach { p ->
            cal.timeInMillis = p.fecha
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            counts[dow]++
        }
        dias.map { d -> d.copy(total = counts[d.diaNum]) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val statsHorasPartidos: StateFlow<List<StatsHoraPartido>> = partidosTabPartidos.map { partidos ->
        val counts = IntArray(24)
        val cal = Calendar.getInstance()
        partidos.forEach { p ->
            cal.timeInMillis = p.fecha
            val h = cal.get(Calendar.HOUR_OF_DAY)
            counts[h]++
        }
        val ordenHoras = (9..23) + (0..8)
        ordenHoras.map { h ->
            StatsHoraPartido(
                hora = h,
                total = counts[h],
                horaTexto = "${h}h"
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val statsClaroOscuro: StateFlow<Pair<StatsEquipoColor, StatsEquipoColor>> = combine(partidosFiltrados, _jugadorInspeccionadoId) { partidos, jId ->
        fun calcStats(eq: EquipoColor): StatsEquipoColor {
            val matches = partidos.filter { p ->
                if (jId == null) {
                    p.equipoJugado == eq
                } else {
                    val det = p.jugadoresDetalle.firstOrNull { it.jugadorId == jId }
                    if (det != null) {
                        val playerEq = if (det.esMiEquipo) p.equipoJugado else (if (p.equipoJugado == EquipoColor.CLARO) EquipoColor.OSCURO else EquipoColor.CLARO)
                        playerEq == eq
                    } else if (p.jugadoresMiEquipo.contains(jId)) {
                        p.equipoJugado == eq
                    } else if (p.jugadoresEquipoRival.contains(jId)) {
                        (if (p.equipoJugado == EquipoColor.CLARO) EquipoColor.OSCURO else EquipoColor.CLARO) == eq
                    } else false
                }
            }
            val pj = matches.size
            var v = 0
            var e = 0
            var d = 0
            for (p in matches) {
                val enMiEquipo = if (jId == null) true else p.jugadoresMiEquipo.contains(jId)
                val enRival = if (jId == null) false else p.jugadoresEquipoRival.contains(jId)
                if (enMiEquipo) {
                    if (p.esVictoria) v++ else if (p.esEmpate) e++ else if (p.esDerrota) d++
                } else if (enRival) {
                    if (p.esDerrota) v++ else if (p.esEmpate) e++ else if (p.esVictoria) d++
                } else {
                    if (p.esVictoria) v++ else if (p.esEmpate) e++ else if (p.esDerrota) d++
                }
            }
            val pct = if (pj > 0) (v * 100 / pj) else 0
            return StatsEquipoColor(color = eq, partidosJugados = pj, victorias = v, empates = e, derrotas = d, porcentajeVictorias = pct)
        }
        Pair(calcStats(EquipoColor.CLARO), calcStats(EquipoColor.OSCURO))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Pair(StatsEquipoColor(EquipoColor.CLARO), StatsEquipoColor(EquipoColor.OSCURO))
    )

    val statsPosicionesFrecuencia: StateFlow<List<StatsPosicionFrecuencia>> = combine(partidosFiltrados, _jugadorInspeccionadoId) { partidos, jId ->
        val posMinutos = mutableMapOf<Posicion, Int>()
        val posPJ = mutableMapOf<Posicion, Int>()
        val posV = mutableMapOf<Posicion, Int>()
        val posE = mutableMapOf<Posicion, Int>()
        val posD = mutableMapOf<Posicion, Int>()
        val posGoles = mutableMapOf<Posicion, Int>()
        val posAsist = mutableMapOf<Posicion, Int>()
        val posPalos = mutableMapOf<Posicion, Int>()

        partidos.forEach { p ->
            val enMiEquipo = if (jId == null) true else p.jugadoresMiEquipo.contains(jId)
            val enRival = if (jId == null) false else p.jugadoresEquipoRival.contains(jId)
            val det = if (jId == null) null else p.jugadoresDetalle.firstOrNull { it.jugadorId == jId }

            val esVic = if (enMiEquipo) p.esVictoria else if (enRival) p.esDerrota else p.esVictoria
            val esEmp = p.esEmpate
            val esDer = if (enMiEquipo) p.esDerrota else if (enRival) p.esVictoria else p.esDerrota

            val duracion = p.duracionMinutos.coerceAtLeast(1)
            val primarias = if (jId == null) {
                (p.posicionesJugadas - p.posicionesSecundarias).ifEmpty { setOf(p.posicionJugada) }
            } else {
                if (det != null) setOf(det.posicionPrincipal)
                else emptySet()
            }
            val secundarias = if (jId == null) {
                p.posicionesSecundarias - primarias
            } else {
                det?.posicionesSecundarias?.toSet() ?: emptySet()
            }

            val pesoPrimaria = 2.0
            val pesoSecundaria = 1.0
            val pesoTotal = (primarias.size * pesoPrimaria) + (secundarias.size * pesoSecundaria)

            if (pesoTotal > 0.0) {
                val minsPrim = if (primarias.isNotEmpty()) (duracion * (pesoPrimaria / pesoTotal)).roundToInt() else 0
                val minsSec = if (secundarias.isNotEmpty()) (duracion * (pesoSecundaria / pesoTotal)).roundToInt() else 0

                primarias.forEach { pos ->
                    posMinutos[pos] = (posMinutos[pos] ?: 0) + minsPrim
                    posPJ[pos] = (posPJ[pos] ?: 0) + 1
                    if (esVic) posV[pos] = (posV[pos] ?: 0) + 1
                    if (esEmp) posE[pos] = (posE[pos] ?: 0) + 1
                    if (esDer) posD[pos] = (posD[pos] ?: 0) + 1
                }
                secundarias.forEach { pos ->
                    posMinutos[pos] = (posMinutos[pos] ?: 0) + minsSec
                    posPJ[pos] = (posPJ[pos] ?: 0) + 1
                    if (esVic) posV[pos] = (posV[pos] ?: 0) + 1
                    if (esEmp) posE[pos] = (posE[pos] ?: 0) + 1
                    if (esDer) posD[pos] = (posD[pos] ?: 0) + 1
                }
            }

            // Goles, asistencias y tiros al palo considerando SOLO la posición principal
            if (jId == null) {
                if (p.jugadoPorMi) {
                    val posPrin = (p.posicionesJugadas - p.posicionesSecundarias).firstOrNull() ?: p.posicionJugada
                    posGoles[posPrin] = (posGoles[posPrin] ?: 0) + p.goles
                    posAsist[posPrin] = (posAsist[posPrin] ?: 0) + p.asistencias
                    posPalos[posPrin] = (posPalos[posPrin] ?: 0) + p.tirosAlPalo
                }
            } else {
                if (det != null && det.statsRegistradas) {
                    val posPrin = det.posicionPrincipal
                    posGoles[posPrin] = (posGoles[posPrin] ?: 0) + det.goles
                    posAsist[posPrin] = (posAsist[posPrin] ?: 0) + det.asistencias
                    posPalos[posPrin] = (posPalos[posPrin] ?: 0) + det.tirosAlPalo
                }
            }
        }

        val totalMinutosSuma = posMinutos.values.sum()

        Posicion.entries.map { pos ->
            val mins = posMinutos[pos] ?: 0
            val pj = posPJ[pos] ?: 0
            val v = posV[pos] ?: 0
            val e = posE[pos] ?: 0
            val d = posD[pos] ?: 0
            val pctV = if (pj > 0) (v * 100 / pj) else 0
            val pctMinutos = if (totalMinutosSuma > 0) (mins.toFloat() * 100f / totalMinutosSuma) else 0f

            StatsPosicionFrecuencia(
                posicion = pos,
                minutos = mins,
                partidosJugados = pj,
                victorias = v,
                empates = e,
                derrotas = d,
                porcentajeVictorias = pctV,
                goles = posGoles[pos] ?: 0,
                asistencias = posAsist[pos] ?: 0,
                tirosAlPalo = posPalos[pos] ?: 0,
                total = mins.toFloat(),
                porcentaje = pctMinutos
            )
        }.sortedByDescending { it.minutos }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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

    private val _criterioOrdenGeneral = MutableStateFlow(CriterioOrdenGeneral.PORCENTAJE)
    val criterioOrdenGeneral: StateFlow<CriterioOrdenGeneral> = _criterioOrdenGeneral.asStateFlow()

    private val _ordenAscendenteGeneral = MutableStateFlow(false)
    val ordenAscendenteGeneral: StateFlow<Boolean> = _ordenAscendenteGeneral.asStateFlow()

    private val _ordenPorPartidoGeneral = MutableStateFlow(false)
    val ordenPorPartidoGeneral: StateFlow<Boolean> = _ordenPorPartidoGeneral.asStateFlow()

    fun toggleOrdenPorPartidoGeneral() {
        _ordenPorPartidoGeneral.value = !_ordenPorPartidoGeneral.value
    }

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
            _ordenAscendenteGeneral,
            _ordenPorPartidoGeneral
        ) { crit, asc, porPartido ->
            Triple(crit, asc, porPartido)
        }
    ) { jugadores, partidos, filtros, (criterio, asc, porPartido) ->
        val calculados = jugadores.map { j ->
            var v = 0
            var e = 0
            var d = 0
            var min = 0
            var goles = 0
            var golesDiestra = 0
            var golesZurda = 0
            var golesCabeza = 0
            var golesOtro = 0
            var asistencias = 0
            var tirosAlPalo = 0
            var fueraArea = 0
            var chilenas = 0
            var tacones = 0
            var partidosPortero = 0
            var golesEncajadosTotal = 0
            var paradas = 0

            var partidosConStats = 0

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

                val det = p.jugadoresDetalle.firstOrNull { it.jugadorId == j.id }
                val esUsuario = j.esUsuarioPropio

                if (det != null && det.statsRegistradas) {
                    partidosConStats++
                    goles += det.goles
                    golesDiestra += det.golesDiestra
                    golesZurda += det.golesZurda
                    golesCabeza += det.golesCabeza
                    golesOtro += det.golesOtro
                    asistencias += det.asistencias
                    tirosAlPalo += det.tirosAlPalo
                    fueraArea += det.golesFueraArea
                    chilenas += det.golesChilena
                    tacones += det.golesTacon
                    val jugoPortero = det.posicionPrincipal == Posicion.POR || det.posicionesSecundarias.contains(Posicion.POR)
                    if (jugoPortero) {
                        paradas += det.paradas
                    }
                    if (det.posicionPrincipal == Posicion.POR && det.posicionesSecundarias.isEmpty()) {
                        partidosPortero++
                        golesEncajadosTotal += if (det.esMiEquipo) p.golesEnContra else p.golesAFavor
                    }
                } else if (esUsuario && p.jugadoPorMi) {
                    partidosConStats++
                    goles += p.goles
                    golesDiestra += p.golesDiestra
                    golesZurda += p.golesZurda
                    golesCabeza += p.golesCabeza
                    golesOtro += p.golesOtro
                    asistencias += p.asistencias
                    tirosAlPalo += p.tirosAlPalo
                    fueraArea += p.golesFueraArea
                    chilenas += p.golesChilena
                    tacones += p.golesTacon
                    val jugoPortero = p.posicionJugada == Posicion.POR || p.posicionesSecundarias.contains(Posicion.POR) || p.posicionesJugadas.contains(Posicion.POR)
                    if (jugoPortero) {
                        paradas += p.paradas
                    }
                    if (p.posicionJugada == Posicion.POR && p.posicionesSecundarias.isEmpty()) {
                        partidosPortero++
                        golesEncajadosTotal += p.golesEnContra
                    }
                } else if (participo && j.posicionesPrimarias.contains(Posicion.POR) && j.posicionesSecundarias.isEmpty()) {
                    partidosPortero++
                    golesEncajadosTotal += if (enMiEquipo) p.golesEnContra else p.golesAFavor
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
                minutosJugados = min,
                partidosConStats = partidosConStats,
                goles = goles,
                golesDiestra = golesDiestra,
                golesZurda = golesZurda,
                golesCabeza = golesCabeza,
                golesOtro = golesOtro,
                asistencias = asistencias,
                tirosAlPalo = tirosAlPalo,
                golesFueraArea = fueraArea,
                golesChilena = chilenas,
                golesTacon = tacones,
                partidosPortero = partidosPortero,
                golesEncajadosTotal = golesEncajadosTotal,
                paradasTotal = paradas
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
            val aptoGolesEncajados = criterio != CriterioOrdenGeneral.GOLES_ENCAJADOS || item.partidosPortero > 0
            val aptoParadas = criterio != CriterioOrdenGeneral.PARADAS || item.paradasTotal > 0 || item.partidosPortero > 0
            coincideBusqueda && coincideFav && coincidePos && aptoGolesEncajados && aptoParadas
        }

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
            CriterioOrdenGeneral.GOLES -> if (porPartido) {
                if (asc) filtrados.sortedWith(compareBy({ it.golesPorPartido }, { it.goles }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesPorPartido }.thenByDescending { it.goles }.thenBy { it.jugador.nombre })
            } else {
                if (asc) filtrados.sortedWith(compareBy({ it.goles }, { it.golesPorPartido }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.goles }.thenByDescending { it.golesPorPartido }.thenBy { it.jugador.nombre })
            }
            CriterioOrdenGeneral.GOLES_DIESTRA -> if (porPartido) {
                if (asc) filtrados.sortedWith(compareBy({ it.golesDiestraPorPartido }, { it.golesDiestra }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesDiestraPorPartido }.thenByDescending { it.golesDiestra }.thenBy { it.jugador.nombre })
            } else {
                if (asc) filtrados.sortedWith(compareBy({ it.golesDiestra }, { it.golesDiestraPorPartido }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesDiestra }.thenByDescending { it.golesDiestraPorPartido }.thenBy { it.jugador.nombre })
            }
            CriterioOrdenGeneral.GOLES_ZURDA -> if (porPartido) {
                if (asc) filtrados.sortedWith(compareBy({ it.golesZurdaPorPartido }, { it.golesZurda }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesZurdaPorPartido }.thenByDescending { it.golesZurda }.thenBy { it.jugador.nombre })
            } else {
                if (asc) filtrados.sortedWith(compareBy({ it.golesZurda }, { it.golesZurdaPorPartido }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesZurda }.thenByDescending { it.golesZurdaPorPartido }.thenBy { it.jugador.nombre })
            }
            CriterioOrdenGeneral.GOLES_CABEZA -> if (porPartido) {
                if (asc) filtrados.sortedWith(compareBy({ it.golesCabezaPorPartido }, { it.golesCabeza }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesCabezaPorPartido }.thenByDescending { it.golesCabeza }.thenBy { it.jugador.nombre })
            } else {
                if (asc) filtrados.sortedWith(compareBy({ it.golesCabeza }, { it.golesCabezaPorPartido }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesCabeza }.thenByDescending { it.golesCabezaPorPartido }.thenBy { it.jugador.nombre })
            }
            CriterioOrdenGeneral.GOLES_OTRO -> if (porPartido) {
                if (asc) filtrados.sortedWith(compareBy({ it.golesOtroPorPartido }, { it.golesOtro }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesOtroPorPartido }.thenByDescending { it.golesOtro }.thenBy { it.jugador.nombre })
            } else {
                if (asc) filtrados.sortedWith(compareBy({ it.golesOtro }, { it.golesOtroPorPartido }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesOtro }.thenByDescending { it.golesOtroPorPartido }.thenBy { it.jugador.nombre })
            }
            CriterioOrdenGeneral.ASISTENCIAS -> if (porPartido) {
                if (asc) filtrados.sortedWith(compareBy({ it.asistenciasPorPartido }, { it.asistencias }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.asistenciasPorPartido }.thenByDescending { it.asistencias }.thenBy { it.jugador.nombre })
            } else {
                if (asc) filtrados.sortedWith(compareBy({ it.asistencias }, { it.asistenciasPorPartido }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.asistencias }.thenByDescending { it.asistenciasPorPartido }.thenBy { it.jugador.nombre })
            }
            CriterioOrdenGeneral.TIROS_AL_PALO -> if (porPartido) {
                if (asc) filtrados.sortedWith(compareBy({ it.tirosAlPaloPorPartido }, { it.tirosAlPalo }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.tirosAlPaloPorPartido }.thenByDescending { it.tirosAlPalo }.thenBy { it.jugador.nombre })
            } else {
                if (asc) filtrados.sortedWith(compareBy({ it.tirosAlPalo }, { it.tirosAlPaloPorPartido }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.tirosAlPalo }.thenByDescending { it.tirosAlPaloPorPartido }.thenBy { it.jugador.nombre })
            }
            CriterioOrdenGeneral.FUERA_AREA -> if (porPartido) {
                if (asc) filtrados.sortedWith(compareBy({ it.golesFueraAreaPorPartido }, { it.golesFueraArea }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesFueraAreaPorPartido }.thenByDescending { it.golesFueraArea }.thenBy { it.jugador.nombre })
            } else {
                if (asc) filtrados.sortedWith(compareBy({ it.golesFueraArea }, { it.golesFueraAreaPorPartido }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesFueraArea }.thenByDescending { it.golesFueraAreaPorPartido }.thenBy { it.jugador.nombre })
            }
            CriterioOrdenGeneral.CHILENAS -> if (porPartido) {
                if (asc) filtrados.sortedWith(compareBy({ it.golesChilenaPorPartido }, { it.golesChilena }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesChilenaPorPartido }.thenByDescending { it.golesChilena }.thenBy { it.jugador.nombre })
            } else {
                if (asc) filtrados.sortedWith(compareBy({ it.golesChilena }, { it.golesChilenaPorPartido }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesChilena }.thenByDescending { it.golesChilenaPorPartido }.thenBy { it.jugador.nombre })
            }
            CriterioOrdenGeneral.TACONES -> if (porPartido) {
                if (asc) filtrados.sortedWith(compareBy({ it.golesTaconPorPartido }, { it.golesTacon }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesTaconPorPartido }.thenByDescending { it.golesTacon }.thenBy { it.jugador.nombre })
            } else {
                if (asc) filtrados.sortedWith(compareBy({ it.golesTacon }, { it.golesTaconPorPartido }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesTacon }.thenByDescending { it.golesTaconPorPartido }.thenBy { it.jugador.nombre })
            }
            CriterioOrdenGeneral.GOLES_ENCAJADOS -> if (porPartido) {
                if (asc) filtrados.sortedWith(compareBy({ it.golesEncajadosPorPartido }, { it.golesEncajadosTotal }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesEncajadosPorPartido }.thenByDescending { it.golesEncajadosTotal }.thenBy { it.jugador.nombre })
            } else {
                if (asc) filtrados.sortedWith(compareBy({ it.golesEncajadosTotal }, { it.golesEncajadosPorPartido }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.golesEncajadosTotal }.thenByDescending { it.golesEncajadosPorPartido }.thenBy { it.jugador.nombre })
            }
            CriterioOrdenGeneral.PARADAS -> if (porPartido) {
                if (asc) filtrados.sortedWith(compareBy({ it.paradasPorPartido }, { it.paradasTotal }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.paradasPorPartido }.thenByDescending { it.paradasTotal }.thenBy { it.jugador.nombre })
            } else {
                if (asc) filtrados.sortedWith(compareBy({ it.paradasTotal }, { it.paradasPorPartido }, { it.jugador.nombre }))
                else filtrados.sortedWith(compareByDescending<EstadisticasJugadorGeneral> { it.paradasTotal }.thenByDescending { it.paradasPorPartido }.thenBy { it.jugador.nombre })
            }
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
                CriterioOrdenGeneral.NOMBRE, CriterioOrdenGeneral.GOLES_ENCAJADOS -> true
                else -> false
            }
        }
    }

    fun toggleOrdenAscendenteGeneral() {
        _ordenAscendenteGeneral.value = !_ordenAscendenteGeneral.value
    }
}
