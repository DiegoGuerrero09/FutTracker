package com.diegoguerrero.futtracker.ui.screens.alineacion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegoguerrero.futtracker.domain.model.*
import com.diegoguerrero.futtracker.domain.usecase.GenerarAlineacionUseCase
import com.diegoguerrero.futtracker.ui.components.CampoFutbol
import com.diegoguerrero.futtracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlineacionScreen(
    plantillaCompleta: List<Jugador>
) {
    var tipoFutbol by remember { mutableStateOf(TipoFutbol.FUTSAL) }
    var searchQuery by remember { mutableStateOf("") }

    // Limpiamos la plantilla completa de posibles duplicados por ID de base de datos
    val plantillaUnica = remember(plantillaCompleta) {
        plantillaCompleta.distinctBy { it.id }
    }

    val plantillaOrdenada = remember(plantillaUnica, searchQuery) {
        plantillaUnica.filter {
            searchQuery.isBlank() || it.nombre.contains(searchQuery, ignoreCase = true)
        }.sortedWith(
            compareByDescending<Jugador> { it.esFavorito }
                .thenBy { it.nombre }
        )
    }

    val formacionesDisponibles = remember(tipoFutbol) {
        when (tipoFutbol) {
            TipoFutbol.FUTSAL -> FORMACIONES_FUTSAL
            TipoFutbol.FUT_6 -> FORMACIONES_FUT_6
            TipoFutbol.FUT_7 -> FORMACIONES_FUT_7
        }.sortedBy { it.nombre }
    }

    var formacionSeleccionada by remember { mutableStateOf(formacionesDisponibles.first()) }
    
    // Lista de convocados controlada por IDs estrictamente únicos
    val convocados = remember { mutableStateListOf<Jugador>() }
    var alineacionMapaCampo by remember { mutableStateOf<Map<Pair<Posicion, Pair<Float, Float>>, Jugador?>?>(null) }

    val useCase = remember { GenerarAlineacionUseCase() }

    LaunchedEffect(tipoFutbol) {
        formacionSeleccionada = formacionesDisponibles.first()
        alineacionMapaCampo = null
    }

    LaunchedEffect(formacionSeleccionada) {
        alineacionMapaCampo = null
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
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
                label = { Text("Fut 6", fontSize = 12.sp) }
            )
            FilterChip(
                selected = tipoFutbol == TipoFutbol.FUT_7,
                onClick = { tipoFutbol = TipoFutbol.FUT_7 },
                label = { Text("Fut 7", fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Alineación", color = TextSecondary, fontSize = 13.sp)
        ScrollableTabRow(
            selectedTabIndex = formacionesDisponibles.indexOf(formacionSeleccionada).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = DarkBackground
        ) {
            formacionesDisponibles.forEach { f ->
                Tab(
                    selected = formacionSeleccionada == f,
                    onClick = { formacionSeleccionada = f },
                    text = { Text(f.nombre, color = if (formacionSeleccionada == f) LimeVolt else TextSecondary, fontSize = 13.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                // Asegurar que los convocados pasados al caso de uso sean estrictamente únicos por ID
                val convocadosUnicos = convocados.distinctBy { it.id }
                val alineacionLista: List<Pair<Posicion, Jugador>> = useCase(convocadosUnicos, formacionSeleccionada)
                val coords = obtenerCoordenadas(formacionSeleccionada)

                // Mapeo limpio evitando asignaciones duplicadas de un mismo jugador en campo
                val jugadoresAsignadosIds = mutableSetOf<String>()
                alineacionMapaCampo = coords.associateWith { (posicionCampo, _) ->
                    val asignacion = alineacionLista.firstOrNull { (posAsignada, jugador) -> 
                        posAsignada == posicionCampo && jugador.id !in jugadoresAsignadosIds
                    }?.second
                    
                    asignacion?.let { jugadoresAsignadosIds.add(it.id) }
                    asignacion
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
            Text("Generar alineación", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            alineacionMapaCampo?.let { mapaAlineacion ->
                item {
                    Text(
                        text = "Alineación Optimizada (${formacionSeleccionada.nombre})",
                        color = LimeVolt,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CampoFutbol(
                        alineacion = mapaAlineacion,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Convocados (${convocados.size}/$numRequerido)",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 15.sp
                        )
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        placeholder = { Text("Buscar jugador...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LimeVolt,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
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
                    Text(
                        text = jugador.nombre,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
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