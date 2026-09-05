package com.diegoguerrero.futtracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegoguerrero.futtracker.domain.model.Clima
import com.diegoguerrero.futtracker.domain.model.EquipoColor
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.ui.screens.estadisticas.*
import com.diegoguerrero.futtracker.ui.theme.*
import kotlin.math.max

private val SoleadoColor = Color(0xFFFFA000)
private val NubladoColor = Color(0xFF78909C)
private val LluviosoColor = Color(0xFF1E88E5)

private val ColoresEstadios = listOf(
    LimeVolt,
    Color(0xFF29B6F6),
    Color(0xFFAB47BC),
    Color(0xFFFF7043),
    Color(0xFF26A69A),
    Color(0xFFFFA726),
    Color(0xFFEC407A),
    Color(0xFF8D6E63)
)

@Composable
fun GraficoClima(
    statsClima: List<StatsClima>,
    totalPartidos: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Clima en los partidos 🌤️",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (totalPartidos == 0) {
                Text(
                    text = "No hay partidos registrados en este periodo",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut Chart
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            val strokeWidth = 18f
                            val diameter = size.minDimension - strokeWidth
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                            val arcSize = Size(diameter, diameter)

                            var startAngle = -90f
                            for (item in statsClima) {
                                if (item.total > 0) {
                                    val sweep = (item.total.toFloat() / totalPartidos) * 360f
                                    val color = when (item.clima) {
                                        Clima.SOLEADO -> SoleadoColor
                                        Clima.NUBLADO -> NubladoColor
                                        Clima.LLUVIOSO -> LluviosoColor
                                    }
                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = sweep - 2f,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    startAngle += sweep
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalPartidos",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "partidos",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Leyenda y conteos
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        statsClima.forEach { item ->
                            val color = when (item.clima) {
                                Clima.SOLEADO -> SoleadoColor
                                Clima.NUBLADO -> NubladoColor
                                Clima.LLUVIOSO -> LluviosoColor
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.clima.emoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.clima.label,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${item.total} PJ",
                                        color = color,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = color.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${String.format("%.0f", item.porcentaje)}%",
                                            color = color,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
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

@Composable
fun GraficoEstadios(
    statsEstadios: List<StatsEstadio>,
    totalPartidos: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estadios / Ubicaciones 🏟️",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (totalPartidos == 0 || statsEstadios.isEmpty()) {
                Text(
                    text = "No hay partidos registrados en estadios",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut Chart
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            val strokeWidth = 18f
                            val diameter = size.minDimension - strokeWidth
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                            val arcSize = Size(diameter, diameter)

                            var startAngle = -90f
                            statsEstadios.forEachIndexed { index, item ->
                                if (item.total > 0) {
                                    val sweep = (item.total.toFloat() / totalPartidos) * 360f
                                    val color = ColoresEstadios[index % ColoresEstadios.size]
                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = if (statsEstadios.size > 1) sweep - 2f else sweep,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    startAngle += sweep
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${statsEstadios.size}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "sedes",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Leyenda de estadios
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        statsEstadios.take(5).forEachIndexed { index, item ->
                            val color = ColoresEstadios[index % ColoresEstadios.size]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.nombre,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${item.total}",
                                        color = color,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = color.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${String.format("%.0f", item.porcentaje)}%",
                                            color = color,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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

@Composable
fun GraficoDiasSemana(
    statsDiasSemana: List<StatsDiaSemana>,
    modifier: Modifier = Modifier
) {
    val totalPartidos = statsDiasSemana.sumOf { it.total }
    val maxCount = statsDiasSemana.maxOfOrNull { it.total } ?: 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Días más jugados 📅",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (maxCount > 0) {
                    val diaTop = statsDiasSemana.maxByOrNull { it.total }?.dia ?: ""
                    Text(
                        text = "Top: $diaTop",
                        color = LimeVolt,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (totalPartidos == 0) {
                Text(
                    text = "No hay partidos registrados en este periodo",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    statsDiasSemana.forEach { item ->
                        val ratio = if (maxCount > 0) item.total.toFloat() / maxCount else 0f
                        val esMax = item.total == maxCount && item.total > 0

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (item.total > 0) "${item.total}" else "",
                                color = if (esMax) LimeVolt else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (esMax) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .fillMaxHeight(fraction = max(ratio, 0.06f))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        if (item.total > 0) {
                                            if (esMax) LimeVolt else LimeVolt.copy(alpha = 0.45f)
                                        } else {
                                            Color.White.copy(alpha = 0.08f)
                                        }
                                    )
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.dia,
                                color = if (esMax) Color.White else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (esMax) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GraficoClaroOscuro(
    statsClaro: StatsEquipoColor,
    statsOscuro: StatsEquipoColor,
    modifier: Modifier = Modifier
) {
    val total = statsClaro.partidosJugados + statsOscuro.partidosJugados

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Camisetas: Claro vs Oscuro ⚪⚫",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (total == 0) {
                Text(
                    text = "No se ha registrado color de camiseta en los partidos de este periodo",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TarjetaEquipoColor(
                        stats = statsClaro,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaEquipoColor(
                        stats = statsOscuro,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Barra comparativa de partidos jugados
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    if (statsClaro.partidosJugados > 0) {
                        Box(
                            modifier = Modifier
                                .weight(statsClaro.partidosJugados.toFloat())
                                .fillMaxHeight()
                                .background(Color(0xFFEEEEEE))
                        )
                    }
                    if (statsOscuro.partidosJugados > 0) {
                        Box(
                            modifier = Modifier
                                .weight(statsOscuro.partidosJugados.toFloat())
                                .fillMaxHeight()
                                .background(Color(0xFF333333))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaEquipoColor(
    stats: StatsEquipoColor,
    modifier: Modifier = Modifier
) {
    val esClaro = stats.color == EquipoColor.CLARO
    val cardBg = if (esClaro) Color(0xFF2A2A2A) else Color(0xFF1E1E1E)
    val cardBorder = if (esClaro) Color(0xFFE0E0E0) else Color(0xFF555555)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stats.color.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stats.color.label,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${stats.partidosJugados} PJ",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // % Victorias destacado
            val colorPct = obtenerColorPorcentaje(stats.porcentajeVictorias.toFloat())
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = colorPct.copy(alpha = 0.15f),
                border = BorderStroke(0.8.dp, colorPct.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Victorias", fontSize = 10.sp, color = colorPct)
                    Text(
                        text = "${stats.porcentajeVictorias}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorPct
                    )
                }
            }

            // V / E / D
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stats.victorias}V",
                    color = GreenWin,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stats.empates}E",
                    color = OrangeDraw,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stats.derrotas}D",
                    color = RedLoss,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun Posicion.nombreLargo(): String = when (this) {
    Posicion.POR -> "Portero"
    Posicion.DFC -> "Defensa Central"
    Posicion.LI -> "Lateral Izquierdo"
    Posicion.LD -> "Lateral Derecho"
    Posicion.MC -> "Centrocampista"
    Posicion.EI -> "Extremo Izquierdo"
    Posicion.ED -> "Extremo Derecho"
    Posicion.DC -> "Delantero Centro"
}

@Composable
fun GraficoMapaCalorPosiciones(
    posicionesFrecuencia: List<StatsPosicionFrecuencia>,
    totalPartidos: Int,
    modifier: Modifier = Modifier
) {
    val maxCount = posicionesFrecuencia.maxOfOrNull { it.total } ?: 0

    // Mapeo táctico de posiciones relativas en el campo (x: 0f..1f, y: 0f..1f)
    val coords = mapOf(
        Posicion.POR to Offset(0.50f, 0.88f),
        Posicion.LI to Offset(0.20f, 0.73f),
        Posicion.DFC to Offset(0.50f, 0.73f),
        Posicion.LD to Offset(0.80f, 0.73f),
        Posicion.MC to Offset(0.50f, 0.45f),
        Posicion.EI to Offset(0.20f, 0.22f),
        Posicion.DC to Offset(0.50f, 0.16f),
        Posicion.ED to Offset(0.80f, 0.22f)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Mapa de calor de posiciones ⚽",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (totalPartidos == 0) {
                Text(
                    text = "No hay partidos registrados en este periodo",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                // Campo táctico con mapa de calor
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF142618))
                        .border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                ) {
                    // Líneas del campo
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = Stroke(width = 1.5f)
                        val lineColor = Color.White.copy(alpha = 0.25f)

                        // Bordes
                        drawRect(color = lineColor, style = stroke)

                        // Línea de medio campo
                        drawLine(
                            color = lineColor,
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = 1.5f
                        )

                        // Círculo central
                        drawCircle(
                            color = lineColor,
                            radius = size.width * 0.16f,
                            center = Offset(size.width / 2, size.height / 2),
                            style = stroke
                        )

                        // Área arriba (rival)
                        drawRect(
                            color = lineColor,
                            topLeft = Offset(size.width * 0.25f, 0f),
                            size = Size(size.width * 0.5f, size.height * 0.18f),
                            style = stroke
                        )

                        // Área abajo (propia)
                        drawRect(
                            color = lineColor,
                            topLeft = Offset(size.width * 0.25f, size.height * 0.82f),
                            size = Size(size.width * 0.5f, size.height * 0.18f),
                            style = stroke
                        )

                        // Puntos de calor
                        posicionesFrecuencia.forEach { item ->
                            val pt = coords[item.posicion] ?: return@forEach
                            if (item.total > 0 && maxCount > 0) {
                                val intensity = item.total.toFloat() / maxCount
                                val px = pt.x * size.width
                                val py = pt.y * size.height

                                // Brillo radial
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            LimeVolt.copy(alpha = 0.65f * intensity),
                                            LimeVolt.copy(alpha = 0.25f * intensity),
                                            Color.Transparent
                                        ),
                                        center = Offset(px, py),
                                        radius = 42f * (0.8f + 0.5f * intensity)
                                    ),
                                    radius = 42f * (0.8f + 0.5f * intensity),
                                    center = Offset(px, py)
                                )
                            }
                        }
                    }

                    // Tokens de posiciones
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val width = maxWidth
                        val height = maxHeight

                        posicionesFrecuencia.forEach { item ->
                            val pt = coords[item.posicion] ?: return@forEach
                            val hasMatches = item.total > 0

                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = width * pt.x - 24.dp,
                                        y = height * pt.y - 12.dp
                                    )
                                    .width(48.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (hasMatches) LimeVolt else Color.Black.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        1.dp,
                                        if (hasMatches) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (hasMatches) "${item.posicion.name} (${item.total})" else item.posicion.name,
                                    color = if (hasMatches) Color.Black else TextSecondary.copy(alpha = 0.6f),
                                    fontSize = 9.sp,
                                    fontWeight = if (hasMatches) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Listado ordenado de posiciones con barra de progreso
                Text(
                    text = "Frecuencia por posición:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    posicionesFrecuencia.filter { it.total > 0 }.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BadgePosicion(label = item.posicion.name, esPrimaria = true)
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = item.posicion.nombreLargo(),
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "${item.total} PJ",
                                color = LimeVolt,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = LimeVolt.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${String.format("%.0f", item.porcentaje)}%",
                                    color = LimeVolt,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Barra de progreso relativa
                        LinearProgressIndicator(
                            progress = { if (maxCount > 0) item.total.toFloat() / maxCount else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = LimeVolt,
                            trackColor = Color.White.copy(alpha = 0.08f),
                        )
                    }
                }
            }
        }
    }
}
