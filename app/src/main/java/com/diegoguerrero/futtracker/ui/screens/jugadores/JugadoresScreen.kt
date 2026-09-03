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
                            onEliminar = { onEliminarJugador(jugador) }
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
    }
}

@Composable
private fun JugadorItem(
    jugador: Jugador,
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
                        text = jugador.posPrincipal.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Column {
                    Text(
                        text = jugador.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    jugador.posSecundaria?.let { sec ->
                        Spacer(modifier = Modifier.height(2.dp))
                        BadgePosicion(sec.name, MaterialTheme.colorScheme.secondary)
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
private fun BadgePosicion(nombre: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = nombre,
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
    var posPrincipal by remember { mutableStateOf(Posicion.DFC) }
    var posSecundaria by remember { mutableStateOf<Posicion?>(null) }

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

                // Dropdown Posición Principal
                ExposedDropdownMenuBox(
                    expanded = expPrincipal,
                    onExpandedChange = { expPrincipal = !expPrincipal }
                ) {
                    OutlinedTextField(
                        value = posPrincipal.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Posición Principal") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expPrincipal) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expPrincipal,
                        onDismissRequest = { expPrincipal = false }
                    ) {
                        Posicion.entries.forEach { pos ->
                            DropdownMenuItem(
                                text = { Text(pos.name) },
                                onClick = {
                                    posPrincipal = pos
                                    expPrincipal = false
                                }
                            )
                        }
                    }
                }

                // Dropdown Posición Secundaria
                ExposedDropdownMenuBox(
                    expanded = expSecundaria,
                    onExpandedChange = { expSecundaria = !expSecundaria }
                ) {
                    OutlinedTextField(
                        value = posSecundaria?.name ?: "Ninguna",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Posición Secundaria (Opcional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expSecundaria) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expSecundaria,
                        onDismissRequest = { expSecundaria = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ninguna") },
                            onClick = {
                                posSecundaria = null
                                expSecundaria = false
                            }
                        )
                        Posicion.entries.forEach { pos ->
                            DropdownMenuItem(
                                text = { Text(pos.name) },
                                onClick = {
                                    posSecundaria = pos
                                    expSecundaria = false
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
                    if (nombre.isNotBlank()) {
                        onGuardar(
                            Jugador(
                                nombre = nombre.trim(),
                                posPrincipal = posPrincipal,
                                posSecundaria = posSecundaria
                            )
                        )
                    }
                },
                enabled = nombre.isNotBlank()
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