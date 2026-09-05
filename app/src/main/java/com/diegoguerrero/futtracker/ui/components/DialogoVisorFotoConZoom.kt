package com.diegoguerrero.futtracker.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.diegoguerrero.futtracker.ui.theme.DarkCard
import com.diegoguerrero.futtracker.ui.theme.LimeVolt
import java.io.File

@Composable
fun DialogoVisorFotoConZoom(
    fotoUri: String,
    nombre: String,
    onDismiss: () -> Unit
) {
    val bitmap = remember(fotoUri) {
        runCatching {
            val file = File(fotoUri)
            if (file.exists() && file.length() > 0) {
                BitmapFactory.decodeFile(fotoUri)?.asImageBitmap()
            } else null
        }.getOrNull()
    }

    if (bitmap == null) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            // Imagen central con gestos de zoom y pan
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1.2f) {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    scale = 2.5f
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 5f)
                            scale = newScale

                            if (newScale > 1f) {
                                val maxOffsetX = (newScale - 1f) * 500f
                                val maxOffsetY = (newScale - 1f) * 600f
                                offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Foto de $nombre",
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        },
                    contentScale = ContentScale.Crop
                )
            }

            // Barra superior con nombre y controles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = nombre,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (scale > 1f) "Zoom: ${(scale * 100).toInt()}%" else "Doble toque o pellizca para zoom",
                        color = LimeVolt,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (scale > 1.05f || offsetX != 0f || offsetY != 0f) {
                        IconButton(
                            onClick = {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(DarkCard, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reiniciar zoom",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(DarkCard, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Indicador inferior
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                color = DarkCard.copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Toca dos veces para ampliar • Desliza para mover",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
