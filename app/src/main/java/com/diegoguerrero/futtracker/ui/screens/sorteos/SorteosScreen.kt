package com.diegoguerrero.futtracker.ui.screens.sorteos

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import com.diegoguerrero.futtracker.ui.components.CampoFutbol
import com.diegoguerrero.futtracker.ui.components.JugadorAvatar
import com.diegoguerrero.futtracker.ui.components.BadgePosicion
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
    var asignacionesClaro by remember { mutableStateOf<List<Pair<Posicion, Jugador>>>(emptyList()) }
    var asignacionesOscuro by remember { mutableStateOf<List<Pair<Posicion, Jugador>>>(emptyList()) }
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
    var soloPosicionPrincipalFilter by remember { mutableStateOf(false) }

    // Tab para visualizar la alineación del equipo claro u oscuro
    var tabEquipoAlineacion by remember { mutableStateOf(0) } // 0: Claro, 1: Oscuro

    // Inicia sin jugadores preseleccionados
    LaunchedEffect(Unit) {
        // Pestaña de sorteos inicia sin jugadores preseleccionados
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

    fun realizarSorteo(equilibradoPorPosiciones: Boolean) {
        val listaConvocados = jugadores.filter { it.id in idsConvocados }
        if (listaConvocados.size != totalJugadoresSeleccionados) return

        val mitad = totalJugadoresSeleccionados / 2

        val (claro, oscuro) = if (!equilibradoPorPosiciones) {
            // Completamente aleatorio sin tener en cuenta posiciones
            val shuffled = listaConvocados.shuffled()
            Pair(shuffled.take(mitad), shuffled.drop(mitad))
        } else {
            // Equilibrado teniendo en cuenta posiciones
            val porteros = listaConvocados.filter { it.zonaPrincipal() == ZonaTactica.PORTERO }.shuffled()
            val defensas = listaConvocados.filter { it.zonaPrincipal() == ZonaTactica.DEFENSA }.shuffled()
            val medios = listaConvocados.filter { it.zonaPrincipal() == ZonaTactica.MEDIO }.shuffled()
            val delanteros = listaConvocados.filter { it.zonaPrincipal() == ZonaTactica.DELANTERO }.shuffled()
            val otros = listaConvocados.filter { it.zonaPrincipal() == ZonaTactica.OTRO }.shuffled()

            val clarosTemp = mutableListOf<Jugador>()
            val oscurosTemp = mutableListOf<Jugador>()
            val grupos = listOf(porteros, defensas, medios, delanteros, otros)

            for (grupo in grupos) {
                for (jugador in grupo) {
                    val cabeEnClaro = clarosTemp.size < mitad
                    val cabeEnOscuro = oscurosTemp.size < mitad

                    if (cabeEnClaro && cabeEnOscuro) {
                        val zona = jugador.zonaPrincipal()
                        val cantZonaClaro = clarosTemp.count { it.zonaPrincipal() == zona }
                        val cantZonaOscuro = oscurosTemp.count { it.zonaPrincipal() == zona }

                        if (cantZonaClaro < cantZonaOscuro) {
                            clarosTemp.add(jugador)
                        } else if (cantZonaOscuro < cantZonaClaro) {
                            oscurosTemp.add(jugador)
                        } else {
                            if (listOf(true, false).random()) clarosTemp.add(jugador) else oscurosTemp.add(jugador)
                        }
                    } else if (cabeEnClaro) {
                        clarosTemp.add(jugador)
                    } else if (cabeEnOscuro) {
                        oscurosTemp.add(jugador)
                    }
                }
            }

            Pair(clarosTemp.shuffled(), oscurosTemp.shuffled())
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
        asignacionesClaro = asigClaro
        asignacionesOscuro = asigOscuro

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

    fun cambiarFormacionClaro(nuevaFormacion: Formacion) {
        formacionSugeridaClaro = nuevaFormacion
        val asigClaro = useCase(equipoClaro, nuevaFormacion)
        asignacionesClaro = asigClaro
        val coordsClaro = obtenerCoordenadas(nuevaFormacion)
        val asignadosClaroIds = mutableSetOf<String>()
        mapaCampoClaro = coordsClaro.associateWith { (pos, _) ->
            val asig = asigClaro.firstOrNull { it.first == pos && it.second.id !in asignadosClaroIds }?.second
            asig?.let { asignadosClaroIds.add(it.id) }
            asig
        }
    }

    fun cambiarFormacionOscuro(nuevaFormacion: Formacion) {
        formacionSugeridaOscuro = nuevaFormacion
        val asigOscuro = useCase(equipoOscuro, nuevaFormacion)
        asignacionesOscuro = asigOscuro
        val coordsOscuro = obtenerCoordenadas(nuevaFormacion)
        val asignadosOscuroIds = mutableSetOf<String>()
        mapaCampoOscuro = coordsOscuro.associateWith { (pos, _) ->
            val asig = asigOscuro.firstOrNull { it.first == pos && it.second.id !in asignadosOscuroIds }?.second
            asig?.let { asignadosOscuroIds.add(it.id) }
            asig
        }
    }

    fun compartirAlineacionesPNG() {
        if (!sorteoRealizado || formacionSugeridaClaro == null || formacionSugeridaOscuro == null) return

        try {
            val width = 1080
            val height = 1860
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)

            // Fondo
            val bgPaint = Paint().apply { color = android.graphics.Color.parseColor("#090A0F") }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Header fondo
            val headerPaint = Paint().apply { color = android.graphics.Color.parseColor("#13151E") }
            canvas.drawRect(0f, 0f, width.toFloat(), 170f, headerPaint)

            // Título
            val titlePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#D4FF00")
                textSize = 48f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("FutTracker - Sorteo y alineaciones", width / 2f, 85f, titlePaint)

            val subtitlePaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 26f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val modalidadNombre = when (totalJugadoresSeleccionados) { 10 -> "Futsal"; 12 -> "Fútbol 6"; else -> "Fútbol 7" }
            canvas.drawText("Modalidad: $modalidadNombre", width / 2f, 135f, subtitlePaint)

            val cardPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#13151E")
                style = Paint.Style.FILL
            }

            val cardBorderPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#222634")
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }

            val sectionTitlePaint = Paint().apply {
                textSize = 34f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val playerTextPaint = Paint().apply {
                textSize = 26f
                color = android.graphics.Color.WHITE
                isAntiAlias = true
            }

            fun dibujarMiniCampo(rect: RectF, formacion: Formacion, asignaciones: List<Pair<Posicion, Jugador>>, colorFicha: Int, colorTextoFicha: Int) {
                val fieldPaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#10251B")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val borderLinePaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#2A4E3B")
                    style = Paint.Style.STROKE
                    strokeWidth = 2.5f
                    isAntiAlias = true
                }
                canvas.drawRoundRect(rect, 14f, 14f, fieldPaint)
                canvas.drawRoundRect(rect, 14f, 14f, borderLinePaint)
                canvas.drawLine(rect.left, rect.centerY(), rect.right, rect.centerY(), borderLinePaint)
                canvas.drawCircle(rect.centerX(), rect.centerY(), rect.width() * 0.16f, borderLinePaint)

                val coords = obtenerCoordenadas(formacion)
                val dotPaint = Paint().apply {
                    color = colorFicha
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val labelPaint = Paint().apply {
                    color = colorTextoFicha
                    textSize = 17f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                val namePaint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 15f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }

                val asignadosIds = mutableSetOf<String>()
                coords.forEach { (posReq, coordPair) ->
                    val asig = asignaciones.firstOrNull { it.first == posReq && it.second.id !in asignadosIds }
                    val jug = asig?.second
                    jug?.let { asignadosIds.add(it.id) }

                    val cx = rect.left + coordPair.first * rect.width()
                    val cy = rect.top + coordPair.second * rect.height()

                    var fotoBitmap: Bitmap? = null
                    if (!jug?.fotoUri.isNullOrBlank()) {
                        try {
                            val path = jug!!.fotoUri!!
                            val file = File(path)
                            val origBmp = if (file.exists() && file.length() > 0) {
                                BitmapFactory.decodeFile(path)
                            } else {
                                context.contentResolver.openInputStream(android.net.Uri.parse(path))?.use {
                                    BitmapFactory.decodeStream(it)
                                }
                            }
                            if (origBmp != null) {
                                val size = 44
                                val scaled = Bitmap.createScaledBitmap(origBmp, size, size, true)
                                val circularBmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                                val c = android.graphics.Canvas(circularBmp)
                                val p = Paint(Paint.ANTI_ALIAS_FLAG)
                                c.drawCircle(size / 2f, size / 2f, size / 2f, p)
                                p.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
                                c.drawBitmap(scaled, 0f, 0f, p)
                                fotoBitmap = circularBmp
                            }
                        } catch (e: Exception) {
                            fotoBitmap = null
                        }
                    }

                    if (fotoBitmap != null) {
                        canvas.drawBitmap(fotoBitmap, cx - 22f, cy - 22f, null)
                        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.STROKE
                            strokeWidth = 3f
                            color = colorFicha
                        }
                        canvas.drawCircle(cx, cy, 22f, strokePaint)
                    } else {
                        canvas.drawCircle(cx, cy, 20f, dotPaint)
                        canvas.drawText(posReq.name, cx, cy + 6f, labelPaint)
                    }

                    if (jug != null) {
                        val originalTextSize = namePaint.textSize
                        if (jug.nombre.length > 9) {
                            namePaint.textSize = 11f
                        }
                        canvas.drawText(jug.nombre, cx, cy + 39f, namePaint)
                        namePaint.textSize = originalTextSize
                    }
                }
            }

            // --- Tarjeta Equipo Claro ---
            val rectClaro = RectF(40f, 195f, width - 40f, 985f)
            canvas.drawRoundRect(rectClaro, 20f, 20f, cardPaint)
            canvas.drawRoundRect(rectClaro, 20f, 20f, cardBorderPaint)

            sectionTitlePaint.color = android.graphics.Color.parseColor("#D4FF00")
            canvas.drawText("⚪ Equipo claro  (Alineación: ${formacionSugeridaClaro?.nombre})", 70f, 255f, sectionTitlePaint)

            var yClaro = 320f
            val listaClaro = if (asignacionesClaro.isNotEmpty()) asignacionesClaro else equipoClaro.map { Posicion.DC to it }
            listaClaro.forEachIndexed { i, (_, j) ->
                canvas.drawText("${i + 1}. ${j.nombre}", 70f, yClaro, playerTextPaint)
                yClaro += 55f
            }

            formacionSugeridaClaro?.let { f ->
                val rectPitchClaro = RectF(540f, 290f, width - 65f, 955f)
                dibujarMiniCampo(rectPitchClaro, f, listaClaro, android.graphics.Color.parseColor("#D4FF00"), android.graphics.Color.BLACK)
            }

            // --- Tarjeta Equipo Oscuro ---
            val rectOscuro = RectF(40f, 1015f, width - 40f, 1805f)
            canvas.drawRoundRect(rectOscuro, 20f, 20f, cardPaint)
            canvas.drawRoundRect(rectOscuro, 20f, 20f, cardBorderPaint)

            sectionTitlePaint.color = android.graphics.Color.parseColor("#80D8FF")
            canvas.drawText("⚫ Equipo oscuro  (Alineación: ${formacionSugeridaOscuro?.nombre})", 70f, 1075f, sectionTitlePaint)

            var yOscuro = 1140f
            val listaOscuro = if (asignacionesOscuro.isNotEmpty()) asignacionesOscuro else equipoOscuro.map { Posicion.DC to it }
            listaOscuro.forEachIndexed { i, (_, j) ->
                canvas.drawText("${i + 1}. ${j.nombre}", 70f, yOscuro, playerTextPaint)
                yOscuro += 55f
            }

            formacionSugeridaOscuro?.let { f ->
                val rectPitchOscuro = RectF(540f, 1110f, width - 65f, 1775f)
                dibujarMiniCampo(rectPitchOscuro, f, listaOscuro, android.graphics.Color.WHITE, android.graphics.Color.BLACK)
            }

            // Pie
            val footerPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#94A3B8")
                textSize = 24f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("Generado con FutTracker ⚽", width / 2f, 1838f, footerPaint)

            // Guardar en cache
            val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
            val imageFile = File(imagesDir, "sorteo_alineaciones.png")
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()

            // Texto para compartir
            val textoCompartir = buildString {
                appendLine("⚽ *FutTracker - Sorteo y alineaciones*")
                appendLine("Modalidad: $modalidadNombre")
                appendLine()
                appendLine("⚪ *EQUIPO CLARO* (${formacionSugeridaClaro?.nombre})")
                listaClaro.forEach { (_, j) ->
                    appendLine("• ${j.nombre}")
                }
                appendLine()
                appendLine("⚫ *EQUIPO OSCURO* (${formacionSugeridaOscuro?.nombre})")
                listaOscuro.forEach { (_, j) ->
                    appendLine("• ${j.nombre}")
                }
            }

            // Compartir por Intent
            val imageUri = FileProvider.getUriForFile(context, "com.diegoguerrero.futtracker.fileprovider", imageFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, textoCompartir)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            intent.setPackage("com.whatsapp")
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                intent.setPackage(null)
                context.startActivity(Intent.createChooser(intent, "Compartir alineaciones en WhatsApp"))
            }
        } catch (e: Exception) {
            // Manejo de excepción
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sorteo de equipos", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkCard
                ),
                actions = {
                    if (sorteoRealizado) {
                        IconButton(onClick = { compartirAlineacionesPNG() }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir alineaciones", tint = LimeVolt)
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
                    listOf(10 to "Futsal", 12 to "Fútbol 6", 14 to "Fútbol 7").forEach { (cantidad, label) ->
                        FilterChip(
                            selected = totalJugadoresSeleccionados == cantidad,
                            onClick = { 
                                totalJugadoresSeleccionados = cantidad
                                sorteoRealizado = false
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            },
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
                        text = "Convocados: ${idsConvocados.size}/$totalJugadoresSeleccionados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { mostrarSelectorManual = !mostrarSelectorManual },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (mostrarSelectorManual) "Ocultar selector" else "Seleccionar jugadores", fontSize = 12.sp)
                    }
                }
            }

            // Selector manual con filtros de posición, favoritos y nombre
            if (mostrarSelectorManual) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
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

                            if (selectedPosicionFilter != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilterChip(
                                        selected = !soloPosicionPrincipalFilter,
                                        onClick = { soloPosicionPrincipalFilter = false },
                                        label = { Text("Ambas posiciones", fontSize = 11.sp) }
                                    )
                                    FilterChip(
                                        selected = soloPosicionPrincipalFilter,
                                        onClick = { soloPosicionPrincipalFilter = true },
                                        label = { Text("Solo posición principal", fontSize = 11.sp) }
                                    )
                                }
                            }

                            val filtrados = jugadores.filter { jugador ->
                                val coincideBusqueda = searchQuery.isBlank() || jugador.nombre.contains(searchQuery, ignoreCase = true)
                                val coincideFav = !soloFavoritos || jugador.esFavorito
                                val coincidePos = selectedPosicionFilter == null ||
                                    if (soloPosicionPrincipalFilter) {
                                        selectedPosicionFilter in jugador.posicionesPrimarias
                                    } else {
                                        selectedPosicionFilter in jugador.posicionesPrimarias ||
                                        selectedPosicionFilter in jugador.posicionesSecundarias
                                    }
                                coincideBusqueda && coincideFav && coincidePos
                            }.sortedWith(
                                compareByDescending<Jugador> { it.esUsuarioPropio }
                                    .thenByDescending { it.id in idsConvocados }
                                    .thenByDescending { it.esFavorito }
                                    .thenBy { it.nombre.lowercase() }
                            )

                            if (filtrados.isEmpty()) {
                                Text("No hay jugadores encontrados.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    filtrados.forEach { j ->
                                        val seleccionado = j.id in idsConvocados
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (seleccionado) {
                                                        idsConvocados.remove(j.id)
                                                    } else if (idsConvocados.size < totalJugadoresSeleccionados) {
                                                        idsConvocados.add(j.id)
                                                    }
                                                }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = seleccionado,
                                                onCheckedChange = { check ->
                                                    if (check) {
                                                        if (!idsConvocados.contains(j.id) && idsConvocados.size < totalJugadoresSeleccionados) {
                                                            idsConvocados.add(j.id)
                                                        }
                                                    } else {
                                                        idsConvocados.remove(j.id)
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = LimeVolt,
                                                    checkmarkColor = Color.Black
                                                )
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            JugadorAvatar(
                                                fotoUri = j.fotoUri,
                                                nombre = j.nombre,
                                                tamano = 36.dp,
                                                permitirZoom = true
                                            )

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(j.nombre, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                val totalPos = j.posicionesPrimarias + j.posicionesSecundarias
                                                if (totalPos.isEmpty()) {
                                                    Text("Sin posición", fontSize = 10.sp, color = TextSecondary)
                                                } else {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        j.posicionesPrimarias.forEach { pos ->
                                                            BadgePosicion(label = pos.name, esPrimaria = true)
                                                        }
                                                        j.posicionesSecundarias.forEach { pos ->
                                                            BadgePosicion(label = pos.name, esPrimaria = false)
                                                        }
                                                    }
                                                }
                                            }

                                            if (j.esFavorito) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Favorito",
                                                    tint = Color(0xFFFFD700),
                                                    modifier = Modifier.size(16.dp)
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

            // Botones de Sorteo: Aleatorio vs Equilibrado
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { realizarSorteo(equilibradoPorPosiciones = false) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCard, contentColor = Color.White),
                        enabled = idsConvocados.size == totalJugadoresSeleccionados
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aleatorio", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Botón Equilibrado con texto en negro
                    Button(
                        onClick = { realizarSorteo(equilibradoPorPosiciones = true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeVolt, contentColor = Color.Black),
                        enabled = idsConvocados.size == totalJugadoresSeleccionados
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
                                Text("⚪ Equipo claro", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                                if (formacionSugeridaClaro != null) {
                                    Text("Formación: ${formacionSugeridaClaro?.nombre}", fontSize = 11.sp, color = Color.DarkGray)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                equipoClaro.forEachIndexed { index, jugador ->
                                    Text("${index + 1}. ${jugador.nombre}", fontSize = 12.sp, color = Color.DarkGray)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3430))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("⚫ Equipo oscuro", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                if (formacionSugeridaOscuro != null) {
                                    Text("Formación: ${formacionSugeridaOscuro?.nombre}", fontSize = 11.sp, color = LimeVolt)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                equipoOscuro.forEachIndexed { index, jugador ->
                                    Text("${index + 1}. ${jugador.nombre}", fontSize = 12.sp, color = Color.LightGray)
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
                                Text("Compartir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                text = { Text("Equipo claro (${formacionSugeridaClaro?.nombre})", fontSize = 12.sp) }
                            )
                            Tab(
                                selected = tabEquipoAlineacion == 1,
                                onClick = { tabEquipoAlineacion = 1 },
                                text = { Text("Equipo oscuro (${formacionSugeridaOscuro?.nombre})", fontSize = 12.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Selector para modificar la formación sugerida
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Cambiar formación:", fontSize = 11.sp, color = TextSecondary)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                val formacionActual = if (tabEquipoAlineacion == 0) formacionSugeridaClaro else formacionSugeridaOscuro
                                items(formacionesModalidad) { f ->
                                    FilterChip(
                                        selected = formacionActual?.id == f.id,
                                        onClick = {
                                            if (tabEquipoAlineacion == 0) {
                                                cambiarFormacionClaro(f)
                                            } else {
                                                cambiarFormacionOscuro(f)
                                            }
                                        },
                                        label = { Text(f.nombre, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val mapaActual = if (tabEquipoAlineacion == 0) mapaCampoClaro else mapaCampoOscuro
                        mapaActual?.let { mapa ->
                            CampoFutbol(
                                alineacion = mapa,
                                modifier = Modifier.fillMaxWidth(),
                                onJugadorIntercambiado = { pos1, pos2 ->
                                    if (tabEquipoAlineacion == 0) {
                                        val j1 = mapaCampoClaro?.get(pos1)
                                        val j2 = mapaCampoClaro?.get(pos2)
                                        mapaCampoClaro = mapaCampoClaro?.toMutableMap()?.apply {
                                            put(pos1, j2)
                                            put(pos2, j1)
                                        }
                                    } else {
                                        val j1 = mapaCampoOscuro?.get(pos1)
                                        val j2 = mapaCampoOscuro?.get(pos2)
                                        mapaCampoOscuro = mapaCampoOscuro?.toMutableMap()?.apply {
                                            put(pos1, j2)
                                            put(pos2, j1)
                                        }
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

