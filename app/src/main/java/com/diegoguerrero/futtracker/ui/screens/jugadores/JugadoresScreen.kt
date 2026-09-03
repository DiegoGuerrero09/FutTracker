package com.diegoguerrero.futtracker.ui.screens.jugadores

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
    onEliminarJugador: (Jugador) -> Unit
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    var jugadorAEliminar by remember { mutableStateOf<Jugador?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plantilla de Jugadores", fontWeight = FontWeight.Bold) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (jugadores.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay jugadores registrados.\nAgrega uno con el botón +",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = jugadores,
                        key = { it.id }
                    ) { jugador ->
                        JugadorItem(
                            jugador = jugador,
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
    onEliminar: () -> Unit
) {
    val primaryPosText = jugador.posicionesPrimarias.joinToString("/") { it.name }.ifEmpty { "-" }

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
                        text = primaryPosText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Column {
                    Text(
                        text = jugador.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (jugador.posicionesSecundarias.isNotEmpty()) {
                            BadgePosicion(
                                label = "Sec: " + jugador.posicionesSecundarias.joinToString("/") { it.name },
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
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

@Composable
private fun BadgePosicion(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Posiciones Primarias
                ExposedDropdownMenuBox(
                    expanded = expPrincipal,
                    onExpandedChange = { expPrincipal = !expPrincipal }
                ) {
                    OutlinedTextField(
                        value = posPrimarias.joinToString { it.name }.ifEmpty { "Seleccionar Primarias" },
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
                        value = posSecundarias.joinToString { it.name }.ifEmpty { "Ninguna (Opcional)" },
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