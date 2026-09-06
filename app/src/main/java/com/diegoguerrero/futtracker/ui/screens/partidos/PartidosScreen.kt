package com.diegoguerrero.futtracker.ui.screens.partidos

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.roundToInt
import com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorPartido
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import com.diegoguerrero.futtracker.ui.components.BadgePosicion
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
import com.diegoguerrero.futtracker.domain.model.obtenerEmoji
import com.diegoguerrero.futtracker.domain.model.obtenerEmojiParaFecha
import com.diegoguerrero.futtracker.ui.components.DialogoRecorteFoto
import com.diegoguerrero.futtracker.ui.components.DialogoVisorFotoConZoom
import com.diegoguerrero.futtracker.ui.components.ImagenLocal
import com.diegoguerrero.futtracker.ui.components.SelectorRangoFechasDosBotones
import androidx.compose.ui.draw.scale
import com.diegoguerrero.futtracker.domain.model.Formacion
import com.diegoguerrero.futtracker.domain.model.FORMACIONES_FUTSAL
import com.diegoguerrero.futtracker.domain.model.FORMACIONES_FUT_6
import com.diegoguerrero.futtracker.domain.model.FORMACIONES_FUT_7
import com.diegoguerrero.futtracker.domain.model.obtenerCoordenadas
import com.diegoguerrero.futtracker.domain.usecase.GenerarAlineacionUseCase
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

    var filtroModalidad by remember { mutableStateOf<TipoFutbol?>(null) }
    var filtroDuracion by remember { mutableStateOf<Int?>(null) }
    var filtroJugadorJugadoPor by remember { mutableStateOf<Jugador?>(null) }
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
            (if (filtroJugadorJugadoPor != null) 1 else 0) +
            (if (filtroParticipantes.isNotEmpty()) 1 else 0) +
            (if (filtroJugadorJugadoPor != null && filtroResultado != null) 1 else 0) +
            (if (filtroJugadorJugadoPor != null && filtroPosicion != null) 1 else 0) +
            (if (filtroJugadorJugadoPor != null && filtroConMisGoles) 1 else 0) +
            (if (filtroJugadorJugadoPor != null && filtroConMisAsistencias) 1 else 0) +
            (if (filtroJugadorJugadoPor != null && filtroConMisPalos) 1 else 0) +
            (if (filtroJugadorJugadoPor != null && filtroFueraArea) 1 else 0) +
            (if (filtroJugadorJugadoPor != null && filtroTacon) 1 else 0) +
            (if (filtroJugadorJugadoPor != null && filtroChilena) 1 else 0)

    val usuarioActual = remember(jugadores) {
        jugadores.firstOrNull { it.esUsuarioPropio || it.id == "usuario_propio_id" }
    }
    val usuarioIds = remember(usuarioActual) {
        setOfNotNull(usuarioActual?.id, "usuario_propio_id")
    }

    fun limpiarFiltros() {
        soloFavoritosFilter = false
        filtroPeriodo = PeriodoPartidos.TOTAL
        mostrarFilaFecha = false
        anioSeleccionadoPartidos = Calendar.getInstance().get(Calendar.YEAR)
        filtroModalidad = null
        filtroDuracion = null
        filtroJugadorJugadoPor = null
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
        jugadores,
        soloFavoritosFilter,
        filtroPeriodo,
        temporadaSeleccionadaPartidos,
        anioSeleccionadoPartidos,
        fechaInicioPartidos,
        fechaFinPartidos,
        filtroModalidad,
        filtroDuracion,
        filtroJugadorJugadoPor,
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

            if (filtroJugadorJugadoPor != null) {
                val target = filtroJugadorJugadoPor!!
                val esTargetYo = target.esUsuarioPropio || target.id == "usuario_propio_id" || target.id in usuarioIds

                val jugoEnPartido = if (esTargetYo) {
                    p.jugadoPorMi ||
                            p.jugadoresIds.any { it in usuarioIds } ||
                            p.jugadoresMiEquipo.any { it in usuarioIds } ||
                            p.jugadoresEquipoRival.any { it in usuarioIds } ||
                            p.jugadoresDetalle.any { it.jugadorId in usuarioIds }
                } else {
                    p.jugadoresIds.contains(target.id) ||
                            p.jugadoresMiEquipo.contains(target.id) ||
                            p.jugadoresEquipoRival.contains(target.id) ||
                            p.jugadoresDetalle.any { it.jugadorId == target.id }
                }
                if (!jugoEnPartido) return@filter false

                val det = if (esTargetYo) {
                    p.jugadoresDetalle.firstOrNull { it.jugadorId in usuarioIds }
                } else {
                    p.jugadoresDetalle.firstOrNull { it.jugadorId == target.id }
                }

                if (filtroResultado != null) {
                    val enMiEquipo = if (esTargetYo) {
                        det?.esMiEquipo ?: (p.jugadoPorMi && !p.jugadoresEquipoRival.any { it in usuarioIds })
                    } else {
                        det?.esMiEquipo ?: p.jugadoresMiEquipo.contains(target.id)
                    }

                    val ganoJugador = if (enMiEquipo) p.esVictoria else p.esDerrota
                    val empatoJugador = p.esEmpate
                    val perdioJugador = if (enMiEquipo) p.esDerrota else p.esVictoria

                    val coincideResultado = when (filtroResultado) {
                        ResultadoFiltro.VICTORIAS -> ganoJugador
                        ResultadoFiltro.EMPATES -> empatoJugador
                        ResultadoFiltro.DERROTAS -> perdioJugador
                        null -> true
                    }
                    if (!coincideResultado) return@filter false
                }

                if (filtroPosicion != null) {
                    val posPrincipal = if (esTargetYo) {
                        det?.posicionPrincipal ?: p.posicionJugada
                    } else {
                        det?.posicionPrincipal ?: target.posicionesPrimarias.firstOrNull() ?: Posicion.DC
                    }
                    val posSecundarias = if (esTargetYo) {
                        det?.posicionesSecundarias ?: p.posicionesSecundarias
                    } else {
                        det?.posicionesSecundarias ?: target.posicionesSecundarias
                    }
                    val coincidePos = if (filtroSoloPosicionPrincipal) {
                        posPrincipal == filtroPosicion
                    } else {
                        posPrincipal == filtroPosicion || posSecundarias.contains(filtroPosicion) || (esTargetYo && p.posicionesJugadas.contains(filtroPosicion))
                    }
                    if (!coincidePos) return@filter false
                }

                val golesJugador = if (esTargetYo) {
                    det?.goles ?: if (p.jugadoPorMi) p.goles else 0
                } else {
                    det?.goles ?: 0
                }

                val asistenciasJugador = if (esTargetYo) {
                    det?.asistencias ?: if (p.jugadoPorMi) p.asistencias else 0
                } else {
                    det?.asistencias ?: 0
                }

                val palosJugador = if (esTargetYo) {
                    det?.tirosAlPalo ?: if (p.jugadoPorMi) p.tirosAlPalo else 0
                } else {
                    det?.tirosAlPalo ?: 0
                }

                val fueraAreaJugador = if (esTargetYo) {
                    det?.golesFueraArea ?: if (p.jugadoPorMi) p.golesFueraArea else 0
                } else {
                    det?.golesFueraArea ?: 0
                }

                val taconJugador = if (esTargetYo) {
                    det?.golesTacon ?: if (p.jugadoPorMi) p.golesTacon else 0
                } else {
                    det?.golesTacon ?: 0
                }

                val chilenaJugador = if (esTargetYo) {
                    det?.golesChilena ?: if (p.jugadoPorMi) p.golesChilena else 0
                } else {
                    det?.golesChilena ?: 0
                }

                if (filtroConMisGoles && golesJugador <= 0) return@filter false
                if (filtroConMisAsistencias && asistenciasJugador <= 0) return@filter false
                if (filtroConMisPalos && palosJugador <= 0) return@filter false
                if (filtroFueraArea && fueraAreaJugador <= 0) return@filter false
                if (filtroTacon && taconJugador <= 0) return@filter false
                if (filtroChilena && chilenaJugador <= 0) return@filter false
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
                containerColor = LimeVoltSolid,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
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

                // Jugados por (al final de la primera fila)
                item {
                    var menuJugadoPorAbierto by remember { mutableStateOf(false) }
                    val labelChip = remember(filtroJugadorJugadoPor) {
                        filtroJugadorJugadoPor?.let { "Jugado por: ${it.nombreConTu()}" } ?: "Jugados por"
                    }

                    Box {
                        FilterChip(
                            selected = filtroJugadorJugadoPor != null,
                            onClick = { menuJugadoPorAbierto = true },
                            label = { Text(labelChip, fontSize = 12.sp) },
                            leadingIcon = {
                                if (filtroJugadorJugadoPor != null) {
                                    JugadorAvatar(
                                        fotoUri = filtroJugadorJugadoPor?.fotoUri,
                                        nombre = filtroJugadorJugadoPor?.nombre ?: "",
                                        tamano = 18.dp,
                                        fontSize = 8.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (filtroJugadorJugadoPor != null) {
                                        IconButton(
                                            onClick = { filtroJugadorJugadoPor = null },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Quitar filtro",
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        )

                        DropdownMenu(
                            expanded = menuJugadoPorAbierto,
                            onDismissRequest = { menuJugadoPorAbierto = false },
                            modifier = Modifier.heightIn(max = 380.dp)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Sin filtrar",
                                        fontWeight = if (filtroJugadorJugadoPor == null) FontWeight.Bold else FontWeight.Normal,
                                        color = if (filtroJugadorJugadoPor == null) LimeVolt else Color.White
                                    )
                                },
                                onClick = {
                                    filtroJugadorJugadoPor = null
                                    menuJugadoPorAbierto = false
                                }
                            )

                            HorizontalDivider(color = DarkCardBorder)

                            val listaOpcionesJugadores = remember(jugadores) {
                                val lista = jugadores.toMutableList()
                                if (lista.none { it.esUsuarioPropio || it.id == "usuario_propio_id" }) {
                                    lista.add(0, Jugador(id = "usuario_propio_id", nombre = "Yo", esUsuarioPropio = true))
                                }
                                lista.sortedWith(
                                    compareByDescending<Jugador> { it.esUsuarioPropio || it.id == "usuario_propio_id" }
                                        .thenByDescending { it.esFavorito }
                                        .thenBy { it.nombre.lowercase() }
                                )
                            }

                            listaOpcionesJugadores.forEach { jug ->
                                val esSeleccionado = filtroJugadorJugadoPor?.id == jug.id ||
                                        (jug.esUsuarioPropio && filtroJugadorJugadoPor?.esUsuarioPropio == true)

                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            JugadorAvatar(
                                                fotoUri = jug.fotoUri,
                                                nombre = jug.nombre,
                                                tamano = 24.dp,
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                text = jug.nombreConTu(),
                                                color = if (esSeleccionado) LimeVolt else Color.White,
                                                fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = {
                                        filtroJugadorJugadoPor = jug
                                        menuJugadoPorAbierto = false
                                    }
                                )
                            }
                        }
                    }
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
                                    onClick = { filtroPeriodo = PeriodoPartidos.RANGO_FECHAS },
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
                            SelectorRangoFechasDosBotones(
                                fechaInicio = fechaInicioPartidos,
                                fechaFin = fechaFinPartidos,
                                onRangoChange = { ini, fin ->
                                    fechaInicioPartidos = ini
                                    fechaFinPartidos = fin
                                }
                            )
                        }
                    }
                }
            }

            // Segunda fila de filtros personales (SOLO si se selecciona un jugador en "Jugados por")
            if (filtroJugadorJugadoPor != null) {
                val esYoFiltro = filtroJugadorJugadoPor?.let { it.esUsuarioPropio || it.id == "usuario_propio_id" || it.id in usuarioIds } == true
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
                        val labelPosicion = filtroPosicion?.name ?: if (esYoFiltro) "Mi posición" else "Posición"
                        Box {
                            FilterChip(
                                selected = filtroPosicion != null,
                                onClick = { menuPosicionAbierto = true },
                                label = { Text(labelPosicion, fontSize = 12.sp) },
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

                    // 3. Con goles, asistencias, etc.
                    val labelGoles = if (esYoFiltro) "Con mis goles" else "Con goles"
                    val labelAsis = if (esYoFiltro) "Con mis asistencias" else "Con asistencias"
                    item {
                        FilterChip(
                            selected = filtroConMisGoles,
                            onClick = { filtroConMisGoles = !filtroConMisGoles },
                            label = { Text(labelGoles, fontSize = 12.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filtroConMisAsistencias,
                            onClick = { filtroConMisAsistencias = !filtroConMisAsistencias },
                            label = { Text(labelAsis, fontSize = 12.sp) }
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
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard
                        ),
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                            }
                        },
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

    var mostrarZoomFoto by remember { mutableStateOf(false) }
    val tieneFotoValida = partido.fotoUri != null && File(partido.fotoUri).exists()

    if (mostrarZoomFoto && tieneFotoValida && partido.fotoUri != null) {
        DialogoVisorFotoConZoom(
            fotoUri = partido.fotoUri,
            nombre = "Partido ${partido.golesAFavor} - ${partido.golesEnContra}",
            onDismiss = { mostrarZoomFoto = false }
        )
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

                    if (partido.clima != null) {
                        Surface(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = partido.clima.obtenerEmojiParaFecha(partido.fecha),
                                color = Color.White,
                                fontSize = 11.sp,
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

            if (estadioPartido != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🏟️ ${estadioPartido.nombre}",
                        color = LimeVolt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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
                            .background(DarkBackground)
                            .then(
                                if (tieneFotoValida) {
                                    Modifier.clickable { mostrarZoomFoto = true }
                                } else Modifier
                            ),
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
                                text = "Jugaste con el equipo ${miColor.emoji}${miColor.label}",
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(22.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = partido.posicionJugada.name,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    style = androidx.compose.ui.text.TextStyle(
                                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                                    )
                                )
                            }
                        }
                        // Posiciones secundarias si las hay
                        partido.posicionesSecundarias.forEach { posSec ->
                            Surface(
                                color = LimeVolt.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.8.dp, LimeVolt.copy(alpha = 0.5f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(22.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = posSec.name,
                                        color = LimeVolt,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        style = androidx.compose.ui.text.TextStyle(
                                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚽ ${partido.goles}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🅰️ ${partido.asistencias}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎯 ${partido.tirosAlPalo}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (partido.golesFueraArea > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🚀 ${partido.golesFueraArea}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (partido.golesChilena > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🤸 ${partido.golesChilena}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (partido.golesTacon > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "👟 ${partido.golesTacon}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    val yoJugoDePortero = partido.posicionJugada == Posicion.POR || partido.posicionesSecundarias.contains(Posicion.POR) || partido.posicionesJugadas.contains(Posicion.POR)
                    if (partido.paradas > 0 && yoJugoDePortero) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🧤 ${partido.paradas}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Desglose de goles si los hay
                val detallesGoles = buildList {
                    if (partido.golesDiestra > 0) add("Diestra: ${partido.golesDiestra}")
                    if (partido.golesZurda > 0) add("Zurda: ${partido.golesZurda}")
                    if (partido.golesCabeza > 0) add("Cabeza: ${partido.golesCabeza}")
                    if (partido.golesTacon > 0) add("Tacón: ${partido.golesTacon}")
                    if (partido.golesChilena > 0) add("Chilena: ${partido.golesChilena}")
                    if (partido.golesFueraArea > 0) add("Fuera área: ${partido.golesFueraArea}")
                    if (partido.golesOtro > 0) add("Otro: ${partido.golesOtro}")
                }

                if (detallesGoles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
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
                        val companeros = partido.jugadoresMiEquipo.map { id ->
                            jugadores.firstOrNull { it.id == id || (id == "usuario_propio_id" && it.esUsuarioPropio) }
                                ?: Jugador(id = id, nombre = "Jugador")
                        }.sortedWith(
                            compareByDescending<Jugador> { it.esUsuarioPropio || it.id == "usuario_propio_id" }
                                .thenBy { it.nombre.lowercase() }
                        )
                        items(companeros) { comp ->
                            val esYo = comp.esUsuarioPropio || comp.id == "usuario_propio_id"
                            val esDef = comp.id.startsWith("defecto_")
                            Surface(
                                color = when {
                                    esYo -> LimeVolt.copy(alpha = 0.35f)
                                    esDef -> Color.White.copy(alpha = 0.05f)
                                    else -> LimeVolt.copy(alpha = 0.15f)
                                },
                                shape = RoundedCornerShape(4.dp),
                                border = if (esYo) BorderStroke(1.dp, LimeVolt) else null
                            ) {
                                Text(
                                    text = if (esYo) "${comp.nombre} (Tú)" else comp.nombre,
                                    color = if (esDef) TextSecondary else LimeVolt,
                                    fontSize = 10.sp,
                                    fontWeight = if (esYo) FontWeight.Bold else (if (esDef) FontWeight.Normal else FontWeight.SemiBold),
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
                        val rivales = partido.jugadoresEquipoRival.map { id ->
                            jugadores.firstOrNull { it.id == id || (id == "usuario_propio_id" && it.esUsuarioPropio) }
                                ?: Jugador(id = id, nombre = "Jugador")
                        }.sortedWith(compareBy { it.nombre.lowercase() })
                        items(rivales) { riv ->
                            val esDef = riv.id.startsWith("defecto_")
                            Surface(
                                color = if (esDef) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = riv.nombre,
                                    color = if (esDef) TextSecondary else Color.White,
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
                    val companeros = partido.jugadoresIds.map { id ->
                        jugadores.firstOrNull { it.id == id || (id == "usuario_propio_id" && it.esUsuarioPropio) }
                            ?: Jugador(id = id, nombre = "Jugador")
                    }
                    items(companeros) { comp ->
                        val esDef = comp.id.startsWith("defecto_")
                        Surface(
                            color = if (esDef) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.07f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = comp.nombre,
                                color = if (esDef) TextSecondary else Color.White,
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
fun DialogoEditarStatsJugadorPartido(
    jugador: Jugador,
    esMiEquipo: Boolean,
    statsActuales: EstadisticasJugadorPartido,
    onDismiss: () -> Unit,
    onGuardar: (EstadisticasJugadorPartido) -> Unit
) {
    var posPrincipal by remember { mutableStateOf(statsActuales.posicionPrincipal) }
    val posSecundarias = remember { mutableStateListOf<Posicion>().apply { addAll(statsActuales.posicionesSecundarias) } }

    var statsRegistradas by remember {
        mutableStateOf(
            if (jugador.esUsuarioPropio) true
            else (statsActuales.statsRegistradas || statsActuales.goles > 0 || statsActuales.asistencias > 0 || statsActuales.tirosAlPalo > 0 || statsActuales.paradas > 0)
        )
    }

    var goles by remember { mutableStateOf(statsActuales.goles) }
    var asistencias by remember { mutableStateOf(statsActuales.asistencias) }
    var palos by remember { mutableStateOf(statsActuales.tirosAlPalo) }
    var paradas by remember { mutableStateOf(statsActuales.paradas) }

    var gDiestra by remember { mutableStateOf(statsActuales.golesDiestra) }
    var gZurda by remember { mutableStateOf(statsActuales.golesZurda) }
    var gCabeza by remember { mutableStateOf(statsActuales.golesCabeza) }
    var gOtro by remember { mutableStateOf(statsActuales.golesOtro) }

    var gFuera by remember { mutableStateOf(statsActuales.golesFueraArea) }
    var gTacon by remember { mutableStateOf(statsActuales.golesTacon) }
    var gChilena by remember { mutableStateOf(statsActuales.golesChilena) }

    fun recalcularGoles() {
        goles = gDiestra + gZurda + gCabeza + gOtro
        if (goles == 0) {
            gFuera = 0
            gTacon = 0
            gChilena = 0
        } else {
            gFuera = gFuera.coerceAtMost(goles)
            gTacon = gTacon.coerceAtMost(goles)
            gChilena = gChilena.coerceAtMost(goles)
        }
    }

    var mostrarZoomFoto by remember { mutableStateOf(false) }

    if (mostrarZoomFoto && jugador.fotoUri != null) {
        DialogoVisorFotoConZoom(
            fotoUri = jugador.fotoUri!!,
            nombre = jugador.nombre,
            onDismiss = { mostrarZoomFoto = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JugadorAvatar(
                    fotoUri = jugador.fotoUri,
                    nombre = jugador.nombre,
                    tamano = 38.dp,
                    fontSize = 13.sp,
                    onClick = {
                        if (jugador.fotoUri != null) {
                            mostrarZoomFoto = true
                        }
                    }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = jugador.nombreConTu(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Surface(
                        color = if (esMiEquipo) LimeVolt.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (esMiEquipo) "Mi equipo" else "Equipo rival",
                            color = if (esMiEquipo) LimeVolt else TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Posición Principal
                Column {
                    Text("Posición principal", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(Posicion.entries.toTypedArray()) { pos ->
                            val sel = posPrincipal == pos
                            FilterChip(
                                selected = sel,
                                onClick = {
                                    posPrincipal = pos
                                    posSecundarias.remove(pos)
                                },
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = sel,
                                    borderColor = Color.Gray.copy(alpha = 0.5f),
                                    selectedBorderColor = LimeVolt
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DarkCard,
                                    selectedLabelColor = LimeVolt,
                                    containerColor = DarkCard,
                                    labelColor = TextSecondary
                                ),
                                label = { Text(pos.name, fontSize = 11.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                }

                // Posiciones Secundarias
                Column {
                    Text("Posiciones secundarias (opcional)", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(Posicion.entries.toTypedArray()) { pos ->
                            if (pos != posPrincipal) {
                                val sel = pos in posSecundarias
                                FilterChip(
                                    selected = sel,
                                    onClick = {
                                        if (sel) posSecundarias.remove(pos)
                                        else posSecundarias.add(pos)
                                    },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = sel,
                                        borderColor = Color.Gray.copy(alpha = 0.5f),
                                        selectedBorderColor = LimeVolt
                                    ),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DarkCard,
                                        selectedLabelColor = LimeVolt,
                                        containerColor = DarkCard,
                                        labelColor = TextSecondary
                                    ),
                                    label = { Text(pos.name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = DarkCardBorder, thickness = 0.8.dp)

                // Checkbox/Switch para estadísticas opcionales
                if (!jugador.esUsuarioPropio) {
                    Surface(
                        color = if (statsRegistradas) LimeVolt.copy(alpha = 0.12f) else DarkCard,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (statsRegistradas) LimeVolt.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { statsRegistradas = !statsRegistradas }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Registrar estadísticas",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (statsRegistradas) LimeVolt else Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = statsRegistradas,
                                onCheckedChange = { statsRegistradas = it },
                                modifier = Modifier.scale(0.8f),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = LimeVoltSolid,
                                    checkedTrackColor = LimeVolt.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                }

                if (statsRegistradas || jugador.esUsuarioPropio) {
                    val jugoDePortero = posPrincipal == Posicion.POR || Posicion.POR in posSecundarias
                    if (jugoDePortero) {
                        // 2x2 con Goles, Asistencias, Palos y Paradas
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    StepperInput(
                                        label = "⚽ Goles",
                                        value = goles,
                                        onValueChange = { nuevoTotal ->
                                            val diff = nuevoTotal - goles
                                            if (diff > 0) {
                                                gDiestra += diff
                                            } else if (diff < 0) {
                                                var porQuitar = -diff
                                                if (gOtro >= porQuitar) { gOtro -= porQuitar; porQuitar = 0 } else { porQuitar -= gOtro; gOtro = 0 }
                                                if (porQuitar > 0 && gCabeza >= porQuitar) { gCabeza -= porQuitar; porQuitar = 0 } else { porQuitar -= gCabeza; gCabeza = 0 }
                                                if (porQuitar > 0 && gZurda >= porQuitar) { gZurda -= porQuitar; porQuitar = 0 } else { porQuitar -= gZurda; gZurda = 0 }
                                                if (porQuitar > 0) { gDiestra = (gDiestra - porQuitar).coerceAtLeast(0) }
                                            }
                                            recalcularGoles()
                                        }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    StepperInput(
                                        label = "🅰️ Asist.",
                                        value = asistencias,
                                        onValueChange = { asistencias = it.coerceAtLeast(0) }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    StepperInput(
                                        label = "🎯 Palos",
                                        value = palos,
                                        onValueChange = { palos = it.coerceAtLeast(0) }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    StepperInput(
                                        label = "🧤 Paradas",
                                        value = paradas,
                                        onValueChange = { paradas = it.coerceAtLeast(0) }
                                    )
                                }
                            }
                        }
                    } else {
                        // Solo 3 estadísticas: Goles, Asistencias y Palos en 1 sola fila
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                StepperInput(
                                    label = "⚽ Goles",
                                    value = goles,
                                    onValueChange = { nuevoTotal ->
                                        val diff = nuevoTotal - goles
                                        if (diff > 0) {
                                            gDiestra += diff
                                        } else if (diff < 0) {
                                            var porQuitar = -diff
                                            if (gOtro >= porQuitar) { gOtro -= porQuitar; porQuitar = 0 } else { porQuitar -= gOtro; gOtro = 0 }
                                            if (porQuitar > 0 && gCabeza >= porQuitar) { gCabeza -= porQuitar; porQuitar = 0 } else { porQuitar -= gCabeza; gCabeza = 0 }
                                            if (porQuitar > 0 && gZurda >= porQuitar) { gZurda -= porQuitar; porQuitar = 0 } else { porQuitar -= gZurda; gZurda = 0 }
                                            if (porQuitar > 0) { gDiestra = (gDiestra - porQuitar).coerceAtLeast(0) }
                                        }
                                        recalcularGoles()
                                    }
                                )
                            }
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                StepperInput(
                                    label = "🅰️ Asist.",
                                    value = asistencias,
                                    onValueChange = { asistencias = it.coerceAtLeast(0) }
                                )
                            }
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                StepperInput(
                                    label = "🎯 Palos",
                                    value = palos,
                                    onValueChange = { palos = it.coerceAtLeast(0) }
                                )
                            }
                        }
                    }

                    // Desglose de goles
                    if (goles > 0) {
                        Text("Parte del cuerpo:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        val partesCuerpo = listOf(
                            Triple("Diestra", gDiestra) { d: Int -> gDiestra = (gDiestra + d).coerceAtLeast(0); recalcularGoles() },
                            Triple("Zurda", gZurda) { d: Int -> gZurda = (gZurda + d).coerceAtLeast(0); recalcularGoles() },
                            Triple("Cabeza", gCabeza) { d: Int -> gCabeza = (gCabeza + d).coerceAtLeast(0); recalcularGoles() },
                            Triple("Otro", gOtro) { d: Int -> gOtro = (gOtro + d).coerceAtLeast(0); recalcularGoles() }
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
                                            border = BorderStroke(1.dp, if (cantidad > 0) LimeVolt else Color.Gray.copy(alpha = 0.5f))
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
                                                        Text("$cantidad", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LimeVolt)
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

                        Text("Atributos extra:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        val atributosExtra = listOf(
                            Triple("Fuera del área", gFuera) { d: Int -> gFuera = (gFuera + d).coerceIn(0, goles) },
                            Triple("Tacón", gTacon) { d: Int -> gTacon = (gTacon + d).coerceIn(0, goles) },
                            Triple("Chilena", gChilena) { d: Int -> gChilena = (gChilena + d).coerceIn(0, goles) }
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            atributosExtra.forEach { (nombre, cantidad, update) ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { update(1) },
                                    color = if (cantidad > 0) LimeVolt.copy(alpha = 0.2f) else DarkCard,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (cantidad > 0) LimeVolt else Color.Gray.copy(alpha = 0.5f))
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
                                                Text("$cantidad", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LimeVolt)
                                                Spacer(modifier = Modifier.width(6.dp))
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val seRegistran = jugador.esUsuarioPropio || statsRegistradas
                    onGuardar(
                        statsActuales.copy(
                            posicionPrincipal = posPrincipal,
                            posicionesSecundarias = posSecundarias.toSet(),
                            statsRegistradas = seRegistran,
                            goles = if (seRegistran) goles else 0,
                            asistencias = if (seRegistran) asistencias else 0,
                            tirosAlPalo = if (seRegistran) palos else 0,
                            paradas = if (seRegistran && (posPrincipal == Posicion.POR || Posicion.POR in posSecundarias)) paradas else 0,
                            golesDiestra = if (seRegistran) gDiestra else 0,
                            golesZurda = if (seRegistran) gZurda else 0,
                            golesCabeza = if (seRegistran) gCabeza else 0,
                            golesOtro = if (seRegistran) gOtro else 0,
                            golesFueraArea = if (seRegistran) gFuera else 0,
                            golesTacon = if (seRegistran) gTacon else 0,
                            golesChilena = if (seRegistran) gChilena else 0
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = LimeVoltSolid, contentColor = Color.Black)
            ) {
                Text("Aceptar", fontWeight = FontWeight.Bold)
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
fun PizarraColocacionPartido(
    jugadores: List<Jugador>,
    detallesJugadores: Map<String, EstadisticasJugadorPartido>,
    nombreEquipo: String,
    emojiEquipo: String,
    colorBordeFicha: Color?,
    formacionesDisponibles: List<Formacion>,
    formacionSeleccionada: Formacion,
    onFormacionSeleccionada: (Formacion) -> Unit,
    onMoverJugador: (String, Float, Float) -> Unit,
    onEditarStats: (Jugador) -> Unit,
    jugadorSwapOrigen: Jugador? = null,
    onSwap: ((Jugador) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val pitchGreen = Color(0xFF1B4D3E)
    val lineColor = Color.White.copy(alpha = 0.35f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏟️ Colocación táctica: $nombreEquipo $emojiEquipo",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Selector de alineaciones / formaciones
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(formacionesDisponibles) { f ->
                    val sel = formacionSeleccionada.id == f.id
                    FilterChip(
                        selected = sel,
                        onClick = { onFormacionSeleccionada(f) },
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = sel,
                            borderColor = LimeVolt.copy(alpha = 0.5f),
                            selectedBorderColor = LimeVolt
                        ),
                        label = {
                            Text(
                                text = f.nombre,
                                fontSize = 11.sp,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (jugadores.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(pitchGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Selecciona jugadores arriba para verlos en el campo",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(pitchGreen)
                ) {
                    val anchoTotal = maxWidth
                    val altoTotal = maxHeight

                    // Dibujo de líneas de campo táctico
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val widthPx = size.width
                        val heightPx = size.height
                        val stroke = 1.5f

                        // Línea de medio campo
                        drawLine(lineColor, Offset(0f, heightPx / 2), Offset(widthPx, heightPx / 2), strokeWidth = stroke)
                        // Círculo central
                        drawCircle(lineColor, radius = widthPx * 0.15f, center = Offset(widthPx / 2, heightPx / 2), style = Stroke(stroke))
                        drawCircle(lineColor, radius = 3f, center = Offset(widthPx / 2, heightPx / 2))
                        // Portería y área superior
                        val areaW = widthPx * 0.5f
                        val areaH = heightPx * 0.18f
                        drawRect(lineColor, topLeft = Offset((widthPx - areaW) / 2, 0f), size = Size(areaW, areaH), style = Stroke(stroke))
                        // Portería y área inferior
                        drawRect(lineColor, topLeft = Offset((widthPx - areaW) / 2, heightPx - areaH), size = Size(areaW, areaH), style = Stroke(stroke))
                    }

                    val coords = remember(formacionSeleccionada) { obtenerCoordenadas(formacionSeleccionada) }

                    // Renderizar jugadores
                    jugadores.forEachIndexed { index, jugador ->
                        val det = detallesJugadores[jugador.id]
                        val slotCoord = coords.getOrNull(index)

                        val posDisplay = det?.posicionPrincipal
                            ?: slotCoord?.first
                            ?: jugador.posicionesPrimarias.firstOrNull()
                            ?: Posicion.DC

                        val posXRel = slotCoord?.second?.first ?: det?.posX ?: when (posDisplay) {
                            Posicion.POR -> 0.50f
                            Posicion.LI -> 0.20f
                            Posicion.DFC -> 0.50f
                            Posicion.LD -> 0.80f
                            Posicion.MC -> 0.50f
                            Posicion.EI -> 0.22f
                            Posicion.DC -> 0.50f
                            Posicion.ED -> 0.78f
                        }
                        val posYRel = slotCoord?.second?.second ?: det?.posY ?: when (posDisplay) {
                            Posicion.POR -> 0.86f
                            Posicion.LI, Posicion.LD -> 0.65f
                            Posicion.DFC -> 0.68f
                            Posicion.MC -> 0.44f
                            Posicion.EI, Posicion.ED -> 0.26f
                            Posicion.DC -> 0.16f
                        }

                        val iconWidth = 64.dp
                        val iconHeight = 44.dp
                        val posXDp = anchoTotal * posXRel
                        val posYDp = altoTotal * posYRel

                        val fichaBorder = colorBordeFicha ?: LimeVolt
                        val fichaBordeAncho = 1.5.dp
                        val esFav = jugador.esFavorito
                        val esSeleccionadoSwap = jugadorSwapOrigen?.id == jugador.id
                        val esDefecto = jugador.id.startsWith("defecto_") || jugador.nombre == "Jugador"

                        Box(
                            modifier = Modifier
                                .offset(x = posXDp - (iconWidth / 2), y = posYDp - (iconHeight / 2))
                                .size(width = iconWidth, height = iconHeight)
                                .clickable {
                                    if (onSwap != null && jugadorSwapOrigen != null) {
                                        onSwap(jugador)
                                    } else if (!jugador.id.startsWith("defecto_")) {
                                        onEditarStats(jugador)
                                    }
                                },
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    if (esSeleccionadoSwap) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .border(2.dp, LimeVolt, CircleShape)
                                        )
                                    } else if (esFav) {
                                        Box(
                                            modifier = Modifier
                                                .size(29.dp)
                                                .border(1.dp, Color(0xFFFFD700), CircleShape)
                                        )
                                    }
                                    JugadorAvatar(
                                        fotoUri = jugador.fotoUri,
                                        nombre = jugador.nombre,
                                        tamano = 26.dp,
                                        fontSize = 8.5.sp,
                                        bordeColor = fichaBorder,
                                        bordeAncho = fichaBordeAncho,
                                        esPorDefecto = esDefecto
                                    )
                                    val badgeColor = colorBordeFicha ?: LimeVolt
                                    Surface(
                                        color = badgeColor,
                                        shape = RoundedCornerShape(2.dp),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .offset(x = 1.dp, y = 0.dp)
                                            .size(width = 17.dp, height = 12.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = posDisplay.name,
                                                color = if (badgeColor == Color.Black) Color.White else Color.Black,
                                                fontSize = 6.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                textAlign = TextAlign.Center,
                                                style = androidx.compose.ui.text.TextStyle(
                                                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                                                ),
                                                modifier = Modifier.offset(y = 0.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = jugador.nombreConTu(),
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    style = androidx.compose.ui.text.TextStyle(
                                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    modifier = Modifier.offset(y = (-2).dp)
                                )
                            }
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
    var clima by remember { mutableStateOf<Clima?>(partidoExistente?.clima) }
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
    var misParadas by remember { mutableStateOf(partidoExistente?.paradas ?: 0) }

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
    val canonicalUserId = usuario?.id ?: "usuario_propio_id"
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

    fun esIdDefecto(id: String): Boolean = id.startsWith("defecto_")

    fun rellenarConDefecto(lista: MutableList<String>, esMiEquipo: Boolean, maxTotal: Int) {
        while (lista.size > maxTotal) {
            val lastDefecto = lista.indexOfLast { esIdDefecto(it) }
            if (lastDefecto != -1) {
                lista.removeAt(lastDefecto)
            } else {
                val nonUser = lista.indexOfLast { it !in usuarioIds && it != canonicalUserId }
                if (nonUser != -1) lista.removeAt(nonUser)
                else lista.removeAt(lista.lastIndex)
            }
        }
        val prefix = if (esMiEquipo) "defecto_mi_" else "defecto_riv_"
        var seq = 1
        while (lista.size < maxTotal) {
            while (lista.contains("$prefix$seq")) {
                seq++
            }
            lista.add("$prefix$seq")
            seq++
        }
    }

    val jugadoresMiEquipo = remember {
        mutableStateListOf<String>().apply {
            if (partidoExistente != null) {
                val existentes = partidoExistente.jugadoresMiEquipo.ifEmpty { partidoExistente.jugadoresIds }
                val saneados = existentes.map { if (it in usuarioIds) canonicalUserId else it }.distinct().toMutableList()
                if (jugadoPorMi) {
                    saneados.removeAll { it in usuarioIds }
                    saneados.add(0, canonicalUserId)
                } else {
                    saneados.removeAll { it in usuarioIds }
                }
                addAll(saneados)
            } else {
                if (jugadoPorMi) {
                    add(canonicalUserId)
                }
            }
            rellenarConDefecto(this, esMiEquipo = true, maxTotal = modoJuego.nJugadoresCampo)
        }
    }

    val jugadoresEquipoRival = remember {
        mutableStateListOf<String>().apply {
            if (partidoExistente != null) {
                val saneados = partidoExistente.jugadoresEquipoRival.map { if (it in usuarioIds) canonicalUserId else it }.distinct().toMutableList()
                if (!jugadoPorMi) {
                    saneados.removeAll { it in usuarioIds }
                }
                addAll(saneados)
            }
            rellenarConDefecto(this, esMiEquipo = false, maxTotal = modoJuego.nJugadoresCampo)
        }
    }

    LaunchedEffect(jugadoresDisponibles, jugadoPorMi, canonicalUserId, modoJuego) {
        val validDbIds = jugadoresDisponibles.map { it.id }.toSet()
        if (validDbIds.isEmpty()) return@LaunchedEffect

        fun sanear(lista: List<String>, esMiEquipo: Boolean): List<String> {
            val resultado = mutableListOf<String>()
            var usuarioIncluido = false
            for (id in lista) {
                if (id in usuarioIds || id == canonicalUserId) {
                    if (esMiEquipo && jugadoPorMi && !usuarioIncluido) {
                        resultado.add(canonicalUserId)
                        usuarioIncluido = true
                    }
                } else if ((id in validDbIds || esIdDefecto(id)) && !resultado.contains(id)) {
                    resultado.add(id)
                }
            }
            if (esMiEquipo && jugadoPorMi && !usuarioIncluido) {
                resultado.add(0, canonicalUserId)
            }
            return resultado
        }

        val miEquipoSaneado = sanear(jugadoresMiEquipo, esMiEquipo = true)
        val rivalSaneado = sanear(jugadoresEquipoRival, esMiEquipo = false)

        val maxCampo = modoJuego.nJugadoresCampo
        jugadoresMiEquipo.clear()
        jugadoresMiEquipo.addAll(miEquipoSaneado.take(maxCampo))
        rellenarConDefecto(jugadoresMiEquipo, esMiEquipo = true, maxTotal = maxCampo)

        jugadoresEquipoRival.clear()
        jugadoresEquipoRival.addAll(rivalSaneado.take(maxCampo))
        rellenarConDefecto(jugadoresEquipoRival, esMiEquipo = false, maxTotal = maxCampo)
    }

    LaunchedEffect(modoJuego) {
        val maxCampo = modoJuego.nJugadoresCampo
        rellenarConDefecto(jugadoresMiEquipo, esMiEquipo = true, maxTotal = maxCampo)
        rellenarConDefecto(jugadoresEquipoRival, esMiEquipo = false, maxTotal = maxCampo)
    }

    val formacionesDisponibles = remember(modoJuego) {
        when (modoJuego) {
            TipoFutbol.FUTSAL -> FORMACIONES_FUTSAL
            TipoFutbol.FUT_6 -> FORMACIONES_FUT_6
            TipoFutbol.FUT_7 -> FORMACIONES_FUT_7
        }
    }
    var formacionMiEquipo by remember(partidoExistente, modoJuego) {
        val formId = partidoExistente?.formacionMiEquipo
        mutableStateOf(
            formacionesDisponibles.firstOrNull { it.id == formId || it.nombre == formId }
                ?: formacionesDisponibles.first()
        )
    }
    var formacionRival by remember(partidoExistente, modoJuego) {
        val formId = partidoExistente?.formacionRival
        mutableStateOf(
            formacionesDisponibles.firstOrNull { it.id == formId || it.nombre == formId }
                ?: formacionesDisponibles.first()
        )
    }

    val detallesJugadores = remember {
        mutableStateMapOf<String, EstadisticasJugadorPartido>().apply {
            partidoExistente?.jugadoresDetalle?.forEach { det ->
                put(det.jugadorId, det)
            }
        }
    }
    var jugadorParaEditarStats by remember { mutableStateOf<Jugador?>(null) }
    var jugadorSwapPizarraOrigen by remember { mutableStateOf<Jugador?>(null) }

    var busquedaEstadio by remember { mutableStateOf("") }
    var filtroEstadioModalidad by remember { mutableStateOf<TipoFutbol?>(null) }
    var filtroEstadioFavoritos by remember { mutableStateOf(false) }
    var ordenEstadioAZ by remember { mutableStateOf(true) }
    var ordenEstadioRecientes by remember { mutableStateOf(true) }
    var criterioOrdenEstadio by remember { mutableStateOf("ALFA") }

    val estadiosFiltrados = remember(estadios, busquedaEstadio, filtroEstadioModalidad, filtroEstadioFavoritos, ordenEstadioAZ, ordenEstadioRecientes, criterioOrdenEstadio) {
        estadios.filter { est ->
            val matchText = busquedaEstadio.isBlank() || est.nombre.contains(busquedaEstadio.trim(), ignoreCase = true)
            val matchFav = !filtroEstadioFavoritos || est.esFavorito
            val matchMod = filtroEstadioModalidad == null || est.modalidades.contains(filtroEstadioModalidad)
            matchText && matchFav && matchMod
        }.sortedWith { a, b ->
            if (a.esFavorito != b.esFavorito) {
                if (a.esFavorito) -1 else 1
            } else {
                if (criterioOrdenEstadio == "FECHA") {
                    if (ordenEstadioRecientes) b.fechaCreacion.compareTo(a.fechaCreacion)
                    else a.fechaCreacion.compareTo(b.fechaCreacion)
                } else {
                    if (ordenEstadioAZ) a.nombre.compareTo(b.nombre, ignoreCase = true)
                    else b.nombre.compareTo(a.nombre, ignoreCase = true)
                }
            }
        }
    }

    LaunchedEffect(modoJuego) {
        val maxTotal = modoJuego.nJugadoresCampo
        while (jugadoresMiEquipo.size > maxTotal) {
            val nonUser = jugadoresMiEquipo.lastOrNull { it !in usuarioIds }
            if (nonUser != null) jugadoresMiEquipo.remove(nonUser)
            else jugadoresMiEquipo.removeAt(jugadoresMiEquipo.lastIndex)
        }
        while (jugadoresEquipoRival.size > maxTotal) {
            jugadoresEquipoRival.removeAt(jugadoresEquipoRival.lastIndex)
        }
    }

    var tabEquipoJugadores by remember { mutableStateOf(0) }
    var busquedaJugador by remember { mutableStateOf("") }
    var filtroSoloFavoritos by remember { mutableStateOf(false) }
    var filtroPosicion by remember { mutableStateOf<Posicion?>(null) }

    val sdfFecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val sdfHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
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
                // Selector de Fecha y Hora
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1.3f)
                            .clickable {
                                cal.timeInMillis = fechaMillis
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val newCal = Calendar.getInstance().apply {
                                            timeInMillis = fechaMillis
                                            set(Calendar.YEAR, year)
                                            set(Calendar.MONTH, month)
                                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        }
                                        fechaMillis = newCal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .background(DarkCard, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fecha", color = TextSecondary, fontSize = 11.5.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = sdfFecha.format(Date(fechaMillis)),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Event, contentDescription = null, tint = LimeVolt, modifier = Modifier.size(15.dp))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                cal.timeInMillis = fechaMillis
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        val newCal = Calendar.getInstance().apply {
                                            timeInMillis = fechaMillis
                                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            set(Calendar.MINUTE, minute)
                                        }
                                        fechaMillis = newCal.timeInMillis
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }
                            .background(DarkCard, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hora", color = TextSecondary, fontSize = 11.5.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = sdfHora.format(Date(fechaMillis)),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = LimeVolt, modifier = Modifier.size(15.dp))
                        }
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
                            if (fotoUri == null) {
                                Text(
                                    "Añadir foto del partido",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                    if (fotoUri != null) {
                        var mostrarZoomDialogo by remember { mutableStateOf(false) }
                        val fotoValida = File(fotoUri!!).exists()
                        if (mostrarZoomDialogo && fotoValida) {
                            DialogoVisorFotoConZoom(
                                fotoUri = fotoUri!!,
                                nombre = "Foto del partido",
                                onDismiss = { mostrarZoomDialogo = false }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (fotoValida) {
                                IconButton(onClick = { mostrarZoomDialogo = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = "Ver foto ampliada",
                                        tint = LimeVolt,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            IconButton(onClick = { fotoUri = null }) {
                                Icon(Icons.Default.Delete, contentDescription = "Quitar foto", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            }
                        }
                    } else {
                        IconButton(onClick = {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Elegir foto", tint = LimeVolt, modifier = Modifier.size(22.dp))
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

                // Clima del partido (debajo de duración y antes de elegir equipo)
                Column {
                    Text("Clima", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(Clima.entries.toTypedArray()) { c ->
                            val sel = clima == c
                            val horaPartido = Calendar.getInstance().apply { timeInMillis = fechaMillis }.get(Calendar.HOUR_OF_DAY)
                            val emoji = c.obtenerEmoji(horaPartido)
                            FilterChip(
                                selected = sel,
                                onClick = { clima = c },
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = sel,
                                    borderColor = LimeVolt.copy(alpha = 0.5f),
                                    selectedBorderColor = LimeVolt
                                ),
                                label = { Text("$emoji ${c.label}", fontSize = 12.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                        item {
                            val sel = clima == null
                            FilterChip(
                                selected = sel,
                                onClick = { clima = null },
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = sel,
                                    borderColor = LimeVolt.copy(alpha = 0.5f),
                                    selectedBorderColor = LimeVolt
                                ),
                                label = { Text("🏠 Techado", fontSize = 12.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                }

                // Ubicación (debajo de duración y clima)
                if (estadios.isNotEmpty()) {
                    Column {
                        Text("Ubicación", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Buscador de estadios por nombre
                        OutlinedTextField(
                            value = busquedaEstadio,
                            onValueChange = { busquedaEstadio = it },
                            placeholder = { Text("Buscar estadio...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            trailingIcon = {
                                if (busquedaEstadio.isNotEmpty()) {
                                    IconButton(onClick = { busquedaEstadio = "" }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LimeVolt,
                                unfocusedBorderColor = LimeVolt.copy(alpha = 0.5f),
                                cursorColor = LimeVolt
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Filtros rápidos de estadio: Favoritos, Modalidad, A-Z / Z-A, Fecha añadido
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                FilterChip(
                                    selected = filtroEstadioFavoritos,
                                    onClick = { filtroEstadioFavoritos = !filtroEstadioFavoritos },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = filtroEstadioFavoritos,
                                        borderColor = Color(0xFFFFD700).copy(alpha = 0.6f),
                                        selectedBorderColor = Color(0xFFFFD700)
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (filtroEstadioFavoritos) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFFFFD700)
                                        )
                                    },
                                    label = { Text("Favoritos", fontSize = 11.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filtroEstadioModalidad == null,
                                    onClick = { filtroEstadioModalidad = null },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = filtroEstadioModalidad == null,
                                        borderColor = LimeVolt.copy(alpha = 0.5f),
                                        selectedBorderColor = LimeVolt
                                    ),
                                    label = { Text("Todas modal.", fontSize = 11.sp) }
                                )
                            }
                            items(TipoFutbol.entries.toTypedArray()) { tipo ->
                                val sel = filtroEstadioModalidad == tipo
                                val tipoLabel = when (tipo) {
                                    TipoFutbol.FUTSAL -> "Futsal"
                                    TipoFutbol.FUT_6 -> "Fútbol 6"
                                    TipoFutbol.FUT_7 -> "Fútbol 7"
                                }
                                FilterChip(
                                    selected = sel,
                                    onClick = { filtroEstadioModalidad = if (sel) null else tipo },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = sel,
                                        borderColor = LimeVolt.copy(alpha = 0.5f),
                                        selectedBorderColor = LimeVolt
                                    ),
                                    label = { Text(tipoLabel, fontSize = 11.sp) }
                                )
                            }
                            item {
                                val activo = criterioOrdenEstadio == "ALFA"
                                FilterChip(
                                    selected = activo,
                                    onClick = {
                                        if (criterioOrdenEstadio == "ALFA") {
                                            ordenEstadioAZ = !ordenEstadioAZ
                                        } else {
                                            criterioOrdenEstadio = "ALFA"
                                        }
                                    },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = activo,
                                        borderColor = LimeVolt.copy(alpha = 0.5f),
                                        selectedBorderColor = LimeVolt
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.SortByAlpha,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (activo) LimeVolt else TextSecondary
                                        )
                                    },
                                    label = { Text(if (ordenEstadioAZ) "A-Z ↓" else "Z-A ↑", fontSize = 11.sp) }
                                )
                            }
                            item {
                                val activo = criterioOrdenEstadio == "FECHA"
                                FilterChip(
                                    selected = activo,
                                    onClick = {
                                        if (criterioOrdenEstadio == "FECHA") {
                                            ordenEstadioRecientes = !ordenEstadioRecientes
                                        } else {
                                            criterioOrdenEstadio = "FECHA"
                                        }
                                    },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = activo,
                                        borderColor = LimeVolt.copy(alpha = 0.5f),
                                        selectedBorderColor = LimeVolt
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.SwapVert,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (activo) LimeVolt else TextSecondary
                                        )
                                    },
                                    label = { Text(if (ordenEstadioRecientes) "Fecha añadido ↓" else "Fecha añadido ↑", fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                val sel = estadioId == null
                                FilterChip(
                                    selected = sel,
                                    onClick = { estadioId = null },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = sel,
                                        borderColor = LimeVolt.copy(alpha = 0.5f),
                                        selectedBorderColor = LimeVolt
                                    ),
                                    label = { Text("Sin especificar", fontSize = 11.sp) }
                                )
                            }
                            items(estadiosFiltrados) { est ->
                                val sel = estadioId == est.id
                                FilterChip(
                                    selected = sel,
                                    onClick = { estadioId = est.id },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = sel,
                                        borderColor = if (est.esFavorito) Color(0xFFFFD700).copy(alpha = 0.6f) else LimeVolt.copy(alpha = 0.5f),
                                        selectedBorderColor = if (est.esFavorito) Color(0xFFFFD700) else LimeVolt,
                                        borderWidth = if (est.esFavorito) 1.dp else 1.dp,
                                        selectedBorderWidth = if (est.esFavorito) 1.dp else 1.5.dp
                                    ),
                                    leadingIcon = if (est.esFavorito) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Favorito",
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    } else null,
                                    label = { Text("🏟️ ${est.nombre}", fontSize = 11.sp) }
                                )
                            }
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
                                    label = {
                                        Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.Center) {
                                            Text(pos.name, fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
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
                                        label = {
                                            Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.Center) {
                                                Text(pos.name, fontSize = 11.sp, textAlign = TextAlign.Center)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Mis estadísticas (Goles, asistencias, palos y paradas)
                    Column {
                        val yoJugoDePortero = posicionPrincipal == Posicion.POR || Posicion.POR in posicionesSecundarias
                        Text("Mis estadísticas", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (yoJugoDePortero) {
                            // 2x2 con Goles, Asistencias, Palos y Paradas
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
                                        label = "🅰️ Asistencias",
                                        value = misAsistencias,
                                        onValueChange = { misAsistencias = it.coerceAtLeast(0) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    StepperInput(
                                        label = "🎯 Palos",
                                        value = tirosAlPalo,
                                        onValueChange = { tirosAlPalo = it.coerceAtLeast(0) }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    StepperInput(
                                        label = "🧤 Paradas",
                                        value = misParadas,
                                        onValueChange = { misParadas = it.coerceAtLeast(0) }
                                    )
                                }
                            }
                        } else {
                            // Solo 3 estadísticas: Goles, Asistencias y Palos en 1 sola fila
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
                                        label = "🅰️ Asistencias",
                                        value = misAsistencias,
                                        onValueChange = { misAsistencias = it.coerceAtLeast(0) }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    StepperInput(
                                        label = "🎯 Palos",
                                        value = tirosAlPalo,
                                        onValueChange = { tirosAlPalo = it.coerceAtLeast(0) }
                                    )
                                }
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
                    val maxTotal = modoJuego.nJugadoresCampo
                    val countMiEquipo = jugadoresMiEquipo.count { id -> id in usuarioIds || jugadoresDisponibles.any { it.id == id } }
                    val countRival = jugadoresEquipoRival.count { id -> id in usuarioIds || jugadoresDisponibles.any { it.id == id } }
                    val tabTexto1 = if (jugadoPorMi) "Mi equipo (${equipoJugado.emoji}) ($countMiEquipo/$maxTotal)" else "⚪ Claro ($countMiEquipo/$maxTotal)"
                    val tabTexto2 = if (jugadoPorMi) "Equipo rival (${rivalCol.emoji}) ($countRival/$maxTotal)" else "⚫ Oscuro ($countRival/$maxTotal)"

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
                                onClick = {
                                    tabEquipoJugadores = 0
                                    jugadorSwapPizarraOrigen = null
                                },
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
                                onClick = {
                                    tabEquipoJugadores = 1
                                    jugadorSwapPizarraOrigen = null
                                },
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
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Gray,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                focusedContainerColor = DarkCard,
                                unfocusedContainerColor = DarkCard
                            ),
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(15.dp), tint = TextSecondary)
                                }
                            },
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
                                val esUsuarioChip = jugador.esUsuarioPropio || jugador.id in usuarioIds
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
                                        val actualId = if (esUsuarioChip) canonicalUserId else jugador.id
                                        if (tabEquipoJugadores == 0) {
                                            if (enMiEquipo) {
                                                if (!(jugadoPorMi && esUsuarioChip)) {
                                                    if (esUsuarioChip) jugadoresMiEquipo.removeAll { it in usuarioIds }
                                                    else jugadoresMiEquipo.remove(jugador.id)
                                                    detallesJugadores.remove(actualId)
                                                    rellenarConDefecto(jugadoresMiEquipo, esMiEquipo = true, maxTotal = maxTotal)
                                                }
                                            } else {
                                                val estabaEnRival = if (esUsuarioChip) jugadoresEquipoRival.any { it in usuarioIds } else jugadoresEquipoRival.contains(jugador.id)
                                                if (estabaEnRival) {
                                                    if (esUsuarioChip) jugadoresEquipoRival.removeAll { it in usuarioIds }
                                                    else jugadoresEquipoRival.remove(jugador.id)
                                                    detallesJugadores.remove(actualId)
                                                    rellenarConDefecto(jugadoresEquipoRival, esMiEquipo = false, maxTotal = maxTotal)
                                                }
                                                val idxDef = jugadoresMiEquipo.indexOfFirst { esIdDefecto(it) }
                                                if (idxDef != -1) {
                                                    val defId = jugadoresMiEquipo[idxDef]
                                                    detallesJugadores.remove(defId)
                                                    jugadoresMiEquipo[idxDef] = actualId
                                                } else if (jugadoresMiEquipo.size < maxTotal) {
                                                    jugadoresMiEquipo.add(actualId)
                                                }
                                                if (!detallesJugadores.containsKey(actualId)) {
                                                    detallesJugadores[actualId] = EstadisticasJugadorPartido(
                                                        jugadorId = actualId,
                                                        esMiEquipo = true,
                                                        posicionPrincipal = if (esUsuarioChip) posicionPrincipal else (jugador.posicionesPrimarias.firstOrNull() ?: Posicion.DC),
                                                        posicionesSecundarias = if (esUsuarioChip) posicionesSecundarias.toSet() else jugador.posicionesSecundarias.toSet(),
                                                        posX = 0.5f,
                                                        posY = 0.5f
                                                    )
                                                }
                                            }
                                        } else {
                                            if (enRival) {
                                                if (esUsuarioChip) jugadoresEquipoRival.removeAll { it in usuarioIds }
                                                else jugadoresEquipoRival.remove(jugador.id)
                                                detallesJugadores.remove(actualId)
                                                rellenarConDefecto(jugadoresEquipoRival, esMiEquipo = false, maxTotal = maxTotal)
                                            } else {
                                                if (esUsuarioChip && jugadoPorMi) {
                                                    // Usuario propio no puede ser rival si jugadoPorMi
                                                } else {
                                                    val estabaEnMi = if (esUsuarioChip) jugadoresMiEquipo.any { it in usuarioIds } else jugadoresMiEquipo.contains(jugador.id)
                                                    if (estabaEnMi) {
                                                        if (esUsuarioChip) jugadoresMiEquipo.removeAll { it in usuarioIds }
                                                        else jugadoresMiEquipo.remove(jugador.id)
                                                        detallesJugadores.remove(actualId)
                                                        rellenarConDefecto(jugadoresMiEquipo, esMiEquipo = true, maxTotal = maxTotal)
                                                    }
                                                    val idxDef = jugadoresEquipoRival.indexOfFirst { esIdDefecto(it) }
                                                    if (idxDef != -1) {
                                                        val defId = jugadoresEquipoRival[idxDef]
                                                        detallesJugadores.remove(defId)
                                                        jugadoresEquipoRival[idxDef] = actualId
                                                    } else if (jugadoresEquipoRival.size < maxTotal) {
                                                        jugadoresEquipoRival.add(actualId)
                                                    }
                                                    if (!detallesJugadores.containsKey(actualId)) {
                                                        detallesJugadores[actualId] = EstadisticasJugadorPartido(
                                                            jugadorId = actualId,
                                                            esMiEquipo = false,
                                                            posicionPrincipal = if (esUsuarioChip) posicionPrincipal else (jugador.posicionesPrimarias.firstOrNull() ?: Posicion.DC),
                                                            posicionesSecundarias = if (esUsuarioChip) posicionesSecundarias.toSet() else jugador.posicionesSecundarias.toSet(),
                                                            posX = 0.5f,
                                                            posY = 0.5f
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = seleccionadoActual, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier.size(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            JugadorAvatar(
                                                fotoUri = jugador.fotoUri,
                                                nombre = jugador.nombre,
                                                tamano = 22.dp,
                                                fontSize = 9.sp
                                            )
                                        }
                                    },
                                    label = { Text(jugador.nombreConTu(), fontSize = 11.sp) }
                                )
                            }
                        }

                        val equipoSeleccionadoIds = if (tabEquipoJugadores == 0) jugadoresMiEquipo else jugadoresEquipoRival
                        val jugadoresParaPizarra = remember(equipoSeleccionadoIds.toList(), jugadoresDisponibles, canonicalUserId) {
                            equipoSeleccionadoIds.map { jId ->
                                if (esIdDefecto(jId)) {
                                    Jugador(
                                        id = jId,
                                        nombre = "Jugador",
                                        esUsuarioPropio = false,
                                        esFavorito = false,
                                        posicionesPrimarias = emptySet(),
                                        posicionesSecundarias = emptySet()
                                    )
                                } else {
                                    jugadoresDisponibles.firstOrNull { it.id == jId || (jId in usuarioIds && (it.esUsuarioPropio || it.id in usuarioIds)) }
                                        ?: Jugador(
                                            id = jId,
                                            nombre = "Jugador",
                                            esUsuarioPropio = false,
                                            esFavorito = false
                                        )
                                }
                            }
                        }

                        val ejecutarSwapParaJugador = { jDestino: Jugador ->
                            if (jugadorSwapPizarraOrigen?.id == jDestino.id) {
                                jugadorSwapPizarraOrigen = null
                            } else if (jugadorSwapPizarraOrigen != null) {
                                val jOrigen = jugadorSwapPizarraOrigen!!
                                val equipoLista = if (tabEquipoJugadores == 0) jugadoresMiEquipo else jugadoresEquipoRival
                                val idx1 = equipoLista.indexOfFirst { it == jOrigen.id || (jOrigen.id in usuarioIds && it in usuarioIds) }
                                val idx2 = equipoLista.indexOfFirst { it == jDestino.id || (jDestino.id in usuarioIds && it in usuarioIds) }
                                val coordsActuales = obtenerCoordenadas(if (tabEquipoJugadores == 0) formacionMiEquipo else formacionRival)
                                if (idx1 != -1 && idx2 != -1) {
                                    val listCopy = equipoLista.toMutableList()
                                    val temp = listCopy[idx1]
                                    listCopy[idx1] = listCopy[idx2]
                                    listCopy[idx2] = temp
                                    equipoLista.clear()
                                    equipoLista.addAll(listCopy)
                                }

                                val id1 = if (jOrigen.id in usuarioIds) canonicalUserId else jOrigen.id
                                val id2 = if (jDestino.id in usuarioIds) canonicalUserId else jDestino.id
                                val pos1Fallback = coordsActuales.getOrNull(idx1)?.first ?: (jOrigen.posicionesPrimarias.firstOrNull() ?: Posicion.DC)
                                val pos2Fallback = coordsActuales.getOrNull(idx2)?.first ?: (jDestino.posicionesPrimarias.firstOrNull() ?: Posicion.DC)
                                val s1 = detallesJugadores[id1] ?: EstadisticasJugadorPartido(
                                    jugadorId = id1,
                                    esMiEquipo = tabEquipoJugadores == 0,
                                    posicionPrincipal = pos1Fallback,
                                    posicionesSecundarias = jOrigen.posicionesSecundarias.toSet(),
                                    posX = coordsActuales.getOrNull(idx1)?.second?.first ?: 0.5f,
                                    posY = coordsActuales.getOrNull(idx1)?.second?.second ?: 0.5f
                                )
                                val s2 = detallesJugadores[id2] ?: EstadisticasJugadorPartido(
                                    jugadorId = id2,
                                    esMiEquipo = tabEquipoJugadores == 0,
                                    posicionPrincipal = pos2Fallback,
                                    posicionesSecundarias = jDestino.posicionesSecundarias.toSet(),
                                    posX = coordsActuales.getOrNull(idx2)?.second?.first ?: 0.5f,
                                    posY = coordsActuales.getOrNull(idx2)?.second?.second ?: 0.5f
                                )
                                val posTemp = s1.posicionPrincipal
                                val xTemp = s1.posX
                                val yTemp = s1.posY
                                detallesJugadores[id1] = s1.copy(posicionPrincipal = s2.posicionPrincipal, posX = s2.posX, posY = s2.posY)
                                detallesJugadores[id2] = s2.copy(posicionPrincipal = posTemp, posX = xTemp, posY = yTemp)
                                if (jOrigen.esUsuarioPropio && jugadoPorMi) {
                                    posicionPrincipal = s2.posicionPrincipal
                                }
                                if (jDestino.esUsuarioPropio && jugadoPorMi) {
                                    posicionPrincipal = posTemp
                                }
                                jugadorSwapPizarraOrigen = null
                            } else {
                                jugadorSwapPizarraOrigen = jDestino
                            }
                        }

                        if (jugadoresParaPizarra.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Pizarra táctica (${if (tabEquipoJugadores == 0) "Mi equipo" else "Equipo rival"}):",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            PizarraColocacionPartido(
                                jugadores = jugadoresParaPizarra,
                                detallesJugadores = detallesJugadores,
                                nombreEquipo = if (tabEquipoJugadores == 0) "Mi equipo" else "Equipo rival",
                                emojiEquipo = if (tabEquipoJugadores == 0) equipoJugado.emoji else (if (equipoJugado == EquipoColor.CLARO) EquipoColor.OSCURO.emoji else EquipoColor.CLARO.emoji),
                                colorBordeFicha = if (tabEquipoJugadores == 0) {
                                    if (equipoJugado == EquipoColor.CLARO) Color.White else Color.Black
                                } else {
                                    if (equipoJugado == EquipoColor.CLARO) Color.Black else Color.White
                                },
                                formacionesDisponibles = formacionesDisponibles,
                                formacionSeleccionada = if (tabEquipoJugadores == 0) formacionMiEquipo else formacionRival,
                                onFormacionSeleccionada = { nuevaForm ->
                                    if (tabEquipoJugadores == 0) formacionMiEquipo = nuevaForm
                                    else formacionRival = nuevaForm
                                },
                                onMoverJugador = { _, _, _ -> },
                                onEditarStats = { j ->
                                    if (!esIdDefecto(j.id)) {
                                        jugadorParaEditarStats = j
                                    }
                                },
                                jugadorSwapOrigen = jugadorSwapPizarraOrigen,
                                onSwap = { j -> ejecutarSwapParaJugador(j) }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                val coordsActuales = obtenerCoordenadas(if (tabEquipoJugadores == 0) formacionMiEquipo else formacionRival)
                                jugadoresParaPizarra.forEachIndexed { idx, jObj ->
                                    val jId = jObj.id
                                    val esDefecto = esIdDefecto(jId)
                                    val posFallback = coordsActuales.getOrNull(idx)?.first ?: (jObj.posicionesPrimarias.firstOrNull() ?: Posicion.DC)
                                    val stats = detallesJugadores[jId] ?: (if (jId in usuarioIds) detallesJugadores[canonicalUserId] else null) ?: EstadisticasJugadorPartido(
                                        jugadorId = jId,
                                        esMiEquipo = tabEquipoJugadores == 0,
                                        posicionPrincipal = posFallback,
                                        posicionesSecundarias = jObj.posicionesSecundarias.toSet(),
                                        posX = coordsActuales.getOrNull(idx)?.second?.first ?: 0.5f,
                                        posY = coordsActuales.getOrNull(idx)?.second?.second ?: 0.5f
                                    )
                                    val esSeleccionadoSwap = jugadorSwapPizarraOrigen?.id == jObj.id
                                    val esFav = jObj.esFavorito

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (jugadorSwapPizarraOrigen != null) {
                                                    ejecutarSwapParaJugador(jObj)
                                                } else if (!esDefecto) {
                                                    jugadorParaEditarStats = jObj
                                                }
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (esSeleccionadoSwap) LimeVolt.copy(alpha = 0.18f) else DarkCard,
                                        border = BorderStroke(
                                            width = if (esSeleccionadoSwap) 1.5.dp else 1.dp,
                                            color = when {
                                                esSeleccionadoSwap -> LimeVolt
                                                esFav -> Color(0xFFFFD700)
                                                else -> Color.Gray.copy(alpha = 0.35f)
                                            }
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // Línea 1: Swap, Avatar, Badge Posición (tamaño uniforme 52x24), Nombre (weight 1f), Botón Editar (solo si no es por defecto)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                IconButton(
                                                    onClick = { ejecutarSwapParaJugador(jObj) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (esSeleccionadoSwap) Icons.Default.CheckCircle else Icons.Default.SwapVert,
                                                        contentDescription = "Intercambiar",
                                                        tint = if (esSeleccionadoSwap) LimeVolt else TextSecondary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                val colorBordeEquipo = if (tabEquipoJugadores == 0) {
                                                    if (equipoJugado == EquipoColor.CLARO) Color.White else Color.Black
                                                } else {
                                                    if (equipoJugado == EquipoColor.CLARO) Color.Black else Color.White
                                                }

                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    if (esFav) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(27.dp)
                                                                .border(1.dp, Color(0xFFFFD700), CircleShape)
                                                        )
                                                    }
                                                    JugadorAvatar(
                                                        fotoUri = jObj.fotoUri,
                                                        nombre = jObj.nombre,
                                                        tamano = 24.dp,
                                                        fontSize = 9.sp,
                                                        bordeColor = colorBordeEquipo,
                                                        bordeAncho = 1.dp,
                                                        esPorDefecto = esDefecto
                                                    )
                                                }

                                                var menuPosicionExpanded by remember { mutableStateOf(false) }
                                                Box {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = LimeVolt.copy(alpha = 0.18f),
                                                        border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.5f)),
                                                        modifier = Modifier
                                                            .width(52.dp)
                                                            .height(24.dp)
                                                            .clickable { menuPosicionExpanded = true }
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Text(
                                                                text = stats.posicionPrincipal.name,
                                                                color = LimeVolt,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                                            )
                                                            Icon(
                                                                imageVector = Icons.Default.ArrowDropDown,
                                                                contentDescription = "Elegir posición",
                                                                tint = LimeVolt,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                        }
                                                    }

                                                    DropdownMenu(
                                                        expanded = menuPosicionExpanded,
                                                        onDismissRequest = { menuPosicionExpanded = false },
                                                        modifier = Modifier.background(DarkCard)
                                                    ) {
                                                        Posicion.entries.forEach { pos ->
                                                            DropdownMenuItem(
                                                                text = {
                                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                                        BadgePosicion(label = pos.name, esPrimaria = pos == stats.posicionPrincipal)
                                                                        Spacer(modifier = Modifier.width(8.dp))
                                                                        Text(
                                                                            text = pos.nombreCompleto,
                                                                            color = if (pos == stats.posicionPrincipal) LimeVolt else Color.White,
                                                                            fontSize = 12.sp,
                                                                            fontWeight = if (pos == stats.posicionPrincipal) FontWeight.Bold else FontWeight.Normal
                                                                        )
                                                                    }
                                                                },
                                                                onClick = {
                                                                    val idActual = if (jId in usuarioIds) canonicalUserId else jId
                                                                    val cleanSec = stats.posicionesSecundarias - pos
                                                                    detallesJugadores[idActual] = stats.copy(posicionPrincipal = pos, posicionesSecundarias = cleanSec)
                                                                    if (jObj.esUsuarioPropio && jugadoPorMi) {
                                                                        posicionPrincipal = pos
                                                                        posicionesSecundarias.clear()
                                                                        posicionesSecundarias.addAll(cleanSec)
                                                                    }
                                                                    menuPosicionExpanded = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = if (esDefecto) "Jugador" else jObj.nombreConTu(),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                if (!esDefecto) {
                                                    IconButton(
                                                        onClick = { jugadorParaEditarStats = jObj },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Editar stats",
                                                            tint = LimeVolt,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            // Línea 2: Selector interactivo dropdown de posiciones secundarias (izq) y Estadísticas si aplica (der)
                                            val secList = stats.posicionesSecundarias.filter { it != stats.posicionPrincipal }
                                            val tieneStats = !esDefecto && (stats.goles > 0 || stats.asistencias > 0 || stats.tirosAlPalo > 0 ||
                                                    (stats.paradas > 0 && (stats.posicionPrincipal == Posicion.POR || stats.posicionesSecundarias.contains(Posicion.POR))))

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 30.dp, end = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                var menuSecundariasExpanded by remember { mutableStateOf(false) }
                                                Box {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color.White.copy(alpha = 0.08f),
                                                        border = BorderStroke(0.8.dp, Color.Gray.copy(alpha = 0.4f)),
                                                        modifier = Modifier.clickable { menuSecundariasExpanded = true }
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Text(
                                                                text = "Sec:",
                                                                fontSize = 9.sp,
                                                                color = TextSecondary,
                                                                fontWeight = FontWeight.SemiBold,
                                                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                                            )
                                                            if (secList.isNotEmpty()) {
                                                                BadgePosicion(label = secList.first().name, esPrimaria = false)
                                                                if (secList.size > 1) {
                                                                    Text(
                                                                        text = "+${secList.size - 1}",
                                                                        fontSize = 9.sp,
                                                                        color = LimeVolt,
                                                                        fontWeight = FontWeight.Bold,
                                                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                                                    )
                                                                }
                                                            } else {
                                                                Text(
                                                                    text = "Añadir +",
                                                                    fontSize = 9.sp,
                                                                    color = LimeVolt,
                                                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                                                )
                                                            }
                                                            Icon(
                                                                imageVector = Icons.Default.ArrowDropDown,
                                                                contentDescription = "Elegir posiciones secundarias",
                                                                tint = TextSecondary,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                        }
                                                    }

                                                    DropdownMenu(
                                                        expanded = menuSecundariasExpanded,
                                                        onDismissRequest = { menuSecundariasExpanded = false },
                                                        modifier = Modifier.background(DarkCard)
                                                    ) {
                                                        Text(
                                                            text = "Posiciones secundarias",
                                                            color = LimeVolt,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                        )
                                                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                                                        Posicion.entries.filter { it != stats.posicionPrincipal }.forEach { pos ->
                                                            val estaSeleccionada = stats.posicionesSecundarias.contains(pos)
                                                            DropdownMenuItem(
                                                                text = {
                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                    ) {
                                                                        Checkbox(
                                                                            checked = estaSeleccionada,
                                                                            onCheckedChange = null,
                                                                            colors = CheckboxDefaults.colors(
                                                                                checkedColor = LimeVolt,
                                                                                checkmarkColor = Color.Black
                                                                            ),
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                        BadgePosicion(label = pos.name, esPrimaria = false)
                                                                        Text(
                                                                            text = pos.nombreCompleto,
                                                                            color = if (estaSeleccionada) LimeVolt else Color.White,
                                                                            fontSize = 12.sp,
                                                                            fontWeight = if (estaSeleccionada) FontWeight.Bold else FontWeight.Normal
                                                                        )
                                                                    }
                                                                },
                                                                onClick = {
                                                                    val idActual = if (jId in usuarioIds) canonicalUserId else jId
                                                                    val nuevasSec = if (estaSeleccionada) {
                                                                        stats.posicionesSecundarias - pos
                                                                    } else {
                                                                        stats.posicionesSecundarias + pos
                                                                    }
                                                                    detallesJugadores[idActual] = stats.copy(posicionesSecundarias = nuevasSec)
                                                                    if (jObj.esUsuarioPropio && jugadoPorMi) {
                                                                        posicionesSecundarias.clear()
                                                                        posicionesSecundarias.addAll(nuevasSec)
                                                                    }
                                                                }
                                                            )
                                                        }
                                                    }
                                                }

                                                if (!esDefecto) {
                                                    val tieneStatsRegistradas = stats.statsRegistradas || (jObj.esUsuarioPropio && jugadoPorMi)
                                                    if (tieneStatsRegistradas) {
                                                        if (tieneStats) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                            ) {
                                                                if (stats.goles > 0) {
                                                                    Text("⚽${stats.goles}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                                }
                                                                if (stats.asistencias > 0) {
                                                                    Text("🅰️${stats.asistencias}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                                }
                                                                if (stats.tirosAlPalo > 0) {
                                                                    Text("🎯${stats.tirosAlPalo}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                                }
                                                                val jugoDePortero = stats.posicionPrincipal == Posicion.POR || stats.posicionesSecundarias.contains(Posicion.POR)
                                                                if (stats.paradas > 0 && jugoDePortero) {
                                                                    Text("🧤${stats.paradas}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                                }
                                                            }
                                                        } else {
                                                            Text(
                                                                text = "-",
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = TextSecondary,
                                                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                                            )
                                                        }
                                                    } else {
                                                        Text(
                                                            text = "Sin stats registradas",
                                                            fontSize = 9.sp,
                                                            color = TextSecondary.copy(alpha = 0.45f),
                                                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Banner de intercambio DEBAJO del listado
                            if (jugadorSwapPizarraOrigen != null) {
                                Surface(
                                    color = LimeVolt.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.SwapVert,
                                            contentDescription = null,
                                            tint = LimeVolt,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Intercambiando a ${jugadorSwapPizarraOrigen?.nombreConTu()}",
                                            color = LimeVolt,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
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

                    val listaDetallesFinal = mutableListOf<EstadisticasJugadorPartido>()
                    val userActualId = canonicalUserId

                    val yoJugoDePortero = posicionPrincipal == Posicion.POR || Posicion.POR in posicionesSecundarias

                    val coords1 = obtenerCoordenadas(formacionMiEquipo)
                    val coords2 = obtenerCoordenadas(formacionRival)

                    equipo1Saneado.forEachIndexed { idx, jId ->
                        val esDefecto = esIdDefecto(jId)
                        val esUsuario = jId in usuarioIds
                        val posObj = if (esDefecto) null else jugadoresDisponibles.firstOrNull { it.id == jId || (esUsuario && (it.esUsuarioPropio || it.id in usuarioIds)) }
                        val detActual = detallesJugadores[jId] ?: (if (esUsuario) detallesJugadores[canonicalUserId] else null)
                        val slotPos = coords1.getOrNull(idx)?.first ?: Posicion.DC
                        val slotCoord = coords1.getOrNull(idx)?.second ?: (0.5f to 0.5f)

                        if (esDefecto) {
                            listaDetallesFinal.add(
                                EstadisticasJugadorPartido(
                                    jugadorId = jId,
                                    esMiEquipo = true,
                                    posicionPrincipal = detActual?.posicionPrincipal ?: slotPos,
                                    posicionesSecundarias = detActual?.posicionesSecundarias ?: emptySet(),
                                    statsRegistradas = false,
                                    posX = detActual?.posX ?: slotCoord.first,
                                    posY = detActual?.posY ?: slotCoord.second
                                )
                            )
                        } else if (esUsuario && jugadoPorMi) {
                            listaDetallesFinal.add(
                                EstadisticasJugadorPartido(
                                    jugadorId = userActualId,
                                    esMiEquipo = true,
                                    posicionPrincipal = posicionPrincipal,
                                    posicionesSecundarias = posicionesSecundarias.toSet(),
                                    statsRegistradas = true,
                                    posX = detActual?.posX ?: slotCoord.first,
                                    posY = detActual?.posY ?: slotCoord.second,
                                    goles = misGoles,
                                    asistencias = misAsistencias,
                                    tirosAlPalo = tirosAlPalo,
                                    paradas = if (yoJugoDePortero) misParadas else 0,
                                    golesZurda = golesZurda,
                                    golesDiestra = golesDiestra,
                                    golesCabeza = golesCabeza,
                                    golesOtro = golesOtro,
                                    golesChilena = golesChilena,
                                    golesTacon = golesTacon,
                                    golesFueraArea = golesFueraArea
                                )
                            )
                        } else {
                            val jugoPort = detActual?.let { it.posicionPrincipal == Posicion.POR || it.posicionesSecundarias.contains(Posicion.POR) } ?: false
                            listaDetallesFinal.add(
                                detActual?.copy(
                                    esMiEquipo = true,
                                    paradas = if (jugoPort) detActual.paradas else 0
                                ) ?: EstadisticasJugadorPartido(
                                    jugadorId = jId,
                                    esMiEquipo = true,
                                    posicionPrincipal = posObj?.posicionesPrimarias?.firstOrNull() ?: slotPos,
                                    posicionesSecundarias = posObj?.posicionesSecundarias?.toSet() ?: emptySet(),
                                    posX = slotCoord.first,
                                    posY = slotCoord.second
                                )
                            )
                        }
                    }

                    equipo2Saneado.forEachIndexed { idx, jId ->
                        val esDefecto = esIdDefecto(jId)
                        val esUsuario = jId in usuarioIds
                        val posObj = if (esDefecto) null else jugadoresDisponibles.firstOrNull { it.id == jId || (esUsuario && (it.esUsuarioPropio || it.id in usuarioIds)) }
                        val detActual = detallesJugadores[jId] ?: (if (esUsuario) detallesJugadores[canonicalUserId] else null)
                        val slotPos = coords2.getOrNull(idx)?.first ?: Posicion.DC
                        val slotCoord = coords2.getOrNull(idx)?.second ?: (0.5f to 0.5f)

                        if (esDefecto) {
                            listaDetallesFinal.add(
                                EstadisticasJugadorPartido(
                                    jugadorId = jId,
                                    esMiEquipo = false,
                                    posicionPrincipal = detActual?.posicionPrincipal ?: slotPos,
                                    posicionesSecundarias = detActual?.posicionesSecundarias ?: emptySet(),
                                    statsRegistradas = false,
                                    posX = detActual?.posX ?: slotCoord.first,
                                    posY = detActual?.posY ?: slotCoord.second
                                )
                            )
                        } else if (esUsuario && jugadoPorMi) {
                            listaDetallesFinal.add(
                                EstadisticasJugadorPartido(
                                    jugadorId = userActualId,
                                    esMiEquipo = false,
                                    posicionPrincipal = posicionPrincipal,
                                    posicionesSecundarias = posicionesSecundarias.toSet(),
                                    statsRegistradas = true,
                                    posX = detActual?.posX ?: slotCoord.first,
                                    posY = detActual?.posY ?: slotCoord.second,
                                    goles = misGoles,
                                    asistencias = misAsistencias,
                                    tirosAlPalo = tirosAlPalo,
                                    paradas = if (yoJugoDePortero) misParadas else 0,
                                    golesZurda = golesZurda,
                                    golesDiestra = golesDiestra,
                                    golesCabeza = golesCabeza,
                                    golesOtro = golesOtro,
                                    golesChilena = golesChilena,
                                    golesTacon = golesTacon,
                                    golesFueraArea = golesFueraArea
                                )
                            )
                        } else {
                            val jugoPort = detActual?.let { it.posicionPrincipal == Posicion.POR || it.posicionesSecundarias.contains(Posicion.POR) } ?: false
                            listaDetallesFinal.add(
                                detActual?.copy(
                                    esMiEquipo = false,
                                    paradas = if (jugoPort) detActual.paradas else 0
                                ) ?: EstadisticasJugadorPartido(
                                    jugadorId = jId,
                                    esMiEquipo = false,
                                    posicionPrincipal = posObj?.posicionesPrimarias?.firstOrNull() ?: slotPos,
                                    posicionesSecundarias = posObj?.posicionesSecundarias?.toSet() ?: emptySet(),
                                    posX = slotCoord.first,
                                    posY = slotCoord.second
                                )
                            )
                        }
                    }

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
                        paradas = if (jugadoPorMi && yoJugoDePortero) misParadas else 0,
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
                        duracionMinutos = duracionMinutos,
                        formacionMiEquipo = formacionMiEquipo.id,
                        formacionRival = formacionRival.id,
                        jugadoresDetalle = listaDetallesFinal
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

    jugadorParaEditarStats?.let { jEdit ->
        if (!esIdDefecto(jEdit.id)) {
            val esMiEquipoDelJugador = jEdit.id in jugadoresMiEquipo || (jEdit.esUsuarioPropio && jugadoresMiEquipo.any { it in usuarioIds })
        val statsActuales = detallesJugadores[jEdit.id] ?: EstadisticasJugadorPartido(
            jugadorId = jEdit.id,
            esMiEquipo = esMiEquipoDelJugador,
            posicionPrincipal = if (jEdit.esUsuarioPropio && jugadoPorMi) posicionPrincipal else (jEdit.posicionesPrimarias.firstOrNull() ?: Posicion.DC),
            posicionesSecundarias = if (jEdit.esUsuarioPropio && jugadoPorMi) posicionesSecundarias.toSet() else jEdit.posicionesSecundarias.toSet(),
            goles = if (jEdit.esUsuarioPropio && jugadoPorMi) misGoles else 0,
            asistencias = if (jEdit.esUsuarioPropio && jugadoPorMi) misAsistencias else 0,
            tirosAlPalo = if (jEdit.esUsuarioPropio && jugadoPorMi) tirosAlPalo else 0,
            paradas = if (jEdit.esUsuarioPropio && jugadoPorMi) (if (posicionPrincipal == Posicion.POR || Posicion.POR in posicionesSecundarias) misParadas else 0) else 0,
            golesZurda = if (jEdit.esUsuarioPropio && jugadoPorMi) golesZurda else 0,
            golesDiestra = if (jEdit.esUsuarioPropio && jugadoPorMi) golesDiestra else 0,
            golesCabeza = if (jEdit.esUsuarioPropio && jugadoPorMi) golesCabeza else 0,
            golesOtro = if (jEdit.esUsuarioPropio && jugadoPorMi) golesOtro else 0,
            golesChilena = if (jEdit.esUsuarioPropio && jugadoPorMi) golesChilena else 0,
            golesTacon = if (jEdit.esUsuarioPropio && jugadoPorMi) golesTacon else 0,
            golesFueraArea = if (jEdit.esUsuarioPropio && jugadoPorMi) golesFueraArea else 0
        )

        DialogoEditarStatsJugadorPartido(
            jugador = jEdit,
            statsActuales = statsActuales,
            esMiEquipo = esMiEquipoDelJugador,
            onDismiss = { jugadorParaEditarStats = null },
            onGuardar = { nuevasStats ->
                detallesJugadores[jEdit.id] = nuevasStats
                if (jEdit.esUsuarioPropio || jEdit.id in usuarioIds || (usuario != null && jEdit.id == usuario.id)) {
                    posicionPrincipal = nuevasStats.posicionPrincipal
                    posicionesSecundarias.clear()
                    posicionesSecundarias.addAll(nuevasStats.posicionesSecundarias)
                    misGoles = nuevasStats.goles
                    misAsistencias = nuevasStats.asistencias
                    tirosAlPalo = nuevasStats.tirosAlPalo
                    val jugoPort = nuevasStats.posicionPrincipal == Posicion.POR || Posicion.POR in nuevasStats.posicionesSecundarias
                    misParadas = if (jugoPort) nuevasStats.paradas else 0
                    golesZurda = nuevasStats.golesZurda
                    golesDiestra = nuevasStats.golesDiestra
                    golesCabeza = nuevasStats.golesCabeza
                    golesOtro = nuevasStats.golesOtro
                    golesChilena = nuevasStats.golesChilena
                    golesTacon = nuevasStats.golesTacon
                    golesFueraArea = nuevasStats.golesFueraArea
                }
                jugadorParaEditarStats = null
            }
        )
        }
    }
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
