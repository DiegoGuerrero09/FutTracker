package com.diegoguerrero.futtracker.ui.screens.jugadores

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    onActualizarJugador: (Jugador) -> Unit,
    onEliminarJugador: (Jugador) -> Unit,
    onToggleFavorito: (Jugador) -> Unit
) {
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var jugadorAEditar by remember { mutableStateOf<Jugador?>(null) }
    var jugadorAEliminar by remember { mutableStateOf<Jugador?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedPosicionFilter by remember { mutableStateOf<Posicion?>(null) }
    var soloFavoritosFilter by remember { mutableStateOf(false) }
    var soloPosicionPrincipalFilter by remember { mutableStateOf(false) }

    val jugadoresFiltrados = remember(jugadores, searchQuery, selectedPosicionFilter, soloFavoritosFilter, soloPosicionPrincipalFilter) {
        jugadores.filter { jugador ->
            val coincideBusqueda = searchQuery.isBlank() ||
                jugador.nombre.contains(searchQuery, ignoreCase = true)

            val coincidePosicion = selectedPosicionFilter == null ||
                if (soloPosicionPrincipalFilter) {
                    jugador.posicionesPrimarias.contains(selectedPosicionFilter)
                } else {
                    jugador.posicionesPrimarias.contains(selectedPosicionFilter) ||
                    jugador.posicionesSecundarias.contains(selectedPosicionFilter)
                }

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
                onClick = { mostrarDialogoCrear = true },
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

            if (selectedPosicionFilter != null) {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
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
                            onClick = { jugadorAEditar = jugador },
                            onToggleFavorito = { onToggleFavorito(jugador) },
                            onEliminar = { jugadorAEliminar = jugador }
                        )
                    }
                }
            }
        }

        if (mostrarDialogoCrear) {
            DialogoJugador(
                jugadorExistente = null,
                onDismiss = { mostrarDialogoCrear = false },
                onGuardar = { nuevo ->
                    onAgregarJugador(nuevo)
                    mostrarDialogoCrear = false
                }
            )
        }

        jugadorAEditar?.let { jugador ->
            DialogoJugador(
                jugadorExistente = jugador,
                onDismiss = { jugadorAEditar = null },
                onGuardar = { actualizado ->
                    onActualizarJugador(actualizado)
                    jugadorAEditar = null
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
    onClick: () -> Unit,
    onToggleFavorito: () -> Unit,
    onEliminar: () -> Unit
) {
    // Lógica para aplicar elipsis visual o resumen si tiene 6 o más posiciones totales
    val totalPosiciones = jugador.posicionesPrimarias.union(jugador.posicionesSecundarias)
    val mostrarElipsis = totalPosiciones.size >= 6

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                        if (mostrarElipsis) {
                            val primerasVisibles = totalPosiciones.take(4)
                            primerasVisibles.forEach { pos ->
                                BadgePosicion(label = pos.name, esPrimaria = pos in jugador.posicionesPrimarias)
                            }
                            BadgePosicion(label = "+${totalPosiciones.size - 4}", esPrimaria = false)
                        } else {
                            jugador.posicionesPrimarias.forEach { pos ->
                                BadgePosicion(label = pos.name, esPrimaria = true)
                            }
                            jugador.posicionesSecundarias.forEach { pos ->
                                BadgePosicion(label = pos.name, esPrimaria = false)
                            }
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
private fun DialogoJugador(
    jugadorExistente: Jugador? = null,
    onDismiss: () -> Unit,
    onGuardar: (Jugador) -> Unit
) {
    var nombre by remember { mutableStateOf(jugadorExistente?.nombre ?: "") }
    var posPrimarias by remember { mutableStateOf(jugadorExistente?.posicionesPrimarias ?: setOf()) }
    var posSecundarias by remember { mutableStateOf(jugadorExistente?.posicionesSecundarias ?: setOf()) }
    var nivel by remember { mutableStateOf(jugadorExistente?.nivel ?: 3) }

    var expPrincipal by remember { mutableStateOf(false) }
    var expSecundaria by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (jugadorExistente == null) "Nuevo Jugador" else "Editar Jugador") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Nivel del jugador (1 al 5)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..5).forEach { lvl ->
                            FilterChip(
                                selected = nivel == lvl,
                                onClick = { nivel = lvl },
                                label = { Text("★ $lvl", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expPrincipal,
                    onExpandedChange = { expPrincipal = !expPrincipal }
                ) {
                    OutlinedTextField(
                        value = posPrimarias.joinToString(transform = { it.name }).ifEmpty { "Seleccionar posición" },
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
                        val jugadorGuardar = jugadorExistente?.copy(
                            nombre = nombre.trim(),
                            posicionesPrimarias = posPrimarias,
                            posicionesSecundarias = posSecundarias,
                            nivel = nivel
                        ) ?: Jugador(
                            nombre = nombre.trim(),
                            posicionesPrimarias = posPrimarias,
                            posicionesSecundarias = posSecundarias,
                            nivel = nivel
                        )
                        onGuardar(jugadorGuardar)
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