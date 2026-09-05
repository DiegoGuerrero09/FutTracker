package com.diegoguerrero.futtracker.ui.screens.estadisticas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.domain.model.TipoFutbol
import com.diegoguerrero.futtracker.domain.model.nombreConTu
import com.diegoguerrero.futtracker.ui.components.BadgePosicion
import com.diegoguerrero.futtracker.ui.components.GraficoGolesAsistencias
import com.diegoguerrero.futtracker.ui.components.GraficoResultados
import com.diegoguerrero.futtracker.ui.components.GraficoResumenGoles
import com.diegoguerrero.futtracker.ui.components.GraficoTirosAlPalo
import com.diegoguerrero.futtracker.ui.components.JugadorAvatar
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
    var pestanaSeleccionada by remember { mutableIntStateOf(0) } // 0: Individual, 1: General

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

    // Estados pestaña General
    val busquedaGeneral by viewModel.busquedaGeneral.collectAsState()
    val soloFavoritosGeneral by viewModel.soloFavoritosGeneral.collectAsState()
    val posicionGeneral by viewModel.posicionGeneral.collectAsState()
    val soloPosicionPrincipalGeneral by viewModel.soloPosicionPrincipalGeneral.collectAsState()
    val criterioOrdenGeneral by viewModel.criterioOrdenGeneral.collectAsState()
    val ordenAscendenteGeneral by viewModel.ordenAscendenteGeneral.collectAsState()
    val jugadoresGeneral by viewModel.jugadoresEstadisticasGeneral.collectAsState()

    var mostrarRangoPicker by remember { mutableStateOf(false) }

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
                                color = if (pestanaSeleccionada == 0) LimeVolt else Color.White
                            )
                        }
                    )
                    Tab(
                        selected = pestanaSeleccionada == 1,
                        onClick = { pestanaSeleccionada = 1 },
                        text = {
                            Text(
                                "General",
                                fontWeight = if (pestanaSeleccionada == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (pestanaSeleccionada == 1) LimeVolt else Color.White
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
                            TipoFiltroEstadisticas.ULTIMAS_4_SEMANAS to "4 semanas",
                            TipoFiltroEstadisticas.ULTIMOS_3_MESES to "3 meses",
                            TipoFiltroEstadisticas.TEMPORADA to "Temporada",
                            TipoFiltroEstadisticas.ANIO_NATURAL to "Año",
                            TipoFiltroEstadisticas.FECHA_PERSONALIZADA to "Por fecha"
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
                            val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
                            OutlinedButton(
                                onClick = { mostrarRangoPicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${sdf.format(Date(fechaInicio))} - ${sdf.format(Date(fechaFin))}",
                                    fontSize = 12.sp
                                )
                            }
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
                    Text(
                        text = "Rendimiento general",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

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

            // 4. GRÁFICA DE TIROS AL PALO
            item {
                GraficoTirosAlPalo(partidos = partidosFiltrados)
            }

            // 5. DESGLOSE Y RESUMEN DE GOLES
            item {
                GraficoResumenGoles(partidos = partidosFiltrados)
            }

            // 6. GRÁFICA DE MINUTOS JUGADOS (Total y por semana)
            item {
                GraficoMinutosJugados(partidos = partidosFiltrados)
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Buscador
            item {
                OutlinedTextField(
                    value = busquedaGeneral,
                    onValueChange = { viewModel.setBusquedaGeneral(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar jugador...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (busquedaGeneral.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setBusquedaGeneral("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true
                )
            }

            // Filtros: Favoritos y Posiciones
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = soloFavoritosGeneral,
                            onClick = { viewModel.toggleSoloFavoritosGeneral() },
                            label = { Text("Favoritos", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = posicionGeneral == null,
                            onClick = { viewModel.setPosicionGeneral(null) },
                            label = { Text("Todos", fontSize = 11.sp) }
                        )
                    }
                    items(Posicion.entries.toTypedArray()) { pos ->
                        FilterChip(
                            selected = posicionGeneral == pos,
                            onClick = {
                                viewModel.setPosicionGeneral(if (posicionGeneral == pos) null else pos)
                            },
                            label = { Text(pos.name, fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Subfiltro Ambas posiciones vs Solo principal
            if (posicionGeneral != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = !soloPosicionPrincipalGeneral,
                            onClick = { viewModel.setSoloPosicionPrincipalGeneral(false) },
                            label = { Text("Ambas posiciones", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = soloPosicionPrincipalGeneral,
                            onClick = { viewModel.setSoloPosicionPrincipalGeneral(true) },
                            label = { Text("Solo posición principal", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Criterios de ordenación
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ordenar por:",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            onClick = { viewModel.toggleOrdenAscendenteGeneral() },
                            shape = RoundedCornerShape(8.dp),
                            color = DarkCard,
                            border = BorderStroke(1.dp, (if (ordenAscendenteGeneral) LimeVolt else BlueCompanero).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val flechaColor = if (ordenAscendenteGeneral) LimeVolt else BlueCompanero
                                Icon(
                                    imageVector = if (ordenAscendenteGeneral) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = if (ordenAscendenteGeneral) "Ascendente" else "Descendente",
                                    tint = flechaColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (ordenAscendenteGeneral) "Ascendente" else "Descendente",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val opciones = listOf(
                            CriterioOrdenGeneral.VICTORIAS to "Victorias",
                            CriterioOrdenGeneral.DERROTAS to "Derrotas",
                            CriterioOrdenGeneral.EMPATES to "Empates",
                            CriterioOrdenGeneral.PARTIDOS to "Partidos",
                            CriterioOrdenGeneral.PORCENTAJE to "% Victorias",
                            CriterioOrdenGeneral.MINUTOS to "Minutos",
                            CriterioOrdenGeneral.NOMBRE to "Nombre"
                        )
                        items(opciones) { (criterio, label) ->
                            FilterChip(
                                selected = criterioOrdenGeneral == criterio,
                                onClick = { viewModel.setCriterioOrdenGeneral(criterio) },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // Lista de jugadores con estadísticas
            if (jugadoresGeneral.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No se encontraron jugadores",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(jugadoresGeneral, key = { it.jugador.id }) { item ->
                    CardJugadorGeneral(item = item)
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
    }

    if (mostrarRangoPicker) {
        val datePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = fechaInicio,
            initialSelectedEndDateMillis = fechaFin
        )
        DatePickerDialog(
            onDismissRequest = { mostrarRangoPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = datePickerState.selectedStartDateMillis
                        val end = datePickerState.selectedEndDateMillis ?: start
                        if (start != null && end != null) {
                            viewModel.setRangoFechas(start, end)
                        }
                        mostrarRangoPicker = false
                    }
                ) {
                    Text("Aplicar", color = LimeVolt)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarRangoPicker = false }) {
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
}

@Composable
fun CardJugadorGeneral(item: EstadisticasJugadorGeneral) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JugadorAvatar(
                    fotoUri = item.jugador.fotoUri,
                    nombre = item.jugador.nombre,
                    tamano = 44.dp,
                    permitirZoom = true
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.jugador.nombreConTu(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (item.jugador.esFavorito) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorito",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val totalPos = item.jugador.posicionesPrimarias + item.jugador.posicionesSecundarias
                    if (totalPos.isEmpty()) {
                        Text("Sin posición", fontSize = 11.sp, color = TextSecondary)
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item.jugador.posicionesPrimarias.forEach { pos ->
                                BadgePosicion(label = pos.name, esPrimaria = true)
                            }
                            item.jugador.posicionesSecundarias.forEach { pos ->
                                BadgePosicion(label = pos.name, esPrimaria = false)
                            }
                        }
                    }
                }

                // Porcentaje de victorias destacado
                val colorPct = obtenerColorPorcentaje(item.porcentajeVictorias.toFloat())
                Surface(
                    color = colorPct.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, colorPct.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${item.porcentajeVictorias}%",
                            color = colorPct,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Victorias",
                            color = colorPct.copy(alpha = 0.85f),
                            fontSize = 9.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = DarkCardBorder, thickness = 0.8.dp)

            // Fila de estadísticas: PJ, Min, V, E, D
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatGeneralPill(
                    label = "PJ",
                    valor = item.partidosJugados.toString(),
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                StatGeneralPill(
                    label = "Min",
                    valor = "${item.minutosJugados}'",
                    color = LimeVolt,
                    modifier = Modifier.weight(1.3f)
                )

                StatGeneralPill(
                    label = "V",
                    valor = item.victorias.toString(),
                    color = GreenWin,
                    modifier = Modifier.weight(1f)
                )

                StatGeneralPill(
                    label = "E",
                    valor = item.empates.toString(),
                    color = OrangeDraw,
                    modifier = Modifier.weight(1f)
                )

                StatGeneralPill(
                    label = "D",
                    valor = item.derrotas.toString(),
                    color = RedLoss,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatGeneralPill(
    label: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = valor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
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
                Text(
                    text = "Minutos por semana:",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    semanasData.forEach { (semana, mins) ->
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
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = semana,
                                color = TextSecondary,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
