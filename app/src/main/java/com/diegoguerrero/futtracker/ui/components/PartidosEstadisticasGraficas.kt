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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.lerp
import com.diegoguerrero.futtracker.domain.model.Clima
import com.diegoguerrero.futtracker.domain.model.EquipoColor
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.ui.screens.estadisticas.*
import com.diegoguerrero.futtracker.ui.theme.*
import java.util.Locale
import kotlin.math.max

private val DespejadoColor = Color(0xFFFFA000)
private val NubladoColor = Color(0xFF78909C)
private val LluviosoColor = Color(0xFF1E88E5)
private val TechadoColor = Color(0xFF8D6E63)

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
                text = "🌤️ Clima en los partidos",
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
                                        Clima.DESPEJADO -> DespejadoColor
                                        Clima.NUBLADO -> NubladoColor
                                        Clima.LLUVIOSO -> LluviosoColor
                                        null -> TechadoColor
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
                                Clima.DESPEJADO -> DespejadoColor
                                Clima.NUBLADO -> NubladoColor
                                Clima.LLUVIOSO -> LluviosoColor
                                null -> TechadoColor
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.emoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.label,
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
                text = "🏟️ Ubicaciones",
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
                                text = if (statsEstadios.size == 1) "campo" else "campos",
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
                    text = "📅 Días más jugados",
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
                        .height(125.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Eje Y con escala
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(bottom = 22.dp, end = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(text = "$maxCount", color = TextSecondary, fontSize = 9.sp)
                        Text(text = "${(maxCount + 1) / 2}", color = TextSecondary, fontSize = 9.sp)
                        Text(text = "0", color = TextSecondary, fontSize = 9.sp)
                    }

                    // Barras
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
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
}

@Composable
fun GraficoHorasPartidos(
    statsHoras: List<StatsHoraPartido>,
    modifier: Modifier = Modifier
) {
    val total = statsHoras.sumOf { it.total }
    val maxCount = statsHoras.maxOfOrNull { it.total } ?: 0

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
                    text = "🕒 Horas más jugadas",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (maxCount > 0) {
                    val horaTop = statsHoras.maxByOrNull { it.total }?.horaTexto ?: ""
                    Text(
                        text = "Top: $horaTop",
                        color = LimeVolt,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (total == 0 || statsHoras.isEmpty()) {
                Text(
                    text = "No hay partidos registrados en este periodo",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Eje Y con escala
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(bottom = 22.dp, end = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(text = "$maxCount", color = TextSecondary, fontSize = 9.sp)
                        Text(text = "${(maxCount + 1) / 2}", color = TextSecondary, fontSize = 9.sp)
                        Text(text = "0", color = TextSecondary, fontSize = 9.sp)
                    }

                    // Barras desplazables
                    LazyRow(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        items(statsHoras) { item ->
                            val ratio = if (maxCount > 0) item.total.toFloat() / maxCount else 0f
                            val esMax = item.total == maxCount && item.total > 0

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.fillMaxHeight()
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
                                        .width(22.dp)
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
                                    text = item.horaTexto,
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
                text = "👕 Claro vs. Oscuro",
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
    Posicion.DFC -> "Defensa central"
    Posicion.LI -> "Lateral izquierdo"
    Posicion.LD -> "Lateral derecho"
    Posicion.MC -> "Centrocampista"
    Posicion.EI -> "Extremo izquierdo"
    Posicion.ED -> "Extremo derecho"
    Posicion.DC -> "Delantero centro"
}

@Composable
fun GraficoMapaCalorPosiciones(
    posicionesFrecuencia: List<StatsPosicionFrecuencia>,
    totalPartidos: Int,
    modifier: Modifier = Modifier
) {
    val maxMins = posicionesFrecuencia.maxOfOrNull { it.minutos.toFloat() } ?: 0f

    // Mapeo táctico de posiciones relativas en el campo (x: 0f..1f, y: 0f..1f)
    val coords = mapOf(
        Posicion.POR to Offset(0.50f, 0.91f),
        Posicion.LI to Offset(0.20f, 0.68f),
        Posicion.DFC to Offset(0.50f, 0.68f),
        Posicion.LD to Offset(0.80f, 0.68f),
        Posicion.MC to Offset(0.50f, 0.44f),
        Posicion.EI to Offset(0.20f, 0.22f),
        Posicion.DC to Offset(0.50f, 0.14f),
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
                text = "🗺️ Mapa de calor de posiciones",
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
                        .height(290.dp)
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
                            if (item.minutos > 0 && maxMins > 0f) {
                                val intensity = item.minutos.toFloat() / maxMins
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
                            val hasMatches = item.minutos > 0
                            val ratio = if (maxMins > 0f) (item.minutos.toFloat() / maxMins).coerceIn(0f, 1f) else 0f
                            val chipBg = if (hasMatches) {
                                lerp(Color(0xFF1B4D25), LimeVolt, ratio)
                            } else {
                                Color.Black.copy(alpha = 0.5f)
                            }
                            val chipTextColor = if (hasMatches && ratio > 0.45f) Color.Black else Color.White

                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = width * pt.x - 28.dp,
                                        y = height * pt.y - 15.dp
                                    )
                                    .width(56.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(chipBg)
                                    .border(
                                        1.dp,
                                        if (hasMatches) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(vertical = 2.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = item.posicion.name,
                                        color = chipTextColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    if (hasMatches) {
                                        Text(
                                            text = "${item.minutos}m · ${item.porcentajeVictorias}%V",
                                            color = chipTextColor.copy(alpha = 0.9f),
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Listado ordenado de posiciones con estadísticas detalladas
                Text(
                    text = "Detalle por posición:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    posicionesFrecuencia.filter { it.minutos > 0 }.forEach { item ->
                        Surface(
                            color = DarkCard.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BadgePosicion(label = item.posicion.name, esPrimaria = true)
                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = item.posicion.nombreLargo(),
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = LimeVolt.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${String.format(Locale.getDefault(), "%.0f", item.porcentaje)}% tiempo",
                                            color = LimeVolt,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⏱️ ${item.minutos} mins",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "🏟️ ${item.partidosJugados} PJ",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "⚖️ ${item.victorias}V/${item.empates}E/${item.derrotas}D",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "🏆 ${item.porcentajeVictorias}% V",
                                        fontSize = 11.sp,
                                        color = if (item.porcentajeVictorias >= 50) LimeVolt else Color(0xFFF59E0B),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { if (maxMins > 0f) item.minutos.toFloat() / maxMins else 0f },
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
    }
}
