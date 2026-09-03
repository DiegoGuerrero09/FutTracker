package com.diegoguerrero.futtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegoguerrero.futtracker.domain.model.*
import com.diegoguerrero.futtracker.ui.theme.*

@Composable
fun CampoFutbol(
    alineacion: Map<Pair<TipoPosicion, CoordenadaCampo>, Jugador?>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PitchGreen)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
    ) {
        val width = maxWidth
        val height = maxHeight

        // Dibujar marcas del campo táctico
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeWidth = 3f

            // Líneas exteriores y centro
            drawRect(color = PitchLines, style = Stroke(strokeWidth))
            drawLine(PitchLines, Offset(0f, h / 2), Offset(w, h / 2), strokeWidth)
            drawCircle(PitchLines, radius = w * 0.18f, center = Offset(w / 2, h / 2), style = Stroke(strokeWidth))

            // Áreas de meta
            drawRect(PitchLines, Offset(w * 0.22f, 0f), androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.16f), style = Stroke(strokeWidth))
            drawRect(PitchLines, Offset(w * 0.22f, h * 0.84f), androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.16f), style = Stroke(strokeWidth))
        }

        // Posicionar jugadores
        alineacion.forEach { (slot, jugador) ->
            val (posTipo, coord) = slot
            val posX = width * coord.x - 28.dp
            val posY = height * coord.y - 28.dp

            Box(
                modifier = Modifier
                    .offset(x = posX, y = posY)
                    .size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DarkCard)
                            .border(2.dp, if (jugador != null) LimeVolt else TextSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = jugador?.nombre?.take(2)?.uppercase() ?: posTipo.shortLabel,
                            color = if (jugador != null) TextPrimary else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = jugador?.nombre ?: posTipo.shortLabel,
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}