package com.diegoguerrero.futtracker.ui.screens.estadisticas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diegoguerrero.futtracker.domain.model.TipoFutbol
import com.diegoguerrero.futtracker.ui.components.GraficoGolesAsistencias
import com.diegoguerrero.futtracker.ui.components.GraficoResultados
import com.diegoguerrero.futtracker.ui.components.GraficoResumenGoles
import com.diegoguerrero.futtracker.ui.theme.DarkBackground
import com.diegoguerrero.futtracker.ui.theme.DarkCard
import com.diegoguerrero.futtracker.ui.theme.LimeVolt
import com.diegoguerrero.futtracker.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    viewModel: EstadisticasViewModel = hiltViewModel()
) {
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

    var mostrarRangoPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCard)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            TipoFiltroEstadisticas.TOTAL to "Total",
                            TipoFiltroEstadisticas.TEMPORADA to "Temporada",
                            TipoFiltroEstadisticas.ANIO_NATURAL to "Año",
                            TipoFiltroEstadisticas.FECHA_PERSONALIZADA to "Por fecha"
                        ).forEach { (tipo, label) ->
                            FilterChip(
                                selected = filtroTiempo == tipo,
                                onClick = { viewModel.setFiltroTiempo(tipo) },
                                label = {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(label, fontSize = 11.sp, textAlign = TextAlign.Center)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Sub-filtros
                    when (filtroTiempo) {
                        TipoFiltroEstadisticas.TOTAL -> {}
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
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Partidos", color = TextSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
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
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("⚽ Goles", color = TextSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
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
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("👟 Asistencias", color = TextSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
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
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🥅 Tiros al palo", color = TextSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
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

                    // Goles del equipo (A favor vs En contra)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("A favor", color = TextSecondary, fontSize = 11.sp)
                                Text(
                                    text = "${resumen.golesAFavor}",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("En contra", color = TextSecondary, fontSize = 11.sp)
                                Text(
                                    text = "${resumen.golesEnContra}",
                                    color = Color(0xFFE53935),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Diferencia", color = TextSecondary, fontSize = 11.sp)
                                val diff = resumen.diferenciaGoles
                                Text(
                                    text = if (diff > 0) "+$diff" else "$diff",
                                    color = if (diff >= 0) LimeVolt else Color(0xFFE53935),
                                    fontSize = 18.sp,
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

            item { Spacer(modifier = Modifier.height(20.dp)) }
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
