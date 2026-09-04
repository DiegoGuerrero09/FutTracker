package com.diegoguerrero.futtracker.ui.screens.enfrentamientos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.diegoguerrero.futtracker.domain.model.ComparativaCaraACara
import com.diegoguerrero.futtracker.domain.model.DuoEstadisticas
import com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorCruzadas
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.ui.components.BadgePosicion
import com.diegoguerrero.futtracker.ui.components.JugadorAvatar
import com.diegoguerrero.futtracker.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnfrentamientosScreen(
    viewModel: EnfrentamientosViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Enfrentamientos",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkCard
                )
            )
        },
        containerColor = DarkBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Pestañas de sección
            TabRow(
                selectedTabIndex = uiState.seccionActual.ordinal,
                containerColor = DarkCard,
                contentColor = LimeVolt
            ) {
                SeccionEnfrentamientos.values().forEach { seccion ->
                    Tab(
                        selected = uiState.seccionActual == seccion,
                        onClick = { viewModel.setSeccion(seccion) },
                        text = {
                            Text(
                                text = seccion.titulo,
                                fontSize = 12.sp,
                                fontWeight = if (uiState.seccionActual == seccion) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selectedContentColor = LimeVolt,
                        unselectedContentColor = TextSecondary
                    )
                }
            }

            when (uiState.seccionActual) {
                SeccionEnfrentamientos.HISTORIAL -> {
                    SeccionHistorialCompanerosRivales(
                        uiState = uiState,
                        onFiltroTextoChange = { viewModel.setFiltroTexto(it) },
                        onFiltroHistorialChange = { viewModel.setFiltroHistorial(it) },
                        onSeleccionarJugador = { viewModel.seleccionarJugadorDetalle(it) }
                    )
                }
                SeccionEnfrentamientos.CARA_A_CARA -> {
                    SeccionCaraACara(
                        uiState = uiState,
                        onSeleccionarA = { viewModel.seleccionarJugadorA(it) },
                        onSeleccionarB = { viewModel.seleccionarJugadorB(it) }
                    )
                }
                SeccionEnfrentamientos.DUOS -> {
                    SeccionDuos(duos = uiState.duos)
                }
            }
        }
    }

    // Modal de detalle de jugador si está seleccionado
    uiState.jugadorDetalle?.let { detalle ->
        DialogoDetalleJugadorCruzado(
            detalle = detalle,
            onDismiss = { viewModel.seleccionarJugadorDetalle(null) }
        )
    }
}

