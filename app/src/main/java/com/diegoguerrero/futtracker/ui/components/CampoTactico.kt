package com.diegoguerrero.futtracker.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegoguerrero.futtracker.domain.model.Formacion
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.domain.model.obtenerCoordenadas

@Composable
fun CampoTactico(
    formacion: Formacion,
    alineacion: List<Pair<Posicion, Jugador>>?,
    modifier: Modifier = Modifier
) {
    val verdeCampo = Color(0xFF2E7D32)
    val lineasCampo = Color.White.copy(alpha = 0.7f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(verdeCampo)
            .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        val anchoTotal = maxWidth
        val altoTotal = maxHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeWidth = 3f

            drawRect(color = lineasCampo, style = Stroke(width = strokeWidth))
            drawLine(
                color = lineasCampo,
                start = Offset(0f, h / 2),
                end = Offset(w, h / 2),
                strokeWidth = strokeWidth
            )
            drawCircle(
                color = lineasCampo,
                center = Offset(w / 2, h / 2),
                radius = w * 0.18f,
                style = Stroke(width = strokeWidth)
            )

            val areaAncho = w * 0.5f
            val areaAlto = h * 0.16f

            drawRect(
                color = lineasCampo,
                topLeft = Offset((w - areaAncho) / 2, 0f),
                size = Size(areaAncho, areaAlto),
                style = Stroke(width = strokeWidth)
            )
            drawRect(
                color = lineasCampo,
                topLeft = Offset((w - areaAncho) / 2, h - areaAlto),
                size = Size(areaAncho, areaAlto),
                style = Stroke(width = strokeWidth)
            )
        }

        val posicionesConCoordenadas = obtenerCoordenadas(formacion)

        posicionesConCoordenadas.forEachIndexed { index, (posicionRequerida, coord) ->
            val (relX, relY) = coord
            val posX = anchoTotal * relX
            val posY = altoTotal * relY

            val jugadorAsignado = alineacion?.getOrNull(index)?.second

            Box(
                modifier = Modifier
                    .offset(x = posX - 32.dp, y = posY - 32.dp)
                    .size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (jugadorAsignado != null) MaterialTheme.colorScheme.primary
                                else Color.Black.copy(alpha = 0.5f)
                            )
                            .border(
                                2.dp,
                                if (jugadorAsignado != null) Color.White else Color.LightGray,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = posicionRequerida.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (jugadorAsignado != null) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.7f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = jugadorAsignado.nombre,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}