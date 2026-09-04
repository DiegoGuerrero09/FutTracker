package com.diegoguerrero.futtracker.ui.screens.alineacion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
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
import com.diegoguerrero.futtracker.domain.model.*
import com.diegoguerrero.futtracker.domain.usecase.GenerarAlineacionUseCase
import com.diegoguerrero.futtracker.ui.components.CampoFutbol
import com.diegoguerrero.futtracker.ui.theme.*
import kotlinx.coroutines.launch

import com.diegoguerrero.futtracker.ui.components.JugadorAvatar
import com.diegoguerrero.futtracker.ui.components.BadgePosicion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlineacionScreen(
    plantillaCompleta: List<Jugador>
) {
    var tipoFutbol by remember { mutableStateOf(TipoFutbol.FUTSAL) }
    var searchQuery by remember { mutableStateOf("") }
    var soloFavoritos by remember { mutableStateOf(false) }
    var posicionFiltro by remember { mutableStateOf<Posicion?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Limpiamos la plantilla completa de posibles duplicados por ID de base de datos
    val plantillaUnica = remember(plantillaCompleta) {
        plantillaCompleta.distinctBy { it.id }
    }

    var soloPosicionPrincipalFilter by remember { mutableStateOf(false) }

    val plantillaOrdenada = remember(plantillaUnica, searchQuery, soloFavoritos, posicionFiltro, soloPosicionPrincipalFilter) {
        plantillaUnica.filter { jugador ->
            val coincideBusqueda = searchQuery.isBlank() || jugador.nombre.contains(searchQuery, ignoreCase = true)
            val coincideFav = !soloFavoritos || jugador.esFavorito
            val coincidePos = posicionFiltro == null ||
                if (soloPosicionPrincipalFilter) {
                    posicionFiltro in jugador.posicionesPrimarias
                } else {
                    posicionFiltro in jugador.posicionesPrimarias ||
                    posicionFiltro in jugador.posicionesSecundarias
                }
            coincideBusqueda && coincideFav && coincidePos
        }.sortedWith(
            compareByDescending<Jugador> { it.esUsuarioPropio }
                .thenByDescending { it.esFavorito }
                .thenBy { it.nombre.lowercase() }
        )
    }

    val formacionesDisponibles = remember(tipoFutbol) {
        when (tipoFutbol) {
            TipoFutbol.FUTSAL -> FORMACIONES_FUTSAL
            TipoFutbol.FUT_6 -> FORMACIONES_FUT_6
            TipoFutbol.FUT_7 -> FORMACIONES_FUT_7
        }.sortedBy { it.nombre }
    }

    // Pestañas: 0 = "⭐ Sugerida", 1..N = formaciones específicas
    val opcionesFormacion = remember(formacionesDisponibles) {
        listOf("⭐ Sugerida") + formacionesDisponibles.map { it.nombre }
    }
    var tabSeleccionadaIndex by remember { mutableStateOf(0) }
    var formacionSeleccionada by remember { mutableStateOf(formacionesDisponibles.first()) }
    
    // Lista de convocados controlada por IDs estrictamente únicos
    val convocados = remember { mutableStateListOf<Jugador>() }
    var alineacionMapaCampo by remember { mutableStateOf<Map<Pair<Posicion, Pair<Float, Float>>, Jugador?>?>(null) }

    val useCase = remember { GenerarAlineacionUseCase() }

    LaunchedEffect(tipoFutbol) {
        tabSeleccionadaIndex = 0
        formacionSeleccionada = formacionesDisponibles.first()
        alineacionMapaCampo = null
    }

    LaunchedEffect(tabSeleccionadaIndex) {
        alineacionMapaCampo = null
        if (tabSeleccionadaIndex > 0) {
            formacionSeleccionada = formacionesDisponibles[tabSeleccionadaIndex - 1]
        }
    }

    // Sincronizar plantilla manteniendo unicidad y cortando si excede el límite del nuevo tipo de fútbol
    LaunchedEffect(plantillaUnica, tipoFutbol) {
        val actualizados = convocados.mapNotNull { convocado ->
            plantillaUnica.find { it.id == convocado.id }
        }.distinctBy { it.id }

        convocados.clear()
        convocados.addAll(actualizados.take(tipoFutbol.nJugadoresCampo))
    }

    val numRequerido = tipoFutbol.nJugadoresCampo

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pizarra", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkCard
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = tipoFutbol == TipoFutbol.FUTSAL,
                onClick = { tipoFutbol = TipoFutbol.FUTSAL },
                label = { Text("Futsal", fontSize = 12.sp) }
            )
            FilterChip(
                selected = tipoFutbol == TipoFutbol.FUT_6,
                onClick = { tipoFutbol = TipoFutbol.FUT_6 },
                label = { Text("Fútbol 6", fontSize = 12.sp) }
            )
            FilterChip(
                selected = tipoFutbol == TipoFutbol.FUT_7,
                onClick = { tipoFutbol = TipoFutbol.FUT_7 },
                label = { Text("Fútbol 7", fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Alineación", color = TextSecondary, fontSize = 13.sp)
        ScrollableTabRow(
            selectedTabIndex = tabSeleccionadaIndex,
            edgePadding = 0.dp,
            containerColor = DarkBackground
        ) {
            opcionesFormacion.forEachIndexed { index, nombre ->
                val isSelected = tabSeleccionadaIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { tabSeleccionadaIndex = index },
                    text = {
                        Text(
                            nombre,
                            color = if (isSelected) LimeVolt else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                val convocadosUnicos = convocados.distinctBy { it.id }

                // Si se eligió "Sugerida", la app elige la mejor formación automáticamente
                val formacionFinal = if (tabSeleccionadaIndex == 0) {
                    useCase.sugerirMejorFormacion(convocadosUnicos, formacionesDisponibles)
                } else {
                    formacionSeleccionada
                }
                formacionSeleccionada = formacionFinal

                val alineacionLista: List<Pair<Posicion, Jugador>> = useCase(convocadosUnicos, formacionFinal)
                val coords = obtenerCoordenadas(formacionFinal)

                val jugadoresAsignadosIds = mutableSetOf<String>()
                alineacionMapaCampo = coords.associateWith { (posicionCampo, _) ->
                    val asignacion = alineacionLista.firstOrNull { (posAsignada, jugador) -> 
                        posAsignada == posicionCampo && jugador.id !in jugadoresAsignadosIds
                    }?.second
                    
                    asignacion?.let { jugadoresAsignadosIds.add(it.id) }
                    asignacion
                }

                // Desplazar inmediatamente hacia arriba para ver el campo sin scrollear
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                }
            },
            enabled = convocados.size == numRequerido,
            colors = ButtonDefaults.buttonColors(
                containerColor = LimeVolt,
                contentColor = Color.Black,
                disabledContainerColor = LimeVolt.copy(alpha = 0.3f),
                disabledContentColor = Color.Black.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (tabSeleccionadaIndex == 0) "Generar alineación sugerida" else "Generar alineación",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Si la alineación está generada, aparece arriba inmediatamente
            alineacionMapaCampo?.let { mapaAlineacion ->
                item {
                    Text(
                        text = "Alineación (${formacionSeleccionada.nombre})",
                        color = LimeVolt,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CampoFutbol(
                        alineacion = mapaAlineacion,
                        modifier = Modifier.fillMaxWidth(),
                        onJugadorIntercambiado = { key1, key2 ->
                            val nuevoMapa = mapaAlineacion.toMutableMap()
                            val temp = nuevoMapa[key1]
                            nuevoMapa[key1] = nuevoMapa[key2]
                            nuevoMapa[key2] = temp
                            alineacionMapaCampo = nuevoMapa
                        }
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Convocados (${convocados.size}/$numRequerido)",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 15.sp
                    )

                    // Buscador con lupa y texto centrados
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(DarkCard, RoundedCornerShape(10.dp))
                            .border(1.dp, TextSecondary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (searchQuery.isEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Buscar jugador...",
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(LimeVolt),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(24.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Filtros por Favoritos y Posición
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = soloFavoritos,
                                onClick = { soloFavoritos = !soloFavoritos },
                                label = { Text("Favoritos", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (soloFavoritos) Icons.Default.Star else Icons.Outlined.StarOutline,
                                        contentDescription = null,
                                        tint = if (soloFavoritos) Color(0xFFFFD700) else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }

                        item {
                            FilterChip(
                                selected = posicionFiltro == null && !soloFavoritos,
                                onClick = {
                                    posicionFiltro = null
                                    soloFavoritos = false
                                },
                                label = { Text("Todos", fontSize = 11.sp) }
                            )
                        }

                        items(Posicion.entries.toTypedArray()) { pos ->
                            FilterChip(
                                selected = posicionFiltro == pos,
                                onClick = {
                                    posicionFiltro = if (posicionFiltro == pos) null else pos
                                },
                                label = { Text(pos.name, fontSize = 11.sp) }
                            )
                        }
                    }
                    if (posicionFiltro != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = !soloPosicionPrincipalFilter,
                                onClick = { soloPosicionPrincipalFilter = false },
                                label = { Text("Ambas posiciones", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = soloPosicionPrincipalFilter,
                                onClick = { soloPosicionPrincipalFilter = true },
                                label = { Text("Solo posición principal", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            items(
                items = plantillaOrdenada,
                key = { it.id }
            ) { jugador ->
                val isSelected = convocados.any { it.id == jugador.id }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isSelected) {
                                convocados.removeAll { it.id == jugador.id }
                            } else if (convocados.size < numRequerido) {
                                if (!convocados.any { it.id == jugador.id }) {
                                    convocados.add(jugador)
                                }
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (convocados.size < numRequerido && !convocados.any { it.id == jugador.id }) {
                                    convocados.add(jugador)
                                }
                            } else {
                                convocados.removeAll { it.id == jugador.id }
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = LimeVolt,
                            checkmarkColor = Color.Black,
                            uncheckedColor = TextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    JugadorAvatar(
                        fotoUri = jugador.fotoUri,
                        nombre = jugador.nombre,
                        tamano = 38.dp,
                        permitirZoom = true
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Nombre y posiciones debajo con principales sombreadas
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = jugador.nombre,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        val totalPos = jugador.posicionesPrimarias + jugador.posicionesSecundarias
                        if (totalPos.isEmpty()) {
                            Text(
                                text = "Sin posición asignada",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                jugador.posicionesPrimarias.forEach { pos ->
                                    BadgePosicion(label = pos.name, esPrimaria = true)
                                }
                                jugador.posicionesSecundarias.forEach { pos ->
                                    BadgePosicion(label = pos.name, esPrimaria = false)
                                }
                            }
                        }
                    }

                    if (jugador.esFavorito) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorito",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
}
