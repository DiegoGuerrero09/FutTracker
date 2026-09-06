package com.diegoguerrero.futtracker.ui.screens.estadisticas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diegoguerrero.futtracker.domain.model.Clima
import com.diegoguerrero.futtracker.domain.model.Estadio
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.domain.model.TipoFutbol
import com.diegoguerrero.futtracker.domain.model.nombreConTu
import com.diegoguerrero.futtracker.ui.components.*
import com.diegoguerrero.futtracker.ui.theme.BlueCompanero
import com.diegoguerrero.futtracker.ui.theme.DarkBackground
import com.diegoguerrero.futtracker.ui.theme.DarkCard
import com.diegoguerrero.futtracker.ui.theme.DarkCardBorder
import com.diegoguerrero.futtracker.ui.theme.GreenWin
import com.diegoguerrero.futtracker.ui.theme.LimeVolt
import com.diegoguerrero.futtracker.ui.theme.OrangeDraw
import com.diegoguerrero.futtracker.ui.theme.RedLoss
import com.diegoguerrero.futtracker.ui.theme.TextSecondary
import com.diegoguerrero.futtracker.ui.theme.obtenerColorPorcentaje
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    viewModel: EstadisticasViewModel = hiltViewModel()
) {
    var pestanaSeleccionada by remember { mutableIntStateOf(0) } // 0: Individual, 1: Partidos

    val filtroModo by viewModel.filtroModoJuego.collectAsState()
    val filtroTiempo by viewModel.filtroTiempo.collectAsState()
    val temporadaSeleccionada by viewModel.temporadaSeleccionada.collectAsState()
    val temporadasConDatos by viewModel.temporadasConDatos.collectAsState()
    val anioSeleccionado by viewModel.anioSeleccionado.collectAsState()
    val aniosConDatos by viewModel.aniosConDatos.collectAsState()
    val fechaInicio by viewModel.fechaInicio.collectAsState()
    val fechaFin by viewModel.fechaFin.collectAsState()

    val partidosFiltrados by viewModel.partidosFiltrados.collectAsState()
    val resumen by viewModel.resumen.collectAsState()

    // Estados pestaña Partidos
    val partidosTabPartidos by viewModel.partidosTabPartidos.collectAsState()
    val statsClima by viewModel.statsClima.collectAsState()
    val statsEstadios by viewModel.statsEstadios.collectAsState()
    val statsDiasSemana by viewModel.statsDiasSemana.collectAsState()
    val statsHorasPartidos by viewModel.statsHorasPartidos.collectAsState()
    val filtroClimas by viewModel.filtroClimas.collectAsState()
    val filtroTechado by viewModel.filtroTechado.collectAsState()
    val filtroEstadios by viewModel.filtroEstadios.collectAsState()
    val filtroDiasSemana by viewModel.filtroDiasSemana.collectAsState()
    val filtroFranjasHorarias by viewModel.filtroFranjasHorarias.collectAsState()
    val estadiosDisponiblesFiltro by viewModel.estadiosDisponiblesFiltro.collectAsState()
    val todosEstadios by viewModel.todosEstadios.collectAsState()
    val hayFiltrosPartidosActivos by viewModel.hayFiltrosPartidosActivos.collectAsState()

    // Estados Individual
    val statsClaroOscuro by viewModel.statsClaroOscuro.collectAsState()
    val statsPosicionesFrecuencia by viewModel.statsPosicionesFrecuencia.collectAsState()
    val todosJugadores by viewModel.todosJugadores.collectAsState()
    val jugadorInspeccionadoId by viewModel.jugadorInspeccionadoId.collectAsState()

    var busquedaJugador by remember { mutableStateOf("") }
    var filtroSoloFavoritos by remember { mutableStateOf(false) }
    var filtroOrdenNombre by remember { mutableStateOf<Boolean?>(null) }
    var filtroOrdenFecha by remember { mutableStateOf<Boolean?>(null) }
    var filtroPosicion by remember { mutableStateOf<Posicion?>(null) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(DarkCard)) {
                TopAppBar(
                    title = { Text("Estadísticas", fontWeight = FontWeight.Bold, color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCard)
                )
                TabRow(
                    selectedTabIndex = pestanaSeleccionada,
                    containerColor = DarkCard,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[pestanaSeleccionada]),
                            color = LimeVolt
                        )
                    }
                ) {
                    Tab(
                        selected = pestanaSeleccionada == 0,
                        onClick = { pestanaSeleccionada = 0 },
                        text = {
                            Text(
                                "Individual",
                                fontWeight = if (pestanaSeleccionada == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (pestanaSeleccionada == 0) LimeVolt else TextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = pestanaSeleccionada == 1,
                        onClick = { pestanaSeleccionada = 1 },
                        text = {
                            Text(
                                "Partidos",
                                fontWeight = if (pestanaSeleccionada == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (pestanaSeleccionada == 1) LimeVolt else TextSecondary
                            )
                        }
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        if (pestanaSeleccionada == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Selector de jugador a analizar (Individual)
            item {
                val miJugador = remember(todosJugadores) { todosJugadores.firstOrNull { it.esUsuarioPropio } }
                val otrosJugadoresFiltrados = remember(
                    todosJugadores, busquedaJugador, filtroSoloFavoritos, filtroOrdenNombre, filtroOrdenFecha, filtroPosicion
                ) {
                    todosJugadores.filter { !it.esUsuarioPropio }
                        .filter { jug ->
                            if (busquedaJugador.isNotBlank() && !jug.nombre.contains(busquedaJugador, ignoreCase = true)) {
                                false
                            } else if (filtroSoloFavoritos && !jug.esFavorito) {
                                false
                            } else if (filtroPosicion != null && !jug.posicionesPrimarias.contains(filtroPosicion) && !jug.posicionesSecundarias.contains(filtroPosicion)) {
                                false
                            } else {
                                true
                            }
                        }
                        .let { lista ->
                            when {
                                filtroOrdenNombre == true -> lista.sortedBy { it.nombre.lowercase() }
                                filtroOrdenNombre == false -> lista.sortedByDescending { it.nombre.lowercase() }
                                filtroOrdenFecha == true -> lista.sortedByDescending { it.fechaCreacion }
                                filtroOrdenFecha == false -> lista.sortedBy { it.fechaCreacion }
                                else -> lista
                            }
                        }
                }
                val mostrarMiJugador = remember(miJugador, busquedaJugador, filtroSoloFavoritos, filtroPosicion) {
                    if (miJugador == null) true
                    else {
                        val cumpleBusqueda = busquedaJugador.isBlank() || miJugador.nombre.contains(busquedaJugador, ignoreCase = true)
                        val cumpleFav = !filtroSoloFavoritos || miJugador.esFavorito
                        val cumplePos = filtroPosicion == null || miJugador.posicionesPrimarias.contains(filtroPosicion) || miJugador.posicionesSecundarias.contains(filtroPosicion)
                        cumpleBusqueda && cumpleFav && cumplePos
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Jugador a analizar:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Buscador de jugador
                    OutlinedTextField(
                        value = busquedaJugador,
                        onValueChange = { busquedaJugador = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar jugador...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar", modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (busquedaJugador.isNotEmpty()) {
                                IconButton(onClick = { busquedaJugador = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LimeVolt,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard
                        )
                    )

                    // Filtros para el selector de jugador
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            FilterChip(
                                selected = filtroSoloFavoritos,
                                onClick = { filtroSoloFavoritos = !filtroSoloFavoritos },
                                label = { Text("Favoritos", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (filtroSoloFavoritos) Icons.Default.Star else Icons.Outlined.StarOutline,
                                        contentDescription = null,
                                        tint = if (filtroSoloFavoritos) Color(0xFFFFD700) else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = filtroSoloFavoritos,
                                    borderColor = Color.White.copy(alpha = 0.2f),
                                    selectedBorderColor = LimeVolt
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = DarkCard,
                                    selectedContainerColor = Color(0xFF222634),
                                    selectedLabelColor = LimeVolt
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = filtroOrdenNombre != null,
                                onClick = {
                                    filtroOrdenFecha = null
                                    filtroOrdenNombre = when (filtroOrdenNombre) {
                                        null -> true
                                        true -> false
                                        false -> null
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.SortByAlpha,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = when (filtroOrdenNombre) {
                                            true -> "Nombre: A-Z"
                                            false -> "Nombre: Z-A"
                                            null -> "Nombre"
                                        },
                                        fontSize = 11.sp
                                    )
                                },
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = filtroOrdenNombre != null,
                                    borderColor = Color.White.copy(alpha = 0.2f),
                                    selectedBorderColor = LimeVolt
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = DarkCard,
                                    selectedContainerColor = Color(0xFF222634),
                                    selectedLabelColor = LimeVolt,
                                    selectedLeadingIconColor = LimeVolt
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = filtroOrdenFecha != null,
                                onClick = {
                                    filtroOrdenNombre = null
                                    filtroOrdenFecha = when (filtroOrdenFecha) {
                                        null -> true
                                        true -> false
                                        false -> null
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = when (filtroOrdenFecha) {
                                            true -> "Recientes"
                                            false -> "Más antiguos"
                                            null -> "Fecha añadido"
                                        },
                                        fontSize = 11.sp
                                    )
                                },
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = filtroOrdenFecha != null,
                                    borderColor = Color.White.copy(alpha = 0.2f),
                                    selectedBorderColor = LimeVolt
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = DarkCard,
                                    selectedContainerColor = Color(0xFF222634),
                                    selectedLabelColor = LimeVolt,
                                    selectedLeadingIconColor = LimeVolt
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = filtroPosicion == null,
                                onClick = { filtroPosicion = null },
                                label = { Text("Todas pos.", fontSize = 11.sp) },
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = filtroPosicion == null,
                                    borderColor = Color.White.copy(alpha = 0.2f),
                                    selectedBorderColor = LimeVolt
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = DarkCard,
                                    selectedContainerColor = Color(0xFF222634),
                                    selectedLabelColor = LimeVolt
                                )
                            )
                        }
                        items(Posicion.entries.toTypedArray()) { pos ->
                            val sel = filtroPosicion == pos
                            FilterChip(
                                selected = sel,
                                onClick = {
                                    filtroPosicion = if (filtroPosicion == pos) null else pos
                                },
                                label = { Text(pos.name, fontSize = 11.sp) },
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = sel,
                                    borderColor = Color.White.copy(alpha = 0.2f),
                                    selectedBorderColor = LimeVolt
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = DarkCard,
                                    selectedContainerColor = Color(0xFF222634),
                                    selectedLabelColor = LimeVolt
                                )
                            )
                        }
                    }

                    // Carrusel de jugadores
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (mostrarMiJugador) {
                            item {
                                val esMiPerfil = jugadorInspeccionadoId == null
                                val miNombre = miJugador?.nombre?.ifBlank { "Mi perfil" } ?: "Mi perfil"
                                FilterChip(
                                    selected = esMiPerfil,
                                    onClick = { viewModel.seleccionarJugadorInspeccionado(null) },
                                    leadingIcon = {
                                        JugadorAvatar(
                                            fotoUri = miJugador?.fotoUri,
                                            nombre = miNombre,
                                            tamano = 20.dp,
                                            fontSize = 9.sp
                                        )
                                    },
                                    trailingIcon = if (miJugador?.esFavorito == true) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Favorito",
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null,
                                    label = { Text(miNombre, fontSize = 11.sp) },
                                    border = if (miJugador?.esFavorito == true) FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = esMiPerfil,
                                        borderColor = Color(0xFFFFD700).copy(alpha = 0.7f),
                                        selectedBorderColor = Color(0xFFFFD700),
                                        borderWidth = 0.8.dp,
                                        selectedBorderWidth = 1.dp
                                    ) else FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = esMiPerfil,
                                        borderColor = Color.White.copy(alpha = 0.2f),
                                        selectedBorderColor = LimeVolt
                                    ),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = DarkCard,
                                        selectedContainerColor = Color(0xFF222634),
                                        selectedLabelColor = LimeVolt
                                    )
                                )
                            }
                        }

                        items(otrosJugadoresFiltrados) { jug ->
                            val seleccionado = jugadorInspeccionadoId == jug.id
                            FilterChip(
                                selected = seleccionado,
                                onClick = { viewModel.seleccionarJugadorInspeccionado(jug.id) },
                                leadingIcon = {
                                    JugadorAvatar(
                                        fotoUri = jug.fotoUri,
                                        nombre = jug.nombre,
                                        tamano = 20.dp,
                                        fontSize = 9.sp
                                    )
                                },
                                trailingIcon = if (jug.esFavorito) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Favorito",
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                label = { Text(jug.nombre, fontSize = 11.sp) },
                                border = if (jug.esFavorito) FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = seleccionado,
                                    borderColor = Color(0xFFFFD700).copy(alpha = 0.7f),
                                    selectedBorderColor = Color(0xFFFFD700),
                                    borderWidth = 0.8.dp,
                                    selectedBorderWidth = 1.dp
                                ) else FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = seleccionado,
                                    borderColor = Color.White.copy(alpha = 0.2f),
                                    selectedBorderColor = LimeVolt
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = DarkCard,
                                    selectedContainerColor = Color(0xFF222634),
                                    selectedLabelColor = LimeVolt
                                )
                            )
                        }

                        if (!mostrarMiJugador && otrosJugadoresFiltrados.isEmpty()) {
                            item {
                                Text(
                                    text = "No se encontraron jugadores",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Filtro por modalidad de juego (Total por defecto)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Modalidad de juego:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val modos = listOf(
                            null to "Total",
                            TipoFutbol.FUTSAL to "Futsal",
                            TipoFutbol.FUT_6 to "Fútbol 6",
                            TipoFutbol.FUT_7 to "Fútbol 7"
                        )
                        modos.forEach { (modo, label) ->
                            val sel = filtroModo == modo
                            FilterChip(
                                selected = sel,
                                onClick = { viewModel.setFiltroModoJuego(modo) },
                                label = {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(label, fontSize = 11.sp, textAlign = TextAlign.Center)
                                    }
                                },
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = sel,
                                    borderColor = Color.White.copy(alpha = 0.2f),
                                    selectedBorderColor = LimeVolt
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = DarkCard,
                                    selectedContainerColor = Color(0xFF222634),
                                    selectedLabelColor = LimeVolt
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Filtro temporal (Total, Temporada, Año, Por fecha)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Periodo de tiempo:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        val filtros = listOf(
                            TipoFiltroEstadisticas.TOTAL to "Total",
                            TipoFiltroEstadisticas.TEMPORADA to "Temporada",
                            TipoFiltroEstadisticas.ANIO_NATURAL to "Año",
                            TipoFiltroEstadisticas.FECHA_PERSONALIZADA to "Por fecha",
                            TipoFiltroEstadisticas.ULTIMOS_3_MESES to "Últimos meses",
                            TipoFiltroEstadisticas.ULTIMAS_4_SEMANAS to "Últimas semanas"
                        )
                        items(filtros) { (tipo, label) ->
                            val sel = filtroTiempo == tipo
                            FilterChip(
                                selected = sel,
                                onClick = { viewModel.setFiltroTiempo(tipo) },
                                label = { Text(label, fontSize = 11.sp) },
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = sel,
                                    borderColor = Color.White.copy(alpha = 0.2f),
                                    selectedBorderColor = LimeVolt
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = DarkCard,
                                    selectedContainerColor = Color(0xFF222634),
                                    selectedLabelColor = LimeVolt
                                )
                            )
                        }
                    }

                    // Sub-filtros
                    when (filtroTiempo) {
                        TipoFiltroEstadisticas.TOTAL,
                        TipoFiltroEstadisticas.ULTIMAS_4_SEMANAS,
                        TipoFiltroEstadisticas.ULTIMOS_3_MESES -> {}
                        TipoFiltroEstadisticas.TEMPORADA -> {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(temporadasConDatos) { temp ->
                                    val sel = temporadaSeleccionada == temp
                                    FilterChip(
                                        selected = sel,
                                        onClick = { viewModel.setTemporada(temp) },
                                        label = { Text(temp, fontSize = 12.sp) },
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = sel,
                                            borderColor = Color.White.copy(alpha = 0.2f),
                                            selectedBorderColor = LimeVolt
                                        ),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = DarkCard,
                                            selectedContainerColor = Color(0xFF222634),
                                            selectedLabelColor = LimeVolt
                                        )
                                    )
                                }
                            }
                        }
                        TipoFiltroEstadisticas.ANIO_NATURAL -> {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(aniosConDatos) { anio ->
                                    val sel = anioSeleccionado == anio
                                    FilterChip(
                                        selected = sel,
                                        onClick = { viewModel.setAnio(anio) },
                                        label = { Text("$anio", fontSize = 12.sp) },
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = sel,
                                            borderColor = Color.White.copy(alpha = 0.2f),
                                            selectedBorderColor = LimeVolt
                                        ),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = DarkCard,
                                            selectedContainerColor = Color(0xFF222634),
                                            selectedLabelColor = LimeVolt
                                        )
                                    )
                                }
                            }
                        }
                        TipoFiltroEstadisticas.FECHA_PERSONALIZADA -> {
                            SelectorRangoFechasDosBotones(
                                fechaInicio = fechaInicio,
                                fechaFin = fechaFin,
                                onRangoChange = { ini, fin ->
                                    viewModel.setRangoFechas(ini, fin)
                                }
                            )
                        }
                    }
                }
            }

            // 1. BALANCE DE RESULTADOS (Debe ir ANTES de las estadísticas individuales y colectivas según Nivel 7)
            item {
                GraficoResultados(
                    partidos = partidosFiltrados,
                    victorias = resumen.victorias,
                    empates = resumen.empates,
                    derrotas = resumen.derrotas,
                    jugadorId = jugadorInspeccionadoId
                )
            }

            // 2. MÉTRICAS PRINCIPALES
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Fila 1: Partidos y Goles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("🏟️ Partidos", color = TextSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${resumen.totalPartidos}",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${resumen.porcentajeVictorias}% victorias",
                                    color = LimeVolt,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("⚽ Goles", color = TextSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${resumen.totalGoles}",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.2f por part.", resumen.promedioGoles),
                                    color = LimeVolt,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Fila 2: Asistencias y Tiros al Palo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("👟 Asistencias", color = TextSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${resumen.totalAsistencias}",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.2f por part.", resumen.promedioAsistencias),
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("🎯 Tiros al palo", color = TextSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${resumen.totalPalos}",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ocasion(es)",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Goles del equipo (A favor vs En contra) alineados verticalmente
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Goles a favor", color = TextSecondary, fontSize = 13.sp)
                                Text(
                                    text = "${resumen.golesAFavor}",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider(color = DarkCardBorder)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Goles en contra", color = TextSecondary, fontSize = 13.sp)
                                Text(
                                    text = "${resumen.golesEnContra}",
                                    color = Color(0xFFE53935),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider(color = DarkCardBorder)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Diferencia de goles", color = TextSecondary, fontSize = 13.sp)
                                val diff = resumen.diferenciaGoles
                                Text(
                                    text = if (diff > 0) "+$diff" else "$diff",
                                    color = if (diff >= 0) LimeVolt else Color(0xFFE53935),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 3. GRÁFICA DE GOLES Y ASISTENCIAS
            item {
                GraficoGolesAsistencias(partidos = partidosFiltrados, jugadorId = jugadorInspeccionadoId)
            }

            // 4. DESGLOSE Y RESUMEN DE GOLES
            item {
                GraficoResumenGoles(partidos = partidosFiltrados, jugadorId = jugadorInspeccionadoId)
            }

            // 5. GRÁFICA DE TIROS AL PALO
            item {
                GraficoTirosAlPalo(partidos = partidosFiltrados, jugadorId = jugadorInspeccionadoId)
            }

            // 6. GRÁFICA DE MINUTOS JUGADOS (Total y por semana)
            item {
                GraficoMinutosJugados(partidos = partidosFiltrados)
            }

            // 7. Estadísticas Claro vs Oscuro
            item {
                GraficoClaroOscuro(
                    statsClaro = statsClaroOscuro.first,
                    statsOscuro = statsClaroOscuro.second
                )
            }

            // 8. Mapa de calor de posiciones más jugadas
            item {
                GraficoMapaCalorPosiciones(
                    posicionesFrecuencia = statsPosicionesFrecuencia,
                    totalPartidos = partidosFiltrados.size
                )
            }

            // 9. Gráfica de goles encajados (solo si ha jugado únicamente como portero en algún partido)
            item {
                val miJugadorObj = remember(todosJugadores) { todosJugadores.firstOrNull { it.esUsuarioPropio } }
                val usuarioIds = remember(miJugadorObj) { setOfNotNull(miJugadorObj?.id, "usuario_propio_id") }
                val partidosSoloPortero = remember(partidosFiltrados, jugadorInspeccionadoId, usuarioIds) {
                    partidosFiltrados.filter { p ->
                        val esYo = jugadorInspeccionadoId == null || jugadorInspeccionadoId in usuarioIds
                        if (esYo) {
                            p.jugadoPorMi && p.posicionJugada == Posicion.POR && p.posicionesSecundarias.isEmpty()
                        } else {
                            val det = p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorInspeccionadoId }
                            det != null && det.posicionPrincipal == Posicion.POR && det.posicionesSecundarias.isEmpty()
                        }
                    }
                }
                if (partidosSoloPortero.isNotEmpty()) {
                    GraficoGolesEncajados(
                        partidos = partidosSoloPortero,
                        jugadorId = jugadorInspeccionadoId
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Filtro por modalidad de juego (Total por defecto)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Modalidad de juego:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val modos = listOf(
                            null to "Total",
                            TipoFutbol.FUTSAL to "Futsal",
                            TipoFutbol.FUT_6 to "Fútbol 6",
                            TipoFutbol.FUT_7 to "Fútbol 7"
                        )
                        modos.forEach { (modo, label) ->
                            FilterChip(
                                selected = filtroModo == modo,
                                onClick = { viewModel.setFiltroModoJuego(modo) },
                                label = {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(label, fontSize = 11.sp, textAlign = TextAlign.Center)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Filtro temporal (Total, Temporada, Año, Por fecha, ...)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Periodo de tiempo:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        val filtros = listOf(
                            TipoFiltroEstadisticas.TOTAL to "Total",
                            TipoFiltroEstadisticas.TEMPORADA to "Temporada",
                            TipoFiltroEstadisticas.ANIO_NATURAL to "Año",
                            TipoFiltroEstadisticas.FECHA_PERSONALIZADA to "Por fecha",
                            TipoFiltroEstadisticas.ULTIMOS_3_MESES to "Últimos meses",
                            TipoFiltroEstadisticas.ULTIMAS_4_SEMANAS to "Últimas semanas"
                        )
                        items(filtros) { (tipo, label) ->
                            FilterChip(
                                selected = filtroTiempo == tipo,
                                onClick = { viewModel.setFiltroTiempo(tipo) },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Sub-filtros
                    when (filtroTiempo) {
                        TipoFiltroEstadisticas.TOTAL,
                        TipoFiltroEstadisticas.ULTIMAS_4_SEMANAS,
                        TipoFiltroEstadisticas.ULTIMOS_3_MESES -> {}
                        TipoFiltroEstadisticas.TEMPORADA -> {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(temporadasConDatos) { temp ->
                                    FilterChip(
                                        selected = temporadaSeleccionada == temp,
                                        onClick = { viewModel.setTemporada(temp) },
                                        label = { Text(temp, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                        TipoFiltroEstadisticas.ANIO_NATURAL -> {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(aniosConDatos) { anio ->
                                    FilterChip(
                                        selected = anioSeleccionado == anio,
                                        onClick = { viewModel.setAnio(anio) },
                                        label = { Text("$anio", fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                        TipoFiltroEstadisticas.FECHA_PERSONALIZADA -> {
                            SelectorRangoFechasDosBotones(
                                fechaInicio = fechaInicio,
                                fechaFin = fechaFin,
                                onRangoChange = { ini, fin ->
                                    viewModel.setRangoFechas(ini, fin)
                                }
                            )
                        }
                    }
                }
            }

            // Filtros de partido: Clima, Estadio, Días, Horas (Multi-selección)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, if (hayFiltrosPartidosActivos) LimeVolt else DarkCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FilterAlt,
                                    contentDescription = null,
                                    tint = LimeVolt,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Filtros de partido",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (hayFiltrosPartidosActivos) {
                                TextButton(
                                    onClick = { viewModel.limpiarFiltrosPartidos() },
                                    modifier = Modifier.height(26.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = null,
                                        tint = LimeVolt,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Limpiar", color = LimeVolt, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // 1. Climas
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Clima:",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(Clima.entries.toTypedArray()) { c ->
                                    val emoji = if (c == Clima.DESPEJADO) "☀️/🌙" else c.emoji
                                    val seleccionado = filtroClimas.contains(c)
                                    FilterChip(
                                        selected = seleccionado,
                                        onClick = { viewModel.toggleFiltroClima(c) },
                                        label = {
                                            Text(
                                                text = "$emoji ${c.label}",
                                                fontSize = 11.sp
                                            )
                                        },
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = seleccionado,
                                            borderColor = DarkCardBorder,
                                            selectedBorderColor = LimeVolt
                                        ),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = DarkCard,
                                            selectedContainerColor = Color(0xFF222634),
                                            selectedLabelColor = LimeVolt
                                        )
                                    )
                                }
                                item {
                                    val seleccionado = filtroTechado
                                    FilterChip(
                                        selected = seleccionado,
                                        onClick = { viewModel.toggleFiltroTechado() },
                                        label = {
                                            Text(
                                                text = "🏠 Techado",
                                                fontSize = 11.sp
                                            )
                                        },
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = seleccionado,
                                            borderColor = DarkCardBorder,
                                            selectedBorderColor = LimeVolt
                                        ),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = DarkCard,
                                            selectedContainerColor = Color(0xFF222634),
                                            selectedLabelColor = LimeVolt
                                        )
                                    )
                                }
                            }
                        }

                        // 2. Estadios / Ubicaciones (Desplegable)
                        var menuUbicacionExpandido by remember { mutableStateOf(false) }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Ubicación:",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    onClick = { menuUbicacionExpandido = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, if (filtroEstadios.isNotEmpty()) LimeVolt else DarkCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (filtroEstadios.isEmpty()) "Todas las ubicaciones" else filtroEstadios.joinToString(", "),
                                            color = if (filtroEstadios.isEmpty()) TextSecondary else Color.White,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = LimeVolt
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = menuUbicacionExpandido,
                                    onDismissRequest = { menuUbicacionExpandido = false },
                                    modifier = Modifier
                                        .background(DarkCard)
                                        .width(280.dp)
                                ) {
                                    val listaEstadios = if (todosEstadios.isNotEmpty()) {
                                        todosEstadios.filter { it.nombre in estadiosDisponiblesFiltro }
                                            .ifEmpty { todosEstadios }
                                    } else {
                                        estadiosDisponiblesFiltro.map { Estadio(nombre = it) }
                                    }

                                    if (listaEstadios.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("No hay estadios disponibles", color = TextSecondary, fontSize = 12.sp) },
                                            onClick = { menuUbicacionExpandido = false }
                                        )
                                    } else {
                                        listaEstadios.forEach { est ->
                                            val seleccionado = filtroEstadios.contains(est.nombre)
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Checkbox(
                                                            checked = seleccionado,
                                                            onCheckedChange = null,
                                                            colors = CheckboxDefaults.colors(
                                                                checkedColor = LimeVolt,
                                                                checkmarkColor = Color.Black
                                                            )
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = est.nombre,
                                                            color = Color.White,
                                                            fontSize = 13.sp,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        if (est.esFavorito) {
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Icon(
                                                                imageVector = Icons.Default.Star,
                                                                contentDescription = "Favorito",
                                                                tint = Color(0xFFFFD700),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.toggleFiltroEstadio(est.nombre)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Días de la semana
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Día de la semana:",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            val dias = listOf(
                                Calendar.MONDAY to "Lun",
                                Calendar.TUESDAY to "Mar",
                                Calendar.WEDNESDAY to "Mié",
                                Calendar.THURSDAY to "Jue",
                                Calendar.FRIDAY to "Vie",
                                Calendar.SATURDAY to "Sáb",
                                Calendar.SUNDAY to "Dom"
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(dias) { (diaNum, diaLabel) ->
                                    val seleccionado = filtroDiasSemana.contains(diaNum)
                                    FilterChip(
                                        selected = seleccionado,
                                        onClick = { viewModel.toggleFiltroDiaSemana(diaNum) },
                                        label = {
                                            Text(
                                                text = diaLabel,
                                                fontSize = 11.sp
                                            )
                                        },
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = seleccionado,
                                            borderColor = DarkCardBorder,
                                            selectedBorderColor = LimeVolt
                                        ),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = DarkCard,
                                            selectedContainerColor = Color(0xFF222634),
                                            selectedLabelColor = LimeVolt
                                        )
                                    )
                                }
                            }
                        }

                        // 4. Franja horaria
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Franja horaria:",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FranjaHoraria.entries.forEach { franja ->
                                    val seleccionado = filtroFranjasHorarias.contains(franja)
                                    FilterChip(
                                        selected = seleccionado,
                                        onClick = { viewModel.toggleFiltroFranjaHoraria(franja) },
                                        label = {
                                            Text(
                                                text = "${franja.emoji} ${franja.label}",
                                                fontSize = 11.5.sp
                                            )
                                        },
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = seleccionado,
                                            borderColor = DarkCardBorder,
                                            selectedBorderColor = LimeVolt
                                        ),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = DarkCard,
                                            selectedContainerColor = Color(0xFF222634),
                                            selectedLabelColor = LimeVolt
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1. Gráfica de Ubicaciones / Estadios
            item {
                GraficoEstadios(
                    statsEstadios = statsEstadios,
                    totalPartidos = partidosTabPartidos.size
                )
            }

            // 2. Gráfica de Clima
            item {
                GraficoClima(
                    statsClima = statsClima,
                    totalPartidos = partidosTabPartidos.size
                )
            }

            // 3. Gráfica Días de la semana más jugados
            item {
                GraficoDiasSemana(
                    statsDiasSemana = statsDiasSemana
                )
            }

            // 4. Gráfica Horas más jugadas
            item {
                GraficoHorasPartidos(
                    statsHoras = statsHorasPartidos
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
    }
}

@Composable
fun GraficoMinutosJugados(partidos: List<Partido>) {
    val totalMinutos = remember(partidos) { partidos.sumOf { it.duracionMinutos } }
    val horas = totalMinutos / 60
    val restoMin = totalMinutos % 60
    val tiempoTexto = if (horas > 0) "${horas}h ${restoMin}m" else "${totalMinutos}m"

    val semanasData = remember(partidos) {
        if (partidos.isEmpty()) emptyList()
        else {
            val cal = Calendar.getInstance()
            val mapaSemanas = mutableMapOf<String, Int>()
            val ordenado = partidos.sortedBy { it.fecha }
            ordenado.forEach { p ->
                cal.timeInMillis = p.fecha
                val sem = "Sem ${cal.get(Calendar.WEEK_OF_YEAR)}"
                mapaSemanas[sem] = (mapaSemanas[sem] ?: 0) + p.duracionMinutos
            }
            mapaSemanas.toList().takeLast(6)
        }
    }

    val maxMinutos = remember(semanasData) {
        (semanasData.maxOfOrNull { it.second } ?: 60).coerceAtLeast(60)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏱️ Minutos jugados",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "$totalMinutos min ($tiempoTexto)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LimeVolt
                )
            }

            if (semanasData.isEmpty()) {
                Text(
                    text = "No hay partidos registrados en este periodo.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Minutos por semana",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Eje Y con escala
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(bottom = 6.dp, end = 6.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${maxMinutos}m",
                                color = TextSecondary,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "${maxMinutos / 2}m",
                                color = TextSecondary,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "0m",
                                color = TextSecondary,
                                fontSize = 9.sp
                            )
                        }

                        // Barras
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            semanasData.forEach { (_, mins) ->
                                val ratio = (mins.toFloat() / maxMinutos).coerceIn(0.08f, 1f)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${mins}'",
                                        color = LimeVolt,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .fillMaxHeight(ratio)
                                            .background(LimeVolt, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    )
                                }
                            }
                        }
                    }

                    // Línea horizontal del Eje X
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 28.dp)
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.25f))
                    )

                    // Valores del Eje X
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 28.dp, top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        semanasData.forEach { (semana, _) ->
                            Text(
                                text = semana,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