@Composable
fun SeccionHistorialCompanerosRivales(
    uiState: EnfrentamientosUiState,
    onFiltroTextoChange: (String) -> Unit,
    onFiltroHistorialChange: (FiltroHistorial) -> Unit,
    onSeleccionarJugador: (EstadisticasJugadorCruzadas) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bloque de destacados
        item {
            Text(
                text = "Destacados",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LimeVolt
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CardDestacado(
                    modifier = Modifier.weight(1f),
                    titulo = "Talismán",
                    subtitulo = "Más victorias juntos",
                    icono = "🏆",
                    colorBorde = BlueCompanero,
                    estadistica = uiState.destacados.companeroMasGana,
                    esCompanero = true
                )
                CardDestacado(
                    modifier = Modifier.weight(1f),
                    titulo = "Gafe",
                    subtitulo = "Más derrotas juntos",
                    icono = "💔",
                    colorBorde = RedLoss,
                    estadistica = uiState.destacados.companeroMasPierde,
                    esCompanero = true
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CardDestacado(
                    modifier = Modifier.weight(1f),
                    titulo = "Rival favorito",
                    subtitulo = "Más victorias contra él",
                    icono = "🎯",
                    colorBorde = GreenWin,
                    estadistica = uiState.destacados.rivalMasGana,
                    esCompanero = false
                )
                CardDestacado(
                    modifier = Modifier.weight(1f),
                    titulo = "Bestia negra",
                    subtitulo = "Más derrotas contra él",
                    icono = "⚠️",
                    colorBorde = RedLoss,
                    estadistica = uiState.destacados.rivalMasPierde,
                    esCompanero = false
                )
            }
        }

        // Filtro y buscador
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.filtroTexto,
                    onValueChange = onFiltroTextoChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar jugador...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LimeVolt,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiltroHistorial.values().forEach { filtro ->
                        val seleccionado = uiState.filtroHistorial == filtro
                        val chipColor = when (filtro) {
                            FiltroHistorial.TODOS -> LimeVolt
                            FiltroHistorial.COMPANEROS -> BlueCompanero
                            FiltroHistorial.RIVALES -> OrangeDraw
                        }
                        FilterChip(
                            selected = seleccionado,
                            onClick = { onFiltroHistorialChange(filtro) },
                            label = { Text(filtro.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor.copy(alpha = 0.2f),
                                selectedLabelColor = chipColor,
                                containerColor = DarkCard,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = seleccionado,
                                borderColor = if (seleccionado) chipColor else Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        // Lista de jugadores con estadísticas
        if (uiState.historial.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron partidos o jugadores para este filtro.",
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(uiState.historial) { item ->
                CardJugadorHistorial(
                    item = item,
                    onClick = { onSeleccionarJugador(item) }
                )
            }
        }
    }
}

@Composable
fun CardDestacado(
    modifier: Modifier = Modifier,
    titulo: String,
    subtitulo: String,
    icono: String,
    colorBorde: Color,
    estadistica: EstadisticasJugadorCruzadas?,
    esCompanero: Boolean
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorBorde.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$icono $titulo",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = subtitulo,
                fontSize = 9.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (estadistica != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    JugadorAvatar(
                        fotoUri = estadistica.jugador.fotoUri,
                        nombre = estadistica.jugador.nombre,
                        tamano = 32.dp,
                        fontSize = 11.sp
                    )
                    Column {
                        Text(
                            text = estadistica.jugador.nombre,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val victorias = if (esCompanero) estadistica.victoriasComoCompanero else estadistica.victoriasComoRival
                        val partidos = if (esCompanero) estadistica.partidosComoCompanero else estadistica.partidosComoRival
                        val derrotas = if (esCompanero) estadistica.derrotasComoCompanero else estadistica.derrotasComoRival
                        val pct = if (partidos > 0) ((victorias.toFloat() / partidos) * 100).toInt() else 0

                        Text(
                            text = "$partidos part • $victorias V - $derrotas D ($pct%)",
                            fontSize = 10.sp,
                            color = colorBorde,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Text(
                    text = "Sin datos aún",
                    fontSize = 11.sp,
                    color = TextSecondary.copy(alpha = 0.6f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun CardJugadorHistorial(
    item: EstadisticasJugadorCruzadas,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            JugadorAvatar(
                fotoUri = item.jugador.fotoUri,
                nombre = item.jugador.nombre,
                tamano = 44.dp,
                fontSize = 14.sp
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.jugador.nombre,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    item.jugador.posicionesPrimarias.take(2).forEach { pos ->
                        BadgePosicion(label = pos.name, esPrimaria = true)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.partidosComoCompanero > 0) {
                        Surface(
                            color = BlueCompanero.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Compañero: ${item.partidosComoCompanero}p (${item.victoriasComoCompanero}V/${item.empatesComoCompanero}E/${item.derrotasComoCompanero}D)",
                                color = BlueCompanero,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (item.partidosComoRival > 0) {
                        Surface(
                            color = Color(0xFF3E2723),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Rival: ${item.partidosComoRival}p (${item.victoriasComoRival}V/${item.empatesComoRival}E/${item.derrotasComoRival}D)",
                                color = OrangeDraw,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DialogoDetalleJugadorCruzado(
    detalle: EstadisticasJugadorCruzadas,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, LimeVolt.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                JugadorAvatar(
                    fotoUri = detalle.jugador.fotoUri,
                    nombre = detalle.jugador.nombre,
                    tamano = 64.dp,
                    fontSize = 20.sp,
                    bordeColor = LimeVolt,
                    bordeAncho = 2.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = detalle.jugador.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    detalle.jugador.posicionesPrimarias.forEach { pos ->
                        BadgePosicion(label = pos.name, esPrimaria = true)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bloque: Como compañero
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BlueCompanero.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Como compañero (mismo equipo)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueCompanero
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Partidos juntos:", fontSize = 12.sp, color = TextSecondary)
                            Text("${detalle.partidosComoCompanero}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Balance (V / E / D):", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                "${detalle.victoriasComoCompanero}V - ${detalle.empatesComoCompanero}E - ${detalle.derrotasComoCompanero}D",
                                fontSize = 12.sp,
                                color = GreenWin,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("% Victorias:", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                String.format(Locale.getDefault(), "%.1f%%", detalle.porcentajeVictoriasCompanero),
                                fontSize = 12.sp,
                                color = BlueCompanero,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Goles (Favor / Contra / Dif):", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                "${detalle.golesFavorComoCompanero} - ${detalle.golesContraComoCompanero} (${if (detalle.diferenciaGolesCompanero >= 0) "+${detalle.diferenciaGolesCompanero}" else "${detalle.diferenciaGolesCompanero}"})",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tus goles con él:", fontSize = 12.sp, color = TextSecondary)
                            Text("${detalle.golesMarcadosComoCompanero}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bloque: Como rival
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OrangeDraw.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Como rival (equipo contrario)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeDraw
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Partidos enfrentados:", fontSize = 12.sp, color = TextSecondary)
                            Text("${detalle.partidosComoRival}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tus victorias vs sus victorias:", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                "${detalle.victoriasComoRival}V - ${detalle.empatesComoRival}E - ${detalle.derrotasComoRival}D",
                                fontSize = 12.sp,
                                color = if (detalle.victoriasComoRival >= detalle.derrotasComoRival) GreenWin else RedLoss,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tu % victorias frente a él:", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                String.format(Locale.getDefault(), "%.1f%%", detalle.porcentajeVictoriasRival),
                                fontSize = 12.sp,
                                color = LimeVolt,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Goles (Favor / Contra / Dif):", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                "${detalle.golesFavorComoRival} - ${detalle.golesContraComoRival} (${if (detalle.diferenciaGolesRival >= 0) "+${detalle.diferenciaGolesRival}" else "${detalle.diferenciaGolesRival}"})",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tus goles frente a él:", fontSize = 12.sp, color = TextSecondary)
                            Text("${detalle.golesMarcadosComoRival}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = LimeVolt),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cerrar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionCaraACara(
    uiState: EnfrentamientosUiState,
    onSeleccionarA: (String) -> Unit,
    onSeleccionarB: (String) -> Unit
) {
    var expandidoA by remember { mutableStateOf(false) }
    var expandidoB by remember { mutableStateOf(false) }

    val jugadorA = uiState.todosLosJugadores.find { it.id == uiState.jugadorAId }
    val jugadorB = uiState.todosLosJugadores.find { it.id == uiState.jugadorBId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Comparativa directa (1 vs 1)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LimeVolt
            )
            Text(
                text = "Selecciona dos jugadores para ver su historial cara a cara y cuando juegan juntos.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Selectores de los 2 jugadores
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Selector Jugador A
                Box(modifier = Modifier.weight(1f)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandidoA = true },
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LimeVolt.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            JugadorAvatar(
                                fotoUri = jugadorA?.fotoUri,
                                nombre = jugadorA?.nombre ?: "A",
                                tamano = 32.dp,
                                fontSize = 11.sp
                            )
                            Text(
                                text = jugadorA?.nombre ?: "Seleccionar A",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = LimeVolt)
                        }
                    }
                    DropdownMenu(
                        expanded = expandidoA,
                        onDismissRequest = { expandidoA = false },
                        modifier = Modifier.background(DarkCard)
                    ) {
                        uiState.todosLosJugadores.forEach { j ->
                            DropdownMenuItem(
                                text = { Text(j.nombre, color = Color.White) },
                                leadingIcon = {
                                    JugadorAvatar(fotoUri = j.fotoUri, nombre = j.nombre, tamano = 24.dp, fontSize = 9.sp)
                                },
                                onClick = {
                                    onSeleccionarA(j.id)
                                    expandidoA = false
                                }
                            )
                        }
                    }
                }

                // Selector Jugador B
                Box(modifier = Modifier.weight(1f)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandidoB = true },
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OrangeDraw.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            JugadorAvatar(
                                fotoUri = jugadorB?.fotoUri,
                                nombre = jugadorB?.nombre ?: "B",
                                tamano = 32.dp,
                                fontSize = 11.sp
                            )
                            Text(
                                text = jugadorB?.nombre ?: "Seleccionar B",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = OrangeDraw)
                        }
                    }
                    DropdownMenu(
                        expanded = expandidoB,
                        onDismissRequest = { expandidoB = false },
                        modifier = Modifier.background(DarkCard)
                    ) {
                        uiState.todosLosJugadores.forEach { j ->
                            DropdownMenuItem(
                                text = { Text(j.nombre, color = Color.White) },
                                leadingIcon = {
                                    JugadorAvatar(fotoUri = j.fotoUri, nombre = j.nombre, tamano = 24.dp, fontSize = 9.sp)
                                },
                                onClick = {
                                    onSeleccionarB(j.id)
                                    expandidoB = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Resultados del cara a cara
        val comp = uiState.comparativaCaraACara
        if (jugadorA != null && jugadorB != null && jugadorA.id == jugadorB.id) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Selecciona dos jugadores diferentes para comparar.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        } else if (comp != null) {
            item {
                // Marcador Cara a Cara
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Enfrentamientos directos",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LimeVolt,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                JugadorAvatar(fotoUri = jugadorA?.fotoUri, nombre = jugadorA?.nombre ?: "", tamano = 48.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(jugadorA?.nombre ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("${comp.victoriasA} victorias", color = GreenWin, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${comp.victoriasA} - ${comp.empates} - ${comp.victoriasB}",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${comp.partidosEnfrentados} partidos",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                JugadorAvatar(fotoUri = jugadorB?.fotoUri, nombre = jugadorB?.nombre ?: "", tamano = 48.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(jugadorB?.nombre ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("${comp.victoriasB} victorias", color = OrangeDraw, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Text("Goles equipo: ${comp.golesEquipoA}", fontSize = 12.sp, color = TextSecondary)
                            Text("Empates: ${comp.empates}", fontSize = 12.sp, color = TextSecondary)
                            Text("Goles equipo: ${comp.golesEquipoB}", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Cuando juegan juntos en el mismo equipo
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BlueCompanero.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Cuando juegan juntos en el mismo equipo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueCompanero,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (comp.partidosJuntos > 0) {
                            val pct = if (comp.partidosJuntos > 0) ((comp.victoriasJuntos.toFloat() / comp.partidosJuntos) * 100).toInt() else 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Partidos juntos:", color = TextSecondary, fontSize = 12.sp)
                                Text("${comp.partidosJuntos}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Balance (V / E / D):", color = TextSecondary, fontSize = 12.sp)
                                Text("${comp.victoriasJuntos}V - ${comp.empatesJuntos}E - ${comp.derrotasJuntos}D", color = GreenWin, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("% Victorias juntos:", color = TextSecondary, fontSize = 12.sp)
                                Text("$pct%", color = BlueCompanero, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            Text(
                                text = "Aún no han jugado juntos en el mismo equipo.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeccionDuos(duos: List<DuoEstadisticas>) {
    var mostrarSoloGanadores by remember { mutableStateOf(true) }

    val duosFiltrados = remember(duos, mostrarSoloGanadores) {
        if (mostrarSoloGanadores) {
            duos.filter { it.partidosJuntos >= 1 }
                .sortedWith(compareByDescending<DuoEstadisticas> { it.porcentajeVictorias }.thenByDescending { it.partidosJuntos })
        } else {
            duos.filter { it.partidosJuntos >= 1 }
                .sortedWith(compareByDescending<DuoEstadisticas> { it.derrotas }.thenBy { it.porcentajeVictorias })
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Parejas de jugadores",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LimeVolt
            )
            Text(
                text = "Conoce qué duplas obtienen mejores o peores resultados jugando en el mismo bando.",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = mostrarSoloGanadores,
                    onClick = { mostrarSoloGanadores = true },
                    label = { Text("🏆 Dúos talismán (más victorias)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LimeVolt.copy(alpha = 0.2f),
                        selectedLabelColor = LimeVolt,
                        containerColor = DarkCard,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = mostrarSoloGanadores,
                        borderColor = if (mostrarSoloGanadores) LimeVolt else Color.Transparent
                    )
                )

                FilterChip(
                    selected = !mostrarSoloGanadores,
                    onClick = { mostrarSoloGanadores = false },
                    label = { Text("💔 Con más derrotas", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RedLoss.copy(alpha = 0.2f),
                        selectedLabelColor = RedLoss,
                        containerColor = DarkCard,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = !mostrarSoloGanadores,
                        borderColor = if (!mostrarSoloGanadores) RedLoss else Color.Transparent
                    )
                )
            }
        }

        if (duosFiltrados.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay datos suficientes de parejas jugando juntas en partidos.",
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(duosFiltrados) { duo ->
                CardDuo(duo = duo, esModoGanador = mostrarSoloGanadores)
            }
        }
    }
}

@Composable
fun CardDuo(duo: DuoEstadisticas, esModoGanador: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatares superpuestos
            Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.CenterStart) {
                JugadorAvatar(
                    fotoUri = duo.jugador1.fotoUri,
                    nombre = duo.jugador1.nombre,
                    tamano = 36.dp,
                    fontSize = 11.sp
                )
                Box(modifier = Modifier.padding(start = 22.dp)) {
                    JugadorAvatar(
                        fotoUri = duo.jugador2.fotoUri,
                        nombre = duo.jugador2.nombre,
                        tamano = 36.dp,
                        fontSize = 11.sp,
                        bordeColor = DarkCard,
                        bordeAncho = 2.dp
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${duo.jugador1.nombre} & ${duo.jugador2.nombre}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${duo.partidosJuntos} partidos • ${duo.victorias}V - ${duo.empates}E - ${duo.derrotas}D",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Goles: ${duo.golesFavor} GF / ${duo.golesContra} GC (${if (duo.diferenciaGoles >= 0) "+${duo.diferenciaGoles}" else "${duo.diferenciaGoles}"})",
                    fontSize = 10.sp,
                    color = TextSecondary.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (esModoGanador) {
                    val pct = duo.porcentajeVictorias.toInt()
                    val colorPct = if (pct >= 50) GreenWin else if (pct >= 30) OrangeDraw else RedLoss
                    Text(
                        text = "$pct%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorPct
                    )
                    Text(
                        text = "Victorias",
                        fontSize = 9.sp,
                        color = TextSecondary
                    )
                } else {
                    val pctDerrotas = if (duo.partidosJuntos > 0) ((duo.derrotas.toFloat() / duo.partidosJuntos) * 100).toInt() else 0
                    Text(
                        text = "$pctDerrotas%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RedLoss
                    )
                    Text(
                        text = "Derrotas",
                        fontSize = 9.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
