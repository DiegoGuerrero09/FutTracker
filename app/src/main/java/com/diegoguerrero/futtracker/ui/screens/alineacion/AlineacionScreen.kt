package com.diegoguerrero.futtracker.ui.screens.alineacion

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.SortByAlpha
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.SwapHoriz
import kotlin.math.roundToInt
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
    var soloPosicionPrincipalFilter by remember { mutableStateOf(false) }
    var ordenNombreAscendente by remember { mutableStateOf<Boolean?>(null) }
    var ordenFechaDescendente by remember { mutableStateOf<Boolean?>(null) }
    var jugadorSeleccionadoPizarraParaMover by remember { mutableStateOf<Jugador?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Limpiamos la plantilla completa de posibles duplicados por ID de base de datos
    val plantillaUnica = remember(plantillaCompleta) {
        plantillaCompleta.distinctBy { it.id }
    }

    val plantillaOrdenada = remember(plantillaUnica, searchQuery, soloFavoritos, posicionFiltro, soloPosicionPrincipalFilter, ordenNombreAscendente, ordenFechaDescendente) {
        val filtrados = plantillaUnica.filter { jugador ->
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
        }
        filtrados.sortedWith { a, b ->
            when {
                ordenNombreAscendente != null -> {
                    if (ordenNombreAscendente == true) a.nombre.compareTo(b.nombre, ignoreCase = true)
                    else b.nombre.compareTo(a.nombre, ignoreCase = true)
                }
                ordenFechaDescendente != null -> {
                    if (ordenFechaDescendente == true) b.fechaCreacion.compareTo(a.fechaCreacion)
                    else a.fechaCreacion.compareTo(b.fechaCreacion)
                }
                else -> {
                    val userA = a.esUsuarioPropio
                    val userB = b.esUsuarioPropio
                    if (userA != userB) return@sortedWith if (userB) 1 else -1
                    val favA = a.esFavorito
                    val favB = b.esFavorito
                    if (favA != favB) return@sortedWith if (favB) 1 else -1
                    a.nombre.compareTo(b.nombre, ignoreCase = true)
                }
            }
        }
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
        Text("Seleccione la modalidad:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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
                FilterChip(
                    selected = tipoFutbol == tipo,
                    onClick = { tipoFutbol = tipo },
                    label = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
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

        val listaAlineados = remember(alineacionMapaCampo) {
            alineacionMapaCampo?.entries
                ?.sortedWith(compareBy<Map.Entry<Pair<Posicion, Pair<Float, Float>>, Jugador?>> { it.key.first.ordinal }.thenBy { it.key.second.first })
                ?.mapNotNull { entry -> entry.value?.let { entry.key.first to it } }
                ?: emptyList()
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Si la alineación está generada, aparece arriba inmediatamente
            val mapaAlineacion = alineacionMapaCampo
            if (mapaAlineacion != null) {
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
                        onJugadorIntercambiado = null
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = LimeVolt,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (jugadorSeleccionadoPizarraParaMover != null) {
                                        "Seleccionado: ${jugadorSeleccionadoPizarraParaMover?.nombreConTu()}. Toca otro para intercambiar."
                                    } else {
                                        "Arrastra o toca jugadores para intercambiar posiciones"
                                    },
                                    color = if (jugadorSeleccionadoPizarraParaMover != null) LimeVolt else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (jugadorSeleccionadoPizarraParaMover != null) FontWeight.Bold else FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            listaAlineados.forEachIndexed { index, (pos, jugador) ->
                                ItemJugadorAlineado(
                                    index = index,
                                    jugador = jugador,
                                    posicion = pos,
                                    esSeleccionado = jugadorSeleccionadoPizarraParaMover?.id == jugador.id,
                                    onDragVertical = { dragY ->
                                        val offsetItems = (dragY / 36.dp.toPx()).roundToInt()
                                        val targetIdx = (index + offsetItems).coerceIn(0, listaAlineados.lastIndex)
                                        if (targetIdx != index) {
                                            val targetJugador = listaAlineados[targetIdx].second
                                            val nuevoMapa = mapaAlineacion.toMutableMap()
                                            val key1 = nuevoMapa.entries.firstOrNull { it.value?.id == jugador.id }?.key
                                            val key2 = nuevoMapa.entries.firstOrNull { it.value?.id == targetJugador.id }?.key
                                            if (key1 != null && key2 != null) {
                                                nuevoMapa[key1] = targetJugador
                                                nuevoMapa[key2] = jugador
                                                alineacionMapaCampo = nuevoMapa
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (jugadorSeleccionadoPizarraParaMover == null) {
                                            jugadorSeleccionadoPizarraParaMover = jugador
                                        } else if (jugadorSeleccionadoPizarraParaMover?.id == jugador.id) {
                                            jugadorSeleccionadoPizarraParaMover = null
                                        } else {
                                            val seleccionado = jugadorSeleccionadoPizarraParaMover!!
                                            val nuevoMapa = mapaAlineacion.toMutableMap()
                                            val key1 = nuevoMapa.entries.firstOrNull { it.value?.id == seleccionado.id }?.key
                                            val key2 = nuevoMapa.entries.firstOrNull { it.value?.id == jugador.id }?.key
                                            if (key1 != null && key2 != null) {
                                                nuevoMapa[key1] = jugador
                                                nuevoMapa[key2] = seleccionado
                                                alineacionMapaCampo = nuevoMapa
                                            }
                                            jugadorSeleccionadoPizarraParaMover = null
                                        }
                                    }
                                )
                            }
                        }
                    }
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

                    // Fila 1: Favoritos y Ordenar
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
                                selected = ordenNombreAscendente != null,
                                onClick = {
                                    ordenFechaDescendente = null
                                    ordenNombreAscendente = when (ordenNombreAscendente) {
                                        null -> true
                                        true -> false
                                        false -> null
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                        contentDescription = "Ordenar por nombre",
                                        tint = if (ordenNombreAscendente != null) LimeVolt else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = when (ordenNombreAscendente) {
                                            true -> "Nombre (A-Z)"
                                            false -> "Nombre (Z-A)"
                                            null -> "Nombre"
                                        },
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }

                        item {
                            FilterChip(
                                selected = ordenFechaDescendente != null,
                                onClick = {
                                    ordenNombreAscendente = null
                                    ordenFechaDescendente = when (ordenFechaDescendente) {
                                        null -> true
                                        true -> false
                                        false -> null
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Ordenar por fecha",
                                        tint = if (ordenFechaDescendente != null) LimeVolt else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = when (ordenFechaDescendente) {
                                            true -> "Añadido recientemente"
                                            false -> "Más antiguos primero"
                                            null -> "Fecha"
                                        },
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }
                    }

                    // Fila 2: Posiciones
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = posicionFiltro == null,
                                onClick = { posicionFiltro = null },
                                label = { Text("Todas", fontSize = 11.sp) }
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) LimeVolt.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)
                    )
                ) {
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
                            .padding(horizontal = 10.dp, vertical = 10.dp),
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

                        Spacer(modifier = Modifier.width(6.dp))

                        JugadorAvatar(
                            fotoUri = jugador.fotoUri,
                            nombre = jugador.nombreConTu(),
                            tamano = 44.dp,
                            permitirZoom = true
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Nombre y posiciones debajo con principales sombreadas
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = jugador.nombreConTu(),
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
}

@Composable
private fun ItemJugadorAlineado(
    index: Int,
    jugador: Jugador,
    posicion: Posicion,
    esSeleccionado: Boolean,
    onDragVertical: Density.(dragY: Float) -> Unit,
    onClick: () -> Unit
) {
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .zIndex(if (isDragging) 99f else 1f)
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .pointerInput(jugador.id) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        val dragY = offsetY
                        isDragging = false
                        offsetY = 0f
                        val umbralVertical = 20.dp.toPx()
                        if (kotlin.math.abs(dragY) > umbralVertical) {
                            onDragVertical(dragY)
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetY += dragAmount.y
                    }
                )
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (esSeleccionado) LimeVolt.copy(alpha = 0.28f) else DarkCard,
        border = BorderStroke(if (esSeleccionado || isDragging) 1.5.dp else 1.dp, if (esSeleccionado || isDragging) LimeVolt else DarkCardBorder),
        shadowElevation = if (isDragging) 6.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Arrastrar",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${index + 1}.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.width(6.dp))
            JugadorAvatar(
                fotoUri = jugador.fotoUri,
                nombre = jugador.nombre,
                tamano = 26.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = jugador.nombreConTu(),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                color = Color(0xFF13151E),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.width(36.dp).height(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = posicion.name,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = LimeVolt,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
