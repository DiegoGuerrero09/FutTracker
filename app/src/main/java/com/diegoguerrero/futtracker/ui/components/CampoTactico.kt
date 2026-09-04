package com.diegoguerrero.futtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
import com.diegoguerrero.futtracker.domain.model.Formacion
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.domain.model.obtenerCoordenadas
import com.diegoguerrero.futtracker.ui.theme.LimeVolt
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun CampoTactico(
    formacion: Formacion,
    alineacion: List<Pair<Posicion, Jugador>>?,
    modifier: Modifier = Modifier,
    onJugadorIntercambiado: ((Int, Int) -> Unit)? = null
) {
    val verdeCampo = Color(0xFF1B4D3E)
    val lineasCampo = Color.White.copy(alpha = 0.35f)
    val density = LocalDensity.current

    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(430.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(verdeCampo)
            .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
    ) {
        val anchoTotal = maxWidth
        val altoTotal = maxHeight
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeWidth = 2.dp.toPx()

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
                radius = w * 0.16f,
                style = Stroke(width = strokeWidth)
            )

            val areaAncho = w * 0.5f
            val areaAlto = h * 0.18f

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

        val posicionesConCoordenadas = remember(formacion) { obtenerCoordenadas(formacion) }

        posicionesConCoordenadas.forEachIndexed { index, (posicionRequerida, coord) ->
            val (relX, relY) = coord
            val posX = anchoTotal * relX
            val posY = altoTotal * relY
            val posXPx = relX * widthPx
            val posYPx = relY * heightPx

            val jugadorAsignado = alineacion?.getOrNull(index)?.second
            val isDragging = draggingIndex == index

            val iconWidth = 88.dp
            val iconHeight = 62.dp

            val dragModifier = if (onJugadorIntercambiado != null) {
                Modifier.pointerInput(index, widthPx, heightPx) {
                    detectDragGestures(
                        onDragStart = {
                            draggingIndex = index
                            dragOffset = Offset.Zero
                        },
                        onDrag = { change, amount ->
                            change.consume()
                            dragOffset += amount
                        },
                        onDragEnd = {
                            val finalX = posXPx + dragOffset.x
                            val finalY = posYPx + dragOffset.y

                            var targetIdx: Int? = null
                            var minDistance = Float.MAX_VALUE

                            posicionesConCoordenadas.forEachIndexed { otherIdx, (_, otherCoord) ->
                                if (otherIdx != index) {
                                    val ox = otherCoord.first * widthPx
                                    val oy = otherCoord.second * heightPx
                                    val dist = sqrt((finalX - ox) * (finalX - ox) + (finalY - oy) * (finalY - oy))
                                    if (dist < minDistance) {
                                        minDistance = dist
                                        targetIdx = otherIdx
                                    }
                                }
                            }

                            val swapThreshold = with(density) { 56.dp.toPx() }
                            if (targetIdx != null && minDistance <= swapThreshold) {
                                onJugadorIntercambiado(index, targetIdx!!)
                            }

                            draggingIndex = null
                            dragOffset = Offset.Zero
                        },
                        onDragCancel = {
                            draggingIndex = null
                            dragOffset = Offset.Zero
                        }
                    )
                }
            } else Modifier

            Box(
                modifier = Modifier
                    .offset(x = posX - (iconWidth / 2), y = posY - 25.dp)
                    .then(
                        if (isDragging) {
                            Modifier
                                .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
                                .zIndex(5f)
                                .scale(1.15f)
                        } else Modifier.zIndex(1f)
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
                            fotoUri = jugadorAsignado?.fotoUri,
                            nombre = jugadorAsignado?.nombre ?: posicionRequerida.name,
                            tamano = avatarTamano,
                            bordeColor = if (jugadorAsignado != null) LimeVolt else Color.White.copy(alpha = 0.4f),
                            bordeAncho = 1.5.dp
                        )

                        // Placa pequeña cuadrada en la esquina inferior
                        Surface(
                            color = if (jugadorAsignado != null) LimeVolt else Color.Black.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier
                                .offset(x = 2.dp, y = 2.dp)
                                .size(width = 19.dp, height = 15.5.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = posicionRequerida.name,
                                    color = if (jugadorAsignado != null) Color.Black else Color.White,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.offset(y = (-1.5).dp)
                                )
                            }
                        }
                    }

                    if (jugadorAsignado != null) {
                        Text(
                            text = jugadorAsignado.nombre,
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