package com.diegoguerrero.futtracker.ui.screens.sorteos

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
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

    var totalJugadoresSeleccionados by remember { mutableStateOf(10) } // 10, 12, 14
    var equipoClaro by remember { mutableStateOf<List<Jugador>>(emptyList()) }
    var equipoOscuro by remember { mutableStateOf<List<Jugador>>(emptyList()) }
    var sorteoRealizado by remember { mutableStateOf(false) }

    // Usamos mutableStateListOf en lugar de mutableStateSetOf
    val idsConvocados = remember { mutableStateListOf<String>() }
    var mostrarSelectorManual by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var soloFavoritos by remember { mutableStateOf(false) }

    // Inicializar convocados por defecto si está vacío
    LaunchedEffect(jugadores) {
        if (idsConvocados.isEmpty() && jugadores.isNotEmpty()) {
            idsConvocados.clear()
            jugadores.take(totalJugadoresSeleccionados).forEach { idsConvocados.add(it.id) }
        }
    }

    fun realizarSorteo(equilibrado: Boolean) {
        val listaConvocados = jugadores.filter { it.id in idsConvocados }
        if (listaConvocados.isEmpty()) return

        val seleccionados = if (equilibrado) {
            val (favoritos, normales) = listaConvocados.partition { it.esFavorito }
            val favShuffled = favoritos.shuffled()
            val normShuffled = normales.shuffled()

            val claroTemp = mutableListOf<Jugador>()
            val oscuroTemp = mutableListOf<Jugador>()

            var turnoClaro = true
            (favShuffled + normShuffled).forEach { jugador ->
                if (turnoClaro) claroTemp.add(jugador) else oscuroTemp.add(jugador)
                turnoClaro = !turnoClaro
            }
            Pair(claroTemp.shuffled(), oscuroTemp.shuffled())
        } else {
            val mezclados = listaConvocados.shuffled()
            val mitad = mezclados.size / 2
            Pair(mezclados.take(mitad), mezclados.drop(mitad))
        }

        equipoClaro = seleccionados.first
        equipoOscuro = seleccionados.second
        sorteoRealizado = true
    }

    fun compartirTextoWhatsApp(context: Context) {
        val textoCompartir = buildString {
            append("🎲 *SORTEO DE EQUIPOS* 🎲\n\n")
            append("⚪ *EQUIPO CLARO*:\n")
            equipoClaro.forEachIndexed { index, it -> append("${index + 1}. ${it.nombre}\n") }
            append("\n")
            append("⚫ *EQUIPO OSCURO*:\n")
            equipoOscuro.forEachIndexed { index, it -> append("${index + 1}. ${it.nombre}\n") }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textoCompartir)
        }
        
        try {
            val chooser = Intent.createChooser(intent, "Compartir Sorteo")
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Error silencioso
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Selecciona la modalidad de jugadores:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10 to "10 (Futsal)", 12 to "12 (Fútbol 6)", 14 to "14 (Fútbol 7)").forEach { (cantidad, label) ->
                        FilterChip(
                            selected = totalJugadoresSeleccionados == cantidad,
                            onClick = { 
                                totalJugadoresSeleccionados = cantidad
                                idsConvocados.clear()
                                jugadores.take(cantidad).forEach { idsConvocados.add(it.id) }
                            },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Convocados para el sorteo: ${idsConvocados.size}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    TextButton(onClick = { mostrarSelectorManual = !mostrarSelectorManual }) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (mostrarSelectorManual) "Ocultar selector" else "Seleccionar jugadores")
                    }
                }
            }

            if (mostrarSelectorManual) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Buscar en plantilla...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = null)
                                        }
                                    }
                                },
                                singleLine = true
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = soloFavoritos,
                                    onClick = { soloFavoritos = !soloFavoritos },
                                    label = { Text("Solo favoritos") },
                                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700)) }
                                )
                            }

                            val filtrados = jugadores.filter { 
                                (searchQuery.isBlank() || it.nombre.contains(searchQuery, ignoreCase = true)) &&
                                (!soloFavoritos || it.esFavorito)
                            }

                            if (filtrados.isEmpty()) {
                                Text("No hay jugadores encontrados.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        filtrados.forEach { j ->
                                            val seleccionado = j.id in idsConvocados
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        if (seleccionado) idsConvocados.remove(j.id)
                                                        else idsConvocados.add(j.id)
                                                    }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(j.nombre, fontSize = 13.sp)
                                                Checkbox(
                                                    checked = seleccionado,
                                                    onCheckedChange = { check ->
                                                        if (check) {
                                                            if (!idsConvocados.contains(j.id)) idsConvocados.add(j.id)
                                                        } else {
                                                            idsConvocados.remove(j.id)
                                                        }
                                                    }
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

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { realizarSorteo(equilibrado = false) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeVolt, contentColor = Color.Black),
                        enabled = idsConvocados.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aleatorio", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { realizarSorteo(equilibrado = true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                        enabled = idsConvocados.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Equilibrado", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            if (sorteoRealizado) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.LightGray)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("⚪ EQUIPO CLARO", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                equipoClaro.forEachIndexed { index, jugador ->
                                    Text("${index + 1}. ${jugador.nombre}", fontSize = 12.sp, color = Color.DarkGray)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("⚫ EQUIPO OSCURO", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                equipoOscuro.forEachIndexed { index, jugador ->
                                    Text("${index + 1}. ${jugador.nombre}", fontSize = 12.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}