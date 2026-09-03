package com.diegoguerrero.futtracker.ui.screens.sorteos

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.diegoguerrero.futtracker.domain.model.*
import com.diegoguerrero.futtracker.domain.usecase.GenerarAlineacionUseCase
import com.diegoguerrero.futtracker.ui.components.CampoFutbol
import com.diegoguerrero.futtracker.ui.theme.DarkCard
import com.diegoguerrero.futtracker.ui.theme.LimeVolt
import com.diegoguerrero.futtracker.ui.theme.TextSecondary
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SorteosScreen(
    jugadores: List<Jugador>
) {
    val context = LocalContext.current
    val useCase = remember { GenerarAlineacionUseCase() }

    var totalJugadoresSeleccionados by remember { mutableStateOf(10) } // 10, 12, 14
    var equipoClaro by remember { mutableStateOf<List<Jugador>>(emptyList()) }
    var equipoOscuro by remember { mutableStateOf<List<Jugador>>(emptyList()) }
    var sorteoRealizado by remember { mutableStateOf(false) }

    // Formaciones y mapas tácticos sugeridos tras sorteo
    var formacionSugeridaClaro by remember { mutableStateOf<Formacion?>(null) }
    var formacionSugeridaOscuro by remember { mutableStateOf<Formacion?>(null) }
    var mapaCampoClaro by remember { mutableStateOf<Map<Pair<Posicion, Pair<Float, Float>>, Jugador?>?>(null) }
    var mapaCampoOscuro by remember { mutableStateOf<Map<Pair<Posicion, Pair<Float, Float>>, Jugador?>?>(null) }

    // Convocados para el sorteo
    val idsConvocados = remember { mutableStateListOf<String>() }
    var mostrarSelectorManual by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var soloFavoritos by remember { mutableStateOf(false) }
    var selectedPosicionFilter by remember { mutableStateOf<Posicion?>(null) }

    // Tab para visualizar la alineación del equipo claro u oscuro
    var tabEquipoAlineacion by remember { mutableStateOf(0) } // 0: Claro, 1: Oscuro

    // Inicializar convocados por defecto si está vacío
    LaunchedEffect(jugadores) {
        if (idsConvocados.isEmpty() && jugadores.isNotEmpty()) {
            idsConvocados.clear()
            jugadores.take(totalJugadoresSeleccionados).forEach { idsConvocados.add(it.id) }
        }
    }

    val tipoFutbolActual = remember(totalJugadoresSeleccionados) {
        when (totalJugadoresSeleccionados) {
            10 -> TipoFutbol.FUTSAL
            12 -> TipoFutbol.FUT_6
            else -> TipoFutbol.FUT_7
        }
    }

    val formacionesModalidad = remember(tipoFutbolActual) {
        when (tipoFutbolActual) {
            TipoFutbol.FUTSAL -> FORMACIONES_FUTSAL
            TipoFutbol.FUT_6 -> FORMACIONES_FUT_6
            TipoFutbol.FUT_7 -> FORMACIONES_FUT_7
        }
    }

    fun realizarSorteo(equilibrado: Boolean) {
        val listaConvocados = jugadores.filter { it.id in idsConvocados }
        if (listaConvocados.isEmpty()) return

        val mitad = totalJugadoresSeleccionados / 2

        val (claro, oscuro) = if (equilibrado) {
            val porteros = listaConvocados.filter { it.zonaPrincipal() == ZonaTactica.PORTERO }.shuffled().sortedByDescending { it.nivel }
            val defensas = listaConvocados.filter { it.zonaPrincipal() == ZonaTactica.DEFENSA }.shuffled().sortedByDescending { it.nivel }
            val medios = listaConvocados.filter { it.zonaPrincipal() == ZonaTactica.MEDIO }.shuffled().sortedByDescending { it.nivel }
            val delanteros = listaConvocados.filter { it.zonaPrincipal() == ZonaTactica.DELANTERO }.shuffled().sortedByDescending { it.nivel }
            val otros = listaConvocados.filter { it.zonaPrincipal() == ZonaTactica.OTRO }.shuffled().sortedByDescending { it.nivel }

            val clarosTemp = mutableListOf<Jugador>()
            val oscurosTemp = mutableListOf<Jugador>()

            val grupos = listOf(porteros, defensas, medios, delanteros, otros)

            for (grupo in grupos) {
                for (jugador in grupo) {
                    val cabeEnClaro = clarosTemp.size < mitad
                    val cabeEnOscuro = oscurosTemp.size < mitad

                    if (cabeEnClaro && cabeEnOscuro) {
                        // Contar cuántos jugadores de esta misma zona táctica tiene cada equipo
                        val zona = jugador.zonaPrincipal()
                        val cantZonaClaro = clarosTemp.count { it.zonaPrincipal() == zona }
                        val cantZonaOscuro = oscurosTemp.count { it.zonaPrincipal() == zona }

                        if (cantZonaClaro < cantZonaOscuro) {
                            clarosTemp.add(jugador)
                        } else if (cantZonaOscuro < cantZonaClaro) {
                            oscurosTemp.add(jugador)
                        } else {
                            // Si están igualados en posición, asignamos al equipo con menor nivel acumulado
                            val nivelClaro = clarosTemp.sumOf { it.nivel }
                            val nivelOscuro = oscurosTemp.sumOf { it.nivel }
                            if (nivelClaro < nivelOscuro) {
                                clarosTemp.add(jugador)
                            } else if (nivelOscuro < nivelClaro) {
                                oscurosTemp.add(jugador)
                            } else {
                                if (listOf(true, false).random()) clarosTemp.add(jugador) else oscurosTemp.add(jugador)
                            }
                        }
                    } else if (cabeEnClaro) {
                        clarosTemp.add(jugador)
                    } else if (cabeEnOscuro) {
                        oscurosTemp.add(jugador)
                    }
                }
            }

            // Optimización: Intercambio 1 a 1 entre jugadores de la MISMA zona táctica si reduce la diferencia de nivel
            var huboMejora = true
            while (huboMejora) {
                huboMejora = false
                val diffActual = kotlin.math.abs(clarosTemp.sumOf { it.nivel } - oscurosTemp.sumOf { it.nivel })
                if (diffActual <= 1) break

                for (i in clarosTemp.indices) {
                    for (j in oscurosTemp.indices) {
                        val jc = clarosTemp[i]
                        val jo = oscurosTemp[j]
                        if (jc.zonaPrincipal() == jo.zonaPrincipal()) {
                            val nuevoClaro = clarosTemp.sumOf { it.nivel } - jc.nivel + jo.nivel
                            val nuevoOscuro = oscurosTemp.sumOf { it.nivel } - jo.nivel + jc.nivel
                            val nuevaDiff = kotlin.math.abs(nuevoClaro - nuevoOscuro)
                            if (nuevaDiff < diffActual) {
                                clarosTemp[i] = jo
                                oscurosTemp[j] = jc
                                huboMejora = true
                                break
                            }
                        }
                    }
                    if (huboMejora) break
                }
            }

            Pair(clarosTemp.shuffled(), oscurosTemp.shuffled())
        } else {
            // Sorteo puramente aleatorio
            val mezclados = listaConvocados.shuffled()
            Pair(mezclados.take(mitad), mezclados.drop(mitad).take(mitad))
        }

        equipoClaro = claro
        equipoOscuro = oscuro

        // Calcular alineaciones sugeridas para ambos equipos
        val fClaro = useCase.sugerirMejorFormacion(claro, formacionesModalidad)
        val fOscuro = useCase.sugerirMejorFormacion(oscuro, formacionesModalidad)

        formacionSugeridaClaro = fClaro
        formacionSugeridaOscuro = fOscuro

        val asigClaro = useCase(claro, fClaro)
        val asigOscuro = useCase(oscuro, fOscuro)

        val coordsClaro = obtenerCoordenadas(fClaro)
        val coordsOscuro = obtenerCoordenadas(fOscuro)

        val asignadosClaroIds = mutableSetOf<String>()
        mapaCampoClaro = coordsClaro.associateWith { (pos, _) ->
            val asig = asigClaro.firstOrNull { it.first == pos && it.second.id !in asignadosClaroIds }?.second
            asig?.let { asignadosClaroIds.add(it.id) }
            asig
        }

        val asignadosOscuroIds = mutableSetOf<String>()
        mapaCampoOscuro = coordsOscuro.associateWith { (pos, _) ->
            val asig = asigOscuro.firstOrNull { it.first == pos && it.second.id !in asignadosOscuroIds }?.second
            asig?.let { asignadosOscuroIds.add(it.id) }
            asig
        }

        sorteoRealizado = true
    }

    fun compartirAlineacionesPNG() {
        if (!sorteoRealizado || formacionSugeridaClaro == null || formacionSugeridaOscuro == null) return

        try {
            val width = 1080
            val height = 1500
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)

            // Fondo
            val bgPaint = Paint().apply { color = android.graphics.Color.parseColor("#121614") }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Header fondo
            val headerPaint = Paint().apply { color = android.graphics.Color.parseColor("#1C2420") }
            canvas.drawRect(0f, 0f, width.toFloat(), 180f, headerPaint)

            // Título
            val titlePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#C6FF00")
                textSize = 52f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("FutTracker - Sorteo de Equipos", width / 2f, 95f, titlePaint)

            val subtitlePaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 28f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("Modalidad: ${when (totalJugadoresSeleccionados) { 10 -> "Futsal"; 12 -> "Fut 6"; else -> "Fut 7" }}", width / 2f, 145f, subtitlePaint)

            val sectionTitlePaint = Paint().apply {
                textSize = 38f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val playerTextPaint = Paint().apply {
                textSize = 30f
                color = android.graphics.Color.WHITE
                isAntiAlias = true
            }

            val cardPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#1E2722")
                style = Paint.Style.FILL
            }

            // Tarjeta Equipo Claro
            val rectClaro = RectF(50f, 220f, width - 50f, 750f)
            canvas.drawRoundRect(rectClaro, 24f, 24f, cardPaint)

            sectionTitlePaint.color = android.graphics.Color.parseColor("#C6FF00")
            canvas.drawText("⚪ EQUIPO CLARO  (Alineación: ${formacionSugeridaClaro?.nombre})", 90f, 290f, sectionTitlePaint)

            var yClaro = 360f
            equipoClaro.forEachIndexed { i, j ->
                val posStr = if (j.posicionesPrimarias.isNotEmpty()) j.posicionesPrimarias.first().name else "-"
                canvas.drawText("${i + 1}. ${j.nombre}  [$posStr]", 100f, yClaro, playerTextPaint)
                yClaro += 60f
            }

            // Tarjeta Equipo Oscuro
            val rectOscuro = RectF(50f, 800f, width - 50f, 1330f)
            canvas.drawRoundRect(rectOscuro, 24f, 24f, cardPaint)

            sectionTitlePaint.color = android.graphics.Color.parseColor("#80D8FF")
            canvas.drawText("⚫ EQUIPO OSCURO  (Alineación: ${formacionSugeridaOscuro?.nombre})", 90f, 870f, sectionTitlePaint)

            var yOscuro = 940f
            equipoOscuro.forEachIndexed { i, j ->
                val posStr = if (j.posicionesPrimarias.isNotEmpty()) j.posicionesPrimarias.first().name else "-"
                canvas.drawText("${i + 1}. ${j.nombre}  [$posStr]", 100f, yOscuro, playerTextPaint)
                yOscuro += 60f
            }

            // Pie
            val footerPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#888888")
                textSize = 24f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("Generado con FutTracker ⚽", width / 2f, 1420f, footerPaint)

            // Guardar en cache
            val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
            val imageFile = File(imagesDir, "sorteo_alineaciones.png")
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()

            // Compartir por Intent
            val imageUri = FileProvider.getUriForFile(context, "com.diegoguerrero.futtracker.fileprovider", imageFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, "🎲 *Sorteo y alineaciones generadas con FutTracker* ⚽")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            intent.setPackage("com.whatsapp")
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                intent.setPackage(null)
                context.startActivity(Intent.createChooser(intent, "Compartir Alineaciones en WhatsApp"))
            }
        } catch (e: Exception) {
            // Manejo de excepción
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sorteo de equipos", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (sorteoRealizado) {
                        IconButton(onClick = { compartirAlineacionesPNG() }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir alineaciones PNG", tint = LimeVolt)
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
            // Modalidad
            item {
                Text("Selecciona la modalidad:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10 to "Futsal", 12 to "Fut 6", 14 to "Fut 7").forEach { (cantidad, label) ->
                        FilterChip(
                            selected = totalJugadoresSeleccionados == cantidad,
                            onClick = { 
                                totalJugadoresSeleccionados = cantidad
                                idsConvocados.clear()
                                jugadores.take(cantidad).forEach { idsConvocados.add(it.id) }
                                sorteoRealizado = false
                            },
                            label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Convocados para el sorteo
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jugadores para el sorteo: $totalJugadoresSeleccionados (${idsConvocados.size}/$totalJugadoresSeleccionados)",
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

            // Selector manual con filtros de posición, favoritos y nombre
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
                                placeholder = { Text("Buscar jugador...") },
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

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item {
                                    FilterChip(
                                        selected = soloFavoritos,
                                        onClick = { soloFavoritos = !soloFavoritos },
                                        label = { Text("Favoritos", fontSize = 11.sp) },
                                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp)) }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = selectedPosicionFilter == null,
                                        onClick = { selectedPosicionFilter = null },
                                        label = { Text("Todos", fontSize = 11.sp) }
                                    )
                                }
                                items(Posicion.entries.toTypedArray()) { pos ->
                                    FilterChip(
                                        selected = selectedPosicionFilter == pos,
                                        onClick = { selectedPosicionFilter = if (selectedPosicionFilter == pos) null else pos },
                                        label = { Text(pos.name, fontSize = 11.sp) }
                                    )
                                }
                            }

                            val filtrados = jugadores.filter { jugador ->
                                val coincideBusqueda = searchQuery.isBlank() || jugador.nombre.contains(searchQuery, ignoreCase = true)
                                val coincideFav = !soloFavoritos || jugador.esFavorito
                                val coincidePos = selectedPosicionFilter == null ||
                                    selectedPosicionFilter in jugador.posicionesPrimarias ||
                                    selectedPosicionFilter in jugador.posicionesSecundarias
                                coincideBusqueda && coincideFav && coincidePos
                            }

                            if (filtrados.isEmpty()) {
                                Text("No hay jugadores encontrados.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 220.dp),
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
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(j.nombre, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                                val posLabel = if (j.posicionesPrimarias.isNotEmpty()) {
                                                    j.posicionesPrimarias.joinToString(", ") { it.name }
                                                } else "Sin posición"
                                                Text(posLabel, fontSize = 10.sp, color = TextSecondary)
                                            }

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

            // Botones de Sorteo: Aleatorio vs Equilibrado
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { realizarSorteo(equilibrado = false) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCard, contentColor = Color.White),
                        enabled = idsConvocados.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aleatorio", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Botón Equilibrado con texto en negro
                    Button(
                        onClick = { realizarSorteo(equilibrado = true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeVolt, contentColor = Color.Black),
                        enabled = idsConvocados.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Equilibrado", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                    }
                }
            }

            // Resultados del sorteo
            if (sorteoRealizado) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("⚪ EQUIPO CLARO", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                                if (formacionSugeridaClaro != null) {
                                    Text("Formación: ${formacionSugeridaClaro?.nombre}", fontSize = 11.sp, color = Color.DarkGray)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                equipoClaro.forEachIndexed { index, jugador ->
                                    val pos = jugador.posicionesPrimarias.firstOrNull()?.name ?: "-"
                                    Text("${index + 1}. ${jugador.nombre} ($pos)", fontSize = 12.sp, color = Color.DarkGray)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3430))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("⚫ EQUIPO OSCURO", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                if (formacionSugeridaOscuro != null) {
                                    Text("Formación: ${formacionSugeridaOscuro?.nombre}", fontSize = 11.sp, color = LimeVolt)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                equipoOscuro.forEachIndexed { index, jugador ->
                                    val pos = jugador.posicionesPrimarias.firstOrNull()?.name ?: "-"
                                    Text("${index + 1}. ${jugador.nombre} ($pos)", fontSize = 12.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }

                // Alineaciones sugeridas tácticas en campo para ambos equipos
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Alineaciones sugeridas",
                                color = LimeVolt,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = { compartirAlineacionesPNG() },
                                colors = ButtonDefaults.buttonColors(containerColor = LimeVolt, contentColor = Color.Black),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PNG a WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TabRow(
                            selectedTabIndex = tabEquipoAlineacion,
                            containerColor = DarkCard,
                            contentColor = LimeVolt
                        ) {
                            Tab(
                                selected = tabEquipoAlineacion == 0,
                                onClick = { tabEquipoAlineacion = 0 },
                                text = { Text("Equipo Claro (${formacionSugeridaClaro?.nombre})", fontSize = 12.sp) }
                            )
                            Tab(
                                selected = tabEquipoAlineacion == 1,
                                onClick = { tabEquipoAlineacion = 1 },
                                text = { Text("Equipo Oscuro (${formacionSugeridaOscuro?.nombre})", fontSize = 12.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val mapaActual = if (tabEquipoAlineacion == 0) mapaCampoClaro else mapaCampoOscuro
                        mapaActual?.let { mapa ->
                            CampoFutbol(
                                alineacion = mapa,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class ZonaTactica { PORTERO, DEFENSA, MEDIO, DELANTERO, OTRO }

private fun Posicion.toZonaTactica(): ZonaTactica = when (this) {
    Posicion.POR -> ZonaTactica.PORTERO
    Posicion.DFC, Posicion.LI, Posicion.LD -> ZonaTactica.DEFENSA
    Posicion.MC, Posicion.EI, Posicion.ED -> ZonaTactica.MEDIO
    Posicion.DC -> ZonaTactica.DELANTERO
}

private fun Jugador.zonaPrincipal(): ZonaTactica {
    val primera = posicionesPrimarias.firstOrNull() ?: posicionesSecundarias.firstOrNull()
    return primera?.toZonaTactica() ?: ZonaTactica.OTRO
}

