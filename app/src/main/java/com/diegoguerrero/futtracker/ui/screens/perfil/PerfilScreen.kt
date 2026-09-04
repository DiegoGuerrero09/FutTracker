package com.diegoguerrero.futtracker.ui.screens.perfil

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
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
import com.diegoguerrero.futtracker.domain.model.Perfil
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.ui.components.DialogoRecorteFoto
import com.diegoguerrero.futtracker.ui.theme.DarkBackground
import com.diegoguerrero.futtracker.ui.theme.DarkCard
import com.diegoguerrero.futtracker.ui.theme.DarkCardBorder
import com.diegoguerrero.futtracker.ui.theme.LimeVolt
import com.diegoguerrero.futtracker.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.UploadFile
import com.diegoguerrero.futtracker.ui.components.DialogoVisorFotoConZoom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    perfil: Perfil,
    onGuardarPerfil: (Perfil) -> Unit,
    onExportarDatos: suspend () -> String,
    onRestaurarDatos: suspend (String) -> Result<Pair<Int, Int>>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var uriParaRecortar by remember { mutableStateOf<Uri?>(null) }
    var exportando by remember { mutableStateOf(false) }
    var restaurando by remember { mutableStateOf(false) }
    var mostrarZoomPerfil by remember { mutableStateOf(false) }

    // Selector de foto desde galería -> pasa por recortador interactivo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { uriParaRecortar = it }
    }

    // Launcher para cargar archivo JSON de copia de seguridad
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    restaurando = true
                    val jsonStr = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            stream.bufferedReader(Charsets.UTF_8).readText()
                        }
                    }
                    if (jsonStr.isNullOrBlank()) {
                        Toast.makeText(context, "El archivo seleccionado está vacío", Toast.LENGTH_SHORT).show()
                    } else {
                        val resultado = onRestaurarDatos(jsonStr)
                        resultado.fold(
                            onSuccess = { (jugadoresCount, partidosCount) ->
                                Toast.makeText(
                                    context,
                                    "Copia restaurada: $jugadoresCount jugadores y $partidosCount partidos",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            onFailure = { e ->
                                Toast.makeText(
                                    context,
                                    "Error al restaurar: ${e.localizedMessage}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al leer archivo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                } finally {
                    restaurando = false
                }
            }
        }
    }

    // Launcher para guardar archivo JSON con SAF
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    exportando = true
                    val json = onExportarDatos()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { stream ->
                            stream.write(json.toByteArray(Charsets.UTF_8))
                        }
                    }
                    Toast.makeText(context, "Copia de seguridad guardada con éxito", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al guardar el archivo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                } finally {
                    exportando = false
                }
            }
        }
    }

    if (uriParaRecortar != null) {
        DialogoRecorteFoto(
            uriOriginal = uriParaRecortar!!,
            onFotoRecortada = { pathGuardado ->
                onGuardarPerfil(perfil.copy(fotoUri = pathGuardado))
                uriParaRecortar = null
            },
            onDismiss = { uriParaRecortar = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkCard
                ),
                actions = {
                    IconButton(onClick = { mostrarDialogoEditar = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar perfil", tint = LimeVolt)
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de información principal
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
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
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .clickable { mostrarZoomPerfil = true },
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
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
                                modifier = Modifier.size(32.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = LimeVolt,
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar foto", modifier = Modifier.size(17.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = perfil.nombre,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

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
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (perfil.sincronizadoConJugadores) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "✓ Sincronizado en la plantilla de jugadores",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Módulo de Exportación y Copia de Seguridad
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Copia de seguridad y exportación",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Exporta todos tus datos (perfil, jugadores, partidos y estadísticas) a un archivo JSON para conservarlos o restaurarlos en otro dispositivo.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val fechaFormato = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                    val fileName = "futtracker_backup_$fechaFormato.json"
                                    createDocumentLauncher.launch(fileName)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = LimeVolt, contentColor = Color.Black),
                                enabled = !exportando
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Guardar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            exportando = true
                                            val json = onExportarDatos()
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, json)
                                                type = "application/json"
                                            }
                                            val shareIntent = Intent.createChooser(sendIntent, "Exportar datos FutTracker")
                                            context.startActivity(shareIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error al compartir: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            exportando = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !exportando
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Compartir", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { openDocumentLauncher.launch("application/json") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkBackground,
                                contentColor = LimeVolt
                            ),
                            border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.5f)),
                            enabled = !exportando && !restaurando
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (restaurando) "Cargando copia..." else "Cargar copia de seguridad (JSON)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarZoomPerfil && perfil.fotoUri != null) {
        DialogoVisorFotoConZoom(
            fotoUri = perfil.fotoUri,
            nombre = perfil.nombre,
            onDismiss = { mostrarZoomPerfil = false }
        )
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
    var posicionFavorita by remember { mutableStateOf(perfilActual.posicionFavorita) }
    var sincronizado by remember { mutableStateOf(perfilActual.sincronizadoConJugadores) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Posiciones:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Posicion.entries.chunked(3).forEach { fila ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            fila.forEach { pos ->
                                val isSelected = pos in posiciones
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        posiciones = if (isSelected) {
                                            if (posiciones.size > 1) posiciones - pos else posiciones
                                        } else {
                                            posiciones + pos
                                        }
                                        if (posicionFavorita !in posiciones) {
                                            posicionFavorita = posiciones.first()
                                        }
                                    },
                                    label = { Text(pos.name, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Text("Posición predilecta:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    posiciones.forEach { pos ->
                        FilterChip(
                            selected = posicionFavorita == pos,
                            onClick = { posicionFavorita = pos },
                            label = { Text(pos.name, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sincronizar en plantilla", fontSize = 13.sp)
                    Switch(
                        checked = sincronizado,
                        onCheckedChange = { sincronizado = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardar(
                        perfilActual.copy(
                            nombre = nombre.trim(),
                            posicionFavorita = posicionFavorita,
                            posiciones = posiciones,
                            sincronizadoConJugadores = sincronizado
                        )
                    )
                },
                enabled = nombre.isNotBlank() && posiciones.isNotEmpty()
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

private fun obtenerIniciales(nombre: String): String {
    val partes = nombre.trim().split("\\s+".toRegex())
    return when {
        partes.isEmpty() -> "??"
        partes.size == 1 -> partes[0].take(2).uppercase()
        else -> "${partes[0].take(1)}${partes[1].take(1)}".uppercase()
    }
}
