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
import com.diegoguerrero.futtracker.domain.model.Posicion
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
    victorias: Int? = null,
    empates: Int? = null,
    derrotas: Int? = null,
    jugadorId: String? = null,
    modifier: Modifier = Modifier
) {
    val vic = victorias ?: partidos.count { p ->
        if (jugadorId == null) p.esVictoria
        else if (p.jugadoresMiEquipo.contains(jugadorId)) p.esVictoria
        else if (p.jugadoresEquipoRival.contains(jugadorId)) p.esDerrota
        else p.esVictoria
    }
    val emp = empates ?: partidos.count { it.esEmpate }
    val der = derrotas ?: partidos.count { p ->
        if (jugadorId == null) p.esDerrota
        else if (p.jugadoresMiEquipo.contains(jugadorId)) p.esDerrota
        else if (p.jugadoresEquipoRival.contains(jugadorId)) p.esVictoria
        else p.esDerrota
    }
    val total = if (victorias != null && empates != null && derrotas != null) {
        victorias + empates + derrotas
    } else {
        partidos.size
    }

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
                    if (vic > 0) {
                        Box(
                            modifier = Modifier
                                .weight(vic.toFloat())
                                .fillMaxHeight()
                                .background(VictoriaColor)
                        )
                    }
                    if (emp > 0) {
                        Box(
                            modifier = Modifier
                                .weight(emp.toFloat())
                                .fillMaxHeight()
                                .background(EmpateColor)
                        )
                    }
                    if (der > 0) {
                        Box(
                            modifier = Modifier
                                .weight(der.toFloat())
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
                        count = vic,
                        color = VictoriaColor,
                        porcentaje = if (total > 0) (vic * 100 / total) else 0
                    )
                    IndicadorResultado(
                        label = "Empates",
                        count = emp,
                        color = EmpateColor,
                        porcentaje = if (total > 0) (emp * 100 / total) else 0
                    )
                    IndicadorResultado(
                        label = "Derrotas",
                        count = der,
                        color = DerrotaColor,
                        porcentaje = if (total > 0) (der * 100 / total) else 0
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
    jugadorId: String? = null,
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
                    partidosRecientes.maxOfOrNull { p ->
                        val g = if (jugadorId == null) p.goles else (p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorId }?.goles ?: 0)
                        val a = if (jugadorId == null) p.asistencias else (p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorId }?.asistencias ?: 0)
                        max(g, a)
                    } ?: 1
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
                                val g = if (jugadorId == null) p.goles else (p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorId }?.goles ?: 0)
                                val a = if (jugadorId == null) p.asistencias else (p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorId }?.asistencias ?: 0)
                                val centerX = (index + 0.5f) * slotWidth
                                val golHeight = (g.toFloat() / maxVal) * canvasHeight
                                val asisHeight = (a.toFloat() / maxVal) * canvasHeight

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

