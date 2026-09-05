package com.diegoguerrero.futtracker.ui.screens.estadios

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegoguerrero.futtracker.domain.model.Estadio
import com.diegoguerrero.futtracker.domain.model.TipoFutbol
import com.diegoguerrero.futtracker.ui.components.DialogoRecorteFoto
import com.diegoguerrero.futtracker.ui.components.DialogoVisorFotoConZoom
import com.diegoguerrero.futtracker.ui.components.ImagenLocal
import com.diegoguerrero.futtracker.ui.theme.DarkBackground
import com.diegoguerrero.futtracker.ui.theme.DarkCard
import com.diegoguerrero.futtracker.ui.theme.DarkCardBorder
import com.diegoguerrero.futtracker.ui.theme.LimeVolt
import com.diegoguerrero.futtracker.ui.theme.TextSecondary
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadiosScreen(
    estadios: List<Estadio>,
    onAgregarEstadio: (Estadio) -> Unit,
    onActualizarEstadio: (Estadio) -> Unit,
    onEliminarEstadio: (Estadio) -> Unit,
    onToggleFavorito: (Estadio) -> Unit = {}
) {
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var estadioAEditar by remember { mutableStateOf<Estadio?>(null) }
    var estadioAEliminar by remember { mutableStateOf<Estadio?>(null) }

    var busqueda by remember { mutableStateOf("") }
    var filtroSoloFavoritos by remember { mutableStateOf(false) }
    var ordenNombreAsc by remember { mutableStateOf<Boolean?>(null) }
    var ordenFechaDesc by remember { mutableStateOf<Boolean?>(null) }
    var modalidadFiltro by remember { mutableStateOf<TipoFutbol?>(null) }

    val estadiosFiltrados = remember(
        estadios, busqueda, filtroSoloFavoritos, ordenNombreAsc, ordenFechaDesc, modalidadFiltro
    ) {
        estadios.filter { est ->
            val matchText = busqueda.isBlank() || est.nombre.contains(busqueda.trim(), ignoreCase = true)
            val matchFav = !filtroSoloFavoritos || est.esFavorito
            val matchMod = modalidadFiltro == null || est.modalidades.contains(modalidadFiltro)
            matchText && matchFav && matchMod
        }.sortedWith { a, b ->
            when {
                ordenNombreAsc != null -> {
                    if (ordenNombreAsc == true) a.nombre.compareTo(b.nombre, ignoreCase = true)
                    else b.nombre.compareTo(a.nombre, ignoreCase = true)
                }
                ordenFechaDesc != null -> {
                    if (ordenFechaDesc == true) b.fechaCreacion.compareTo(a.fechaCreacion)
                    else a.fechaCreacion.compareTo(b.fechaCreacion)
                }
                else -> {
                    compareByDescending<Estadio> { it.esFavorito }
                        .thenBy { it.nombre.lowercase() }
                        .compare(a, b)
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoCrear = true },
                containerColor = LimeVolt,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir estadio")
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Buscador
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar estadio...", fontSize = 13.sp, color = TextSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = LimeVolt, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (busqueda.isNotEmpty()) {
                        IconButton(onClick = { busqueda = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LimeVolt,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fila 1: Ordenar y Favoritos
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filtro Favoritos
                item {
                    FilterChip(
                        selected = filtroSoloFavoritos,
                        onClick = { filtroSoloFavoritos = !filtroSoloFavoritos },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFD700).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFFFFD700),
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filtroSoloFavoritos,
                            borderColor = DarkCardBorder,
                            selectedBorderColor = Color(0xFFFFD700)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = if (filtroSoloFavoritos) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (filtroSoloFavoritos) Color(0xFFFFD700) else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Favoritos", fontSize = 11.sp) }
                    )
                }

                // Orden por Nombre
                item {
                    val labelNombre = when (ordenNombreAsc) {
                        true -> "Nombre (A-Z) ↑"
                        false -> "Nombre (Z-A) ↓"
                        null -> "Nombre"
                    }
                    FilterChip(
                        selected = ordenNombreAsc != null,
                        onClick = {
                            ordenFechaDesc = null
                            ordenNombreAsc = when (ordenNombreAsc) {
                                null -> true
                                true -> false
                                false -> null
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LimeVolt.copy(alpha = 0.2f),
                            selectedLabelColor = LimeVolt,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = ordenNombreAsc != null,
                            borderColor = DarkCardBorder,
                            selectedBorderColor = LimeVolt
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.SortByAlpha,
                                contentDescription = null,
                                tint = if (ordenNombreAsc != null) LimeVolt else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(labelNombre, fontSize = 11.sp) }
                    )
                }

                // Orden por Fecha
                item {
                    val labelFecha = when (ordenFechaDesc) {
                        true -> "Recientes ↓"
                        false -> "Antiguos ↑"
                        null -> "Fecha añadido"
                    }
                    FilterChip(
                        selected = ordenFechaDesc != null,
                        onClick = {
                            ordenNombreAsc = null
                            ordenFechaDesc = when (ordenFechaDesc) {
                                null -> true
                                true -> false
                                false -> null
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LimeVolt.copy(alpha = 0.2f),
                            selectedLabelColor = LimeVolt,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = ordenFechaDesc != null,
                            borderColor = DarkCardBorder,
                            selectedBorderColor = LimeVolt
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = if (ordenFechaDesc != null) LimeVolt else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(labelFecha, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Fila 2: Modalidad
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = modalidadFiltro == null,
                        onClick = { modalidadFiltro = null },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LimeVolt.copy(alpha = 0.2f),
                            selectedLabelColor = LimeVolt,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = modalidadFiltro == null,
                            borderColor = DarkCardBorder,
                            selectedBorderColor = LimeVolt
                        ),
                        label = { Text("Todas", fontSize = 11.sp) }
                    )
                }

                items(TipoFutbol.entries.toTypedArray()) { mod ->
                    val sel = modalidadFiltro == mod
                    val label = when (mod) {
                        TipoFutbol.FUTSAL -> "Futsal"
                        TipoFutbol.FUT_6 -> "Fútbol 6"
                        TipoFutbol.FUT_7 -> "Fútbol 7"
                    }
                    FilterChip(
                        selected = sel,
                        onClick = { modalidadFiltro = if (sel) null else mod },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LimeVolt.copy(alpha = 0.2f),
                            selectedLabelColor = LimeVolt,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = sel,
                            borderColor = DarkCardBorder,
                            selectedBorderColor = LimeVolt
                        ),
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (estadiosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Stadium,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (estadios.isEmpty()) "No hay estadios registrados" else "No se encontraron estadios",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (estadios.isEmpty()) "Toca el botón + para añadir uno" else "Prueba cambiando los filtros",
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(estadiosFiltrados, key = { it.id }) { estadio ->
                        EstadioCard(
                            estadio = estadio,
                            onEditar = { estadioAEditar = estadio },
                            onEliminar = { estadioAEliminar = estadio },
                            onToggleFavorito = { onToggleFavorito(estadio) }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogoCrear) {
        DialogoEstadio(
            estadioExistente = null,
            onDismiss = { mostrarDialogoCrear = false },
            onGuardar = { nuevo ->
                onAgregarEstadio(nuevo)
                mostrarDialogoCrear = false
            }
        )
    }

    estadioAEditar?.let { estadio ->
        DialogoEstadio(
            estadioExistente = estadio,
            onDismiss = { estadioAEditar = null },
            onGuardar = { actualizado ->
                onActualizarEstadio(actualizado)
                estadioAEditar = null
            }
        )
    }

    estadioAEliminar?.let { estadio ->
        AlertDialog(
            onDismissRequest = { estadioAEliminar = null },
            title = { Text("Eliminar estadio") },
            text = { Text("¿Estás seguro de que deseas eliminar '${estadio.nombre}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onEliminarEstadio(estadio)
                        estadioAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { estadioAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun EstadioCard(
    estadio: Estadio,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onToggleFavorito: () -> Unit = {}
) {
    var mostrarZoomFoto by remember { mutableStateOf(false) }
    val fotoValida = estadio.fotoUri != null && File(estadio.fotoUri).exists()

    if (mostrarZoomFoto && fotoValida && estadio.fotoUri != null) {
        DialogoVisorFotoConZoom(
            fotoUri = estadio.fotoUri,
            nombre = estadio.nombre,
            onDismiss = { mostrarZoomFoto = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.8.dp, if (estadio.esFavorito) Color(0xFFFFD700).copy(alpha = 0.5f) else DarkCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto o Icono por defecto (Permite zoom al pulsar la foto)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LimeVolt.copy(alpha = 0.12f))
                    .then(
                        if (fotoValida) {
                            Modifier.clickable { mostrarZoomFoto = true }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (fotoValida) {
                    ImagenLocal(
                        fotoUri = estadio.fotoUri,
                        contentDescription = estadio.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Stadium,
                        contentDescription = null,
                        tint = LimeVolt,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = estadio.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(estadio.modalidades.toList()) { mod ->
                        val label = when (mod) {
                            TipoFutbol.FUTSAL -> "Futsal"
                            TipoFutbol.FUT_6 -> "Fútbol 6"
                            TipoFutbol.FUT_7 -> "Fútbol 7"
                        }
                        Surface(
                            color = LimeVolt.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.8.dp, LimeVolt.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = label,
                                color = LimeVolt,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onToggleFavorito, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (estadio.esFavorito) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorito",
                    tint = if (estadio.esFavorito) Color(0xFFFFD700) else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onEditar, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun DialogoEstadio(
    estadioExistente: Estadio?,
    onDismiss: () -> Unit,
    onGuardar: (Estadio) -> Unit
) {
    var nombre by remember { mutableStateOf(estadioExistente?.nombre ?: "") }
    var modalidades by remember {
        mutableStateOf(estadioExistente?.modalidades ?: setOf(TipoFutbol.FUTSAL))
    }
    var fotoUri by remember { mutableStateOf(estadioExistente?.fotoUri) }
    var uriParaRecortar by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { uriParaRecortar = it }
    }

    if (uriParaRecortar != null) {
        DialogoRecorteFoto(
            uriOriginal = uriParaRecortar!!,
            onFotoRecortada = { pathGuardado ->
                fotoUri = pathGuardado
                uriParaRecortar = null
            },
            onDismiss = { uriParaRecortar = null }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (estadioExistente == null) "Nuevo estadio" else "Editar estadio",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Selector de Foto
                var mostrarZoomDialogo by remember { mutableStateOf(false) }
                val fotoValida = fotoUri != null && File(fotoUri!!).exists()

                if (mostrarZoomDialogo && fotoValida && fotoUri != null) {
                    DialogoVisorFotoConZoom(
                        fotoUri = fotoUri!!,
                        nombre = nombre.ifBlank { "Estadio" },
                        onDismiss = { mostrarZoomDialogo = false }
                    )
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkBackground)
                            .clickable { photoPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoValida) {
                            ImagenLocal(
                                fotoUri = fotoUri,
                                contentDescription = "Foto estadio",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Añadir foto",
                                    tint = LimeVolt,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Añadir foto",
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                    if (fotoUri != null) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 12.dp, y = (-4).dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (fotoValida) {
                                IconButton(
                                    onClick = { mostrarZoomDialogo = true },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                ) {
                                    Icon(Icons.Default.ZoomIn, contentDescription = "Ver foto ampliada", tint = LimeVolt, modifier = Modifier.size(16.dp))
                                }
                            }
                            IconButton(
                                onClick = { fotoUri = null },
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar foto", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del estadio") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LimeVolt,
                        focusedLabelColor = LimeVolt
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text(
                        text = "Modalidades (selecciona 1 o varias):",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            TipoFutbol.FUTSAL to "Futsal",
                            TipoFutbol.FUT_6 to "Fútbol 6",
                            TipoFutbol.FUT_7 to "Fútbol 7"
                        ).forEach { (tipo, label) ->
                            val sel = tipo in modalidades
                            FilterChip(
                                selected = sel,
                                onClick = {
                                    val nuevas = modalidades.toMutableSet()
                                    if (sel) {
                                        if (nuevas.size > 1) nuevas.remove(tipo)
                                    } else {
                                        nuevas.add(tipo)
                                    }
                                    modalidades = nuevas
                                },
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = sel,
                                    borderColor = LimeVolt.copy(alpha = 0.5f),
                                    selectedBorderColor = LimeVolt
                                ),
                                label = {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(label, fontSize = 11.sp)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nuevo = (estadioExistente ?: Estadio(nombre = nombre.trim())).copy(
                        nombre = nombre.trim(),
                        modalidades = modalidades.ifEmpty { setOf(TipoFutbol.FUTSAL) },
                        fotoUri = fotoUri
                    )
                    onGuardar(nuevo)
                },
                enabled = nombre.isNotBlank() && modalidades.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = LimeVolt, contentColor = Color.Black)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}
