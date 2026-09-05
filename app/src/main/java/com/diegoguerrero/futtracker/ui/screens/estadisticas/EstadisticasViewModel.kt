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
    val minutosJugados: Int = 0
)

data class StatsClima(
    val clima: Clima?,
    val label: String,
    val emoji: String,
    val total: Int,
    val porcentaje: Float
)

data class StatsEstadio(
    val nombre: String,
    val total: Int,
    val porcentaje: Float
)

data class StatsDiaSemana(
    val dia: String,
    val diaNum: Int,
    val total: Int
)

data class StatsEquipoColor(
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
    DERROTAS
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
        lista = lista.filter { it.jugadoPorMi }

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

    // --- Pestaña Partidos ---

    val todosEstadios: StateFlow<List<Estadio>> = estadioRepository.obtenerEstadios()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _filtroClimas = MutableStateFlow<Set<Clima>>(emptySet())
    val filtroClimas: StateFlow<Set<Clima>> = _filtroClimas.asStateFlow()

    private val _filtroTechado = MutableStateFlow(false)
    val filtroTechado: StateFlow<Boolean> = _filtroTechado.asStateFlow()

    private val _filtroEstadios = MutableStateFlow<Set<String>>(emptySet())
    val filtroEstadios: StateFlow<Set<String>> = _filtroEstadios.asStateFlow()

    private val _filtroDiasSemana = MutableStateFlow<Set<Int>>(emptySet())
    val filtroDiasSemana: StateFlow<Set<Int>> = _filtroDiasSemana.asStateFlow()

    private val _filtroHoras = MutableStateFlow<Set<Int>>(emptySet())
    val filtroHoras: StateFlow<Set<Int>> = _filtroHoras.asStateFlow()

    val hayFiltrosPartidosActivos: StateFlow<Boolean> = combine(
        _filtroClimas, _filtroTechado, _filtroEstadios, _filtroDiasSemana, _filtroHoras
    ) { c, t, e, d, h ->
        c.isNotEmpty() || t || e.isNotEmpty() || d.isNotEmpty() || h.isNotEmpty()
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

    fun toggleFiltroHora(hora: Int) {
        _filtroHoras.value = if (_filtroHoras.value.contains(hora)) {
            _filtroHoras.value - hora
        } else {
            _filtroHoras.value + hora
        }
    }

    fun limpiarFiltrosPartidos() {
        _filtroClimas.value = emptySet()
        _filtroTechado.value = false
        _filtroEstadios.value = emptySet()
        _filtroDiasSemana.value = emptySet()
        _filtroHoras.value = emptySet()
    }

    val estadiosDisponiblesFiltro: StateFlow<List<String>> = combine(partidosFiltrados, todosEstadios) { partidos, estadios ->
        val map = estadios.associateBy { it.id }
        val names = partidos.map { p -> p.estadioId?.let { map[it]?.nombre } ?: "Sin ubicación" }.distinct().sorted()
        names
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val horasDisponiblesFiltro: StateFlow<List<Int>> = partidosFiltrados.map { partidos ->
        val cal = Calendar.getInstance()
        partidos.map { p ->
            cal.timeInMillis = p.fecha
            cal.get(Calendar.HOUR_OF_DAY)
        }.distinct().sorted()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val partidosTabPartidos: StateFlow<List<Partido>> = combine(
        listOf(
            partidosFiltrados,
            todosEstadios,
            _filtroClimas,
            _filtroTechado,
            _filtroEstadios,
            _filtroDiasSemana,
            _filtroHoras
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
        val horas = flows[6] as Set<Int>

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
            if (horas.isNotEmpty()) {
                val h = cal.get(Calendar.HOUR_OF_DAY)
                if (!horas.contains(h)) {
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
        if (countTechado > 0) {
            val pct = if (total > 0) (countTechado * 100f / total) else 0f
            lista.add(
                StatsClima(
                    clima = null,
                    label = "Techado",
                    emoji = "🏠",
                    total = countTechado,
                    porcentaje = pct
                )
            )
        }

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
        val horasConPartidos = (0..23).filter { counts[it] > 0 }
        if (horasConPartidos.isEmpty()) {
            emptyList()
        } else {
            val minHora = (horasConPartidos.minOrNull() ?: 9).coerceAtLeast(8)
            val maxHora = (horasConPartidos.maxOrNull() ?: 22).coerceAtMost(23)
            (minHora..maxHora).map { h ->
                StatsHoraPartido(
                    hora = h,
                    total = counts[h],
                    horaTexto = "${h}h"
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val statsClaroOscuro: StateFlow<Pair<StatsEquipoColor, StatsEquipoColor>> = partidosFiltrados.map { partidos ->
        fun calcStats(eq: EquipoColor): StatsEquipoColor {
            val matches = partidos.filter { it.equipoJugado == eq }
            val pj = matches.size
            val v = matches.count { it.esVictoria }
            val e = matches.count { it.esEmpate }
            val d = matches.count { it.esDerrota }
            val pct = if (pj > 0) (v * 100 / pj) else 0
            return StatsEquipoColor(color = eq, partidosJugados = pj, victorias = v, empates = e, derrotas = d, porcentajeVictorias = pct)
        }
        Pair(calcStats(EquipoColor.CLARO), calcStats(EquipoColor.OSCURO))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Pair(StatsEquipoColor(EquipoColor.CLARO), StatsEquipoColor(EquipoColor.OSCURO))
    )

    val statsPosicionesFrecuencia: StateFlow<List<StatsPosicionFrecuencia>> = partidosFiltrados.map { partidos ->
        val partidosJugados = partidos.filter { it.jugadoPorMi }
        val posMinutos = mutableMapOf<Posicion, Int>()
        val posPJ = mutableMapOf<Posicion, Int>()
        val posV = mutableMapOf<Posicion, Int>()
        val posE = mutableMapOf<Posicion, Int>()
        val posD = mutableMapOf<Posicion, Int>()

        partidosJugados.forEach { p ->
            val duracion = p.duracionMinutos.coerceAtLeast(1)
            val primarias = (p.posicionesJugadas - p.posicionesSecundarias).ifEmpty { setOf(p.posicionJugada) }
            val secundarias = p.posicionesSecundarias - primarias

            val pesoPrimaria = 2.0
            val pesoSecundaria = 1.0
            val pesoTotal = (primarias.size * pesoPrimaria) + (secundarias.size * pesoSecundaria)

            if (pesoTotal > 0.0) {
                val minsPrim = if (primarias.isNotEmpty()) (duracion * (pesoPrimaria / pesoTotal)).roundToInt() else 0
                val minsSec = if (secundarias.isNotEmpty()) (duracion * (pesoSecundaria / pesoTotal)).roundToInt() else 0

                primarias.forEach { pos ->
                    posMinutos[pos] = (posMinutos[pos] ?: 0) + minsPrim
                    posPJ[pos] = (posPJ[pos] ?: 0) + 1
                    if (p.esVictoria) posV[pos] = (posV[pos] ?: 0) + 1
                    if (p.esEmpate) posE[pos] = (posE[pos] ?: 0) + 1
                    if (p.esDerrota) posD[pos] = (posD[pos] ?: 0) + 1
                }
                secundarias.forEach { pos ->
                    posMinutos[pos] = (posMinutos[pos] ?: 0) + minsSec
                    posPJ[pos] = (posPJ[pos] ?: 0) + 1
                    if (p.esVictoria) posV[pos] = (posV[pos] ?: 0) + 1
                    if (p.esEmpate) posE[pos] = (posE[pos] ?: 0) + 1
                    if (p.esDerrota) posD[pos] = (posD[pos] ?: 0) + 1
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
