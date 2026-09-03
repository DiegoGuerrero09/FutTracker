package com.diegoguerrero.futtracker.ui.screens.partidos

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.domain.model.TipoFutbol
import com.diegoguerrero.futtracker.ui.components.GraficoResultados
import com.diegoguerrero.futtracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartidosScreen(
    partidos: List<Partido>,
    jugadores: List<Jugador>,
    onAgregarPartido: (Partido) -> Unit,
    onActualizarPartido: (Partido) -> Unit,
    onEliminarPartido: (Partido) -> Unit
) {
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var partidoAEditar by remember { mutableStateOf<Partido?>(null) }
    var partidoAEliminar by remember { mutableStateOf<Partido?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partidos", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoCrear = true },
                containerColor = LimeVolt,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar Partido")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Resumen de partidos y gráfica
            item {
                GraficoResultados(partidos = partidos)
            }

            // Encabezado de la lista
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historial (${partidos.size})",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (partidos.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Aún no has registrado partidos",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Toca el botón + para registrar tu primer encuentro",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(
                    items = partidos,
                    key = { it.id }
                ) { partido ->
                    PartidoItem(
                        partido = partido,
                        jugadores = jugadores,
                        onEditar = { partidoAEditar = partido },
                        onEliminar = { partidoAEliminar = partido }
                    )
                }
            }
        }
    }

    if (mostrarDialogoCrear) {
        DialogoPartido(
            partidoExistente = null,
            jugadoresDisponibles = jugadores,
            onDismiss = { mostrarDialogoCrear = false },
            onGuardar = { nuevo ->
                onAgregarPartido(nuevo)
                mostrarDialogoCrear = false
            }
        )
    }

    partidoAEditar?.let { partido ->
        DialogoPartido(
            partidoExistente = partido,
            jugadoresDisponibles = jugadores,
            onDismiss = { partidoAEditar = null },
            onGuardar = { actualizado ->
                onActualizarPartido(actualizado)
                partidoAEditar = null
            }
        )
    }

    partidoAEliminar?.let { partido ->
        AlertDialog(
            onDismissRequest = { partidoAEliminar = null },
            title = { Text("Eliminar partido") },
            text = { Text("¿Deseas eliminar este registro del partido?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEliminarPartido(partido)
                        partidoAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { partidoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PartidoItem(
    partido: Partido,
    jugadores: List<Jugador>,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val fechaFormateada = remember(partido.fecha) { sdf.format(Date(partido.fecha)) }

    val estadoColor = when {
        partido.esVictoria -> Color(0xFF4CAF50)
        partido.esEmpate -> Color(0xFFFFB300)
        else -> Color(0xFFE53935)
    }

    val estadoTexto = when {
        partido.esVictoria -> "Victoria"
        partido.esEmpate -> "Empate"
        else -> "Derrota"
    }

    val modalidadTexto = when (partido.modoJuego) {
        TipoFutbol.FUTSAL -> "Futsal"
        TipoFutbol.FUT_6 -> "Fut 6"
        TipoFutbol.FUT_7 -> "Fut 7"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditar() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Fila superior: Fecha, Modalidad y Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = fechaFormateada, color = TextSecondary, fontSize = 12.sp)

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = modalidadTexto,
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEditar, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEliminar, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Marcador y badge de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${partido.golesAFavor} - ${partido.golesEnContra}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Surface(
                    color = estadoColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = estadoTexto,
                        color = estadoColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Estadísticas personales: Posición, Goles y Asistencias
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Posición: ", color = TextSecondary, fontSize = 12.sp)
                    Surface(
                        color = LimeVolt.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = partido.posicionJugada.name,
                            color = LimeVolt,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⚽ ${partido.goles} goles", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "👟 ${partido.asistencias} asist.", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Notas si las hay
            if (partido.notas.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = partido.notas,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            // Jugadores del partido opcionales
            if (partido.jugadoresIds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Jugadores del partido:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val companeros = jugadores.filter { it.id in partido.jugadoresIds }
                    items(companeros) { comp ->
                        Surface(
                            color = Color.White.copy(alpha = 0.07f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = comp.nombre,
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoPartido(
    partidoExistente: Partido? = null,
    jugadoresDisponibles: List<Jugador>,
    onDismiss: () -> Unit,
    onGuardar: (Partido) -> Unit
) {
    val context = LocalContext.current

    var fechaMillis by remember { mutableStateOf(partidoExistente?.fecha ?: System.currentTimeMillis()) }
    var modoJuego by remember { mutableStateOf(partidoExistente?.modoJuego ?: TipoFutbol.FUTSAL) }
    var golesAFavor by remember { mutableStateOf(partidoExistente?.golesAFavor ?: 0) }
    var golesEnContra by remember { mutableStateOf(partidoExistente?.golesEnContra ?: 0) }
    var posicionJugada by remember { mutableStateOf(partidoExistente?.posicionJugada ?: Posicion.DC) }
    var misGoles by remember { mutableStateOf(partidoExistente?.goles ?: 0) }
    var misAsistencias by remember { mutableStateOf(partidoExistente?.asistencias ?: 0) }
    var notas by remember { mutableStateOf(partidoExistente?.notas ?: "") }
    val jugadoresIds = remember { mutableStateListOf<String>().apply { addAll(partidoExistente?.jugadoresIds ?: emptyList()) } }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val cal = Calendar.getInstance()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (partidoExistente == null) "Nuevo Partido" else "Editar Partido",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Selector de Fecha
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            cal.timeInMillis = fechaMillis
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth)
                                    }
                                    fechaMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .background(DarkCard, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fecha del partido", color = TextSecondary, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = sdf.format(Date(fechaMillis)),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Event, contentDescription = null, tint = LimeVolt, modifier = Modifier.size(18.dp))
                    }
                }

                // Selector de Modalidad
                Column {
                    Text("Modalidad", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            TipoFutbol.FUTSAL to "Futsal",
                            TipoFutbol.FUT_6 to "Fut 6",
                            TipoFutbol.FUT_7 to "Fut 7"
                        ).forEach { (tipo, label) ->
                            FilterChip(
                                selected = modoJuego == tipo,
                                onClick = { modoJuego = tipo },
                                label = { Text(label, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Resultado del partido
                Column {
                    Text("Resultado (Marcador)", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepperInput(
                            label = "A favor",
                            value = golesAFavor,
                            onValueChange = { golesAFavor = it.coerceAtLeast(0) }
                        )
                        Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        StepperInput(
                            label = "En contra",
                            value = golesEnContra,
                            onValueChange = { golesEnContra = it.coerceAtLeast(0) }
                        )
                    }
                }

                // Posición jugada
                Column {
                    Text("Posición jugada", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(Posicion.entries.toTypedArray()) { pos ->
                            FilterChip(
                                selected = posicionJugada == pos,
                                onClick = { posicionJugada = pos },
                                label = { Text(pos.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Mis estadísticas
                Column {
                    Text("Mis estadísticas", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StepperInput(
                            label = "⚽ Goles",
                            value = misGoles,
                            onValueChange = { misGoles = it.coerceAtLeast(0) }
                        )
                        StepperInput(
                            label = "👟 Asistencias",
                            value = misAsistencias,
                            onValueChange = { misAsistencias = it.coerceAtLeast(0) }
                        )
                    }
                }

                // Notas
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas / Crónica del partido") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Jugadores del partido (opcionales)
                if (jugadoresDisponibles.isNotEmpty()) {
                    Column {
                        Text("Jugadores del partido (opcionales)", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(jugadoresDisponibles) { jugador ->
                                val selected = jugador.id in jugadoresIds
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        if (selected) jugadoresIds.remove(jugador.id)
                                        else jugadoresIds.add(jugador.id)
                                    },
                                    label = { Text(jugador.nombre, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = (partidoExistente ?: Partido()).copy(
                        fecha = fechaMillis,
                        modoJuego = modoJuego,
                        golesAFavor = golesAFavor,
                        golesEnContra = golesEnContra,
                        posicionJugada = posicionJugada,
                        goles = misGoles,
                        asistencias = misAsistencias,
                        notas = notas.trim(),
                        jugadoresIds = jugadoresIds.toList()
                    )
                    onGuardar(p)
                },
                colors = ButtonDefaults.buttonColors(containerColor = LimeVolt, contentColor = Color.Black)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun StepperInput(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(
                onClick = { onValueChange(value - 1) },
                modifier = Modifier.size(32.dp),
                enabled = value > 0
            ) {
                Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Text(
                text = "$value",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.widthIn(min = 24.dp)
            )

            FilledTonalIconButton(
                onClick = { onValueChange(value + 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
