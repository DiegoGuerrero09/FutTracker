package com.diegoguerrero.futtracker.ui.screens.perfil

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.domain.model.Perfil
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.ui.components.GraficoGolesAsistencias
import com.diegoguerrero.futtracker.ui.components.GraficoResultados
import com.diegoguerrero.futtracker.ui.components.GraficoResumenGoles
import com.diegoguerrero.futtracker.ui.screens.jugadores.obtenerIniciales
import com.diegoguerrero.futtracker.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    perfil: Perfil,
    partidosFiltrados: List<Partido>,
    filtroTipo: TipoFiltroPerfil,
    temporadaSeleccionada: String,
    anioSeleccionado: Int,
    fechaInicio: Long,
    fechaFin: Long,
    temporadasDisponibles: List<String> = emptyList(),
    aniosDisponibles: List<Int> = emptyList(),
    onGuardarPerfil: (Perfil) -> Unit,
    onCambiarFiltro: (TipoFiltroPerfil) -> Unit,
    onCambiarTemporada: (String) -> Unit,
    onCambiarAnio: (Int) -> Unit,
    onCambiarRangoFechas: (Long, Long) -> Unit
) {
    val context = LocalContext.current
    var mostrarDialogoEditar by remember { mutableStateOf(false) }

    // Selector de foto desde galería
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            runCatching {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                val profileDir = File(context.filesDir, "profile").apply { mkdirs() }
                val targetFile = File(profileDir, "avatar_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(targetFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                onGuardarPerfil(perfil.copy(fotoUri = targetFile.absolutePath))
            }
        }
    }

    val totalPartidos = partidosFiltrados.size
    val totalGoles = partidosFiltrados.sumOf { it.goles }
    val totalAsistencias = partidosFiltrados.sumOf { it.asistencias }
    val promedioGoles = if (totalPartidos > 0) {
        String.format(Locale.getDefault(), "%.2f", totalGoles.toFloat() / totalPartidos)
    } else "0.0"

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LimeVolt,
                    titleContentColor = Color.Black
                ),
                actions = {
                    IconButton(onClick = { mostrarDialogoEditar = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar perfil", tint = Color.Black)
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
            // Tarjeta de perfil con foto
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            val bitmap = remember(perfil.fotoUri) {
                                perfil.fotoUri?.let { path ->
                                    val file = File(path)
                                    if (file.exists()) BitmapFactory.decodeFile(path)?.asImageBitmap() else null
                                }
                            }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = obtenerIniciales(perfil.nombre),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            FilledIconButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier.size(30.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = LimeVolt,
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar foto", modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = perfil.nombre,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val posicionesPerfil = perfil.posiciones.ifEmpty { setOf(perfil.posicionFavorita) }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            posicionesPerfil.forEach { pos ->
                                Surface(
                                    color = LimeVolt.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = pos.name,
                                        color = LimeVolt,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (perfil.sincronizadoConJugadores) {
                            Text(
                                text = "✓ Añadido como jugador en plantilla",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Selector de filtro de estadísticas
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Filtrar estadísticas:",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            TipoFiltroPerfil.TOTAL to "Total",
                            TipoFiltroPerfil.TEMPORADA to "Temporada",
                            TipoFiltroPerfil.ANIO_NATURAL to "Año",
                            TipoFiltroPerfil.FECHA_PERSONALIZADA to "Por fecha"
                        ).forEach { (tipo, label) ->
                            FilterChip(
                                selected = filtroTipo == tipo,
                                onClick = { onCambiarFiltro(tipo) },
                                label = {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Sub-filtros dinámicos
                    when (filtroTipo) {
                        TipoFiltroPerfil.TOTAL -> {}
                        TipoFiltroPerfil.TEMPORADA -> {
                            val listaTemporadas = temporadasDisponibles.ifEmpty { listOf(temporadaSeleccionada) }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(listaTemporadas) { temp ->
                                    FilterChip(
                                        selected = temporadaSeleccionada == temp,
                                        onClick = { onCambiarTemporada(temp) },
                                        label = { Text(temp) }
                                    )
                                }
                            }
                        }
                        TipoFiltroPerfil.ANIO_NATURAL -> {
                            val listaAnios = aniosDisponibles.ifEmpty { listOf(anioSeleccionado) }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(listaAnios) { anio ->
                                    FilterChip(
                                        selected = anioSeleccionado == anio,
                                        onClick = { onCambiarAnio(anio) },
                                        label = { Text("$anio") }
                                    )
                                }
                            }
                        }
                        TipoFiltroPerfil.FECHA_PERSONALIZADA -> {
                            val cal = Calendar.getInstance()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        cal.timeInMillis = fechaInicio
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                val c = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }
                                                onCambiarRangoFechas(c.timeInMillis, fechaFin)
                                            },
                                            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkCard)
                                ) {
                                    Text("Desde: ${sdf.format(Date(fechaInicio))}", fontSize = 11.sp, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        cal.timeInMillis = fechaFin
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                val c = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59) }
                                                onCambiarRangoFechas(fechaInicio, c.timeInMillis)
                                            },
                                            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkCard)
                                ) {
                                    Text("Hasta: ${sdf.format(Date(fechaFin))}", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Métricas numéricas
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricaCard(
                        titulo = "Partidos",
                        valor = "$totalPartidos",
                        subtitulo = "Jugados",
                        color = LimeVolt,
                        modifier = Modifier.weight(1f)
                    )
                    MetricaCard(
                        titulo = "Goles",
                        valor = "$totalGoles",
                        subtitulo = "$promedioGoles / partido",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    MetricaCard(
                        titulo = "Asistencias",
                        valor = "$totalAsistencias",
                        subtitulo = "Totales",
                        color = Color(0xFF29B6F6),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Gráfica de Goles y Asistencias
            item {
                GraficoGolesAsistencias(partidos = partidosFiltrados)
            }

            // Gráfica de Balance de Resultados
            item {
                GraficoResultados(partidos = partidosFiltrados)
            }

            // Gráfica de Resumen de Goles
            item {
                GraficoResumenGoles(partidos = partidosFiltrados)
            }
        }
    }

    if (mostrarDialogoEditar) {
        DialogoEditarPerfil(
            perfilActual = perfil,
            onDismiss = { mostrarDialogoEditar = false },
            onGuardar = { actualizado ->
                onGuardarPerfil(actualizado)
                mostrarDialogoEditar = false
            }
        )
    }
}

@Composable
private fun MetricaCard(
    titulo: String,
    valor: String,
    subtitulo: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = titulo,
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = valor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitulo,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoEditarPerfil(
    perfilActual: Perfil,
    onDismiss: () -> Unit,
    onGuardar: (Perfil) -> Unit
) {
    var nombre by remember { mutableStateOf(perfilActual.nombre) }
    var posiciones by remember {
        mutableStateOf(perfilActual.posiciones.ifEmpty { setOf(perfilActual.posicionFavorita) })
    }
    var nivel by remember { mutableStateOf(perfilActual.nivel) }
    var sincronizado by remember { mutableStateOf(perfilActual.sincronizadoConJugadores) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar perfil", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Mi nombre / Apodo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Mis posiciones habituales", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(Posicion.entries.toTypedArray()) { pos ->
                            val isSelected = pos in posiciones
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    posiciones = if (isSelected) {
                                        if (posiciones.size > 1) posiciones - pos else posiciones
                                    } else {
                                        posiciones + pos
                                    }
                                },
                                label = { Text(pos.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Column {
                    Text("Nivel como jugador (1 al 5)", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..5).forEach { lvl ->
                            FilterChip(
                                selected = nivel == lvl,
                                onClick = { nivel = lvl },
                                label = {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("★ $lvl", fontSize = 12.sp, textAlign = TextAlign.Center)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sincronizado = !sincronizado },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = sincronizado,
                        onCheckedChange = { sincronizado = it },
                        colors = CheckboxDefaults.colors(checkedColor = LimeVolt, checkmarkColor = Color.Black)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Añadir / sincronizar mi usuario en la plantilla de jugadores",
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank() && posiciones.isNotEmpty()) {
                        onGuardar(
                            perfilActual.copy(
                                nombre = nombre.trim(),
                                posiciones = posiciones,
                                posicionFavorita = posiciones.first(),
                                nivel = nivel,
                                sincronizadoConJugadores = sincronizado
                            )
                        )
                    }
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
