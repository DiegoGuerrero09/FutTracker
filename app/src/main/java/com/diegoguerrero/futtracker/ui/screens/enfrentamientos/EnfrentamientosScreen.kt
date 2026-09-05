package com.diegoguerrero.futtracker.ui.screens.enfrentamientos

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.diegoguerrero.futtracker.domain.model.nombreConTu
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.diegoguerrero.futtracker.domain.model.Posicion
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
                        text = "Versus",
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
                SeccionEnfrentamientos.INDIVIDUAL -> {
                    SeccionHistorialCompanerosRivales(
                        uiState = uiState,
                        onFiltroTextoChange = { viewModel.setFiltroTexto(it) },
                        onFiltroHistorialChange = { viewModel.setFiltroHistorial(it) },
                        onToggleFavorito = { viewModel.toggleFiltroSoloFavoritos() },
                        onPosicionChange = { viewModel.setFiltroPosicion(it) },
                        onSoloPosicionPrincipalChange = { viewModel.setFiltroSoloPosicionPrincipal(it) },
                        onSeleccionarJugador = { viewModel.seleccionarJugadorDetalle(it) }
                    )
                }
                SeccionEnfrentamientos.GENERAL -> {
                    SeccionGeneralCabraLacra(
                        uiState = uiState,
                        onSeleccionarJugador = { viewModel.seleccionarJugadorGeneral(it) },
                        onBusquedaChange = { viewModel.setBusquedaGeneral(it) },
                        onToggleFavorito = { viewModel.toggleSoloFavoritosGeneral() },
                        onPosicionChange = { viewModel.setPosicionGeneral(it) },
                        onSoloPosicionPrincipalChange = { viewModel.setSoloPosicionPrincipalGeneral(it) }
                    )
                }
                SeccionEnfrentamientos.DUOS -> {
                    SeccionDuos(duos = uiState.duos)
                }
                SeccionEnfrentamientos.H2H -> {
                    SeccionCaraACara(
                        uiState = uiState,
                        onSeleccionarA = { viewModel.seleccionarJugadorA(it) },
                        onSeleccionarB = { viewModel.seleccionarJugadorB(it) }
                    )
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
    onToggleFavorito: () -> Unit,
    onPosicionChange: (Posicion?) -> Unit,
    onSoloPosicionPrincipalChange: (Boolean) -> Unit,
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
                    titulo = "La cabra",
                    subtitulo = "Más victorias juntos",
                    icono = "🐐",
                    colorBorde = BlueCompanero,
                    estadisticas = uiState.destacados.companerosMasGanan,
                    esCompanero = true
                )
                CardDestacado(
                    modifier = Modifier.weight(1f),
                    titulo = "La lacra",
                    subtitulo = "Más derrotas juntos",
                    icono = "💔",
                    colorBorde = RedLoss,
                    estadisticas = uiState.destacados.companerosMasPierden,
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
                    titulo = "El caramelito",
                    subtitulo = "Más victorias contra él",
                    icono = "🍬",
                    colorBorde = BlueCompanero,
                    estadisticas = uiState.destacados.rivalesMasGanan,
                    esCompanero = false
                )
                CardDestacado(
                    modifier = Modifier.weight(1f),
                    titulo = "La bestia",
                    subtitulo = "Más derrotas contra él",
                    icono = "🦁",
                    colorBorde = RedLoss,
                    estadisticas = uiState.destacados.rivalesMasPierden,
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

                // Filtros de favoritos y posición
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.filtroSoloFavoritos,
                            onClick = onToggleFavorito,
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
                            selected = uiState.filtroPosicion == null,
                            onClick = { onPosicionChange(null) },
                            label = { Text("Todas", fontSize = 11.sp) }
                        )
                    }
                    items(Posicion.entries.toTypedArray()) { pos ->
                        FilterChip(
                            selected = uiState.filtroPosicion == pos,
                            onClick = {
                                onPosicionChange(if (uiState.filtroPosicion == pos) null else pos)
                            },
                            label = { Text(pos.name, fontSize = 11.sp) }
                        )
                    }
                }

                if (uiState.filtroPosicion != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = !uiState.filtroSoloPosicionPrincipal,
                            onClick = { onSoloPosicionPrincipalChange(false) },
                            label = { Text("Ambas posiciones", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = uiState.filtroSoloPosicionPrincipal,
                            onClick = { onSoloPosicionPrincipalChange(true) },
                            label = { Text("Solo posición principal", fontSize = 11.sp) }
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
    estadisticas: List<EstadisticasJugadorCruzadas> = emptyList(),
    estadistica: EstadisticasJugadorCruzadas? = null,
    esCompanero: Boolean
) {
    val lista = remember(estadisticas, estadistica) {
        if (estadisticas.isNotEmpty()) estadisticas else listOfNotNull(estadistica)
    }
    var currentIndex by remember(lista) { mutableIntStateOf(0) }

    LaunchedEffect(lista) {
        if (lista.size > 1) {
            while (true) {
                kotlinx.coroutines.delay(3500)
                currentIndex = (currentIndex + 1) % lista.size
            }
        }
    }

    val itemActual = if (lista.isNotEmpty()) lista.getOrNull(currentIndex % lista.size) else null

    Card(
        modifier = modifier.height(100.dp),
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
                if (lista.size > 1) {
                    Text(
                        text = "${(currentIndex % lista.size) + 1}/${lista.size}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorBorde
                    )
                }
            }
            Text(
                text = subtitulo,
                fontSize = 9.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = itemActual,
                transitionSpec = {
                    fadeIn(tween(500)) togetherWith fadeOut(tween(500))
                },
                label = "DestacadoRotation"
            ) { actualEst ->
                if (actualEst != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        JugadorAvatar(
                            fotoUri = actualEst.jugador.fotoUri,
                            nombre = actualEst.jugador.nombre,
                            tamano = 32.dp,
                            fontSize = 11.sp,
                            bordeColor = colorBorde,
                            bordeAncho = 1.5.dp
                        )
                        Column {
                            Text(
                                text = actualEst.jugador.nombreConTu(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val victorias = if (esCompanero) actualEst.victoriasComoCompanero else actualEst.victoriasComoRival
                            val partidos = if (esCompanero) actualEst.partidosComoCompanero else actualEst.partidosComoRival
                            val derrotas = if (esCompanero) actualEst.derrotasComoCompanero else actualEst.derrotasComoRival
                            val pct = if (partidos > 0) ((victorias.toFloat() / partidos) * 100).toInt() else 0

                            Text(
                                text = "$partidos PJ • $victorias V - $derrotas D ($pct%)",
                                fontSize = 10.sp,
                                color = colorBorde,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
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
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DarkCardBorder)
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
                fontSize = 14.sp,
                permitirZoom = true
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.jugador.nombreConTu(),
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
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.partidosComoCompanero > 0) {
                        Surface(
                            color = BlueCompanero.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Compañero: ${item.partidosComoCompanero}p (${item.victoriasComoCompanero}V/${item.empatesComoCompanero}E/${item.derrotasComoCompanero}D)",
                                color = BlueCompanero,
                                fontSize = 8.5.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
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
                                fontSize = 8.5.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
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
                    bordeAncho = 2.dp,
                    permitirZoom = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = detalle.jugador.nombreConTu(),
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
                            Text("Balance:", fontSize = 12.sp, color = TextSecondary)
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
fun SeccionGeneralCabraLacra(
    uiState: EnfrentamientosUiState,
    onSeleccionarJugador: (String) -> Unit,
    onBusquedaChange: (String) -> Unit,
    onToggleFavorito: () -> Unit,
    onPosicionChange: (Posicion?) -> Unit,
    onSoloPosicionPrincipalChange: (Boolean) -> Unit
) {
    val jugadorSeleccionado = uiState.todosLosJugadores.firstOrNull { it.id == uiState.jugadorSeleccionadoGeneralId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Buscador y filtros para elegir el jugador a inspeccionar
        item {
            OutlinedTextField(
                value = uiState.busquedaGeneral,
                onValueChange = onBusquedaChange,
                placeholder = { Text("Buscar jugador...", color = TextSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextSecondary)
                },
                trailingIcon = {
                    if (uiState.busquedaGeneral.isNotBlank()) {
                        IconButton(onClick = { onBusquedaChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextSecondary)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LimeVolt,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = LimeVolt
                ),
                singleLine = true
            )
        }

        // Filtros rápidos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.soloFavoritosGeneral,
                    onClick = onToggleFavorito,
                    label = { Text("★ Favoritos", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LimeVolt.copy(alpha = 0.2f),
                        selectedLabelColor = LimeVolt,
                        containerColor = DarkCard,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = uiState.soloFavoritosGeneral,
                        borderColor = if (uiState.soloFavoritosGeneral) LimeVolt else DarkCardBorder
                    )
                )

                FilterChip(
                    selected = uiState.soloPosicionPrincipalGeneral,
                    onClick = { onSoloPosicionPrincipalChange(!uiState.soloPosicionPrincipalGeneral) },
                    label = {
                        Text(
                            text = if (uiState.soloPosicionPrincipalGeneral) "Solo principal" else "Ambas posiciones",
                            fontSize = 11.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LimeVolt.copy(alpha = 0.2f),
                        selectedLabelColor = LimeVolt,
                        containerColor = DarkCard,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = uiState.soloPosicionPrincipalGeneral,
                        borderColor = if (uiState.soloPosicionPrincipalGeneral) LimeVolt else DarkCardBorder
                    )
                )
            }
        }

        // Selector horizontal de posiciones
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = uiState.posicionGeneral == null,
                    onClick = { onPosicionChange(null) },
                    label = { Text("Todas", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LimeVolt.copy(alpha = 0.2f),
                        selectedLabelColor = LimeVolt,
                        containerColor = DarkCard,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = uiState.posicionGeneral == null,
                        borderColor = if (uiState.posicionGeneral == null) LimeVolt else DarkCardBorder
                    )
                )

                Posicion.values().forEach { pos ->
                    val sel = uiState.posicionGeneral == pos
                    FilterChip(
                        selected = sel,
                        onClick = { onPosicionChange(if (sel) null else pos) },
                        label = { Text(pos.name, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LimeVolt.copy(alpha = 0.2f),
                            selectedLabelColor = LimeVolt,
                            containerColor = DarkCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = sel,
                            borderColor = if (sel) LimeVolt else DarkCardBorder
                        )
                    )
                }
            }
        }

        // Lista horizontal de jugadores para seleccionar
        item {
            Text(
                text = "Seleccionar jugador:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (uiState.jugadoresFiltradosGeneral.isEmpty()) {
                Text(
                    text = "No hay jugadores que coincidan con los filtros.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.jugadoresFiltradosGeneral) { jug ->
                        val esSeleccionado = jug.id == jugadorSeleccionado?.id
                        Card(
                            modifier = Modifier
                                .clickable { onSeleccionarJugador(jug.id) }
                                .width(85.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (esSeleccionado) LimeVolt.copy(alpha = 0.15f) else DarkCard
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (esSeleccionado) LimeVolt else DarkCardBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                JugadorAvatar(
                                    fotoUri = jug.fotoUri,
                                    nombre = jug.nombre,
                                    tamano = 36.dp,
                                    fontSize = 12.sp,
                                    bordeColor = if (esSeleccionado) LimeVolt else Color.Transparent,
                                    bordeAncho = 1.5.dp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = jug.nombreConTu(),
                                    fontSize = 11.sp,
                                    fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Normal,
                                    color = if (esSeleccionado) LimeVolt else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Ficha del jugador actualmente seleccionado
        if (jugadorSeleccionado != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        JugadorAvatar(
                            fotoUri = jugadorSeleccionado.fotoUri,
                            nombre = jugadorSeleccionado.nombre,
                            tamano = 46.dp,
                            fontSize = 16.sp,
                            permitirZoom = true,
                            bordeColor = LimeVolt,
                            bordeAncho = 1.5.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = jugadorSeleccionado.nombreConTu(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (jugadorSeleccionado.esFavorito) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Favorito",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                jugadorSeleccionado.posicionesPrimarias.forEach { pos ->
                                    BadgePosicion(label = pos.name, esPrimaria = true)
                                }
                                jugadorSeleccionado.posicionesSecundarias.forEach { pos ->
                                    BadgePosicion(label = pos.name, esPrimaria = false)
                                }
                            }
                        }
                    }
                }
            }

            // Destacados para este jugador
            item {
                Text(
                    text = "Destacados de ${jugadorSeleccionado.nombreConTu()}",
                    fontSize = 14.sp,
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
                        titulo = "La cabra",
                        subtitulo = "Más victorias juntos",
                        icono = "🐐",
                        colorBorde = BlueCompanero,
                        estadisticas = uiState.destacadosGeneral.companerosMasGanan,
                        esCompanero = true
                    )
                    CardDestacado(
                        modifier = Modifier.weight(1f),
                        titulo = "La lacra",
                        subtitulo = "Más derrotas juntos",
                        icono = "💔",
                        colorBorde = RedLoss,
                        estadisticas = uiState.destacadosGeneral.companerosMasPierden,
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
                        titulo = "El caramelito",
                        subtitulo = "Más victorias contra él",
                        icono = "🍬",
                        colorBorde = BlueCompanero,
                        estadisticas = uiState.destacadosGeneral.rivalesMasGanan,
                        esCompanero = false
                    )
                    CardDestacado(
                        modifier = Modifier.weight(1f),
                        titulo = "La bestia",
                        subtitulo = "Más derrotas contra él",
                        icono = "🦁",
                        colorBorde = RedLoss,
                        estadisticas = uiState.destacadosGeneral.rivalesMasPierden,
                        esCompanero = false
                    )
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
    val jugadoresOrdenados = remember(uiState.todosLosJugadores) {
        uiState.todosLosJugadores.sortedWith(
            compareByDescending<Jugador> { it.esUsuarioPropio || it.id == "usuario_propio_id" }
                .thenByDescending { it.esFavorito }
                .thenBy { it.nombre.lowercase() }
        )
    }

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
                            val esYoA = jugadorA?.let { it.esUsuarioPropio || it.id == "usuario_propio_id" } ?: false
                            Text(
                                text = if (jugadorA == null) "Seleccionar A" else if (esYoA) "${jugadorA.nombre} (Tú)" else jugadorA.nombre,
                                fontSize = 12.sp,
                                color = if (esYoA) LimeVolt else Color.White,
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
                        jugadoresOrdenados.forEach { j ->
                            val esYo = j.esUsuarioPropio || j.id == "usuario_propio_id"
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (esYo) "${j.nombre} (Tú)" else j.nombre,
                                        color = if (esYo) LimeVolt else Color.White,
                                        fontWeight = if (esYo) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
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
                            val esYoB = jugadorB?.let { it.esUsuarioPropio || it.id == "usuario_propio_id" } ?: false
                            Text(
                                text = if (jugadorB == null) "Seleccionar B" else if (esYoB) "${jugadorB.nombre} (Tú)" else jugadorB.nombre,
                                fontSize = 12.sp,
                                color = if (esYoB) LimeVolt else Color.White,
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
                        jugadoresOrdenados.forEach { j ->
                            val esYo = j.esUsuarioPropio || j.id == "usuario_propio_id"
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (esYo) "${j.nombre} (Tú)" else j.nombre,
                                        color = if (esYo) LimeVolt else Color.White,
                                        fontWeight = if (esYo) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
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
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkCardBorder)
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
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
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
                                JugadorAvatar(fotoUri = jugadorA?.fotoUri, nombre = jugadorA?.nombre ?: "", tamano = 48.dp, permitirZoom = true)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(jugadorA?.nombreConTu() ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                JugadorAvatar(fotoUri = jugadorB?.fotoUri, nombre = jugadorB?.nombre ?: "", tamano = 48.dp, permitirZoom = true)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(jugadorB?.nombreConTu() ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                .sortedWith(
                    compareByDescending<DuoEstadisticas> { it.victorias }
                        .thenByDescending { it.porcentajeVictorias }
                        .thenByDescending { it.partidosJuntos }
                )
                .take(10)
        } else {
            duos.filter { it.partidosJuntos >= 1 }
                .sortedWith(
                    compareByDescending<DuoEstadisticas> { it.derrotas }
                        .thenBy { it.porcentajeVictorias }
                        .thenByDescending { it.partidosJuntos }
                )
                .take(10)
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
                    label = { Text("🏆 Con más victorias", fontSize = 11.sp) },
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
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DarkCardBorder)
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
                    fontSize = 11.sp,
                    permitirZoom = true
                )
                Box(modifier = Modifier.padding(start = 22.dp)) {
                    JugadorAvatar(
                        fotoUri = duo.jugador2.fotoUri,
                        nombre = duo.jugador2.nombre,
                        tamano = 36.dp,
                        fontSize = 11.sp,
                        bordeColor = DarkCard,
                        bordeAncho = 2.dp,
                        permitirZoom = true
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${duo.jugador1.nombreConTu()} & ${duo.jugador2.nombreConTu()}",
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
                    val colorPct = obtenerColorPorcentaje(duo.porcentajeVictorias)
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
