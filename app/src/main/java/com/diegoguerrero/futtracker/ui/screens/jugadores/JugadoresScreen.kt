package com.diegoguerrero.futtracker.ui.screens.jugadores

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JugadoresScreen(
    jugadores: List<Jugador>,
    onAgregarJugador: (Jugador) -> Unit,
    onEliminarJugador: (Jugador) -> Unit,
    onToggleFavorito: (Jugador) -> Unit
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    var jugadorAEliminar by remember { mutableStateOf<Jugador?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedPosicionFilter by remember { mutableStateOf<Posicion?>(null) }
    var soloFavoritosFilter by remember { mutableStateOf(false) }

    val jugadoresFiltrados = remember(jugadores, searchQuery, selectedPosicionFilter, soloFavoritosFilter) {
        jugadores.filter { jugador ->
            val coincideBusqueda = searchQuery.isBlank() ||
                jugador.nombre.contains(searchQuery, ignoreCase = true)

            val coincidePosicion = selectedPosicionFilter == null ||
                jugador.posicionesPrimarias.contains(selectedPosicionFilter) ||
                jugador.posicionesSecundarias.contains(selectedPosicionFilter)

            val coincideFavorito = !soloFavoritosFilter || jugador.esFavorito

            coincideBusqueda && coincidePosicion && coincideFavorito
        }.sortedWith(
            compareByDescending<Jugador> { it.esFavorito }
                .thenBy { it.nombre }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plantilla", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Jugador", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Buscador por nombre
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filtros rápidos
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = soloFavoritosFilter,
                        onClick = { soloFavoritosFilter = !soloFavoritosFilter },
                        label = { Text("Favoritos") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (soloFavoritosFilter) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = null,
                                tint = if (soloFavoritosFilter) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedPosicionFilter == null && !soloFavoritosFilter,
                        onClick = {
                            selectedPosicionFilter = null
                            soloFavoritosFilter = false
                        },
                        label = { Text("Todos") }
                    )
                }
                items(Posicion.entries.toTypedArray()) { pos ->
                    FilterChip(
                        selected = selectedPosicionFilter == pos,
                        onClick = {
                            selectedPosicionFilter = if (selectedPosicionFilter == pos) null else pos
                        },
                        label = { Text(pos.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (jugadoresFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (jugadores.isEmpty()) {
                            "No hay jugadores registrados.\nAgrega uno con el botón +"
                        } else {
                            "No se encontraron jugadores"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = jugadoresFiltrados,
                        key = { it.id }
                    ) { jugador ->
                        JugadorItem(
                            jugador = jugador,
                            onToggleFavorito = { onToggleFavorito(jugador) },
                            onEliminar = { jugadorAEliminar = jugador }
                        )
                    }
                }
            }
        }

        if (mostrarDialogo) {
            DialogoNuevoJugador(
                onDismiss = { mostrarDialogo = false },
                onGuardar = { nuevoJugador ->
                    onAgregarJugador(nuevoJugador)
                    mostrarDialogo = false
                }
            )
        }

        jugadorAEliminar?.let { jugador ->
            AlertDialog(
                onDismissRequest = { jugadorAEliminar = null },
                title = { Text("Eliminar jugador") },
                text = { Text("¿Estás seguro de que quieres eliminar a ${jugador.nombre}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onEliminarJugador(jugador)
                            jugadorAEliminar = null
                        }
                    ) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { jugadorAEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun JugadorItem(
    jugador: Jugador,
    onToggleFavorito: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = obtenerIniciales(jugador.nombre),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = jugador.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

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

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFavorito) {
                    Icon(
                        imageVector = if (jugador.esFavorito) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Marcar como favorito",
                        tint = if (jugador.esFavorito) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onEliminar) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgePosicion(label: String, esPrimaria: Boolean) {
    val bgColor = if (esPrimaria) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
    }
    val textColor = if (esPrimaria) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = if (esPrimaria) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            lineHeight = 14.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoNuevoJugador(
    onDismiss: () -> Unit,
    onGuardar: (Jugador) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var posPrimarias by remember { mutableStateOf(setOf<Posicion>()) }
    var posSecundarias by remember { mutableStateOf(setOf<Posicion>()) }

    var expPrincipal by remember { mutableStateOf(false) }
    var expSecundaria by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Jugador") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Posiciones Primarias
                ExposedDropdownMenuBox(
                    expanded = expPrincipal,
                    onExpandedChange = { expPrincipal = !expPrincipal }
                ) {
                    OutlinedTextField(
                        value = posPrimarias.joinToString(transform = { it.name }).ifEmpty { "Seleccionar Primarias" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Posiciones Primarias") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expPrincipal) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expPrincipal,
                        onDismissRequest = { expPrincipal = false }
                    ) {
                        Posicion.entries.forEach { pos ->
                            val isSelected = pos in posPrimarias
                            DropdownMenuItem(
                                text = { Text("${if (isSelected) "✓ " else ""}${pos.name}") },
                                onClick = {
                                    posPrimarias = if (isSelected) posPrimarias - pos else posPrimarias + pos
                                    posSecundarias = posSecundarias - pos
                                }
                            )
                        }
                    }
                }

                // Posiciones Secundarias
                ExposedDropdownMenuBox(
                    expanded = expSecundaria,
                    onExpandedChange = { expSecundaria = !expSecundaria }
                ) {
                    OutlinedTextField(
                        value = posSecundarias.joinToString(transform = { it.name }).ifEmpty { "Ninguna (Opcional)" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Posiciones Secundarias") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expSecundaria) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expSecundaria,
                        onDismissRequest = { expSecundaria = false }
                    ) {
                        Posicion.entries.filter { it !in posPrimarias }.forEach { pos ->
                            val isSelected = pos in posSecundarias
                            DropdownMenuItem(
                                text = { Text("${if (isSelected) "✓ " else ""}${pos.name}") },
                                onClick = {
                                    posSecundarias = if (isSelected) posSecundarias - pos else posSecundarias + pos
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank() && posPrimarias.isNotEmpty()) {
                        onGuardar(
                            Jugador(
                                nombre = nombre.trim(),
                                posicionesPrimarias = posPrimarias,
                                posicionesSecundarias = posSecundarias
                            )
                        )
                    }
                },
                enabled = nombre.isNotBlank() && posPrimarias.isNotEmpty()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

fun obtenerIniciales(nombre: String): String {
    val partes = nombre.trim().split("\\s+".toRegex())
    return when {
        partes.isEmpty() -> "??"
        partes.size == 1 -> partes[0].take(2).uppercase()
        else -> "${partes[0].take(1)}${partes[1].take(1)}".uppercase()
    }
}