package com.diegoguerrero.futtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.ui.theme.DarkCard
import com.diegoguerrero.futtracker.ui.theme.LimeVolt
import com.diegoguerrero.futtracker.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

private val VictoriaColor = Color(0xFF4CAF50)
private val EmpateColor = Color(0xFFFFB300)
private val DerrotaColor = Color(0xFFE53935)
private val AsistenciaColor = Color(0xFF29B6F6)

@Composable
fun GraficoResultados(
    partidos: List<Partido>,
    modifier: Modifier = Modifier
) {
    val victorias = partidos.count { it.esVictoria }
    val empates = partidos.count { it.esEmpate }
    val derrotas = partidos.count { it.esDerrota }
    val total = partidos.size

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Balance de Resultados",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (total == 0) {
                Text(
                    text = "No hay partidos registrados",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                // Barra segmentada de porcentaje
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    if (victorias > 0) {
                        Box(
                            modifier = Modifier
                                .weight(victorias.toFloat())
                                .fillMaxHeight()
                                .background(VictoriaColor)
                        )
                    }
                    if (empates > 0) {
                        Box(
                            modifier = Modifier
                                .weight(empates.toFloat())
                                .fillMaxHeight()
                                .background(EmpateColor)
                        )
                    }
                    if (derrotas > 0) {
                        Box(
                            modifier = Modifier
                                .weight(derrotas.toFloat())
                                .fillMaxHeight()
                                .background(DerrotaColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IndicadorResultado(
                        label = "Victorias",
                        count = victorias,
                        color = VictoriaColor,
                        porcentaje = if (total > 0) (victorias * 100 / total) else 0
                    )
                    IndicadorResultado(
                        label = "Empates",
                        count = empates,
                        color = EmpateColor,
                        porcentaje = if (total > 0) (empates * 100 / total) else 0
                    )
                    IndicadorResultado(
                        label = "Derrotas",
                        count = derrotas,
                        color = DerrotaColor,
                        porcentaje = if (total > 0) (derrotas * 100 / total) else 0
                    )
                }
            }
        }
    }
}

@Composable
private fun IndicadorResultado(label: String, count: Int, color: Color, porcentaje: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "$count ($porcentaje%)",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun GraficoGolesAsistencias(
    partidos: List<Partido>,
    modifier: Modifier = Modifier
) {
    // Tomamos hasta los últimos 7 partidos ordenados cronológicamente
    val partidosRecientes = partidos.sortedBy { it.fecha }.takeLast(7)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Goles y Asistencias",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LimeVolt)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Goles", color = TextSecondary, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AsistenciaColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Asist.", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (partidosRecientes.isEmpty()) {
                Text(
                    text = "No hay partidos registrados aún",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                val maxVal = max(
                    1,
                    partidosRecientes.maxOfOrNull { max(it.goles, it.asistencias) } ?: 1
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height - 24.dp.toPx()
                        val n = partidosRecientes.size
                        val slotWidth = canvasWidth / n
                        val barWidth = (slotWidth * 0.28f).coerceAtMost(16.dp.toPx())

                        // Líneas de guía horizontal
                        val guideLineCount = 3
                        for (i in 0..guideLineCount) {
                            val y = canvasHeight * (i.toFloat() / guideLineCount)
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1f
                            )
                        }

                        partidosRecientes.forEachIndexed { index, p ->
                            val centerX = (index + 0.5f) * slotWidth
                            val golHeight = (p.goles.toFloat() / maxVal) * canvasHeight
                            val asisHeight = (p.asistencias.toFloat() / maxVal) * canvasHeight

                            // Barra Goles
                            if (golHeight > 0) {
                                drawRoundRect(
                                    color = LimeVolt,
                                    topLeft = Offset(centerX - barWidth - 2.dp.toPx(), canvasHeight - golHeight),
                                    size = Size(barWidth, golHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                            } else {
                                drawCircle(
                                    color = LimeVolt.copy(alpha = 0.3f),
                                    radius = 2.dp.toPx(),
                                    center = Offset(centerX - barWidth / 2 - 2.dp.toPx(), canvasHeight - 2.dp.toPx())
                                )
                            }

                            // Barra Asistencias
                            if (asisHeight > 0) {
                                drawRoundRect(
                                    color = AsistenciaColor,
                                    topLeft = Offset(centerX + 2.dp.toPx(), canvasHeight - asisHeight),
                                    size = Size(barWidth, asisHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                            } else {
                                drawCircle(
                                    color = AsistenciaColor.copy(alpha = 0.3f),
                                    radius = 2.dp.toPx(),
                                    center = Offset(centerX + barWidth / 2 + 2.dp.toPx(), canvasHeight - 2.dp.toPx())
                                )
                            }
                        }
                    }

                    // Etiquetas de fecha debajo de las barras
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                        partidosRecientes.forEach { p ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dateFormat.format(Date(p.fecha)),
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
