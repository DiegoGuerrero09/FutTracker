package com.diegoguerrero.futtracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
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
import com.diegoguerrero.futtracker.ui.theme.DarkCardBorder
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
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚖️ Balance de resultados",
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
    val partidosRecientes = partidos.sortedBy { it.fecha }.takeLast(7)

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
                    text = "⚽ Goles y asistencias",
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .width(20.dp)
                            .height(116.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("$maxVal", color = TextSecondary, fontSize = 9.sp)
                        Text(if (maxVal > 1) "${(maxVal + 1) / 2}" else "", color = TextSecondary, fontSize = 9.sp)
                        Text("0", color = TextSecondary, fontSize = 9.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height - 24.dp.toPx()
                            val n = partidosRecientes.size
                            val slotWidth = canvasWidth / n
                            val barWidth = (slotWidth * 0.28f).coerceAtMost(16.dp.toPx())

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
}

private val ColorDiestra = LimeVolt
private val ColorZurda = Color(0xFF38BDF8)
private val ColorCabeza = Color(0xFFFB923C)
private val ColorTacon = Color(0xFFA855F7)
private val ColorChilena = Color(0xFFEC4899)
private val ColorOtro = Color(0xFF94A3B8)

@Composable
fun GraficoResumenGoles(
    partidos: List<Partido>,
    modifier: Modifier = Modifier
) {
    val diestra = partidos.sumOf { it.golesDiestra }
    val zurda = partidos.sumOf { it.golesZurda }
    val cabeza = partidos.sumOf { it.golesCabeza }
    val tacon = partidos.sumOf { it.golesTacon }
    val chilena = partidos.sumOf { it.golesChilena }
    val otro = partidos.sumOf { it.golesOtro }
    val total = diestra + zurda + cabeza + tacon + chilena + otro

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
                    text = "⚽ Resumen de goles",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (total > 0) {
                    Text(
                        text = "$total goles",
                        color = LimeVolt,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (total == 0) {
                Text(
                    text = "No hay goles registrados en este período",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    if (diestra > 0) Box(modifier = Modifier.weight(diestra.toFloat()).fillMaxHeight().background(ColorDiestra))
                    if (zurda > 0) Box(modifier = Modifier.weight(zurda.toFloat()).fillMaxHeight().background(ColorZurda))
                    if (cabeza > 0) Box(modifier = Modifier.weight(cabeza.toFloat()).fillMaxHeight().background(ColorCabeza))
                    if (tacon > 0) Box(modifier = Modifier.weight(tacon.toFloat()).fillMaxHeight().background(ColorTacon))
                    if (chilena > 0) Box(modifier = Modifier.weight(chilena.toFloat()).fillMaxHeight().background(ColorChilena))
                    if (otro > 0) Box(modifier = Modifier.weight(otro.toFloat()).fillMaxHeight().background(ColorOtro))
                }

                Spacer(modifier = Modifier.height(14.dp))

                val items = listOf(
                    Triple("Diestra", diestra, ColorDiestra),
                    Triple("Zurda", zurda, ColorZurda),
                    Triple("Cabeza", cabeza, ColorCabeza),
                    Triple("Tacón", tacon, ColorTacon),
                    Triple("Chilena", chilena, ColorChilena),
                    Triple("Otro", otro, ColorOtro)
                ).filter { it.second > 0 }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.chunked(2).forEach { fila ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            fila.forEach { (nombre, cantidad, color) ->
                                val pct = (cantidad.toFloat() / total * 100).toInt()
                                Surface(
                                    color = DarkCard.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(nombre, fontSize = 11.sp, color = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("$cantidad ($pct%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                                    }
                                }
                            }
                            // Rellenar celda vacía si la fila tiene 1 elemento
                            if (fila.size < 2) {
                                repeat(2 - fila.size) {
                                    Spacer(modifier = Modifier.weight(1f))
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
fun GraficoTirosAlPalo(
    partidos: List<Partido>,
    modifier: Modifier = Modifier
) {
    val partidosRecientes = partidos.sortedBy { it.fecha }.takeLast(7)
    val totalPalos = partidos.sumOf { it.tirosAlPalo }

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
                    text = "🥅 Tiros al palo",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                if (totalPalos > 0) {
                    Text(
                        text = "$totalPalos palos",
                        color = Color(0xFFF59E0B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                val maxVal = max(1, partidosRecientes.maxOfOrNull { it.tirosAlPalo } ?: 1)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .width(20.dp)
                            .height(116.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("$maxVal", color = TextSecondary, fontSize = 9.sp)
                        Text(if (maxVal > 1) "${(maxVal + 1) / 2}" else "", color = TextSecondary, fontSize = 9.sp)
                        Text("0", color = TextSecondary, fontSize = 9.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height - 24.dp.toPx()
                            val n = partidosRecientes.size
                            val slotWidth = canvasWidth / n
                            val barWidth = (slotWidth * 0.35f).coerceAtMost(20.dp.toPx())

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

                            val paloColor = Color(0xFFF59E0B)
                            partidosRecientes.forEachIndexed { index, p ->
                                val centerX = (index + 0.5f) * slotWidth
                                val paloHeight = (p.tirosAlPalo.toFloat() / maxVal) * canvasHeight

                                if (paloHeight > 0) {
                                    drawRoundRect(
                                        color = paloColor,
                                        topLeft = Offset(centerX - barWidth / 2, canvasHeight - paloHeight),
                                        size = Size(barWidth, paloHeight),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                } else {
                                    drawCircle(
                                        color = paloColor.copy(alpha = 0.3f),
                                        radius = 2.dp.toPx(),
                                        center = Offset(centerX, canvasHeight - 2.dp.toPx())
                                    )
                                }
                            }
                        }

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
}