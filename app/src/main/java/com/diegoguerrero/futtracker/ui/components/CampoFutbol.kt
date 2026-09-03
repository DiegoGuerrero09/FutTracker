package com.diegoguerrero.futtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.ui.theme.LimeVolt

private val PitchGreen = Color(0xFF1B4D3E)

@Composable
fun CampoFutbol(
    alineacion: Map<Pair<Posicion, Pair<Float, Float>>, Jugador?>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(PitchGreen)
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        // Líneas tácticas del campo
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineColor = Color.White.copy(alpha = 0.35f)
            val strokeWidth = 2.dp.toPx()

            // Líneas límite
            drawRect(
                color = lineColor,
                style = Stroke(width = strokeWidth)
            )

            // Línea central
            drawLine(
                color = lineColor,
                start = Offset(0f, height / 2),
                end = Offset(width, height / 2),
                strokeWidth = strokeWidth
            )

            // Círculo central
            drawCircle(
                color = lineColor,
                radius = width * 0.15f,
                center = Offset(width / 2, height / 2),
                style = Stroke(width = strokeWidth)
            )

            // Área de portería (Abajo)
            drawRect(
                color = lineColor,
                topLeft = Offset(width * 0.25f, height * 0.82f),
                size = Size(width * 0.5f, height * 0.18f),
                style = Stroke(width = strokeWidth)
            )

            // Área de portería (Arriba)
            drawRect(
                color = lineColor,
                topLeft = Offset(width * 0.25f, 0f),
                size = Size(width * 0.5f, height * 0.18f),
                style = Stroke(width = strokeWidth)
            )
        }

        // Renderizado de jugadores / posiciones
        alineacion.forEach { (posConCoords, jugador) ->
            val posicionEnum = posConCoords.first
            val coords = posConCoords.second
            val normX = coords.first
            val normY = coords.second

            val posX = normX * width
            val posY = normY * height

            val iconSize = 44.dp

            val xDp = with(density) { posX.toDp() } - 22.dp
            val yDp = with(density) { posY.toDp() } - 22.dp

            Box(
                modifier = Modifier
                    .offset(x = xDp, y = yDp)
                    .size(iconSize),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Ficha de posición
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (jugador != null) LimeVolt else Color.Gray.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = posicionEnum.name,
                            color = if (jugador != null) Color.Black else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Nombre del jugador asignado
                    jugador?.let {
                        Text(
                            text = it.nombre,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}