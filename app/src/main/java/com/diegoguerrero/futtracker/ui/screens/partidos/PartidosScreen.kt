package com.diegoguerrero.futtracker.ui.screens.partidos

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.domain.model.TipoFutbol
import com.diegoguerrero.futtracker.domain.model.nombreConTu
import com.diegoguerrero.futtracker.ui.components.GraficoResultados
import com.diegoguerrero.futtracker.ui.components.JugadorAvatar
import com.diegoguerrero.futtracker.ui.theme.*
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import com.diegoguerrero.futtracker.domain.model.Clima
import com.diegoguerrero.futtracker.domain.model.EquipoColor
import com.diegoguerrero.futtracker.domain.model.Estadio
import com.diegoguerrero.futtracker.ui.components.DialogoRecorteFoto
import com.diegoguerrero.futtracker.ui.components.ImagenLocal
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

enum class PeriodoPartidos(val label: String) {
    TOTAL("Total"),
    TEMPORADA("Temporada"),
    ANIO_NATURAL("Año natural"),
    RANGO_FECHAS("Por fecha"),
    ULTIMOS_MESES("Últimos meses"),
    ULTIMAS_SEMANAS("Últimas semanas")
}

enum class ResultadoFiltro(val label: String) {
    VICTORIAS("Victorias"),
    EMPATES("Empates"),
    DERROTAS("Derrotas")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartidosScreen(
    partidos: List<Partido>,
    jugadores: List<Jugador>,
    estadios: List<Estadio> = emptyList(),
    onAgregarPartido: (Partido) -> Unit,
    onActualizarPartido: (Partido) -> Unit,
    onEliminarPartido: (Partido) -> Unit,
    mostrarTopBar: Boolean = true
) {
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var partidoAEditar by remember { mutableStateOf<Partido?>(null) }
    var partidoAEliminar by remember { mutableStateOf<Partido?>(null) }

    var soloFavoritosFilter by remember { mutableStateOf(false) }
    var filtroPeriodo by remember { mutableStateOf(PeriodoPartidos.TOTAL) }
    var mostrarFilaFecha by remember { mutableStateOf(false) }
    var temporadaSeleccionadaPartidos by remember {
        mutableStateOf(
            run {
                val cal = Calendar.getInstance()
                val mesActual = cal.get(Calendar.MONTH)
                val anioActual = cal.get(Calendar.YEAR)
                val anioInicio = if (mesActual >= Calendar.SEPTEMBER) anioActual else anioActual - 1
                "$anioInicio/${anioInicio + 1}"
            }
        )
    }
    var anioSeleccionadoPartidos by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var fechaInicioPartidos by remember { mutableStateOf(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) }
    var fechaFinPartidos by remember { mutableStateOf(System.currentTimeMillis()) }
    var mostrarRangoPickerPartidos by remember { mutableStateOf(false) }

    var filtroModalidad by remember { mutableStateOf<TipoFutbol?>(null) }
    var filtroDuracion by remember { mutableStateOf<Int?>(null) }
    var soloJugadosPorMi by remember { mutableStateOf(false) }
    var filtroParticipantes by remember { mutableStateOf<Set<String>>(emptySet()) }
    var mostrarDialogoParticipante by remember { mutableStateOf(false) }
    var filtroResultado by remember { mutableStateOf<ResultadoFiltro?>(null) }
    var filtroPosicion by remember { mutableStateOf<Posicion?>(null) }
    var filtroSoloPosicionPrincipal by remember { mutableStateOf(false) }
    var filtroConMisGoles by remember { mutableStateOf(false) }
    var filtroConMisAsistencias by remember { mutableStateOf(false) }
    var filtroConMisPalos by remember { mutableStateOf(false) }
    var filtroFueraArea by remember { mutableStateOf(false) }
    var filtroTacon by remember { mutableStateOf(false) }
    var filtroChilena by remember { mutableStateOf(false) }

    val temporadasConDatosPartidos = remember(partidos) {
        val list = partidos.map { p ->
            val cal = Calendar.getInstance().apply { timeInMillis = p.fecha }
            val mes = cal.get(Calendar.MONTH)
            val anio = cal.get(Calendar.YEAR)
            val anioInicio = if (mes >= Calendar.SEPTEMBER) anio else anio - 1
            "$anioInicio/${anioInicio + 1}"
        }.distinct().sortedDescending()
        val cal = Calendar.getInstance()
        val mes = cal.get(Calendar.MONTH)
        val anio = cal.get(Calendar.YEAR)
        val actual = "${if (mes >= Calendar.SEPTEMBER) anio else anio - 1}/${if (mes >= Calendar.SEPTEMBER) anio + 1 else anio}"
        (list + actual).distinct().sortedDescending()
    }

    val aniosConDatosPartidos = remember(partidos) {
        val cal = Calendar.getInstance()
        val set = partidos.map {
            cal.timeInMillis = it.fecha
            cal.get(Calendar.YEAR)
        }.toSet() + Calendar.getInstance().get(Calendar.YEAR)
        set.sortedDescending()
    }

    val numFiltrosActivos = (if (soloFavoritosFilter) 1 else 0) +
            (if (filtroPeriodo != PeriodoPartidos.TOTAL) 1 else 0) +
            (if (filtroModalidad != null) 1 else 0) +
            (if (filtroDuracion != null) 1 else 0) +
            (if (soloJugadosPorMi) 1 else 0) +
            (if (filtroParticipantes.isNotEmpty()) 1 else 0) +
            (if (soloJugadosPorMi && filtroResultado != null) 1 else 0) +
            (if (soloJugadosPorMi && filtroPosicion != null) 1 else 0) +
            (if (soloJugadosPorMi && filtroConMisGoles) 1 else 0) +
            (if (soloJugadosPorMi && filtroConMisAsistencias) 1 else 0) +
            (if (soloJugadosPorMi && filtroConMisPalos) 1 else 0) +
            (if (soloJugadosPorMi && filtroFueraArea) 1 else 0) +
            (if (soloJugadosPorMi && filtroTacon) 1 else 0) +
            (if (soloJugadosPorMi && filtroChilena) 1 else 0)

    fun limpiarFiltros() {
        soloFavoritosFilter = false
        filtroPeriodo = PeriodoPartidos.TOTAL
        mostrarFilaFecha = false
        anioSeleccionadoPartidos = Calendar.getInstance().get(Calendar.YEAR)
        filtroModalidad = null
        filtroDuracion = null
        soloJugadosPorMi = false
        filtroParticipantes = emptySet()
        filtroResultado = null
        filtroPosicion = null
        filtroSoloPosicionPrincipal = false
        filtroConMisGoles = false
        filtroConMisAsistencias = false
        filtroConMisPalos = false
        filtroFueraArea = false
        filtroTacon = false
        filtroChilena = false
    }

    val partidosFiltrados = remember(
        partidos,
        soloFavoritosFilter,
        filtroPeriodo,
        temporadaSeleccionadaPartidos,
        anioSeleccionadoPartidos,
        fechaInicioPartidos,
        fechaFinPartidos,
        filtroModalidad,
        filtroDuracion,
        soloJugadosPorMi,
        filtroParticipantes,
        filtroResultado,
        filtroPosicion,
        filtroSoloPosicionPrincipal,
        filtroConMisGoles,
        filtroConMisAsistencias,
        filtroConMisPalos,
        filtroFueraArea,
        filtroTacon,
        filtroChilena
    ) {
        val ahora = System.currentTimeMillis()
        val ultimasSemanas = ahora - 28L * 24 * 60 * 60 * 1000
        val ultimosMeses = ahora - 90L * 24 * 60 * 60 * 1000

        val (tempStart, tempEnd) = run {
            val partes = temporadaSeleccionadaPartidos.split("/")
            val anioInicio = partes.getOrNull(0)?.toIntOrNull() ?: run {
                val cal = Calendar.getInstance()
                val mesActual = cal.get(Calendar.MONTH)
                val anioActual = cal.get(Calendar.YEAR)
                if (mesActual >= Calendar.SEPTEMBER) anioActual else anioActual - 1
            }
            val start = Calendar.getInstance().apply {
                set(anioInicio, Calendar.SEPTEMBER, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val end = Calendar.getInstance().apply {
                set(anioInicio + 1, Calendar.AUGUST, 31, 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            start to end
        }

        val (anioStart, anioEnd) = run {
            val cal = Calendar.getInstance()
            cal.set(anioSeleccionadoPartidos, Calendar.JANUARY, 1, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.set(anioSeleccionadoPartidos, Calendar.DECEMBER, 31, 23, 59, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val end = cal.timeInMillis
            start to end
        }

        partidos.filter { p ->
            if (soloFavoritosFilter && !p.esFavorito) return@filter false
            if (filtroModalidad != null && p.modoJuego != filtroModalidad) return@filter false
            if (filtroDuracion != null && p.duracionMinutos != filtroDuracion) return@filter false
            if (soloJugadosPorMi && !p.jugadoPorMi) return@filter false

            val cumplePeriodo = when (filtroPeriodo) {
                PeriodoPartidos.TOTAL -> true
                PeriodoPartidos.TEMPORADA -> p.fecha in tempStart..tempEnd
                PeriodoPartidos.ANIO_NATURAL -> p.fecha in anioStart..anioEnd
                PeriodoPartidos.RANGO_FECHAS -> p.fecha in fechaInicioPartidos..fechaFinPartidos
                PeriodoPartidos.ULTIMOS_MESES -> p.fecha >= ultimosMeses
                PeriodoPartidos.ULTIMAS_SEMANAS -> p.fecha >= ultimasSemanas
            }
            if (!cumplePeriodo) return@filter false

            if (filtroParticipantes.isNotEmpty()) {
                val participa = p.jugadoresIds.any { it in filtroParticipantes } ||
                        p.jugadoresMiEquipo.any { it in filtroParticipantes } ||
                        p.jugadoresEquipoRival.any { it in filtroParticipantes }
                if (!participa) return@filter false
            }

            if (soloJugadosPorMi) {
                if (filtroResultado != null) {
                    val coincide = when (filtroResultado) {
                        ResultadoFiltro.VICTORIAS -> p.esVictoria
                        ResultadoFiltro.EMPATES -> p.esEmpate
                        ResultadoFiltro.DERROTAS -> p.esDerrota
                        null -> true
                    }
                    if (!coincide) return@filter false
                }

                if (filtroPosicion != null) {
                    val coincide = if (filtroSoloPosicionPrincipal) {
                        p.posicionJugada == filtroPosicion
                    } else {
                        p.posicionJugada == filtroPosicion || p.posicionesJugadas.contains(filtroPosicion) || p.posicionesSecundarias.contains(filtroPosicion)
                    }
                    if (!coincide) return@filter false
                }

                if (filtroConMisGoles && p.goles <= 0) return@filter false
                if (filtroConMisAsistencias && p.asistencias <= 0) return@filter false
                if (filtroConMisPalos && p.tirosAlPalo <= 0) return@filter false
                if (filtroFueraArea && p.golesFueraArea <= 0) return@filter false
                if (filtroTacon && p.golesTacon <= 0) return@filter false
                if (filtroChilena && p.golesChilena <= 0) return@filter false
            }

            true
        }
    }

    Scaffold(
        topBar = {
            if (mostrarTopBar) {
                TopAppBar(
                    title = { Text("Partidos", fontWeight = FontWeight.Bold, color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCard)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoCrear = true },
                containerColor = LimeVolt,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar partido", tint = Color.Black)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de filtros horizontales con estilo similar a jugadores
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Limpiar filtros
                if (numFiltrosActivos > 0) {
                    item {
                        FilterChip(
                            selected = true,
                            onClick = { limpiarFiltros() },
                            label = { Text("Limpiar ($numFiltrosActivos)", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LimeVolt.copy(alpha = 0.2f),
                                selectedLabelColor = LimeVolt
                            )
                        )
                    }
                }

                // Favoritos
                item {
                    FilterChip(
                        selected = soloFavoritosFilter,
                        onClick = { soloFavoritosFilter = !soloFavoritosFilter },
                        label = { Text("Favoritos", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (soloFavoritosFilter) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = null,
                                tint = if (soloFavoritosFilter) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                // Periodo / Fecha
                item {
                    val textoFecha = when (filtroPeriodo) {
                        PeriodoPartidos.TOTAL -> "Fecha"
                        PeriodoPartidos.TEMPORADA -> "Temp. $temporadaSeleccionadaPartidos"
                        PeriodoPartidos.ANIO_NATURAL -> "Año $anioSeleccionadoPartidos"
                        PeriodoPartidos.RANGO_FECHAS -> {
                            val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                            "${sdf.format(Date(fechaInicioPartidos))}-${sdf.format(Date(fechaFinPartidos))}"
                        }
                        PeriodoPartidos.ULTIMOS_MESES -> "Últimos meses"
                        PeriodoPartidos.ULTIMAS_SEMANAS -> "Últimas semanas"
                    }
                    FilterChip(
                        selected = filtroPeriodo != PeriodoPartidos.TOTAL || mostrarFilaFecha,
                        onClick = { mostrarFilaFecha = !mostrarFilaFecha },
                        label = { Text(textoFecha, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        trailingIcon = {
                            Icon(
                                if (mostrarFilaFecha) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                // Modalidad
                item {
                    var menuModalidadAbierto by remember { mutableStateOf(false) }
                    Box {
                        FilterChip(
                            selected = filtroModalidad != null,
                            onClick = { menuModalidadAbierto = true },
                            label = {
                                Text(
                                    when (filtroModalidad) {
                                        null -> "Modalidad"
                                        TipoFutbol.FUTSAL -> "Futsal"
                                        TipoFutbol.FUT_6 -> "Fútbol 6"
                                        TipoFutbol.FUT_7 -> "Fútbol 7"
                                    },
                                    fontSize = 12.sp
                                )
                            },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        DropdownMenu(
                            expanded = menuModalidadAbierto,
                            onDismissRequest = { menuModalidadAbierto = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas") },
                                onClick = {
                                    filtroModalidad = null
                                    menuModalidadAbierto = false
                                }
                            )
                            listOf(
                                TipoFutbol.FUTSAL to "Futsal",
                                TipoFutbol.FUT_6 to "Fútbol 6",
                                TipoFutbol.FUT_7 to "Fútbol 7"
                            ).forEach { (modo, nombre) ->
                                DropdownMenuItem(
                                    text = { Text(nombre) },
                                    onClick = {
                                        filtroModalidad = modo
                                        menuModalidadAbierto = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Duración
                item {
                    var menuDuracionAbierto by remember { mutableStateOf(false) }
                    Box {
                        FilterChip(
                            selected = filtroDuracion != null,
                            onClick = { menuDuracionAbierto = true },
                            label = { Text(if (filtroDuracion == null) "Duración" else "$filtroDuracion min", fontSize = 12.sp) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        DropdownMenu(
                            expanded = menuDuracionAbierto,
                            onDismissRequest = { menuDuracionAbierto = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas") },
                                onClick = {
                                    filtroDuracion = null
                                    menuDuracionAbierto = false
                                }
                            )
                            listOf(60, 90, 120).forEach { dur ->
                                DropdownMenuItem(
                                    text = { Text("$dur min") },
                                    onClick = {
                                        filtroDuracion = dur
                                        menuDuracionAbierto = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Participantes (Multi-selección)
                item {
                    val labelParticipantes = when {
                        filtroParticipantes.isEmpty() -> "Participantes"
                        filtroParticipantes.size == 1 -> {
                            val jugador = jugadores.find { it.id == filtroParticipantes.first() }
                            jugador?.let { "Con: ${it.nombre}" } ?: "1 participante"
                        }
                        else -> "Con: ${filtroParticipantes.size} jug."
                    }
                    FilterChip(
                        selected = filtroParticipantes.isNotEmpty(),
                        onClick = { mostrarDialogoParticipante = true },
                        label = { Text(labelParticipantes, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        trailingIcon = {
                            if (filtroParticipantes.isNotEmpty()) {
                                IconButton(onClick = { filtroParticipantes = emptySet() }, modifier = Modifier.size(16.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = "Quitar", modifier = Modifier.size(12.dp))
                                }
                            } else {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                }

                // Jugados por mí (al final de la primera fila)
                item {
                    FilterChip(
                        selected = soloJugadosPorMi,
                        onClick = { soloJugadosPorMi = !soloJugadosPorMi },
                        label = { Text("Jugados por mí", fontSize = 12.sp) }
                    )
                }
            }

            // Subfila de selección de fecha (cuando se pulsa el botón Fecha)
            if (mostrarFilaFecha) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    color = DarkCard.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = filtroPeriodo == PeriodoPartidos.TOTAL,
                                    onClick = { filtroPeriodo = PeriodoPartidos.TOTAL },
                                    label = { Text("Total", fontSize = 11.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filtroPeriodo == PeriodoPartidos.TEMPORADA,
                                    onClick = { filtroPeriodo = PeriodoPartidos.TEMPORADA },
                                    label = { Text("Temporada", fontSize = 11.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filtroPeriodo == PeriodoPartidos.ANIO_NATURAL,
                                    onClick = { filtroPeriodo = PeriodoPartidos.ANIO_NATURAL },
                                    label = { Text("Año", fontSize = 11.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filtroPeriodo == PeriodoPartidos.RANGO_FECHAS,
                                    onClick = {
                                        filtroPeriodo = PeriodoPartidos.RANGO_FECHAS
                                        mostrarRangoPickerPartidos = true
                                    },
                                    label = { Text("Por fecha", fontSize = 11.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filtroPeriodo == PeriodoPartidos.ULTIMOS_MESES,
                                    onClick = { filtroPeriodo = PeriodoPartidos.ULTIMOS_MESES },
                                    label = { Text("Últimos meses", fontSize = 11.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filtroPeriodo == PeriodoPartidos.ULTIMAS_SEMANAS,
                                    onClick = { filtroPeriodo = PeriodoPartidos.ULTIMAS_SEMANAS },
                                    label = { Text("Últimas semanas", fontSize = 11.sp) }
                                )
                            }
                        }

                        if (filtroPeriodo == PeriodoPartidos.TEMPORADA) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(temporadasConDatosPartidos) { temp ->
                                    FilterChip(
                                        selected = temporadaSeleccionadaPartidos == temp,
                                        onClick = { temporadaSeleccionadaPartidos = temp },
                                        label = { Text(temp, fontSize = 11.sp) }
                                    )
                                }
                            }
                        } else if (filtroPeriodo == PeriodoPartidos.ANIO_NATURAL) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(aniosConDatosPartidos) { anio ->
                                    FilterChip(
                                        selected = anioSeleccionadoPartidos == anio,
                                        onClick = { anioSeleccionadoPartidos = anio },
                                        label = { Text("$anio", fontSize = 11.sp) }
                                    )
                                }
                            }
                        } else if (filtroPeriodo == PeriodoPartidos.RANGO_FECHAS) {
                            val sdfRango = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${sdfRango.format(Date(fechaInicioPartidos))} - ${sdfRango.format(Date(fechaFinPartidos))}",
                                    color = LimeVolt,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                TextButton(onClick = { mostrarRangoPickerPartidos = true }) {
                                    Text("Cambiar rango", fontSize = 11.sp, color = LimeVolt)
                                }
                            }
                        }
                    }
                }
            }

            // Segunda fila de filtros personales (SOLO si se marca "Jugados por mí")
            if (soloJugadosPorMi) {
                Spacer(modifier = Modifier.height(2.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Resultado
                    item {
                        var menuResultadoAbierto by remember { mutableStateOf(false) }
                        Box {
                            FilterChip(
                                selected = filtroResultado != null,
                                onClick = { menuResultadoAbierto = true },
                                label = { Text(filtroResultado?.label ?: "Resultado", fontSize = 12.sp) },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenu(
                                expanded = menuResultadoAbierto,
                                onDismissRequest = { menuResultadoAbierto = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todos") },
                                    onClick = {
                                        filtroResultado = null
                                        menuResultadoAbierto = false
                                    }
                                )
                                ResultadoFiltro.entries.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r.label) },
                                        onClick = {
                                            filtroResultado = r
                                            menuResultadoAbierto = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 2. Mi Posición
                    item {
                        var menuPosicionAbierto by remember { mutableStateOf(false) }
                        Box {
                            FilterChip(
                                selected = filtroPosicion != null,
                                onClick = { menuPosicionAbierto = true },
                                label = { Text(filtroPosicion?.name ?: "Mi posición", fontSize = 12.sp) },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenu(
                                expanded = menuPosicionAbierto,
                                onDismissRequest = { menuPosicionAbierto = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todas las posiciones") },
                                    onClick = {
                                        filtroPosicion = null
                                        menuPosicionAbierto = false
                                    }
                                )
                                Posicion.entries.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p.name) },
                                        onClick = {
                                            filtroPosicion = p
                                            menuPosicionAbierto = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Toggle solo principal vs ambas
                    if (filtroPosicion != null) {
                        item {
                            FilterChip(
                                selected = filtroSoloPosicionPrincipal,
                                onClick = { filtroSoloPosicionPrincipal = !filtroSoloPosicionPrincipal },
                                label = { Text(if (filtroSoloPosicionPrincipal) "Solo principal" else "Ambas posiciones", fontSize = 12.sp) }
                            )
                        }
                    }

                    // 3. Con mis goles, etc.
                    item {
                        FilterChip(
                            selected = filtroConMisGoles,
                            onClick = { filtroConMisGoles = !filtroConMisGoles },
                            label = { Text("Con mis goles", fontSize = 12.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filtroConMisAsistencias,
                            onClick = { filtroConMisAsistencias = !filtroConMisAsistencias },
                            label = { Text("Con mis asistencias", fontSize = 12.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filtroConMisPalos,
                            onClick = { filtroConMisPalos = !filtroConMisPalos },
                            label = { Text("Con tiros al palo", fontSize = 12.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filtroFueraArea,
                            onClick = { filtroFueraArea = !filtroFueraArea },
                            label = { Text("Fuera del área", fontSize = 12.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filtroTacon,
                            onClick = { filtroTacon = !filtroTacon },
                            label = { Text("De tacón", fontSize = 12.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filtroChilena,
                            onClick = { filtroChilena = !filtroChilena },
                            label = { Text("De chilena", fontSize = 12.sp) }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Encabezado de la lista
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historial (${partidosFiltrados.size})",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (partidos.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Aún no has registrado partidos",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Toca el botón + para registrar tu primer encuentro",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else if (partidosFiltrados.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Sin resultados con estos filtros",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { limpiarFiltros() },
                                    colors = ButtonDefaults.buttonColors(containerColor = LimeVolt, contentColor = Color.Black)
                                ) {
                                    Text("Limpiar filtros", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(
                        items = partidosFiltrados,
                        key = { it.id }
                    ) { partido ->
                        PartidoItem(
                            partido = partido,
                            jugadores = jugadores,
                            estadios = estadios,
                            onToggleFavorito = {
                                onActualizarPartido(partido.copy(esFavorito = !partido.esFavorito))
                            },
                            onEditar = { partidoAEditar = partido },
                            onEliminar = { partidoAEliminar = partido }
                        )
                    }
                }
            }
        }
    }

    if (mostrarRangoPickerPartidos) {
        val datePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = fechaInicioPartidos,
            initialSelectedEndDateMillis = fechaFinPartidos
        )
        DatePickerDialog(
            onDismissRequest = { mostrarRangoPickerPartidos = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = datePickerState.selectedStartDateMillis
                        val end = datePickerState.selectedEndDateMillis ?: start
                        if (start != null && end != null) {
                            fechaInicioPartidos = start
                            fechaFinPartidos = end
                        }
                        mostrarRangoPickerPartidos = false
                    }
                ) {
                    Text("Aplicar", color = LimeVolt)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarRangoPickerPartidos = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DateRangePicker(
                state = datePickerState,
                title = { Text("Selecciona rango de fechas", modifier = Modifier.padding(16.dp)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (mostrarDialogoParticipante) {
        var busquedaParticipante by remember { mutableStateOf("") }
        val jugadoresFiltradosDialog = remember(jugadores, busquedaParticipante) {
            if (busquedaParticipante.isBlank()) jugadores
            else jugadores.filter { it.nombre.contains(busquedaParticipante, ignoreCase = true) }
        }
        AlertDialog(
            onDismissRequest = { mostrarDialogoParticipante = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Participantes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (filtroParticipantes.isNotEmpty()) {
                        TextButton(onClick = { filtroParticipantes = emptySet() }) {
                            Text("Limpiar (${filtroParticipantes.size})", color = LimeVolt, fontSize = 12.sp)
                        }
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                    OutlinedTextField(
                        value = busquedaParticipante,
                        onValueChange = { busquedaParticipante = it },
                        placeholder = { Text("Buscar jugador...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (busquedaParticipante.isNotEmpty()) {
                                IconButton(onClick = { busquedaParticipante = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(jugadoresFiltradosDialog) { j ->
                            val sel = j.id in filtroParticipantes
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        filtroParticipantes = if (sel) {
                                            filtroParticipantes - j.id
                                        } else {
                                            filtroParticipantes + j.id
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (sel) LimeVolt.copy(alpha = 0.18f) else DarkCard
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = sel,
                                        onCheckedChange = { check ->
                                            filtroParticipantes = if (check) filtroParticipantes + j.id else filtroParticipantes - j.id
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = LimeVolt, checkmarkColor = Color.Black)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    JugadorAvatar(fotoUri = j.fotoUri, nombre = j.nombre, tamano = 32.dp, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(j.nombreConTu(), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
                                        Text(
                                            j.posicionesPrimarias.joinToString(", ") { it.name },
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { mostrarDialogoParticipante = false },
                    colors = ButtonDefaults.buttonColors(containerColor = LimeVolt, contentColor = Color.Black)
                ) {
                    Text("Listo", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (mostrarDialogoCrear) {
        DialogoPartido(
            partidoExistente = null,
            jugadoresDisponibles = jugadores,
            estadios = estadios,
            onDismiss = { mostrarDialogoCrear = false },
            onGuardar = { nuevo ->
                onAgregarPartido(nuevo)
                mostrarDialogoCrear = false
            }
        )
    }

    partidoAEditar?.let { partido ->
        DialogoPartido(
            partidoExistente = partido,
            jugadoresDisponibles = jugadores,
            estadios = estadios,
            onDismiss = { partidoAEditar = null },
            onGuardar = { actualizado ->
                onActualizarPartido(actualizado)
                partidoAEditar = null
            }
        )
    }

    partidoAEliminar?.let { partido ->
        AlertDialog(
            onDismissRequest = { partidoAEliminar = null },
            title = { Text("Eliminar partido") },
            text = { Text("¿Deseas eliminar este registro del partido?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEliminarPartido(partido)
                        partidoAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { partidoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PartidoItem(
    partido: Partido,
    jugadores: List<Jugador>,
    estadios: List<Estadio> = emptyList(),
    onToggleFavorito: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val fechaFormateada = remember(partido.fecha) { sdf.format(Date(partido.fecha)) }

    val estadoColor = when {
        !partido.jugadoPorMi -> Color(0xFF64748B)
        partido.esVictoria -> Color(0xFF4CAF50)
        partido.esEmpate -> Color(0xFFFFB300)
        else -> Color(0xFFE53935)
    }

    val estadoTexto = when {
        !partido.jugadoPorMi -> "Externo"
        partido.esVictoria -> "Victoria"
        partido.esEmpate -> "Empate"
        else -> "Derrota"
    }

    val modalidadTexto = when (partido.modoJuego) {
        TipoFutbol.FUTSAL -> "Futsal"
        TipoFutbol.FUT_6 -> "Fútbol 6"
        TipoFutbol.FUT_7 -> "Fútbol 7"
    }

    val estadioPartido = remember(partido.estadioId, estadios) {
        estadios.find { it.id == partido.estadioId }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditar() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, if (partido.esFavorito) Color(0xFFFFD700).copy(alpha = 0.6f) else DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Fila superior: Fecha, Modalidad, Duración, Clima, Estadio y Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(text = fechaFormateada, color = TextSecondary, fontSize = 12.sp)

                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = modalidadTexto,
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = LimeVolt.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${partido.duracionMinutos} min",
                            color = LimeVolt,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${partido.clima.emoji} ${partido.clima.label}",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (estadioPartido != null) {
                        Surface(
                            color = LimeVolt.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "🏟️ ${estadioPartido.nombre}",
                                color = LimeVolt,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (!partido.jugadoPorMi) {
                        Surface(
                            color = Color(0xFF64748B).copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Externo",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorito, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (partido.esFavorito) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Favorito",
                            tint = if (partido.esFavorito) Color(0xFFFFD700) else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onEditar, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEliminar, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fila central: Foto del partido / Icono + Marcador y badge de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        if (partido.fotoUri != null) {
                            ImagenLocal(
                                fotoUri = partido.fotoUri,
                                contentDescription = "Foto del partido",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "${partido.golesAFavor} - ${partido.golesEnContra}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (partido.jugadoPorMi) {
                            val miColor = partido.equipoJugado ?: EquipoColor.CLARO
                            Text(
                                text = "Jugaste con ${miColor.emoji}${miColor.label}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        } else {
                            Text(
                                text = "⚪ Claro vs ⚫ Oscuro",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Surface(
                    color = estadoColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = estadoTexto,
                        color = estadoColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (partido.jugadoPorMi) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Estadísticas personales: Posición principal y secundarias, Goles y Asistencias
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Posición: ",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        // Posición principal
                        Surface(
                            color = LimeVolt,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = partido.posicionJugada.name,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        // Posiciones secundarias si las hay
                        partido.posicionesSecundarias.forEach { posSec ->
                            Surface(
                                color = LimeVolt.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.8.dp, LimeVolt.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = posSec.name,
                                    color = LimeVolt,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚽ ${partido.goles} goles", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👟 ${partido.asistencias} asist.", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Desglose de goles si los hay
                val detallesGoles = buildList {
                    if (partido.golesDiestra > 0) add("Diestra: ${partido.golesDiestra}")
                    if (partido.golesZurda > 0) add("Zurda: ${partido.golesZurda}")
                    if (partido.golesCabeza > 0) add("Cabeza: ${partido.golesCabeza}")
                    if (partido.golesTacon > 0) add("Tacón: ${partido.golesTacon}")
                    if (partido.golesChilena > 0) add("Chilena: ${partido.golesChilena}")
                    if (partido.golesOtro > 0) add("Otro: ${partido.golesOtro}")
                }

                if (detallesGoles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Goles:", color = TextSecondary, fontSize = 11.sp)
                        detallesGoles.forEach { detalle ->
                            Surface(
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = detalle,
                                    color = LimeVolt,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Notas si las hay
            if (partido.notas.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = partido.notas,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            // Jugadores del partido por equipo
            val tieneEquipos = partido.jugadoresMiEquipo.isNotEmpty() || partido.jugadoresEquipoRival.isNotEmpty()
            if (tieneEquipos) {
                Spacer(modifier = Modifier.height(8.dp))
                val miColor = partido.equipoJugado ?: EquipoColor.CLARO
                val rivalColor = if (miColor == EquipoColor.CLARO) EquipoColor.OSCURO else EquipoColor.CLARO

                val etiquetaEquipo1 = if (partido.jugadoPorMi) "Mi equipo (${miColor.emoji}${miColor.label}):" else "⚪ Equipo claro:"
                val etiquetaEquipo2 = if (partido.jugadoPorMi) "Equipo rival (${rivalColor.emoji}${rivalColor.label}):" else "⚫ Equipo oscuro:"

                if (partido.jugadoresMiEquipo.isNotEmpty()) {
                    Text(
                        text = etiquetaEquipo1,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val companeros = jugadores.filter { it.id in partido.jugadoresMiEquipo }
                            .sortedWith(
                                compareByDescending<Jugador> { it.esUsuarioPropio || it.id == "usuario_propio_id" }
                                    .thenBy { it.nombre.lowercase() }
                            )
                        items(companeros) { comp ->
                            val esYo = comp.esUsuarioPropio || comp.id == "usuario_propio_id"
                            Surface(
                                color = if (esYo) LimeVolt.copy(alpha = 0.35f) else LimeVolt.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp),
                                border = if (esYo) BorderStroke(1.dp, LimeVolt) else null
                            ) {
                                Text(
                                    text = if (esYo) "${comp.nombre} (Tú)" else comp.nombre,
                                    color = LimeVolt,
                                    fontSize = 10.sp,
                                    fontWeight = if (esYo) FontWeight.Bold else FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                if (partido.jugadoresEquipoRival.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = etiquetaEquipo2,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val rivales = jugadores.filter { it.id in partido.jugadoresEquipoRival }
                        items(rivales) { riv ->
                            Surface(
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = riv.nombre,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            } else if (partido.jugadoresIds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Jugadores del partido:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val companeros = jugadores.filter { it.id in partido.jugadoresIds }
                    items(companeros) { comp ->
                        Surface(
                            color = Color.White.copy(alpha = 0.07f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = comp.nombre,
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoPartido(
    partidoExistente: Partido? = null,
    jugadoresDisponibles: List<Jugador>,
    estadios: List<Estadio> = emptyList(),
    onDismiss: () -> Unit,
    onGuardar: (Partido) -> Unit
) {
    val context = LocalContext.current

    var fechaMillis by remember { mutableStateOf(partidoExistente?.fecha ?: System.currentTimeMillis()) }
    var modoJuego by remember { mutableStateOf(partidoExistente?.modoJuego ?: TipoFutbol.FUTSAL) }
    var esFavorito by remember { mutableStateOf(partidoExistente?.esFavorito ?: false) }
    var clima by remember { mutableStateOf(partidoExistente?.clima ?: Clima.SOLEADO) }
    var fotoUri by remember { mutableStateOf(partidoExistente?.fotoUri) }
    var uriSeleccionadaParaRecorte by remember { mutableStateOf<Uri?>(null) }
    var equipoJugado by remember { mutableStateOf(partidoExistente?.equipoJugado ?: EquipoColor.CLARO) }
    var estadioId by remember { mutableStateOf<Long?>(partidoExistente?.estadioId) }

    var posicionPrincipal by remember { mutableStateOf(partidoExistente?.posicionJugada ?: Posicion.DC) }
    val posicionesSecundarias = remember {
        mutableStateListOf<Posicion>().apply {
            if (partidoExistente != null) {
                addAll(partidoExistente.posicionesSecundarias)
            }
        }
    }

    var golesAFavor by remember { mutableStateOf(partidoExistente?.golesAFavor ?: 0) }
    var golesEnContra by remember { mutableStateOf(partidoExistente?.golesEnContra ?: 0) }

    var misGoles by remember { mutableStateOf(partidoExistente?.goles ?: 0) }
    var misAsistencias by remember { mutableStateOf(partidoExistente?.asistencias ?: 0) }
    var tirosAlPalo by remember { mutableStateOf(partidoExistente?.tirosAlPalo ?: 0) }

    var golesZurda by remember { mutableStateOf(partidoExistente?.golesZurda ?: 0) }
    var golesDiestra by remember { mutableStateOf(partidoExistente?.golesDiestra ?: 0) }
    var golesCabeza by remember { mutableStateOf(partidoExistente?.golesCabeza ?: 0) }
    var golesOtro by remember { mutableStateOf(partidoExistente?.golesOtro ?: 0) }
    var golesChilena by remember { mutableStateOf(partidoExistente?.golesChilena ?: 0) }
    var golesTacon by remember { mutableStateOf(partidoExistente?.golesTacon ?: 0) }
    var golesFueraArea by remember { mutableStateOf(partidoExistente?.golesFueraArea ?: 0) }
    var jugadoPorMi by remember { mutableStateOf(partidoExistente?.jugadoPorMi ?: true) }
    var duracionMinutos by remember { mutableStateOf(partidoExistente?.duracionMinutos ?: 60) }

    var notas by remember { mutableStateOf(partidoExistente?.notas ?: "") }

    val usuario = jugadoresDisponibles.firstOrNull { it.esUsuarioPropio || it.id == "usuario_propio_id" }
    val usuarioIds = setOfNotNull(usuario?.id, "usuario_propio_id")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { uriSeleccionadaParaRecorte = it }
    }

    if (uriSeleccionadaParaRecorte != null) {
        DialogoRecorteFoto(
            uriOriginal = uriSeleccionadaParaRecorte!!,
            onFotoRecortada = { uriRecortada ->
                fotoUri = uriRecortada
                uriSeleccionadaParaRecorte = null
            },
            onDismiss = { uriSeleccionadaParaRecorte = null }
        )
    }

    val jugadoresMiEquipo = remember {
        mutableStateListOf<String>().apply {
            val userActualId = usuario?.id ?: "usuario_propio_id"
            if (partidoExistente != null) {
                val existentes = partidoExistente.jugadoresMiEquipo.ifEmpty { partidoExistente.jugadoresIds }
                val saneados = existentes.map { if (it in usuarioIds) userActualId else it }.distinct()
                addAll(saneados)
                if (jugadoPorMi) {
                    if (userActualId !in this) {
                        add(0, userActualId)
                    } else if (firstOrNull() != userActualId) {
                        remove(userActualId)
                        add(0, userActualId)
                    }
                } else {
                    removeAll { it in usuarioIds }
                }
            } else {
                if (jugadoPorMi) {
                    add(userActualId)
                }
            }
        }
    }

    val jugadoresEquipoRival = remember {
        mutableStateListOf<String>().apply {
            if (partidoExistente != null) {
                addAll(partidoExistente.jugadoresEquipoRival)
                if (!jugadoPorMi) {
                    removeAll { it in usuarioIds }
                }
            }
        }
    }

    LaunchedEffect(jugadoresDisponibles, jugadoPorMi) {
        val u = jugadoresDisponibles.firstOrNull { it.esUsuarioPropio || it.id == "usuario_propio_id" }
        if (u != null) {
            val uIds = setOf(u.id, "usuario_propio_id")
            if (jugadoPorMi) {
                val count = jugadoresMiEquipo.count { it in uIds }
                if (count > 1) {
                    jugadoresMiEquipo.removeAll { it in uIds }
                    jugadoresMiEquipo.add(0, u.id)
                } else if (count == 1) {
                    val idx = jugadoresMiEquipo.indexOfFirst { it in uIds }
                    if (idx != -1 && jugadoresMiEquipo[idx] != u.id) {
                        jugadoresMiEquipo[idx] = u.id
                    }
                } else if (count == 0) {
                    jugadoresMiEquipo.add(0, u.id)
                }
            } else {
                jugadoresMiEquipo.removeAll { it in uIds }
                jugadoresEquipoRival.removeAll { it in uIds }
            }
        }
    }

    var tabEquipoJugadores by remember { mutableStateOf(0) }
    var busquedaJugador by remember { mutableStateOf("") }
    var filtroSoloFavoritos by remember { mutableStateOf(false) }
    var filtroPosicion by remember { mutableStateOf<Posicion?>(null) }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val cal = Calendar.getInstance()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (partidoExistente == null) "Nuevo partido" else "Editar partido",
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { esFavorito = !esFavorito },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (esFavorito) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Favorito",
                        tint = if (esFavorito) Color(0xFFFFD700) else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Selector de Fecha
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            cal.timeInMillis = fechaMillis
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth)
                                    }
                                    fechaMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .background(DarkCard, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fecha del partido", color = TextSecondary, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = sdf.format(Date(fechaMillis)),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Event, contentDescription = null, tint = LimeVolt, modifier = Modifier.size(18.dp))
                    }
                }

                // Selector de si he jugado yo o no
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, if (jugadoPorMi) LimeVolt.copy(alpha = 0.5f) else DarkCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¿He jugado yo este partido?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (jugadoPorMi) "Tus estadísticas personales contarán" else "Partido de otros jugadores (no computa para ti)",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = jugadoPorMi,
                            onCheckedChange = { jugadoPorMi = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = LimeVolt,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = DarkBackground
                            )
                        )
                    }
                }

                // Foto del partido
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkCard, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBackground)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (fotoUri != null) {
                                ImagenLocal(
                                    fotoUri = fotoUri,
                                    contentDescription = "Foto partido",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.SportsSoccer,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Foto del partido", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text(
                                if (fotoUri != null) "Toca para recortar otra" else "Añadir foto del partido",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    if (fotoUri != null) {
                        IconButton(onClick = { fotoUri = null }) {
                            Icon(Icons.Default.Delete, contentDescription = "Quitar foto", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        }
                    } else {
                        IconButton(onClick = {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Elegir foto", tint = LimeVolt, modifier = Modifier.size(22.dp))
                        }
                    }
                }

                // Clima del partido
                Column {
                    Text("Clima", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Clima.entries.forEach { c ->
                            val sel = clima == c
                            FilterChip(
                                selected = sel,
                                onClick = { clima = c },
                                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                label = {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("${c.emoji} ${c.label}", fontSize = 12.sp, textAlign = TextAlign.Center)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Estadio / Ubicación
                if (estadios.isNotEmpty()) {
                    Column {
                        Text("Estadio / Ubicación", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = estadioId == null,
                                    onClick = { estadioId = null },
                                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = estadioId == null, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                    label = { Text("Sin especificar", fontSize = 11.sp) }
                                )
                            }
                            items(estadios) { est ->
                                val sel = estadioId == est.id
                                FilterChip(
                                    selected = sel,
                                    onClick = { estadioId = est.id },
                                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                    label = { Text("🏟️ ${est.nombre}", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                // Selector de Modalidad (centrado horizontalmente)
                Column {
                    Text("Modalidad", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            TipoFutbol.FUTSAL to "Futsal",
                            TipoFutbol.FUT_6 to "Fútbol 6",
                            TipoFutbol.FUT_7 to "Fútbol 7"
                        ).forEach { (tipo, label) ->
                            val sel = modoJuego == tipo
                            FilterChip(
                                selected = sel,
                                onClick = { modoJuego = tipo },
                                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                label = {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, fontSize = 12.sp, textAlign = TextAlign.Center)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Duración del partido (60, 90, 120 min, 60 por defecto)
                Column {
                    Text("Duración", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(60, 90, 120).forEach { dur ->
                            val sel = duracionMinutos == dur
                            FilterChip(
                                selected = sel,
                                onClick = { duracionMinutos = dur },
                                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                label = {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("$dur min", fontSize = 12.sp, textAlign = TextAlign.Center)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Elección de equipo si he jugado yo
                if (jugadoPorMi) {
                    Column {
                        Text("¿En qué equipo jugaste?", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                EquipoColor.CLARO to "⚪ Claro",
                                EquipoColor.OSCURO to "⚫ Oscuro"
                            ).forEach { (color, label) ->
                                val sel = equipoJugado == color
                                FilterChip(
                                    selected = sel,
                                    onClick = { equipoJugado = color },
                                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                    label = {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(label, fontSize = 12.sp, textAlign = TextAlign.Center)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Resultado del partido (alineación simétrica y guión perfectamente centrado con los botones)
                val labelEquipo1 = if (jugadoPorMi) "A favor (${equipoJugado.emoji})" else "⚪ Claro"
                val rivalColor = if (equipoJugado == EquipoColor.CLARO) EquipoColor.OSCURO else EquipoColor.CLARO
                val labelEquipo2 = if (jugadoPorMi) "En contra (${rivalColor.emoji})" else "⚫ Oscuro"

                Column {
                    Text("Resultado (Marcador)", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = labelEquipo1, fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(
                                    onClick = { golesAFavor = (golesAFavor - 1).coerceAtLeast(0) },
                                    modifier = Modifier.size(28.dp),
                                    enabled = golesAFavor > 0
                                ) {
                                    Text("-", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Text(
                                    text = "$golesAFavor",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    color = Color.White
                                )
                                FilledTonalIconButton(
                                    onClick = { golesAFavor++ },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                        Text(
                            text = "-",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 18.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = labelEquipo2, fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(
                                    onClick = { golesEnContra = (golesEnContra - 1).coerceAtLeast(0) },
                                    modifier = Modifier.size(28.dp),
                                    enabled = golesEnContra > 0
                                ) {
                                    Text("-", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Text(
                                    text = "$golesEnContra",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    color = Color.White
                                )
                                FilledTonalIconButton(
                                    onClick = { golesEnContra++ },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }

                if (jugadoPorMi) {
                    // Posición principal (solo una)
                    Column {
                        Text("Posición principal (1)", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(Posicion.entries.toTypedArray()) { pos ->
                                val sel = posicionPrincipal == pos
                                FilterChip(
                                    selected = sel,
                                    onClick = {
                                        posicionPrincipal = pos
                                        posicionesSecundarias.remove(pos)
                                    },
                                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                    label = { Text(pos.name, fontSize = 11.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) }
                                )
                            }
                        }
                    }

                    // Posiciones secundarias (varias opcionales)
                    Column {
                        Text("Posiciones secundarias (opcional)", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(Posicion.entries.toTypedArray()) { pos ->
                                if (pos != posicionPrincipal) {
                                    val sel = pos in posicionesSecundarias
                                    FilterChip(
                                        selected = sel,
                                        onClick = {
                                            if (sel) posicionesSecundarias.remove(pos)
                                            else posicionesSecundarias.add(pos)
                                        },
                                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                        label = { Text(pos.name, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // Mis estadísticas (Goles, asistencias y tiros al palo)
                    Column {
                        Text("Mis estadísticas", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                StepperInput(
                                    label = "⚽ Goles",
                                    value = misGoles,
                                    onValueChange = { nuevoTotal ->
                                        val diff = nuevoTotal - misGoles
                                        if (diff > 0) {
                                            golesDiestra += diff
                                        } else if (diff < 0) {
                                            var porQuitar = -diff
                                            if (golesOtro >= porQuitar) { golesOtro -= porQuitar; porQuitar = 0 } else { porQuitar -= golesOtro; golesOtro = 0 }
                                            if (porQuitar > 0 && golesCabeza >= porQuitar) { golesCabeza -= porQuitar; porQuitar = 0 } else { porQuitar -= golesCabeza; golesCabeza = 0 }
                                            if (porQuitar > 0 && golesZurda >= porQuitar) { golesZurda -= porQuitar; porQuitar = 0 } else { porQuitar -= golesZurda; golesZurda = 0 }
                                            if (porQuitar > 0) { golesDiestra = (golesDiestra - porQuitar).coerceAtLeast(0) }
                                        }
                                        misGoles = (golesDiestra + golesZurda + golesCabeza + golesOtro).coerceAtLeast(0)
                                    }
                                )
                            }
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                StepperInput(
                                    label = "👟 Asistencias",
                                    value = misAsistencias,
                                    onValueChange = { misAsistencias = it.coerceAtLeast(0) }
                                )
                            }
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                StepperInput(
                                    label = "🥅 Palos",
                                    value = tirosAlPalo,
                                    onValueChange = { tirosAlPalo = it.coerceAtLeast(0) }
                                )
                            }
                        }

                        // Detalle de goles: Parte del cuerpo (conteo base en 2x2)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Parte del cuerpo:",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        fun recalcularGolesYAtributos() {
                            misGoles = golesDiestra + golesZurda + golesCabeza + golesOtro
                            if (misGoles == 0) {
                                golesFueraArea = 0
                                golesTacon = 0
                                golesChilena = 0
                            } else {
                                golesFueraArea = golesFueraArea.coerceAtMost(misGoles)
                                golesTacon = golesTacon.coerceAtMost(misGoles)
                                golesChilena = golesChilena.coerceAtMost(misGoles)
                            }
                        }

                        val partesCuerpo = listOf(
                            Triple("Diestra", golesDiestra) { d: Int ->
                                golesDiestra = (golesDiestra + d).coerceAtLeast(0)
                                recalcularGolesYAtributos()
                            },
                            Triple("Zurda", golesZurda) { d: Int ->
                                golesZurda = (golesZurda + d).coerceAtLeast(0)
                                recalcularGolesYAtributos()
                            },
                            Triple("Cabeza", golesCabeza) { d: Int ->
                                golesCabeza = (golesCabeza + d).coerceAtLeast(0)
                                recalcularGolesYAtributos()
                            },
                            Triple("Otro", golesOtro) { d: Int ->
                                golesOtro = (golesOtro + d).coerceAtLeast(0)
                                recalcularGolesYAtributos()
                            }
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            partesCuerpo.chunked(2).forEach { fila ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    fila.forEach { (nombre, cantidad, update) ->
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { update(1) },
                                            color = if (cantidad > 0) LimeVolt.copy(alpha = 0.2f) else DarkCard,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, if (cantidad > 0) LimeVolt else LimeVolt.copy(alpha = 0.45f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = nombre,
                                                    fontSize = 12.sp,
                                                    color = if (cantidad > 0) LimeVolt else Color.White,
                                                    fontWeight = if (cantidad > 0) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (cantidad > 0) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "$cantidad",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = LimeVolt
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .clickable { update(-1) },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("-", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Atributos extra (3 en vertical para que quepa bien el texto de Fuera del área)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Atributos extra:",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val puedeElegirExtra = misGoles > 0
                        val atributosExtra = listOf(
                            Triple("Fuera del área", golesFueraArea) { d: Int ->
                                if (puedeElegirExtra) {
                                    golesFueraArea = (golesFueraArea + d).coerceIn(0, misGoles)
                                }
                            },
                            Triple("Tacón", golesTacon) { d: Int ->
                                if (puedeElegirExtra) {
                                    golesTacon = (golesTacon + d).coerceIn(0, misGoles)
                                }
                            },
                            Triple("Chilena", golesChilena) { d: Int ->
                                if (puedeElegirExtra) {
                                    golesChilena = (golesChilena + d).coerceIn(0, misGoles)
                                }
                            }
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            atributosExtra.forEach { (nombre, cantidad, update) ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = puedeElegirExtra) { update(1) },
                                    color = if (cantidad > 0) LimeVolt.copy(alpha = 0.2f) else DarkCard,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        when {
                                            !puedeElegirExtra -> DarkCardBorder
                                            cantidad > 0 -> LimeVolt
                                            else -> LimeVolt.copy(alpha = 0.45f)
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = nombre,
                                            fontSize = 12.sp,
                                            color = when {
                                                !puedeElegirExtra -> TextSecondary.copy(alpha = 0.4f)
                                                cantidad > 0 -> LimeVolt
                                                else -> Color.White
                                            },
                                            fontWeight = if (cantidad > 0) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (cantidad > 0 && puedeElegirExtra) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "$cantidad",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = LimeVolt
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clickable { update(-1) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("-", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Jugadores del partido por equipo con buscador interactivo
                if (jugadoresDisponibles.isNotEmpty()) {
                    val jugadoresFiltrados = remember(jugadoresDisponibles, busquedaJugador, filtroSoloFavoritos, filtroPosicion, jugadoPorMi) {
                        jugadoresDisponibles.filter { j ->
                            if (!jugadoPorMi && (j.esUsuarioPropio || j.id == "usuario_propio_id")) return@filter false
                            val matchText = busquedaJugador.isBlank() || j.nombre.contains(busquedaJugador.trim(), ignoreCase = true)
                            val matchFav = !filtroSoloFavoritos || j.esFavorito
                            val matchPos = filtroPosicion == null || (j.posicionesPrimarias.contains(filtroPosicion) || j.posicionesSecundarias.contains(filtroPosicion))
                            matchText && matchFav && matchPos
                        }.sortedWith(
                            compareByDescending<Jugador> { it.esUsuarioPropio }
                                .thenByDescending { it.esFavorito }
                                .thenBy { it.nombre.lowercase() }
                        )
                    }

                    val rivalCol = if (equipoJugado == EquipoColor.CLARO) EquipoColor.OSCURO else EquipoColor.CLARO
                    val tabTexto1 = if (jugadoPorMi) "Mi equipo (${equipoJugado.emoji}) (${jugadoresMiEquipo.size})" else "⚪ Claro (${jugadoresMiEquipo.size})"
                    val tabTexto2 = if (jugadoPorMi) "Equipo rival (${rivalCol.emoji}) (${jugadoresEquipoRival.size})" else "⚫ Oscuro (${jugadoresEquipoRival.size})"

                    Column {
                        Text("Jugadores participantes (por equipo)", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Pestañas Mi equipo / Rival
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = tabEquipoJugadores == 0,
                                onClick = { tabEquipoJugadores = 0 },
                                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = tabEquipoJugadores == 0, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                label = {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(tabTexto1, fontSize = 11.sp, textAlign = TextAlign.Center)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = tabEquipoJugadores == 1,
                                onClick = { tabEquipoJugadores = 1 },
                                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = tabEquipoJugadores == 1, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                label = {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(tabTexto2, fontSize = 11.sp, textAlign = TextAlign.Center)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Buscador y filtros de posición / favoritos
                        OutlinedTextField(
                            value = busquedaJugador,
                            onValueChange = { busquedaJugador = it },
                            placeholder = { Text("Buscar jugador...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            trailingIcon = {
                                if (busquedaJugador.isNotEmpty()) {
                                    IconButton(onClick = { busquedaJugador = "" }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                FilterChip(
                                    selected = filtroSoloFavoritos,
                                    onClick = { filtroSoloFavoritos = !filtroSoloFavoritos },
                                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = filtroSoloFavoritos, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                    leadingIcon = {
                                        Icon(
                                            if (filtroSoloFavoritos) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (filtroSoloFavoritos) LimeVolt else TextSecondary
                                        )
                                    },
                                    label = { Text("Favs", fontSize = 11.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filtroPosicion == null,
                                    onClick = { filtroPosicion = null },
                                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = filtroPosicion == null, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                    label = { Text("Todas", fontSize = 11.sp) }
                                )
                            }
                            items(Posicion.entries.toTypedArray()) { pos ->
                                val sel = filtroPosicion == pos
                                FilterChip(
                                    selected = sel,
                                    onClick = {
                                        filtroPosicion = if (filtroPosicion == pos) null else pos
                                    },
                                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                    label = { Text(pos.name, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Lista de jugadores filtrados
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(jugadoresFiltrados) { jugador ->
                                val esUsuarioChip = jugador.esUsuarioPropio || jugador.id == "usuario_propio_id" || (usuario != null && jugador.id == usuario.id)
                                val enMiEquipo = if (esUsuarioChip) {
                                    jugadoresMiEquipo.any { it in usuarioIds }
                                } else {
                                    jugador.id in jugadoresMiEquipo
                                }
                                val enRival = if (esUsuarioChip) {
                                    jugadoresEquipoRival.any { it in usuarioIds }
                                } else {
                                    jugador.id in jugadoresEquipoRival
                                }
                                val seleccionadoActual = if (tabEquipoJugadores == 0) enMiEquipo else enRival

                                FilterChip(
                                    selected = seleccionadoActual,
                                    onClick = {
                                        val actualId = if (esUsuarioChip) (usuario?.id ?: jugador.id) else jugador.id
                                        if (tabEquipoJugadores == 0) {
                                            if (enMiEquipo) {
                                                if (esUsuarioChip) jugadoresMiEquipo.removeAll { it in usuarioIds }
                                                else jugadoresMiEquipo.remove(actualId)
                                            } else {
                                                if (esUsuarioChip) {
                                                    jugadoresEquipoRival.removeAll { it in usuarioIds }
                                                    jugadoresMiEquipo.removeAll { it in usuarioIds }
                                                    jugadoresMiEquipo.add(0, actualId)
                                                } else {
                                                    jugadoresEquipoRival.remove(actualId)
                                                    jugadoresMiEquipo.add(actualId)
                                                }
                                            }
                                        } else {
                                            if (enRival) {
                                                if (esUsuarioChip) jugadoresEquipoRival.removeAll { it in usuarioIds }
                                                else jugadoresEquipoRival.remove(actualId)
                                            } else {
                                                if (esUsuarioChip) {
                                                    jugadoresMiEquipo.removeAll { it in usuarioIds }
                                                    jugadoresEquipoRival.removeAll { it in usuarioIds }
                                                    jugadoresEquipoRival.add(actualId)
                                                } else {
                                                    jugadoresMiEquipo.remove(actualId)
                                                    jugadoresEquipoRival.add(actualId)
                                                }
                                            }
                                        }
                                    },
                                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = seleccionadoActual, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                    leadingIcon = {
                                        JugadorAvatar(
                                            fotoUri = jugador.fotoUri,
                                            nombre = jugador.nombre,
                                            tamano = 22.dp,
                                            fontSize = 9.sp
                                        )
                                    },
                                    label = { Text(jugador.nombreConTu(), fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                // Notas del partido (ubicadas DEBAJO de participantes)
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas / crónica del partido") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val equipo1Saneado = if (jugadoPorMi) jugadoresMiEquipo.toList() else jugadoresMiEquipo.filter { it !in usuarioIds }
                    val equipo2Saneado = if (jugadoPorMi) jugadoresEquipoRival.toList() else jugadoresEquipoRival.filter { it !in usuarioIds }
                    val p = (partidoExistente ?: Partido()).copy(
                        fecha = fechaMillis,
                        modoJuego = modoJuego,
                        clima = clima,
                        fotoUri = fotoUri,
                        equipoJugado = if (jugadoPorMi) equipoJugado else null,
                        estadioId = estadioId,
                        jugadoPorMi = jugadoPorMi,
                        esFavorito = esFavorito,
                        golesAFavor = golesAFavor,
                        golesEnContra = golesEnContra,
                        posicionJugada = if (jugadoPorMi) posicionPrincipal else Posicion.DC,
                        posicionesSecundarias = if (jugadoPorMi) posicionesSecundarias.toSet() else emptySet(),
                        posicionesJugadas = if (jugadoPorMi) (setOf(posicionPrincipal) + posicionesSecundarias) else emptySet(),
                        goles = if (jugadoPorMi) misGoles else 0,
                        asistencias = if (jugadoPorMi) misAsistencias else 0,
                        tirosAlPalo = if (jugadoPorMi) tirosAlPalo else 0,
                        golesFueraArea = if (jugadoPorMi) golesFueraArea else 0,
                        notas = notas.trim(),
                        jugadoresMiEquipo = equipo1Saneado,
                        jugadoresEquipoRival = equipo2Saneado,
                        jugadoresIds = (equipo1Saneado + equipo2Saneado).distinct(),
                        golesZurda = if (jugadoPorMi) golesZurda else 0,
                        golesDiestra = if (jugadoPorMi) golesDiestra else 0,
                        golesCabeza = if (jugadoPorMi) golesCabeza else 0,
                        golesOtro = if (jugadoPorMi) golesOtro else 0,
                        golesChilena = if (jugadoPorMi) golesChilena else 0,
                        golesTacon = if (jugadoPorMi) golesTacon else 0,
                        duracionMinutos = duracionMinutos
                    )
                    onGuardar(p)
                },
                colors = ButtonDefaults.buttonColors(containerColor = LimeVolt, contentColor = Color.Black)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun StepperInput(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            FilledTonalIconButton(
                onClick = { onValueChange(value - 1) },
                modifier = Modifier.size(28.dp),
                enabled = value > 0
            ) {
                Text("-", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Text(
                text = "$value",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(32.dp)
            )

            FilledTonalIconButton(
                onClick = { onValueChange(value + 1) },
                modifier = Modifier.size(28.dp)
            ) {
                Text("+", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
