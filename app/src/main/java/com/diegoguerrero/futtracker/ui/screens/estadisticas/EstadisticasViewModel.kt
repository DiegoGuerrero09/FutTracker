package com.diegoguerrero.futtracker.ui.screens.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.domain.model.TipoFutbol
import com.diegoguerrero.futtracker.domain.repository.PartidoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

enum class TipoFiltroEstadisticas { TOTAL, TEMPORADA, ANIO_NATURAL, FECHA_PERSONALIZADA }

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

@HiltViewModel
class EstadisticasViewModel @Inject constructor(
    private val partidoRepository: PartidoRepository
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
}
