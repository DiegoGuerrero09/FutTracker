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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diegoguerrero.futtracker.domain.model.Clima
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
    val filtroHoras by viewModel.filtroHoras.collectAsState()
    val estadiosDisponiblesFiltro by viewModel.estadiosDisponiblesFiltro.collectAsState()
    val horasDisponiblesFiltro by viewModel.horasDisponiblesFiltro.collectAsState()
    val hayFiltrosPartidosActivos by viewModel.hayFiltrosPartidosActivos.collectAsState()

    // Estados Individual
    val statsClaroOscuro by viewModel.statsClaroOscuro.collectAsState()
    val statsPosicionesFrecuencia by viewModel.statsPosicionesFrecuencia.collectAsState()

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

            // 1. BALANCE DE RESULTADOS (Debe ir ANTES de las estadísticas individuales y colectivas según Nivel 7)
            item {
                GraficoResultados(partidos = partidosFiltrados)
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
                                Text("🥅 Tiros al palo", color = TextSecondary, fontSize = 12.sp)
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
                GraficoGolesAsistencias(partidos = partidosFiltrados)
            }

            // 4. DESGLOSE Y RESUMEN DE GOLES
            item {
                GraficoResumenGoles(partidos = partidosFiltrados)
            }

            // 5. GRÁFICA DE TIROS AL PALO
            item {
                GraficoTirosAlPalo(partidos = partidosFiltrados)
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
                            modifier = Modifier.fillMaxWidth(),
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
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = null,
                                        tint = LimeVolt,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Limpiar", color = LimeVolt, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = LimeVolt,
                                            selectedLabelColor = Color.Black
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
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = LimeVolt,
                                            selectedLabelColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }

                        // 2. Estadios / Ubicaciones
                        if (estadiosDisponiblesFiltro.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Ubicación / Estadio:",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(estadiosDisponiblesFiltro) { nomEstadio ->
                                        val seleccionado = filtroEstadios.contains(nomEstadio)
                                        FilterChip(
                                            selected = seleccionado,
                                            onClick = { viewModel.toggleFiltroEstadio(nomEstadio) },
                                            label = {
                                                Text(
                                                    text = nomEstadio,
                                                    fontSize = 11.sp
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = LimeVolt,
                                                selectedLabelColor = Color.Black
                                            )
                                        )
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
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = LimeVolt,
                                            selectedLabelColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }

                        // 4. Horas
                        if (horasDisponiblesFiltro.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Hora de inicio:",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(horasDisponiblesFiltro) { hora ->
                                        val seleccionado = filtroHoras.contains(hora)
                                        FilterChip(
                                            selected = seleccionado,
                                            onClick = { viewModel.toggleFiltroHora(hora) },
                                            label = {
                                                Text(
                                                    text = "${hora}h",
                                                    fontSize = 11.sp
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = LimeVolt,
                                                selectedLabelColor = Color.Black
                                            )
                                        )
                                    }
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
