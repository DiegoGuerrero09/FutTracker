package com.diegoguerrero.futtracker.ui.screens.enfrentamientos

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.diegoguerrero.futtracker.domain.model.DuoEstadisticas
import com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorCruzadas
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.ui.components.BadgePosicion
import com.diegoguerrero.futtracker.ui.components.FilaBadgesPosiciones
import com.diegoguerrero.futtracker.ui.components.JugadorAvatar
import com.diegoguerrero.futtracker.ui.components.SelectorRangoFechasDosBotones
import com.diegoguerrero.futtracker.ui.screens.partidos.PeriodoPartidos
import com.diegoguerrero.futtracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
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
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs: Individual vs Dúos
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
                                fontSize = 13.sp,
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
                    SeccionIndividual(
                        uiState = uiState,
                        onSeleccionarJugadorInspeccionado = { viewModel.seleccionarJugadorInspeccionado(it) },
                        onBusquedaJugadorInspeccionadoChange = { viewModel.setBusquedaJugadorInspeccionado(it) },
                        onToggleFavoritoInspeccionado = { viewModel.toggleSoloFavoritosInspeccionado() },
                        onPosicionInspeccionadoChange = { viewModel.setPosicionInspeccionado(it) },
                        onSoloPosicionPrincipalInspeccionadoChange = { viewModel.setSoloPosicionPrincipalInspeccionado(it) },
                        onPeriodoChange = { viewModel.setFiltroPeriodo(it) },
                        onAnioChange = { viewModel.setAnio(it) },
                        onTemporadaChange = { viewModel.setTemporada(it) },
                        onRangoFechasChange = { ini, fin -> viewModel.setRangoFechas(ini, fin) },
                        onFiltroTextoHistorialChange = { viewModel.setFiltroTexto(it) },
                        onFiltroHistorialChange = { viewModel.setFiltroHistorial(it) },
                        onToggleFavoritoHistorial = { viewModel.toggleFiltroSoloFavoritos() },
                        onPosicionHistorialChange = { viewModel.setFiltroPosicion(it) },
                        onSoloPosicionPrincipalHistorialChange = { viewModel.setFiltroSoloPosicionPrincipal(it) },
                        onOrdenCruzadoChange = { viewModel.setOrdenCruzado(it) },
                        onSeleccionarJugadorDetalle = { viewModel.seleccionarJugadorDetalle(it) }
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
        val targetJugador = uiState.todosLosJugadores.firstOrNull { it.id == uiState.jugadorSeleccionadoId }
        DialogoDetalleJugadorCruzado(
            detalle = detalle,
            jugadorTarget = targetJugador,
            onDismiss = { viewModel.seleccionarJugadorDetalle(null) }
        )
    }
}

