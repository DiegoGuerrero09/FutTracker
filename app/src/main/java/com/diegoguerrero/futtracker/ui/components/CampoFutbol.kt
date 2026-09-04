package com.diegoguerrero.futtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.ui.theme.LimeVolt
import java.io.File
import kotlin.math.roundToInt
import kotlin.math.sqrt

private val PitchGreen = Color(0xFF1B4D3E)

@Composable
fun CampoFutbol(
    alineacion: Map<Pair<Posicion, Pair<Float, Float>>, Jugador?>,
    modifier: Modifier = Modifier,
    onJugadorIntercambiado: ((Pair<Posicion, Pair<Float, Float>>, Pair<Posicion, Pair<Float, Float>>) -> Unit)? = null,
    onJugadorMovido: ((Pair<Posicion, Pair<Float, Float>>, Float, Float) -> Unit)? = null
) {
    val density = LocalDensity.current

    var draggingKey by remember { mutableStateOf<Pair<Posicion, Pair<Float, Float>>?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(430.dp)
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

        val iconWidth = 88.dp
        val iconHeight = 62.dp

        // Renderizado de jugadores / posiciones
        alineacion.forEach { (posConCoords, jugador) ->
            val posicionEnum = posConCoords.first
            val coords = posConCoords.second
            val normX = coords.first
            val normY = coords.second

            val posX = normX * width
            val posY = normY * height

            val isDragging = draggingKey == posConCoords

            val xDp = with(density) { posX.toDp() } - (iconWidth / 2)
            val yDp = with(density) { posY.toDp() } - 20.dp

            val dragModifier = if (onJugadorIntercambiado != null || onJugadorMovido != null) {
                Modifier.pointerInput(posConCoords, width, height) {
                    detectDragGestures(
                        onDragStart = {
                            draggingKey = posConCoords
                            dragOffset = Offset.Zero
                        },
                        onDrag = { change, amount ->
                            change.consume()
                            dragOffset += amount
                        },
                        onDragEnd = {
                            val finalX = posX + dragOffset.x
                            val finalY = posY + dragOffset.y

                            // Comprobar si soltó sobre otra ficha para intercambiar
                            var targetKey: Pair<Posicion, Pair<Float, Float>>? = null
                            var minDistance = Float.MAX_VALUE

                            alineacion.keys.forEach { otherKey ->
                                if (otherKey != posConCoords) {
                                    val ox = otherKey.second.first * width
                                    val oy = otherKey.second.second * height
                                    val dist = sqrt((finalX - ox) * (finalX - ox) + (finalY - oy) * (finalY - oy))
                                    if (dist < minDistance) {
                                        minDistance = dist
                                        targetKey = otherKey
                                    }
                                }
                            }

                            val swapThreshold = with(density) { 44.dp.toPx() }
                            if (targetKey != null && minDistance <= swapThreshold && onJugadorIntercambiado != null) {
                                onJugadorIntercambiado(posConCoords, targetKey!!)
                            } else if (onJugadorMovido != null) {
                                val newNormX = (finalX / width).coerceIn(0.08f, 0.92f)
                                val newNormY = (finalY / height).coerceIn(0.08f, 0.92f)
                                onJugadorMovido(posConCoords, newNormX, newNormY)
                            }

                            draggingKey = null
                            dragOffset = Offset.Zero
                        },
                        onDragCancel = {
                            draggingKey = null
                            dragOffset = Offset.Zero
                        }
                    )
                }
            } else Modifier

            Box(
                modifier = Modifier
                    .offset(x = xDp, y = yDp)
                    .then(
                        if (isDragging) {
                            Modifier
                                .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
                                .zIndex(5f)
                                .scale(1.15f)
                        } else {
                            Modifier.zIndex(1f)
                        }
                    )
                    .size(width = iconWidth, height = iconHeight)
                    .then(dragModifier),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    val avatarTamano = 38.dp

                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.size(avatarTamano + 4.dp)
                    ) {
                        JugadorAvatar(
                            fotoUri = jugador?.fotoUri,
                            nombre = jugador?.nombre ?: posicionEnum.name,
                            tamano = avatarTamano,
                            bordeColor = if (jugador != null) LimeVolt else Color.White.copy(alpha = 0.4f),
                            bordeAncho = 1.5.dp
                        )

                        // Placa pequeña cuadrada en la esquina inferior para no tapar los rostros
                        Surface(
                            color = if (jugador != null) LimeVolt else Color.Black.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier
                                .offset(x = 2.dp, y = 2.dp)
                                .size(width = 17.dp, height = 13.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = posicionEnum.name,
                                    color = if (jugador != null) Color.Black else Color.White,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Nombre del jugador asignado con longitud incrementada
                    if (jugador != null) {
                        Text(
                            text = jugador.nombre,
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            lineHeight = 11.sp,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}