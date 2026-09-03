package com.diegoguerrero.futtracker.ui.screens.sorteos

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.ui.theme.LimeVolt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SorteosScreen(
    jugadores: List<Jugador>
) {
    val context = LocalContext.current

    var totalJugadoresSeleccionados by remember { mutableStateOf(10) } // 10 (Futsal), 12 (Fut6), 14 (Fut7)
    var equipoClaro by remember { mutableStateOf<List<Jugador>>(emptyList()) }
    var equipoOscuro by remember { mutableStateOf<List<Jugador>>(emptyList()) }
    var sorteoRealizado by remember { mutableStateOf(false) }

    val jugadoresDisponibles = remember(jugadores) { jugadores.shuffled() }

    fun realizarSorteo() {
        if (jugadores.isEmpty()) return
        val cantidadReal = minOf(totalJugadoresSeleccionados, jugadoresDisponibles.size)
        val seleccionados = jugadoresDisponibles.take(cantidadReal).shuffled()
        val mitad = seleccionados.size / 2
        equipoClaro = seleccionados.take(mitad)
        equipoOscuro = seleccionados.drop(mitad)
        sorteoRealizado = true
    }

    fun compartirTextoWhatsApp(context: Context) {
        val textoCompartir = buildString {
            append("🎲 *SORTEO DE EQUIPOS* 🎲\n\n")
            append("⚪ *EQUIPO CLARO*:\n")
            equipoClaro.forEach { append("• ${it.nombre}\n") }
            append("\n")
            append("⚫ *EQUIPO OSCURO*:\n")
            equipoOscuro.forEach { append("• ${it.nombre}\n") }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textoCompartir)
        }
        
        try {
            val chooser = Intent.createChooser(intent, "Compartir Sorteo")
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Manejo de error silencioso si no hay apps compatibles
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sorteo de Equipos", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (sorteoRealizado) {
                        IconButton(onClick = { compartirTextoWhatsApp(context) }) {
                            Icon(Icons.Default.Share, contentDescription = "Enviar por WhatsApp", tint = LimeVolt)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Selecciona la modalidad de jugadores:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(10 to "10 (Futsal)", 12 to "12 (Fútbol 6)", 14 to "14 (Fútbol 7)").forEach { (cantidad, label) ->
                    FilterChip(
                        selected = totalJugadoresSeleccionados == cantidad,
                        onClick = { totalJugadoresSeleccionados = cantidad },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Button(
                onClick = { realizarSorteo() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = LimeVolt, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Casino, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Realizar Sorteo Aleatorio", fontWeight = FontWeight.Bold)
            }

            if (!sorteoRealizado) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (jugadores.isEmpty()) "No hay jugadores en la base de datos" else "Configura los jugadores y pulsa sortear",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        TarjetaEquipo(
                            titulo = "⚪ Equipo Claro (${equipoClaro.size})",
                            colorFondo = Color(0xFFF0F0F0),
                            colorTexto = Color.Black,
                            jugadores = equipoClaro
                        )
                    }
                    item {
                        TarjetaEquipo(
                            titulo = "⚫ Equipo Oscuro (${equipoOscuro.size})",
                            colorFondo = Color(0xFF2C2C2C),
                            colorTexto = Color.White,
                            jugadores = equipoOscuro
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaEquipo(
    titulo: String,
    colorFondo: Color,
    colorTexto: Color,
    jugadores: List<Jugador>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colorTexto)
            Spacer(modifier = Modifier.height(8.dp))
            if (jugadores.isEmpty()) {
                Text("Sin jugadores asignados", fontSize = 14.sp, color = colorTexto)
            } else {
                jugadores.forEach { jugador ->
                    Text("• ${jugador.nombre}", fontSize = 14.sp, color = colorTexto, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}