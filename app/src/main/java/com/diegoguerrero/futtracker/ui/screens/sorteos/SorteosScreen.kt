package com.diegoguerrero.futtracker.ui.screens.sorteos

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.unit.Density
import kotlin.math.roundToInt
import com.diegoguerrero.futtracker.ui.components.CampoFutbol
import com.diegoguerrero.futtracker.ui.components.JugadorAvatar
import com.diegoguerrero.futtracker.ui.components.BadgePosicion
import com.diegoguerrero.futtracker.ui.components.FilaBadgesPosiciones
import com.diegoguerrero.futtracker.ui.theme.DarkCard
import com.diegoguerrero.futtracker.ui.theme.DarkCardBorder
import com.diegoguerrero.futtracker.ui.theme.LimeVolt
import com.diegoguerrero.futtracker.ui.theme.TextSecondary
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SorteosScreen(
    jugadores: List<Jugador>,
    estadios: List<Estadio> = emptyList()
) {
    val context = LocalContext.current
    val useCase = remember { GenerarAlineacionUseCase() }

    var totalJugadoresSeleccionados by remember { mutableStateOf(10) } // 10, 12, 14
    var equipoClaro by remember { mutableStateOf<List<Jugador>>(emptyList()) }
    var equipoOscuro by remember { mutableStateOf<List<Jugador>>(emptyList()) }
    var asignacionesClaro by remember { mutableStateOf<List<Pair<Posicion, Jugador>>>(emptyList()) }
    var asignacionesOscuro by remember { mutableStateOf<List<Pair<Posicion, Jugador>>>(emptyList()) }
    var sorteoRealizado by remember { mutableStateOf(false) }

    // Fecha, hora y lugar del partido para compartir
    var fechaHoraMillis by remember { mutableStateOf<Long?>(null) }
    var lugarSorteo by remember { mutableStateOf("") }
    var expandedEstadiosDropdown by remember { mutableStateOf(false) }
    val estadiosOrdenados = remember(estadios) { estadios.sortedBy { it.nombre.lowercase() } }

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
    var ordenNombreAscendente by remember { mutableStateOf<Boolean?>(null) }
    var ordenFechaDescendente by remember { mutableStateOf<Boolean?>(null) }
    var selectedPosicionFilter by remember { mutableStateOf<Posicion?>(null) }
    var soloPosicionPrincipalFilter by remember { mutableStateOf(false) }

    // Tab para visualizar la alineación del equipo claro u oscuro
    var tabEquipoAlineacion by remember { mutableStateOf(0) } // 0: Claro, 1: Oscuro

    // Jugador seleccionado para intercambio entre listas / posiciones
    var jugadorSeleccionadoParaMover by remember { mutableStateOf<Jugador?>(null) }

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

    fun volverASugeridaClaro() {
        val f = useCase.sugerirMejorFormacion(equipoClaro, formacionesModalidad)
        cambiarFormacionClaro(f)
    }

    fun volverASugeridaOscuro() {
        val f = useCase.sugerirMejorFormacion(equipoOscuro, formacionesModalidad)
        cambiarFormacionOscuro(f)
    }

    fun intercambiarJugadores(jugador1: Jugador, jugador2: Jugador) {
        if (jugador1.id == jugador2.id) return
        val estaEnClaro1 = equipoClaro.any { it.id == jugador1.id }
        val estaEnOscuro1 = equipoOscuro.any { it.id == jugador1.id }
        val estaEnClaro2 = equipoClaro.any { it.id == jugador2.id }
        val estaEnOscuro2 = equipoOscuro.any { it.id == jugador2.id }

        if (estaEnClaro1 && estaEnOscuro2) {
            val nuevoMapaClaro = mapaCampoClaro?.toMutableMap()
            val keyClaro = nuevoMapaClaro?.entries?.firstOrNull { it.value?.id == jugador1.id }?.key
            if (keyClaro != null) nuevoMapaClaro[keyClaro] = jugador2
            mapaCampoClaro = nuevoMapaClaro

            val nuevoMapaOscuro = mapaCampoOscuro?.toMutableMap()
            val keyOscuro = nuevoMapaOscuro?.entries?.firstOrNull { it.value?.id == jugador2.id }?.key
            if (keyOscuro != null) nuevoMapaOscuro[keyOscuro] = jugador1
            mapaCampoOscuro = nuevoMapaOscuro

            asignacionesClaro = asignacionesClaro.map { if (it.second.id == jugador1.id) it.first to jugador2 else it }
            asignacionesOscuro = asignacionesOscuro.map { if (it.second.id == jugador2.id) it.first to jugador1 else it }

            equipoClaro = equipoClaro.map { if (it.id == jugador1.id) jugador2 else it }
            equipoOscuro = equipoOscuro.map { if (it.id == jugador2.id) jugador1 else it }
        } else if (estaEnOscuro1 && estaEnClaro2) {
            intercambiarJugadores(jugador2, jugador1)
        } else if (estaEnClaro1 && estaEnClaro2) {
            val nuevoMapaClaro = mapaCampoClaro?.toMutableMap()
            val key1 = nuevoMapaClaro?.entries?.firstOrNull { it.value?.id == jugador1.id }?.key
            val key2 = nuevoMapaClaro?.entries?.firstOrNull { it.value?.id == jugador2.id }?.key
            if (key1 != null && key2 != null) {
                nuevoMapaClaro[key1] = jugador2
                nuevoMapaClaro[key2] = jugador1
                mapaCampoClaro = nuevoMapaClaro
            }
            val idx1 = asignacionesClaro.indexOfFirst { it.second.id == jugador1.id }
            val idx2 = asignacionesClaro.indexOfFirst { it.second.id == jugador2.id }
            if (idx1 != -1 && idx2 != -1) {
                val list = asignacionesClaro.toMutableList()
                val tempJ = list[idx1].second
                list[idx1] = list[idx1].first to list[idx2].second
                list[idx2] = list[idx2].first to tempJ
                asignacionesClaro = list
            }
            equipoClaro = equipoClaro.map {
                when (it.id) {
                    jugador1.id -> jugador2
                    jugador2.id -> jugador1
                    else -> it
                }
            }
        } else if (estaEnOscuro1 && estaEnOscuro2) {
            val nuevoMapaOscuro = mapaCampoOscuro?.toMutableMap()
            val key1 = nuevoMapaOscuro?.entries?.firstOrNull { it.value?.id == jugador1.id }?.key
            val key2 = nuevoMapaOscuro?.entries?.firstOrNull { it.value?.id == jugador2.id }?.key
            if (key1 != null && key2 != null) {
                nuevoMapaOscuro[key1] = jugador2
                nuevoMapaOscuro[key2] = jugador1
                mapaCampoOscuro = nuevoMapaOscuro
            }
            val idx1 = asignacionesOscuro.indexOfFirst { it.second.id == jugador1.id }
            val idx2 = asignacionesOscuro.indexOfFirst { it.second.id == jugador2.id }
            if (idx1 != -1 && idx2 != -1) {
                val list = asignacionesOscuro.toMutableList()
                val tempJ = list[idx1].second
                list[idx1] = list[idx1].first to list[idx2].second
                list[idx2] = list[idx2].first to tempJ
                asignacionesOscuro = list
            }
            equipoOscuro = equipoOscuro.map {
                when (it.id) {
                    jugador1.id -> jugador2
                    jugador2.id -> jugador1
                    else -> it
                }
            }
        }
    }

    fun compartirAlineacionesPNG() {
        if (!sorteoRealizado || formacionSugeridaClaro == null || formacionSugeridaOscuro == null) return

        try {
            val width = 1080
            val height = 1380
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
            canvas.drawText("Sorteo de equipos", width / 2f, 85f, titlePaint)

            val subtitlePaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 26f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val modalidadNombre = when (totalJugadoresSeleccionados) { 10 -> "Futsal"; 12 -> "Fútbol 6"; else -> "Fútbol 7" }
            val subTextBuilder = StringBuilder("🏟️ Modalidad: $modalidadNombre")
            if (fechaHoraMillis != null) {
                val sdf = SimpleDateFormat("EEEE, d 'de' MMMM - HH:mm", Locale("es", "ES"))
                subTextBuilder.append("  |  ").append(sdf.format(Date(fechaHoraMillis!!)).replaceFirstChar { it.uppercase() })
            }
            if (lugarSorteo.isNotBlank()) {
                subTextBuilder.append("  |  ").append(lugarSorteo.trim())
            }
            canvas.drawText(subTextBuilder.toString(), width / 2f, 135f, subtitlePaint)

            val cardPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#13151E")
                style = Paint.Style.FILL
            }

            val cardBorderClaroPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 1.8f
                isAntiAlias = true
            }

            val cardBorderOscuroPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#4A4D57")
                style = Paint.Style.STROKE
                strokeWidth = 1.8f
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

            fun dibujarMiniCampo(rect: RectF, mapaCampo: Map<Pair<Posicion, Pair<Float, Float>>, Jugador?>, colorFicha: Int, colorTextoFicha: Int) {
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

                mapaCampo.forEach { (posConCoord, jug) ->
                    val posReq = posConCoord.first
                    val coordPair = posConCoord.second
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

                        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = colorFicha
                            style = Paint.Style.FILL
                        }
                        val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = colorTextoFicha
                            textSize = 9.5f
                            isFakeBoldText = true
                            textAlign = Paint.Align.CENTER
                        }
                        val badgeRect = RectF(cx + 4f, cy + 4f, cx + 27f, cy + 20f)
                        canvas.drawRoundRect(badgeRect, 3f, 3f, badgeBgPaint)
                        canvas.drawText(posReq.name, badgeRect.centerX(), badgeRect.centerY() + 3.5f, badgeTextPaint)
                    } else {
                        canvas.drawCircle(cx, cy, 20f, dotPaint)
                        canvas.drawText(posReq.name, cx, cy + 6f, labelPaint)
                    }

                    if (jug != null) {
                        val originalTextSize = namePaint.textSize
                        val nombreMostrado = jug.nombre
                        if (nombreMostrado.length > 9) {
                            namePaint.textSize = 11f
                        }
                        canvas.drawText(nombreMostrado, cx, cy + 39f, namePaint)
                        namePaint.textSize = originalTextSize
                    }
                }
            }

            // --- Tarjeta Equipo Claro ---
            val rectClaro = RectF(40f, 185f, width - 40f, 755f)
            canvas.drawRoundRect(rectClaro, 20f, 20f, cardPaint)
            canvas.drawRoundRect(rectClaro, 20f, 20f, cardBorderClaroPaint)

            sectionTitlePaint.color = android.graphics.Color.parseColor("#D4FF00")
            canvas.drawText("⚪ Equipo claro", 70f, 240f, sectionTitlePaint)

            val listaClaro = mapaCampoClaro?.entries
                ?.sortedWith(compareBy<Map.Entry<Pair<Posicion, Pair<Float, Float>>, Jugador?>> { it.key.first.ordinal }.thenBy { it.key.second.first })
                ?.mapNotNull { entry -> entry.value?.let { entry.key.first to it } }
                ?: (if (asignacionesClaro.isNotEmpty()) asignacionesClaro.sortedBy { it.first.ordinal } else equipoClaro.map { Posicion.DC to it })

            var yClaro = 300f
            listaClaro.forEachIndexed { i, (_, j) ->
                canvas.drawText("${i + 1}. ${j.nombre}", 70f, yClaro, playerTextPaint)
                yClaro += 58f
            }

            mapaCampoClaro?.let { mapa ->
                val rectPitchClaro = RectF(460f, 220f, width - 55f, 740f)
                dibujarMiniCampo(rectPitchClaro, mapa, android.graphics.Color.WHITE, android.graphics.Color.BLACK)
            }

            // --- Tarjeta Equipo Oscuro ---
            val rectOscuro = RectF(40f, 780f, width - 40f, 1350f)
            canvas.drawRoundRect(rectOscuro, 20f, 20f, cardPaint)
            canvas.drawRoundRect(rectOscuro, 20f, 20f, cardBorderOscuroPaint)

            sectionTitlePaint.color = android.graphics.Color.parseColor("#80D8FF")
            canvas.drawText("⚫ Equipo oscuro", 70f, 835f, sectionTitlePaint)

            val listaOscuro = mapaCampoOscuro?.entries
                ?.sortedWith(compareBy<Map.Entry<Pair<Posicion, Pair<Float, Float>>, Jugador?>> { it.key.first.ordinal }.thenBy { it.key.second.first })
                ?.mapNotNull { entry -> entry.value?.let { entry.key.first to it } }
                ?: (if (asignacionesOscuro.isNotEmpty()) asignacionesOscuro.sortedBy { it.first.ordinal } else equipoOscuro.map { Posicion.DC to it })

            var yOscuro = 895f
            listaOscuro.forEachIndexed { i, (_, j) ->
                canvas.drawText("${i + 1}. ${j.nombre}", 70f, yOscuro, playerTextPaint)
                yOscuro += 58f
            }

            mapaCampoOscuro?.let { mapa ->
                val rectPitchOscuro = RectF(460f, 815f, width - 55f, 1335f)
                dibujarMiniCampo(rectPitchOscuro, mapa, android.graphics.Color.BLACK, android.graphics.Color.WHITE)
            }

            // Guardar en cache
            val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
            val imageFile = File(imagesDir, "sorteo_alineaciones.png")
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()

            // Texto para compartir
            val textoCompartir = buildString {
                appendLine("⚽ *Sorteo de equipos*")
                appendLine("🏟️ Modalidad: $modalidadNombre")
                if (fechaHoraMillis != null) {
                    val sdf = SimpleDateFormat("EEEE, d 'de' MMMM - HH:mm", Locale("es", "ES"))
                    val fStr = sdf.format(Date(fechaHoraMillis!!)).replaceFirstChar { it.uppercase() }
                    appendLine("📅 Fecha: $fStr")
                }
                if (lugarSorteo.isNotBlank()) {
                    appendLine("📍 Lugar: ${lugarSorteo.trim()}")
                }
                appendLine()
                appendLine("⚪ *EQUIPO CLARO*")
                listaClaro.forEach { (_, j) ->
                    appendLine("• ${j.nombre}")
                }
                appendLine()
                appendLine("⚫ *EQUIPO OSCURO*")
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

            // Detalles del partido para compartir (Fecha, Hora, Lugar)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Detalles del partido:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        // Selector de fecha y hora
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val cal = Calendar.getInstance().apply {
                                        timeInMillis = fechaHoraMillis ?: System.currentTimeMillis()
                                    }
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            TimePickerDialog(
                                                context,
                                                { _, hourOfDay, minute ->
                                                    val newCal = Calendar.getInstance().apply {
                                                        set(Calendar.YEAR, year)
                                                        set(Calendar.MONTH, month)
                                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                                                        set(Calendar.MINUTE, minute)
                                                    }
                                                    fechaHoraMillis = newCal.timeInMillis
                                                },
                                                cal.get(Calendar.HOUR_OF_DAY),
                                                cal.get(Calendar.MINUTE),
                                                true
                                            ).show()
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = LimeVolt,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = fechaHoraMillis?.let {
                                        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM - HH:mm", Locale("es", "ES"))
                                        sdf.format(Date(it)).replaceFirstChar { c -> c.uppercase() }
                                    } ?: "Fecha y hora (opcional)",
                                    color = if (fechaHoraMillis != null) Color.White else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (fechaHoraMillis != null) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                            if (fechaHoraMillis != null) {
                                IconButton(
                                    onClick = { fechaHoraMillis = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Limpiar fecha",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Lugar / Estadio (Dropdown)
                        ExposedDropdownMenuBox(
                            expanded = expandedEstadiosDropdown,
                            onExpandedChange = { expandedEstadiosDropdown = !expandedEstadiosDropdown },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = lugarSorteo,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = {
                                    Text(
                                        if (estadios.isEmpty()) "No hay estadios registrados" else "Seleccionar estadio...",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = LimeVolt, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (lugarSorteo.isNotEmpty()) {
                                            IconButton(
                                                onClick = { lugarSorteo = "" },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Clear, contentDescription = "Limpiar estadio", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstadiosDropdown)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LimeVolt,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedEstadiosDropdown,
                                onDismissRequest = { expandedEstadiosDropdown = false },
                                modifier = Modifier.background(DarkCard)
                            ) {
                                if (estadiosOrdenados.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No hay estadios creados (añade uno en Datos)", color = TextSecondary, fontSize = 13.sp) },
                                        onClick = { expandedEstadiosDropdown = false }
                                    )
                                } else {
                                    estadiosOrdenados.forEach { est ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Stadium, contentDescription = null, tint = LimeVolt, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(est.nombre, color = Color.White, fontSize = 13.sp)
                                                }
                                            },
                                            onClick = {
                                                lugarSorteo = est.nombre
                                                expandedEstadiosDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
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

                            // Fila 1: Ordenar y Favoritos
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item {
                                    FilterChip(
                                        selected = soloFavoritos,
                                        onClick = { soloFavoritos = !soloFavoritos },
                                        label = { Text("Favoritos", fontSize = 11.sp) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = ordenNombreAscendente != null,
                                        onClick = {
                                            ordenFechaDescendente = null
                                            ordenNombreAscendente = when (ordenNombreAscendente) {
                                                null -> true
                                                true -> false
                                                false -> null
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.SortByAlpha,
                                                contentDescription = "Ordenar por nombre",
                                                tint = if (ordenNombreAscendente != null) LimeVolt else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = when (ordenNombreAscendente) {
                                                    true -> "Nombre (A-Z)"
                                                    false -> "Nombre (Z-A)"
                                                    null -> "Nombre"
                                                },
                                                fontSize = 11.sp
                                            )
                                        }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = ordenFechaDescendente != null,
                                        onClick = {
                                            ordenNombreAscendente = null
                                            ordenFechaDescendente = when (ordenFechaDescendente) {
                                                null -> true
                                                true -> false
                                                false -> null
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Ordenar por fecha",
                                                tint = if (ordenFechaDescendente != null) LimeVolt else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = when (ordenFechaDescendente) {
                                                    true -> "Añadido recientemente"
                                                    false -> "Más antiguos primero"
                                                    null -> "Fecha añadido"
                                                },
                                                fontSize = 11.sp
                                            )
                                        }
                                    )
                                }
                            }

                            // Fila 2: Posiciones
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item {
                                    FilterChip(
                                        selected = selectedPosicionFilter == null,
                                        onClick = { selectedPosicionFilter = null },
                                        label = { Text("Todas", fontSize = 11.sp) }
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
                            }.sortedWith { a, b ->
                                when {
                                    ordenNombreAscendente != null -> {
                                        if (ordenNombreAscendente == true) a.nombre.compareTo(b.nombre, ignoreCase = true)
                                        else b.nombre.compareTo(a.nombre, ignoreCase = true)
                                    }
                                    ordenFechaDescendente != null -> {
                                        if (ordenFechaDescendente == true) b.fechaCreacion.compareTo(a.fechaCreacion)
                                        else a.fechaCreacion.compareTo(b.fechaCreacion)
                                    }
                                    else -> {
                                        val convA = a.id in idsConvocados
                                        val convB = b.id in idsConvocados
                                        if (convA != convB) return@sortedWith if (convB) 1 else -1
                                        val userA = a.esUsuarioPropio
                                        val userB = b.esUsuarioPropio
                                        if (userA != userB) return@sortedWith if (userB) 1 else -1
                                        val favA = a.esFavorito
                                        val favB = b.esFavorito
                                        if (favA != favB) return@sortedWith if (favB) 1 else -1
                                        a.nombre.compareTo(b.nombre, ignoreCase = true)
                                    }
                                }
                            }

                            if (filtrados.isEmpty()) {
                                Text("No hay jugadores encontrados.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 260.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    filtrados.forEach { j ->
                                        val seleccionado = j.id in idsConvocados
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (seleccionado) {
                                                        idsConvocados.remove(j.id)
                                                    } else if (idsConvocados.size < totalJugadoresSeleccionados) {
                                                        idsConvocados.add(j.id)
                                                    }
                                                },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (seleccionado) LimeVolt.copy(alpha = 0.08f) else DarkCard
                                            ),
                                            border = BorderStroke(
                                                1.dp,
                                                if (seleccionado) LimeVolt.copy(alpha = 0.5f) else DarkCardBorder
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 10.dp),
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

                                                Spacer(modifier = Modifier.width(6.dp))

                                                JugadorAvatar(
                                                    fotoUri = j.fotoUri,
                                                    nombre = j.nombre,
                                                    tamano = 42.dp,
                                                    permitirZoom = true
                                                )

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(j.nombre, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    FilaBadgesPosiciones(
                                                        primarias = j.posicionesPrimarias,
                                                        secundarias = j.posicionesSecundarias,
                                                        maxVisibles = 3
                                                    )
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
                // Banner informativo / feedback de arrastre o pulsación
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        color = LimeVolt.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = LimeVolt,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (jugadorSeleccionadoParaMover != null) {
                                    "Seleccionado: ${jugadorSeleccionadoParaMover?.nombreConTu()}. Toca otro jugador para intercambiar."
                                } else {
                                    "Arrastra jugadores entre listas o tócalos para intercambiarlos"
                                },
                                color = if (jugadorSeleccionadoParaMover != null) LimeVolt else Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (jugadorSeleccionadoParaMover != null) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                item {
                    val listaMostradaClaro = mapaCampoClaro?.entries
                        ?.sortedWith(compareBy<Map.Entry<Pair<Posicion, Pair<Float, Float>>, Jugador?>> { it.key.first.ordinal }.thenBy { it.key.second.first })
                        ?.mapNotNull { entry -> entry.value?.let { entry.key.first to it } }
                        ?: (if (asignacionesClaro.isNotEmpty()) asignacionesClaro.sortedBy { it.first.ordinal } else equipoClaro.map { Posicion.DC to it })

                    val listaMostradaOscuro = mapaCampoOscuro?.entries
                        ?.sortedWith(compareBy<Map.Entry<Pair<Posicion, Pair<Float, Float>>, Jugador?>> { it.key.first.ordinal }.thenBy { it.key.second.first })
                        ?.mapNotNull { entry -> entry.value?.let { entry.key.first to it } }
                        ?: (if (asignacionesOscuro.isNotEmpty()) asignacionesOscuro.sortedBy { it.first.ordinal } else equipoOscuro.map { Posicion.DC to it })

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE)),
                            border = BorderStroke(1.5.dp, Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("⚪ Equipo claro", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                                if (formacionSugeridaClaro != null) {
                                    Text("Formación: ${formacionSugeridaClaro?.nombre}", fontSize = 11.sp, color = Color.DarkGray)
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                listaMostradaClaro.forEachIndexed { index, (pos, jugador) ->
                                    ItemJugadorSorteo(
                                        index = index,
                                        jugador = jugador,
                                        posicion = pos,
                                        esEquipoClaro = true,
                                        esSeleccionado = jugadorSeleccionadoParaMover?.id == jugador.id,
                                        onDragAEquipoContrario = { dragY ->
                                            val itemHeightPx = 44.dp.toPx()
                                            val targetIndex = (index + (dragY / itemHeightPx).roundToInt()).coerceIn(0, listaMostradaOscuro.size - 1)
                                            if (targetIndex in listaMostradaOscuro.indices) {
                                                intercambiarJugadores(jugador, listaMostradaOscuro[targetIndex].second)
                                            }
                                        },
                                        onDragVerticalMismoEquipo = { dragY ->
                                            val itemHeightPx = 44.dp.toPx()
                                            val targetIndex = (index + (dragY / itemHeightPx).roundToInt()).coerceIn(0, listaMostradaClaro.size - 1)
                                            if (targetIndex != index && targetIndex in listaMostradaClaro.indices) {
                                                intercambiarJugadores(jugador, listaMostradaClaro[targetIndex].second)
                                            }
                                        },
                                        onClick = {
                                            if (jugadorSeleccionadoParaMover == null) {
                                                jugadorSeleccionadoParaMover = jugador
                                            } else if (jugadorSeleccionadoParaMover?.id == jugador.id) {
                                                jugadorSeleccionadoParaMover = null
                                            } else {
                                                intercambiarJugadores(jugadorSeleccionadoParaMover!!, jugador)
                                                jugadorSeleccionadoParaMover = null
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3430)),
                            border = BorderStroke(1.5.dp, Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("⚫ Equipo oscuro", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                if (formacionSugeridaOscuro != null) {
                                    Text("Formación: ${formacionSugeridaOscuro?.nombre}", fontSize = 11.sp, color = LimeVolt)
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                listaMostradaOscuro.forEachIndexed { index, (pos, jugador) ->
                                    ItemJugadorSorteo(
                                        index = index,
                                        jugador = jugador,
                                        posicion = pos,
                                        esEquipoClaro = false,
                                        esSeleccionado = jugadorSeleccionadoParaMover?.id == jugador.id,
                                        onDragAEquipoContrario = { dragY ->
                                            val itemHeightPx = 44.dp.toPx()
                                            val targetIndex = (index + (dragY / itemHeightPx).roundToInt()).coerceIn(0, listaMostradaClaro.size - 1)
                                            if (targetIndex in listaMostradaClaro.indices) {
                                                intercambiarJugadores(jugador, listaMostradaClaro[targetIndex].second)
                                            }
                                        },
                                        onDragVerticalMismoEquipo = { dragY ->
                                            val itemHeightPx = 44.dp.toPx()
                                            val targetIndex = (index + (dragY / itemHeightPx).roundToInt()).coerceIn(0, listaMostradaOscuro.size - 1)
                                            if (targetIndex != index && targetIndex in listaMostradaOscuro.indices) {
                                                intercambiarJugadores(jugador, listaMostradaOscuro[targetIndex].second)
                                            }
                                        },
                                        onClick = {
                                            if (jugadorSeleccionadoParaMover == null) {
                                                jugadorSeleccionadoParaMover = jugador
                                            } else if (jugadorSeleccionadoParaMover?.id == jugador.id) {
                                                jugadorSeleccionadoParaMover = null
                                            } else {
                                                intercambiarJugadores(jugadorSeleccionadoParaMover!!, jugador)
                                                jugadorSeleccionadoParaMover = null
                                            }
                                        }
                                    )
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
                                val fSugerida = if (tabEquipoAlineacion == 0) useCase.sugerirMejorFormacion(equipoClaro, formacionesModalidad) else useCase.sugerirMejorFormacion(equipoOscuro, formacionesModalidad)
                                val esSugeridaSeleccionada = formacionActual?.id == fSugerida.id

                                item {
                                    FilterChip(
                                        selected = esSugeridaSeleccionada,
                                        onClick = {
                                            if (tabEquipoAlineacion == 0) {
                                                volverASugeridaClaro()
                                            } else {
                                                volverASugeridaOscuro()
                                            }
                                        },
                                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = esSugeridaSeleccionada, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
                                        label = { Text("⭐ Sugerida", fontSize = 11.sp) }
                                    )
                                }

                                items(formacionesModalidad) { f ->
                                    val sel = formacionActual?.id == f.id && !esSugeridaSeleccionada
                                    FilterChip(
                                        selected = sel,
                                        onClick = {
                                            if (tabEquipoAlineacion == 0) {
                                                cambiarFormacionClaro(f)
                                            } else {
                                                cambiarFormacionOscuro(f)
                                            }
                                        },
                                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, borderColor = LimeVolt.copy(alpha = 0.5f), selectedBorderColor = LimeVolt),
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
                                colorBordeFicha = if (tabEquipoAlineacion == 0) Color.White else Color.Black,
                                modifier = Modifier.fillMaxWidth(),
                                onJugadorIntercambiado = null
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

@Composable
private fun ItemJugadorSorteo(
    index: Int,
    jugador: Jugador,
    posicion: Posicion,
    esEquipoClaro: Boolean,
    esSeleccionado: Boolean,
    onDragAEquipoContrario: Density.(dragY: Float) -> Unit,
    onDragVerticalMismoEquipo: Density.(dragY: Float) -> Unit,
    onClick: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val bgColor = when {
        esSeleccionado -> LimeVolt.copy(alpha = 0.28f)
        isDragging -> if (esEquipoClaro) Color.White else Color(0xFF333E38)
        esEquipoClaro -> Color.White.copy(alpha = 0.95f)
        else -> Color(0xFF202723)
    }

    val borderColor = when {
        esSeleccionado -> LimeVolt
        isDragging -> LimeVolt
        esEquipoClaro -> Color(0xFFD4D4D4)
        else -> Color(0xFF3B4842)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.5.dp)
            .zIndex(if (isDragging) 99f else 1f)
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(jugador.id) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                    },
                    onDragEnd = {
                        val dragX = offsetX
                        val dragY = offsetY
                        isDragging = false
                        offsetX = 0f
                        offsetY = 0f

                        val umbralHorizontal = 45.dp.toPx()
                        val umbralVertical = 25.dp.toPx()

                        if (esEquipoClaro && dragX > umbralHorizontal) {
                            onDragAEquipoContrario(dragY)
                        } else if (!esEquipoClaro && dragX < -umbralHorizontal) {
                            onDragAEquipoContrario(dragY)
                        } else if (kotlin.math.abs(dragY) > umbralVertical) {
                            onDragVerticalMismoEquipo(dragY)
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(if (esSeleccionado || isDragging) 1.5.dp else 1.dp, borderColor),
        shadowElevation = if (isDragging) 6.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Arrastrar",
                tint = if (esEquipoClaro) Color.Gray else Color(0xFF888888),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "${index + 1}.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (esEquipoClaro) Color.DarkGray else Color(0xFFCCCCCC)
            )
            Spacer(modifier = Modifier.width(4.dp))
            JugadorAvatar(
                fotoUri = jugador.fotoUri,
                nombre = jugador.nombre,
                tamano = 22.dp
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = jugador.nombreConTu(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (esEquipoClaro) Color.Black else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                color = if (esEquipoClaro) Color(0xFFEEEEEE) else Color(0xFF13151E),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.width(36.dp).height(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = posicion.name,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                        ),
                        color = if (esEquipoClaro) Color(0xFF333333) else LimeVolt,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(y = (-1).dp)
                    )
                }
            }
        }
    }
}