@Composable
fun GraficoGolesEncajados(
    partidos: List<Partido>,
    jugadorId: String? = null,
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
                    text = "🥅 Goles encajados",
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
                                .background(Color(0xFFEF5350))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Goles enc.", color = TextSecondary, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LimeVolt)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paradas", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (partidosRecientes.isEmpty()) {
                Text(
                    text = "No hay partidos como portero aún",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                val maxVal = max(
                    1,
                    partidosRecientes.maxOfOrNull { p ->
                        val esYo = jugadorId == null || jugadorId == "usuario_propio_id"
                        val det = if (esYo) p.jugadoresDetalle.firstOrNull { it.jugadorId == "usuario_propio_id" } ?: p.jugadoresDetalle.firstOrNull { it.esMiEquipo }
                                  else p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorId }
                        val enMiEquipo = det?.esMiEquipo ?: if (esYo) p.jugadoPorMi else p.jugadoresMiEquipo.contains(jugadorId)
                        val enc = if (enMiEquipo) p.golesEnContra else p.golesAFavor
                        val jugoPortero = det?.let { it.posicionPrincipal == Posicion.POR || it.posicionesSecundarias.contains(Posicion.POR) }
                            ?: if (esYo && p.jugadoPorMi) (p.posicionJugada == Posicion.POR || p.posicionesSecundarias.contains(Posicion.POR) || p.posicionesJugadas.contains(Posicion.POR)) else false
                        val paradas = if (jugoPortero) (det?.paradas ?: if (esYo && p.jugadoPorMi) p.paradas else 0) else 0
                        max(enc, paradas)
                    } ?: 1
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
                                val esYo = jugadorId == null || jugadorId == "usuario_propio_id"
                                val det = if (esYo) p.jugadoresDetalle.firstOrNull { it.jugadorId == "usuario_propio_id" } ?: p.jugadoresDetalle.firstOrNull { it.esMiEquipo }
                                          else p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorId }
                                val enMiEquipo = det?.esMiEquipo ?: if (esYo) p.jugadoPorMi else p.jugadoresMiEquipo.contains(jugadorId)
                                val enc = if (enMiEquipo) p.golesEnContra else p.golesAFavor
                                val jugoPortero = det?.let { it.posicionPrincipal == Posicion.POR || it.posicionesSecundarias.contains(Posicion.POR) }
                                    ?: if (esYo && p.jugadoPorMi) (p.posicionJugada == Posicion.POR || p.posicionesSecundarias.contains(Posicion.POR) || p.posicionesJugadas.contains(Posicion.POR)) else false
                                val paradas = if (jugoPortero) (det?.paradas ?: if (esYo && p.jugadoPorMi) p.paradas else 0) else 0

                                val centerX = (index + 0.5f) * slotWidth
                                val encHeight = (enc.toFloat() / maxVal) * canvasHeight
                                val paradasHeight = (paradas.toFloat() / maxVal) * canvasHeight

                                if (encHeight > 0) {
                                    drawRoundRect(
                                        color = Color(0xFFEF5350),
                                        topLeft = Offset(centerX - barWidth - 2.dp.toPx(), canvasHeight - encHeight),
                                        size = Size(barWidth, encHeight),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                } else {
                                    drawCircle(
                                        color = Color(0xFFEF5350).copy(alpha = 0.3f),
                                        radius = 2.dp.toPx(),
                                        center = Offset(centerX - barWidth / 2 - 2.dp.toPx(), canvasHeight - 2.dp.toPx())
                                    )
                                }

                                if (paradasHeight > 0) {
                                    drawRoundRect(
                                        color = LimeVolt,
                                        topLeft = Offset(centerX + 2.dp.toPx(), canvasHeight - paradasHeight),
                                        size = Size(barWidth, paradasHeight),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                } else {
                                    drawCircle(
                                        color = LimeVolt.copy(alpha = 0.3f),
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
    jugadorId: String? = null,
    modifier: Modifier = Modifier
) {
    val diestra = if (jugadorId == null) partidos.sumOf { it.golesDiestra } else partidos.sumOf { p -> p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorId }?.golesDiestra ?: 0 }
    val zurda = if (jugadorId == null) partidos.sumOf { it.golesZurda } else partidos.sumOf { p -> p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorId }?.golesZurda ?: 0 }
    val cabeza = if (jugadorId == null) partidos.sumOf { it.golesCabeza } else partidos.sumOf { p -> p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorId }?.golesCabeza ?: 0 }
    val tacon = if (jugadorId == null) partidos.sumOf { it.golesTacon } else partidos.sumOf { p -> p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorId }?.golesTacon ?: 0 }
    val chilena = if (jugadorId == null) partidos.sumOf { it.golesChilena } else partidos.sumOf { p -> p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorId }?.golesChilena ?: 0 }
    val otro = if (jugadorId == null) partidos.sumOf { it.golesOtro } else partidos.sumOf { p -> p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorId }?.golesOtro ?: 0 }
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
                    text = "📓 Resumen de goles",
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
    jugadorId: String? = null,
    modifier: Modifier = Modifier
) {
    val partidosRecientes = partidos.sortedBy { it.fecha }.takeLast(7)
    val totalPalos = if (jugadorId == null) partidos.sumOf { it.tirosAlPalo } else partidos.sumOf { p -> p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorId }?.tirosAlPalo ?: 0 }

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
                    text = "🎯 Tiros al palo",
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
                val maxVal = max(
                    1,
                    partidosRecientes.maxOfOrNull { p ->
                        if (jugadorId == null) p.tirosAlPalo else (p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorId }?.tirosAlPalo ?: 0)
                    } ?: 1
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
                                val pPalos = if (jugadorId == null) p.tirosAlPalo else (p.jugadoresDetalle.firstOrNull { it.jugadorId == jugadorId }?.tirosAlPalo ?: 0)
                                val centerX = (index + 0.5f) * slotWidth
                                val paloHeight = (pPalos.toFloat() / maxVal) * canvasHeight

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