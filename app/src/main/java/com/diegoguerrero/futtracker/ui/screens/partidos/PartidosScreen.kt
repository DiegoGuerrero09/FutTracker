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
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.domain.model.TipoFutbol
import com.diegoguerrero.futtracker.ui.components.GraficoResultados
import com.diegoguerrero.futtracker.ui.components.JugadorAvatar
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
                    containerColor = LimeVolt,
                    titleContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoCrear = true },
                containerColor = LimeVolt,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar partido", tint = Color.Black)
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

            // Estadísticas personales: Posiciones, Goles y Asistencias
            val posicionesMostrar = partido.posicionesJugadas.ifEmpty { setOf(partido.posicionJugada) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (posicionesMostrar.size > 1) "Posiciones: " else "Posición: ",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    posicionesMostrar.forEach { pos ->
                        Surface(
                            color = LimeVolt.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = pos.name,
                                color = LimeVolt,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⚽ ${partido.goles} goles", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "👟 ${partido.asistencias} asist.", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Desglose de goles si los hay
            val detallesGoles = buildList {
                if (partido.golesDiestra > 0) add("Diestra: ${partido.golesDiestra}")
                if (partido.golesZurda > 0) add("Zurda: ${partido.golesZurda}")
                if (partido.golesCabeza > 0) add("Cabeza: ${partido.golesCabeza}")
                if (partido.golesTacon > 0) add("Tacón: ${partido.golesTacon}")
                if (partido.golesChilena > 0) add("Chilena: ${partido.golesChilena}")
                if (partido.golesOtro > 0) add("Otro: ${partido.golesOtro}")
            }

            if (detallesGoles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Goles:", color = TextSecondary, fontSize = 11.sp)
                    detallesGoles.forEach { detalle ->
                        Surface(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = detalle,
                                color = LimeVolt,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
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

            // Jugadores del partido por equipo
            val tieneEquipos = partido.jugadoresMiEquipo.isNotEmpty() || partido.jugadoresEquipoRival.isNotEmpty()
            if (tieneEquipos) {
                Spacer(modifier = Modifier.height(8.dp))
                if (partido.jugadoresMiEquipo.isNotEmpty()) {
                    Text(text = "Mi equipo:", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(3.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val companeros = jugadores.filter { it.id in partido.jugadoresMiEquipo }
                        items(companeros) { comp ->
                            val esYo = comp.esUsuarioPropio || comp.id == "usuario_propio_id"
                            Surface(
                                color = if (esYo) LimeVolt.copy(alpha = 0.35f) else LimeVolt.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp),
                                border = if (esYo) BorderStroke(1.dp, LimeVolt) else null
                            ) {
                                Text(
                                    text = if (esYo) "${comp.nombre} (Tú)" else comp.nombre,
                                    color = LimeVolt,
                                    fontSize = 10.sp,
                                    fontWeight = if (esYo) FontWeight.Bold else FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                if (partido.jugadoresEquipoRival.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Equipo rival:", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(3.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val rivales = jugadores.filter { it.id in partido.jugadoresEquipoRival }
                        items(rivales) { riv ->
                            Surface(
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = riv.nombre,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            } else if (partido.jugadoresIds.isNotEmpty()) {
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

    val posicionesJugadas = remember {
        mutableStateListOf<Posicion>().apply {
            if (partidoExistente != null) {
                addAll(partidoExistente.posicionesJugadas.ifEmpty { setOf(partidoExistente.posicionJugada) })
            } else {
                add(Posicion.DC)
            }
        }
    }

    var misGoles by remember { mutableStateOf(partidoExistente?.goles ?: 0) }
    var misAsistencias by remember { mutableStateOf(partidoExistente?.asistencias ?: 0) }
    var tirosAlPalo by remember { mutableStateOf(partidoExistente?.tirosAlPalo ?: 0) }

    var golesZurda by remember { mutableStateOf(partidoExistente?.golesZurda ?: 0) }
    var golesDiestra by remember { mutableStateOf(partidoExistente?.golesDiestra ?: 0) }
    var golesCabeza by remember { mutableStateOf(partidoExistente?.golesCabeza ?: 0) }
    var golesOtro by remember { mutableStateOf(partidoExistente?.golesOtro ?: 0) }
    var golesChilena by remember { mutableStateOf(partidoExistente?.golesChilena ?: 0) }
    var golesTacon by remember { mutableStateOf(partidoExistente?.golesTacon ?: 0) }
    var golesFueraArea by remember { mutableStateOf(partidoExistente?.golesFueraArea ?: 0) }

    var notas by remember { mutableStateOf(partidoExistente?.notas ?: "") }

    val jugadoresMiEquipo = remember {
        mutableStateListOf<String>().apply {
            if (partidoExistente != null) {
                addAll(partidoExistente.jugadoresMiEquipo.ifEmpty { partidoExistente.jugadoresIds })
            } else {
                val usuario = jugadoresDisponibles.firstOrNull { it.esUsuarioPropio || it.id == "usuario_propio_id" }
                if (usuario != null) {
                    add(usuario.id)
                }
            }
        }
    }
    val jugadoresEquipoRival = remember {
        mutableStateListOf<String>().apply {
            if (partidoExistente != null) {
                addAll(partidoExistente.jugadoresEquipoRival)
            }
        }
    }

    var tabEquipoJugadores by remember { mutableStateOf(0) }
    var busquedaJugador by remember { mutableStateOf("") }
    var filtroSoloFavoritos by remember { mutableStateOf(false) }
    var filtroPosicion by remember { mutableStateOf<Posicion?>(null) }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val cal = Calendar.getInstance()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (partidoExistente == null) "Nuevo partido" else "Editar partido",
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

                // Selector de Modalidad (centrado horizontalmente)
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
                                label = {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, fontSize = 12.sp, textAlign = TextAlign.Center)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Resultado del partido (alineación simétrica y guión perfectamente centrado con los botones)
                Column {
                    Text("Resultado (marcador)", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = "A favor", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(
                                    onClick = { golesAFavor = (golesAFavor - 1).coerceAtLeast(0) },
                                    modifier = Modifier.size(32.dp),
                                    enabled = golesAFavor > 0
                                ) {
                                    Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Text(
                                    text = "$golesAFavor",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    color = Color.White
                                )
                                FilledTonalIconButton(
                                    onClick = { golesAFavor++ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                        Text(
                            text = "-",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 18.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = "En contra", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(
                                    onClick = { golesEnContra = (golesEnContra - 1).coerceAtLeast(0) },
                                    modifier = Modifier.size(32.dp),
                                    enabled = golesEnContra > 0
                                ) {
                                    Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Text(
                                    text = "$golesEnContra",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    color = Color.White
                                )
                                FilledTonalIconButton(
                                    onClick = { golesEnContra++ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }

                // Posiciones jugadas (permite seleccionar varias)
                Column {
                    Text("Posición / posiciones jugadas", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(Posicion.entries.toTypedArray()) { pos ->
                            val isSelected = pos in posicionesJugadas
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        if (posicionesJugadas.size > 1) posicionesJugadas.remove(pos)
                                    } else {
                                        posicionesJugadas.add(pos)
                                    }
                                },
                                label = { Text(pos.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Mis estadísticas (Goles, asistencias y tiros al palo)
                Column {
                    Text("Mis estadísticas", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            StepperInput(
                                label = "⚽ Goles",
                                value = misGoles,
                                onValueChange = { nuevoTotal ->
                                    val diff = nuevoTotal - misGoles
                                    if (diff > 0) {
                                        golesDiestra += diff
                                    } else if (diff < 0) {
                                        var porQuitar = -diff
                                        if (golesOtro >= porQuitar) { golesOtro -= porQuitar; porQuitar = 0 } else { porQuitar -= golesOtro; golesOtro = 0 }
                                        if (porQuitar > 0 && golesCabeza >= porQuitar) { golesCabeza -= porQuitar; porQuitar = 0 } else { porQuitar -= golesCabeza; golesCabeza = 0 }
                                        if (porQuitar > 0 && golesZurda >= porQuitar) { golesZurda -= porQuitar; porQuitar = 0 } else { porQuitar -= golesZurda; golesZurda = 0 }
                                        if (porQuitar > 0) { golesDiestra = (golesDiestra - porQuitar).coerceAtLeast(0) }
                                    }
                                    misGoles = (golesDiestra + golesZurda + golesCabeza + golesOtro).coerceAtLeast(0)
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            StepperInput(
                                label = "👟 Asistencias",
                                value = misAsistencias,
                                onValueChange = { misAsistencias = it.coerceAtLeast(0) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            StepperInput(
                                label = "🥅 Palos",
                                value = tirosAlPalo,
                                onValueChange = { tirosAlPalo = it.coerceAtLeast(0) }
                            )
                        }
                    }

                    // Detalle de goles: Parte del cuerpo (conteo base)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Parte del cuerpo (suman al total de goles):",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val partesCuerpo = listOf(
                        Triple("Diestra", golesDiestra) { d: Int ->
                            golesDiestra = (golesDiestra + d).coerceAtLeast(0)
                            misGoles = golesDiestra + golesZurda + golesCabeza + golesOtro
                        },
                        Triple("Zurda", golesZurda) { d: Int ->
                            golesZurda = (golesZurda + d).coerceAtLeast(0)
                            misGoles = golesDiestra + golesZurda + golesCabeza + golesOtro
                        },
                        Triple("Cabeza", golesCabeza) { d: Int ->
                            golesCabeza = (golesCabeza + d).coerceAtLeast(0)
                            misGoles = golesDiestra + golesZurda + golesCabeza + golesOtro
                        },
                        Triple("Otro", golesOtro) { d: Int ->
                            golesOtro = (golesOtro + d).coerceAtLeast(0)
                            misGoles = golesDiestra + golesZurda + golesCabeza + golesOtro
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        partesCuerpo.forEach { (nombre, cantidad, update) ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { update(1) },
                                color = if (cantidad > 0) LimeVolt.copy(alpha = 0.2f) else DarkCard,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (cantidad > 0) LimeVolt else DarkCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = nombre,
                                        fontSize = 11.sp,
                                        color = if (cantidad > 0) LimeVolt else Color.White,
                                        fontWeight = if (cantidad > 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (cantidad > 0) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "$cantidad",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = LimeVolt
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable { update(-1) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("-", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Atributos extra acumulativos (no excluyentes con la parte del cuerpo)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Atributos extra (acumulativos en los goles):",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val atributosExtra = listOf(
                        Triple("Fuera área", golesFueraArea) { d: Int ->
                            golesFueraArea = (golesFueraArea + d).coerceIn(0, misGoles.coerceAtLeast(1))
                        },
                        Triple("Tacón", golesTacon) { d: Int ->
                            golesTacon = (golesTacon + d).coerceIn(0, misGoles.coerceAtLeast(1))
                        },
                        Triple("Chilena", golesChilena) { d: Int ->
                            golesChilena = (golesChilena + d).coerceIn(0, misGoles.coerceAtLeast(1))
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        atributosExtra.forEach { (nombre, cantidad, update) ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { update(1) },
                                color = if (cantidad > 0) LimeVolt.copy(alpha = 0.2f) else DarkCard,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (cantidad > 0) LimeVolt else DarkCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = nombre,
                                        fontSize = 10.sp,
                                        color = if (cantidad > 0) LimeVolt else Color.White,
                                        fontWeight = if (cantidad > 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (cantidad > 0) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "$cantidad",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = LimeVolt
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable { update(-1) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("-", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Jugadores del partido por equipo con buscador interactivo
                if (jugadoresDisponibles.isNotEmpty()) {
                    val jugadoresFiltrados = remember(jugadoresDisponibles, busquedaJugador, filtroSoloFavoritos, filtroPosicion) {
                        jugadoresDisponibles.filter { j ->
                            val matchText = busquedaJugador.isBlank() || j.nombre.contains(busquedaJugador.trim(), ignoreCase = true)
                            val matchFav = !filtroSoloFavoritos || j.esFavorito
                            val matchPos = filtroPosicion == null || (j.posicionesPrimarias.contains(filtroPosicion) || j.posicionesSecundarias.contains(filtroPosicion))
                            matchText && matchFav && matchPos
                        }
                    }

                    Column {
                        Text("Jugadores participantes (por equipo)", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Pestañas Mi equipo / Rival
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = tabEquipoJugadores == 0,
                                onClick = { tabEquipoJugadores = 0 },
                                label = {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("Mi equipo (${jugadoresMiEquipo.size})", fontSize = 11.sp, textAlign = TextAlign.Center)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = tabEquipoJugadores == 1,
                                onClick = { tabEquipoJugadores = 1 },
                                label = {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("Equipo rival (${jugadoresEquipoRival.size})", fontSize = 11.sp, textAlign = TextAlign.Center)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Buscador y filtros de posición / favoritos
                        OutlinedTextField(
                            value = busquedaJugador,
                            onValueChange = { busquedaJugador = it },
                            placeholder = { Text("Buscar jugador...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            trailingIcon = {
                                if (busquedaJugador.isNotEmpty()) {
                                    IconButton(onClick = { busquedaJugador = "" }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                FilterChip(
                                    selected = filtroSoloFavoritos,
                                    onClick = { filtroSoloFavoritos = !filtroSoloFavoritos },
                                    leadingIcon = {
                                        Icon(
                                            if (filtroSoloFavoritos) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (filtroSoloFavoritos) LimeVolt else TextSecondary
                                        )
                                    },
                                    label = { Text("Favs", fontSize = 11.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filtroPosicion == null,
                                    onClick = { filtroPosicion = null },
                                    label = { Text("Todas", fontSize = 11.sp) }
                                )
                            }
                            items(Posicion.entries.toTypedArray()) { pos ->
                                FilterChip(
                                    selected = filtroPosicion == pos,
                                    onClick = {
                                        filtroPosicion = if (filtroPosicion == pos) null else pos
                                    },
                                    label = { Text(pos.name, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Lista de jugadores filtrados
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(jugadoresFiltrados) { jugador ->
                                val enMiEquipo = jugador.id in jugadoresMiEquipo
                                val enRival = jugador.id in jugadoresEquipoRival
                                val seleccionadoActual = if (tabEquipoJugadores == 0) enMiEquipo else enRival

                                FilterChip(
                                    selected = seleccionadoActual,
                                    onClick = {
                                        if (tabEquipoJugadores == 0) {
                                            if (enMiEquipo) jugadoresMiEquipo.remove(jugador.id)
                                            else {
                                                jugadoresEquipoRival.remove(jugador.id)
                                                jugadoresMiEquipo.add(jugador.id)
                                            }
                                        } else {
                                            if (enRival) jugadoresEquipoRival.remove(jugador.id)
                                            else {
                                                jugadoresMiEquipo.remove(jugador.id)
                                                jugadoresEquipoRival.add(jugador.id)
                                            }
                                        }
                                    },
                                    leadingIcon = {
                                        JugadorAvatar(
                                            fotoUri = jugador.fotoUri,
                                            nombre = jugador.nombre,
                                            tamano = 22.dp,
                                            fontSize = 9.sp
                                        )
                                    },
                                    label = { Text(jugador.nombre, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                // Notas del partido (ubicadas DEBAJO de participantes)
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas / crónica del partido") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
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
                        posicionJugada = posicionesJugadas.firstOrNull() ?: Posicion.DC,
                        posicionesJugadas = posicionesJugadas.toSet(),
                        goles = misGoles,
                        asistencias = misAsistencias,
                        tirosAlPalo = tirosAlPalo,
                        golesFueraArea = golesFueraArea,
                        notas = notas.trim(),
                        jugadoresMiEquipo = jugadoresMiEquipo.toList(),
                        jugadoresEquipoRival = jugadoresEquipoRival.toList(),
                        jugadoresIds = (jugadoresMiEquipo + jugadoresEquipoRival).distinct(),
                        golesZurda = golesZurda,
                        golesDiestra = golesDiestra,
                        golesCabeza = golesCabeza,
                        golesOtro = golesOtro,
                        golesChilena = golesChilena,
                        golesTacon = golesTacon
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
            horizontalArrangement = Arrangement.Center
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
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp)
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