@Composable
fun SeccionIndividual(
    uiState: EnfrentamientosUiState,
    onSeleccionarJugadorInspeccionado: (String) -> Unit,
    onBusquedaJugadorInspeccionadoChange: (String) -> Unit,
    onToggleFavoritoInspeccionado: () -> Unit,
    onPosicionInspeccionadoChange: (Posicion?) -> Unit,
    onSoloPosicionPrincipalInspeccionadoChange: (Boolean) -> Unit,
    onPeriodoChange: (PeriodoPartidos) -> Unit,
    onAnioChange: (Int) -> Unit,
    onTemporadaChange: (String) -> Unit,
    onRangoFechasChange: (Long, Long) -> Unit,
    onFiltroTextoHistorialChange: (String) -> Unit,
    onFiltroHistorialChange: (FiltroHistorial) -> Unit,
    onToggleFavoritoHistorial: () -> Unit,
    onPosicionHistorialChange: (Posicion?) -> Unit,
    onSoloPosicionPrincipalHistorialChange: (Boolean) -> Unit,
    onOrdenCruzadoChange: (OrdenHistorialCruzado) -> Unit,
    onSeleccionarJugadorDetalle: (EstadisticasJugadorCruzadas) -> Unit
) {
    val jugadorSeleccionado = uiState.todosLosJugadores.firstOrNull { it.id == uiState.jugadorSeleccionadoId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Filtro de Período (antes que el jugador según requerimiento)
        item {
            Text(
                text = "Periodo de tiempo:",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FiltroFechaStatsChips(
                periodoSeleccionado = uiState.filtroPeriodo,
                onPeriodoChange = onPeriodoChange,
                temporadaSeleccionada = uiState.temporadaSeleccionada,
                onTemporadaChange = onTemporadaChange,
                temporadasDisponibles = uiState.temporadasDisponibles,
                anioSeleccionado = uiState.anioSeleccionado,
                onAnioChange = onAnioChange,
                aniosDisponibles = uiState.aniosDisponibles,
                fechaInicio = uiState.fechaInicio,
                fechaFin = uiState.fechaFin,
                onRangoFechasChange = onRangoFechasChange
            )
        }

        // 2. Buscador y filtros para elegir el jugador a inspeccionar
        item {
            Text(
                text = "Jugador a analizar:",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(DarkCard, RoundedCornerShape(10.dp))
                    .border(1.dp, TextSecondary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (uiState.busquedaJugadorInspeccionado.isEmpty()) {
                            Text(
                                text = "Buscar jugador...",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                        BasicTextField(
                            value = uiState.busquedaJugadorInspeccionado,
                            onValueChange = onBusquedaJugadorInspeccionadoChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start
                            ),
                            cursorBrush = SolidColor(LimeVolt),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (uiState.busquedaJugadorInspeccionado.isNotEmpty()) {
                        IconButton(
                            onClick = { onBusquedaJugadorInspeccionadoChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Filtros rápidos del jugador a inspeccionar
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = uiState.soloFavoritosInspeccionado,
                        onClick = onToggleFavoritoInspeccionado,
                        label = { Text("★ Favoritos", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LimeVolt.copy(alpha = 0.2f),
                            selectedLabelColor = LimeVolt,
                            containerColor = DarkCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.soloFavoritosInspeccionado,
                            borderColor = if (uiState.soloFavoritosInspeccionado) LimeVolt else DarkCardBorder
                        )
                    )
                }

                item {
                    FilterChip(
                        selected = !uiState.soloPosicionPrincipalInspeccionado,
                        onClick = { onSoloPosicionPrincipalInspeccionadoChange(false) },
                        label = { Text("Ambas posiciones", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LimeVolt.copy(alpha = 0.2f),
                            selectedLabelColor = LimeVolt,
                            containerColor = DarkCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = !uiState.soloPosicionPrincipalInspeccionado,
                            borderColor = if (!uiState.soloPosicionPrincipalInspeccionado) LimeVolt else DarkCardBorder
                        )
                    )
                }

                item {
                    FilterChip(
                        selected = uiState.soloPosicionPrincipalInspeccionado,
                        onClick = { onSoloPosicionPrincipalInspeccionadoChange(true) },
                        label = { Text("Solo principal", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LimeVolt.copy(alpha = 0.2f),
                            selectedLabelColor = LimeVolt,
                            containerColor = DarkCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.soloPosicionPrincipalInspeccionado,
                            borderColor = if (uiState.soloPosicionPrincipalInspeccionado) LimeVolt else DarkCardBorder
                        )
                    )
                }
            }
        }

        // Selector horizontal de posiciones del jugador a inspeccionar
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.posicionInspeccionado == null,
                        onClick = { onPosicionInspeccionadoChange(null) },
                        label = { Text("Todas", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LimeVolt.copy(alpha = 0.2f),
                            selectedLabelColor = LimeVolt,
                            containerColor = DarkCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.posicionInspeccionado == null,
                            borderColor = if (uiState.posicionInspeccionado == null) LimeVolt else DarkCardBorder
                        )
                    )
                }

                items(Posicion.values()) { pos ->
                    val sel = uiState.posicionInspeccionado == pos
                    FilterChip(
                        selected = sel,
                        onClick = { onPosicionInspeccionadoChange(if (sel) null else pos) },
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

        // Carrusel horizontal de jugadores para seleccionar (el primero es "Tú", seleccionado por defecto)
        item {
            Text(
                text = "Seleccionar jugador:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (uiState.jugadoresFiltradosInspeccionados.isEmpty()) {
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
                    items(uiState.jugadoresFiltradosInspeccionados) { jug ->
                        val esSeleccionado = jug.id == jugadorSeleccionado?.id
                        Card(
                            modifier = Modifier
                                .clickable { onSeleccionarJugadorInspeccionado(jug.id) }
                                .width(88.dp)
                                .height(88.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (esSeleccionado) LimeVolt.copy(alpha = 0.15f) else DarkCard
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                if (esSeleccionado) 1.5.dp else 1.dp,
                                if (esSeleccionado) LimeVolt else if (jug.esFavorito) Color(0xFFFFD700) else DarkCardBorder
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    JugadorAvatar(
                                        fotoUri = jug.fotoUri,
                                        nombre = jug.nombre,
                                        tamano = 36.dp,
                                        fontSize = 12.sp,
                                        bordeColor = if (esSeleccionado) LimeVolt else if (jug.esFavorito) Color(0xFFFFD700) else Color.Transparent,
                                        bordeAncho = if (esSeleccionado) 1.5.dp else 1.dp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = jug.nombreConTu(),
                                            fontSize = 11.sp,
                                            fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Normal,
                                            color = if (esSeleccionado) LimeVolt else Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                        if (jug.esFavorito) {
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Favorito",
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }
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
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
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
                            FilaBadgesPosiciones(
                                primarias = jugadorSeleccionado.posicionesPrimarias,
                                secundarias = jugadorSeleccionado.posicionesSecundarias,
                                maxVisibles = 6
                            )
                        }
                    }
                }
            }
        }


        // Destacados para este jugador
        item {
            Text(
                text = "Destacados de ${jugadorSeleccionado?.nombreConTu() ?: "este jugador"}",
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

        // Separador y título de la sección comparativa cruzada
        item {
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = DarkCardBorder)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Comparativa con otros jugadores",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Filtro y buscador para los demás jugadores (compañeros y rivales)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(DarkCard, RoundedCornerShape(10.dp))
                        .border(1.dp, TextSecondary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (uiState.filtroTexto.isEmpty()) {
                            Text(
                                text = "Buscar jugador...",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                        BasicTextField(
                            value = uiState.filtroTexto,
                            onValueChange = onFiltroTextoHistorialChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start
                            ),
                            cursorBrush = SolidColor(LimeVolt),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (uiState.filtroTexto.isNotEmpty()) {
                        IconButton(
                            onClick = { onFiltroTextoHistorialChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(FiltroHistorial.values()) { filtro ->
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
                            onClick = onToggleFavoritoHistorial,
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
                            onClick = { onPosicionHistorialChange(null) },
                            label = { Text("Todas", fontSize = 11.sp) }
                        )
                    }
                    items(Posicion.entries.toTypedArray()) { pos ->
                        FilterChip(
                            selected = uiState.filtroPosicion == pos,
                            onClick = {
                                onPosicionHistorialChange(if (uiState.filtroPosicion == pos) null else pos)
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
                            onClick = { onSoloPosicionPrincipalHistorialChange(false) },
                            label = { Text("Ambas posiciones", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = uiState.filtroSoloPosicionPrincipal,
                            onClick = { onSoloPosicionPrincipalHistorialChange(true) },
                            label = { Text("Solo posición principal", fontSize = 11.sp) }
                        )
                    }
                }

                // Ordenación de la comparativa cruzada
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Text(
                            text = "Ordenar:",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(end = 2.dp)
                        )
                    }
                    items(
                        listOf(
                            OrdenHistorialCruzado.MAS_GANADOS_COMPANERO,
                            OrdenHistorialCruzado.MAS_PERDIDOS_COMPANERO,
                            OrdenHistorialCruzado.MAS_GANADOS_RIVAL,
                            OrdenHistorialCruzado.MAS_PERDIDOS_RIVAL
                        )
                    ) { ordenItem ->
                        val sel = uiState.ordenCruzado == ordenItem
                        val chipColor = when (ordenItem) {
                            OrdenHistorialCruzado.MAS_GANADOS_COMPANERO -> BlueCompanero
                            OrdenHistorialCruzado.MAS_PERDIDOS_COMPANERO -> RedLoss
                            OrdenHistorialCruzado.MAS_GANADOS_RIVAL -> LimeVolt
                            OrdenHistorialCruzado.MAS_PERDIDOS_RIVAL -> OrangeDraw
                            else -> LimeVolt
                        }
                        FilterChip(
                            selected = sel,
                            onClick = {
                                onOrdenCruzadoChange(if (sel) OrdenHistorialCruzado.DEFECTO else ordenItem)
                            },
                            label = { Text(ordenItem.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor.copy(alpha = 0.2f),
                                selectedLabelColor = chipColor,
                                containerColor = DarkCard,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = sel,
                                borderColor = if (sel) chipColor else Color.Transparent
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
                    onClick = { onSeleccionarJugadorDetalle(item) }
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
                        Column(verticalArrangement = Arrangement.Center) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = actualEst.jugador.nombreConTu(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (actualEst.jugador.esFavorito) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Favorito",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
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
        border = BorderStroke(if (item.jugador.esFavorito) 0.8.dp else 1.dp, if (item.jugador.esFavorito) Color(0xFFFFD700).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.16f))
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
                bordeColor = if (item.jugador.esFavorito) Color(0xFFFFD700).copy(alpha = 0.7f) else Color.Transparent,
                bordeAncho = 1.dp,
                permitirZoom = true
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
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
                    if (item.jugador.esFavorito) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorito",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    FilaBadgesPosiciones(
                        primarias = item.jugador.posicionesPrimarias,
                        secundarias = item.jugador.posicionesSecundarias,
                        maxVisibles = 3
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                val metricasTexto = buildString {
                    append("⚽ ${item.goles} • 🅰️ ${item.asistencias} • 🎯 ${item.tirosAlPalo} • 🚀 ${item.fueraArea} • 🧤 ${item.paradas}")
                    if (item.haJugadoPortero) {
                        append(" • 🥅 ${item.golesEncajados}")
                    }
                }
                Text(
                    text = metricasTexto,
                    fontSize = 9.sp,
                    color = LimeVolt,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

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
    jugadorTarget: Jugador? = null,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                JugadorAvatar(
                    fotoUri = detalle.jugador.fotoUri,
                    nombre = detalle.jugador.nombre,
                    tamano = 64.dp,
                    fontSize = 20.sp,
                    bordeColor = if (detalle.jugador.esFavorito) Color(0xFFFFD700).copy(alpha = 0.7f) else LimeVolt,
                    bordeAncho = 1.5.dp,
                    permitirZoom = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = detalle.jugador.nombreConTu(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (detalle.jugador.esFavorito) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorito",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                FilaBadgesPosiciones(
                    primarias = detalle.jugador.posicionesPrimarias,
                    secundarias = detalle.jugador.posicionesSecundarias,
                    modifier = Modifier.padding(vertical = 4.dp),
                    maxVisibles = 6
                )

                Spacer(modifier = Modifier.height(14.dp))

                val nombreTarget = jugadorTarget?.nombreConTu() ?: "Tú"
                val nombreDetalle = detalle.jugador.nombre

                // Bloque: Total
                val totalPartidos = detalle.totalPartidos
                val victoriasTotal = detalle.victoriasComoCompanero + detalle.victoriasComoRival
                val empatesTotal = detalle.empatesComoCompanero + detalle.empatesComoRival
                val derrotasTotal = detalle.derrotasComoCompanero + detalle.derrotasComoRival
                val porcentajeVictoriasTotal = if (totalPartidos > 0) (victoriasTotal.toFloat() / totalPartidos) * 100f else 0f
                val golesFavorTotal = detalle.golesFavorComoCompanero + detalle.golesFavorComoRival
                val golesContraTotal = detalle.golesContraComoCompanero + detalle.golesContraComoRival
                val diferenciaGolesTotal = golesFavorTotal - golesContraTotal

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Total",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LimeVolt
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Partidos compartidos:", fontSize = 12.sp, color = TextSecondary)
                            Text("$totalPartidos", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Balance (V / E / D):", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                "${victoriasTotal}V - ${empatesTotal}E - ${derrotasTotal}D",
                                fontSize = 12.sp,
                                color = if (victoriasTotal >= derrotasTotal) GreenWin else RedLoss,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("% Victorias:", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                String.format(Locale.getDefault(), "%.1f%%", porcentajeVictoriasTotal),
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
                                "${golesFavorTotal} - ${golesContraTotal} (${if (diferenciaGolesTotal >= 0) "+$diferenciaGolesTotal" else "$diferenciaGolesTotal"})",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TablaMetricasComparativas(
                            targetNombre = nombreTarget,
                            detalleNombre = nombreDetalle,
                            golesTarget = detalle.golesTarget,
                            golesDetalle = detalle.goles,
                            golesDiestraTarget = detalle.golesDiestraTarget,
                            golesDiestraDetalle = detalle.golesDiestra,
                            golesZurdaTarget = detalle.golesZurdaTarget,
                            golesZurdaDetalle = detalle.golesZurda,
                            golesCabezaTarget = detalle.golesCabezaTarget,
                            golesCabezaDetalle = detalle.golesCabeza,
                            golesOtroTarget = detalle.golesOtroTarget,
                            golesOtroDetalle = detalle.golesOtro,
                            asistenciasTarget = detalle.asistenciasTarget,
                            asistenciasDetalle = detalle.asistencias,
                            palosTarget = detalle.tirosAlPaloTarget,
                            palosDetalle = detalle.tirosAlPalo,
                            fueraAreaTarget = detalle.fueraAreaTarget,
                            fueraAreaDetalle = detalle.fueraArea,
                            chilenaTarget = detalle.chilenaTarget,
                            chilenaDetalle = detalle.chilena,
                            taconTarget = detalle.taconTarget,
                            taconDetalle = detalle.tacon,
                            haJugadoPorteroTarget = detalle.haJugadoPorteroTarget,
                            golesEncajadosTarget = detalle.golesEncajadosTarget,
                            haJugadoPorteroDetalle = detalle.haJugadoPortero,
                            golesEncajadosDetalle = detalle.golesEncajados,
                            paradasTarget = detalle.paradasTarget,
                            paradasDetalle = detalle.paradas
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bloque: Como compañero
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BlueCompanero.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Como compañero",
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

                        Spacer(modifier = Modifier.height(10.dp))
                        TablaMetricasComparativas(
                            targetNombre = nombreTarget,
                            detalleNombre = nombreDetalle,
                            golesTarget = detalle.golesTargetComoCompaneroInd,
                            golesDetalle = detalle.golesComoCompaneroInd,
                            golesDiestraTarget = detalle.golesDiestraTargetComoCompaneroInd,
                            golesDiestraDetalle = detalle.golesDiestraComoCompaneroInd,
                            golesZurdaTarget = detalle.golesZurdaTargetComoCompaneroInd,
                            golesZurdaDetalle = detalle.golesZurdaComoCompaneroInd,
                            golesCabezaTarget = detalle.golesCabezaTargetComoCompaneroInd,
                            golesCabezaDetalle = detalle.golesCabezaComoCompaneroInd,
                            golesOtroTarget = detalle.golesOtroTargetComoCompaneroInd,
                            golesOtroDetalle = detalle.golesOtroComoCompaneroInd,
                            asistenciasTarget = detalle.asistenciasTargetComoCompaneroInd,
                            asistenciasDetalle = detalle.asistenciasComoCompaneroInd,
                            palosTarget = detalle.tirosAlPaloTargetComoCompaneroInd,
                            palosDetalle = detalle.tirosAlPaloComoCompaneroInd,
                            fueraAreaTarget = detalle.fueraAreaTargetComoCompaneroInd,
                            fueraAreaDetalle = detalle.fueraAreaComoCompaneroInd,
                            chilenaTarget = detalle.chilenaTargetComoCompaneroInd,
                            chilenaDetalle = detalle.chilenaComoCompaneroInd,
                            taconTarget = detalle.taconTargetComoCompaneroInd,
                            taconDetalle = detalle.taconComoCompaneroInd,
                            haJugadoPorteroTarget = detalle.haJugadoPorteroTargetComoCompaneroInd,
                            golesEncajadosTarget = detalle.golesEncajadosTargetComoCompaneroInd,
                            haJugadoPorteroDetalle = detalle.haJugadoPorteroComoCompaneroInd,
                            golesEncajadosDetalle = detalle.golesEncajadosComoCompaneroInd,
                            paradasTarget = detalle.paradasTargetComoCompaneroInd,
                            paradasDetalle = detalle.paradasComoCompaneroInd
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bloque: Como rival
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, OrangeDraw.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Como rival",
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

                        Spacer(modifier = Modifier.height(10.dp))
                        TablaMetricasComparativas(
                            targetNombre = nombreTarget,
                            detalleNombre = nombreDetalle,
                            golesTarget = detalle.golesTargetComoRivalInd,
                            golesDetalle = detalle.golesComoRivalInd,
                            golesDiestraTarget = detalle.golesDiestraTargetComoRivalInd,
                            golesDiestraDetalle = detalle.golesDiestraComoRivalInd,
                            golesZurdaTarget = detalle.golesZurdaTargetComoRivalInd,
                            golesZurdaDetalle = detalle.golesZurdaComoRivalInd,
                            golesCabezaTarget = detalle.golesCabezaTargetComoRivalInd,
                            golesCabezaDetalle = detalle.golesCabezaComoRivalInd,
                            golesOtroTarget = detalle.golesOtroTargetComoRivalInd,
                            golesOtroDetalle = detalle.golesOtroComoRivalInd,
                            asistenciasTarget = detalle.asistenciasTargetComoRivalInd,
                            asistenciasDetalle = detalle.asistenciasComoRivalInd,
                            palosTarget = detalle.tirosAlPaloTargetComoRivalInd,
                            palosDetalle = detalle.tirosAlPaloComoRivalInd,
                            fueraAreaTarget = detalle.fueraAreaTargetComoRivalInd,
                            fueraAreaDetalle = detalle.fueraAreaComoRivalInd,
                            chilenaTarget = detalle.chilenaTargetComoRivalInd,
                            chilenaDetalle = detalle.chilenaComoRivalInd,
                            taconTarget = detalle.taconTargetComoRivalInd,
                            taconDetalle = detalle.taconComoRivalInd,
                            haJugadoPorteroTarget = detalle.haJugadoPorteroTargetComoRivalInd,
                            golesEncajadosTarget = detalle.golesEncajadosTargetComoRivalInd,
                            haJugadoPorteroDetalle = detalle.haJugadoPorteroComoRivalInd,
                            golesEncajadosDetalle = detalle.golesEncajadosComoRivalInd,
                            paradasTarget = detalle.paradasTargetComoRivalInd,
                            paradasDetalle = detalle.paradasComoRivalInd
                        )
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

@Composable
private fun TablaMetricasComparativas(
    targetNombre: String,
    detalleNombre: String,
    golesTarget: Int,
    golesDetalle: Int,
    golesDiestraTarget: Int = 0,
    golesDiestraDetalle: Int = 0,
    golesZurdaTarget: Int = 0,
    golesZurdaDetalle: Int = 0,
    golesCabezaTarget: Int = 0,
    golesCabezaDetalle: Int = 0,
    golesOtroTarget: Int = 0,
    golesOtroDetalle: Int = 0,
    asistenciasTarget: Int,
    asistenciasDetalle: Int,
    palosTarget: Int,
    palosDetalle: Int,
    fueraAreaTarget: Int,
    fueraAreaDetalle: Int,
    chilenaTarget: Int,
    chilenaDetalle: Int,
    taconTarget: Int,
    taconDetalle: Int,
    haJugadoPorteroTarget: Boolean,
    golesEncajadosTarget: Int,
    haJugadoPorteroDetalle: Boolean,
    golesEncajadosDetalle: Int,
    paradasTarget: Int,
    paradasDetalle: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Métrica",
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1.3f)
        )
        Text(
            text = targetNombre,
            fontSize = 11.sp,
            color = LimeVolt,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = detalleNombre,
            fontSize = 11.sp,
            color = OrangeDraw,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }

    HorizontalDivider(
        modifier = Modifier.padding(vertical = 6.dp),
        color = Color.White.copy(alpha = 0.1f)
    )

    FilaMetricaComparativa("⚽", "Goles", "$golesTarget", "$golesDetalle")
    FilaMetricaComparativa("🦵", "Diestra", "$golesDiestraTarget", "$golesDiestraDetalle")
    FilaMetricaComparativa("🦶", "Zurda", "$golesZurdaTarget", "$golesZurdaDetalle")
    FilaMetricaComparativa("🗣️", "Cabeza", "$golesCabezaTarget", "$golesCabezaDetalle")
    FilaMetricaComparativa("✨", "Otros", "$golesOtroTarget", "$golesOtroDetalle")
    FilaMetricaComparativa("🅰️", "Asistencias", "$asistenciasTarget", "$asistenciasDetalle")
    FilaMetricaComparativa("🎯", "Palos", "$palosTarget", "$palosDetalle")
    FilaMetricaComparativa("🚀", "Fuera área", "$fueraAreaTarget", "$fueraAreaDetalle")
    FilaMetricaComparativa("🤸", "Chilena", "$chilenaTarget", "$chilenaDetalle")
    FilaMetricaComparativa("👟", "Tacón", "$taconTarget", "$taconDetalle")
    FilaMetricaComparativa("🧤", "Paradas", "$paradasTarget", "$paradasDetalle")

    if (haJugadoPorteroTarget && haJugadoPorteroDetalle) {
        FilaMetricaComparativa("🥅", "Goles enc.", "$golesEncajadosTarget", "$golesEncajadosDetalle")
    }
}

@Composable
private fun FilaMetricaComparativa(
    icono: String,
    label: String,
    valA: String,
    valB: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1.3f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(icono, fontSize = 12.sp)
            Text(label, fontSize = 11.sp, color = TextSecondary)
        }
        Text(
            text = valA,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valB,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SeccionDuos(duos: List<DuoEstadisticas>) {
    var mostrarSoloGanadores by remember { mutableStateOf(true) }

    val duosFiltrados = remember(duos, mostrarSoloGanadores) {
        if (mostrarSoloGanadores) {
            duos.filter { it.partidosJuntos >= 5 }
                .sortedWith(
                    compareByDescending<DuoEstadisticas> { it.victorias }
                        .thenByDescending { it.porcentajeVictorias }
                        .thenByDescending { it.partidosJuntos }
                )
                .take(10)
        } else {
            duos.filter { it.partidosJuntos >= 5 }
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
                text = "Conoce qué duplas obtienen mejores o peores resultados jugando en el mismo bando (mínimo 5 partidos).",
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
                            text = "No hay datos suficientes de parejas con al menos 5 partidos jugando juntas.",
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

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
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

@Composable
private fun FiltroFechaStatsChips(
    periodoSeleccionado: PeriodoPartidos,
    onPeriodoChange: (PeriodoPartidos) -> Unit,
    temporadaSeleccionada: String,
    onTemporadaChange: (String) -> Unit,
    temporadasDisponibles: List<String>,
    anioSeleccionado: Int,
    onAnioChange: (Int) -> Unit,
    aniosDisponibles: List<Int>,
    fechaInicio: Long,
    fechaFin: Long,
    onRangoFechasChange: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Fila 1: Opciones principales
        val opciones = listOf(
            PeriodoPartidos.TOTAL to "Total",
            PeriodoPartidos.TEMPORADA to "Temporada",
            PeriodoPartidos.ANIO_NATURAL to "Año",
            PeriodoPartidos.RANGO_FECHAS to "Por fecha",
            PeriodoPartidos.ULTIMOS_MESES to "Últimos meses",
            PeriodoPartidos.ULTIMAS_SEMANAS to "Últimas semanas"
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(opciones) { (periodo, label) ->
                FilterChip(
                    selected = periodoSeleccionado == periodo,
                    onClick = {
                        onPeriodoChange(periodo)
                    },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        // Fila 2: Sub-filtros
        when (periodoSeleccionado) {
            PeriodoPartidos.TOTAL,
            PeriodoPartidos.ULTIMOS_MESES,
            PeriodoPartidos.ULTIMAS_SEMANAS -> {}
            PeriodoPartidos.TEMPORADA -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(temporadasDisponibles) { temp ->
                        FilterChip(
                            selected = temporadaSeleccionada == temp,
                            onClick = { onTemporadaChange(temp) },
                            label = { Text(temp, fontSize = 12.sp) }
                        )
                    }
                }
            }
            PeriodoPartidos.ANIO_NATURAL -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(aniosDisponibles) { anio ->
                        FilterChip(
                            selected = anioSeleccionado == anio,
                            onClick = { onAnioChange(anio) },
                            label = { Text("$anio", fontSize = 12.sp) }
                        )
                    }
                }
            }
            PeriodoPartidos.RANGO_FECHAS -> {
                SelectorRangoFechasDosBotones(
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin,
                    onRangoChange = onRangoFechasChange
                )
            }
        }
    }
}
