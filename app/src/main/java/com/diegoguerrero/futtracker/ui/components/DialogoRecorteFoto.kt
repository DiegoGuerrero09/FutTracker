package com.diegoguerrero.futtracker.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.diegoguerrero.futtracker.ui.theme.DarkBackground
import com.diegoguerrero.futtracker.ui.theme.DarkCard
import com.diegoguerrero.futtracker.ui.theme.LimeVolt
import com.diegoguerrero.futtracker.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

@Composable
fun DialogoRecorteFoto(
    uriOriginal: Uri,
    onFotoRecortada: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var bitmapOriginal by remember { mutableStateOf<Bitmap?>(null) }
    var rotacion by remember { mutableFloatStateOf(0f) }
    var escala by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var procesando by remember { mutableStateOf(false) }

    LaunchedEffect(uriOriginal) {
        withContext(Dispatchers.IO) {
            try {
                val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uriOriginal)
                    android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uriOriginal)
                }
                bitmapOriginal = bmp
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = DarkCard
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ajustar foto",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Arrastra y haz zoom para encuadrar tu foto",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                val visorSize = 250.dp
                Box(
                    modifier = Modifier
                        .size(visorSize)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                escala = (escala * zoom).coerceIn(0.6f, 4.0f)
                                offset += pan
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val bmp = bitmapOriginal
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Previsualización",
                            modifier = Modifier
                                .fillMaxSize()
                                .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                                .scale(escala)
                                .rotate(rotacion)
                        )
                    } else {
                        CircularProgressIndicator(color = LimeVolt, modifier = Modifier.size(36.dp))
                    }

                    // Guía circular de recorte
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = LimeVolt.copy(alpha = 0.7f),
                            radius = size.minDimension / 2f - 4.dp.toPx(),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Controles de ajuste
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { escala = (escala - 0.2f).coerceAtLeast(0.6f) }
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Alejar", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = { rotacion = (rotacion + 90f) % 360f }
                    ) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotar", tint = LimeVolt)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = { escala = (escala + 0.2f).coerceAtMost(4.0f) }
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Acercar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !procesando
                    ) {
                        Text("Cancelar", color = TextSecondary)
                    }
                    Button(
                        onClick = {
                            val bmp = bitmapOriginal ?: return@Button
                            procesando = true
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val salidaSize = 512
                                    val cropped = Bitmap.createBitmap(salidaSize, salidaSize, Bitmap.Config.ARGB_8888)
                                    val canvas = android.graphics.Canvas(cropped)

                                    val matrix = Matrix()
                                    // Centrado del bitmap original
                                    val srcW = bmp.width.toFloat()
                                    val srcH = bmp.height.toFloat()
                                    matrix.postTranslate(-srcW / 2f, -srcH / 2f)
                                    matrix.postRotate(rotacion)

                                    // Ratio entre visor en px y tamaño salida
                                    val factorVisorASalida = salidaSize / 250f
                                    val scaleTotal = escala * (salidaSize.toFloat() / maxOf(srcW, srcH))
                                    matrix.postScale(scaleTotal, scaleTotal)
                                    matrix.postTranslate(salidaSize / 2f + offset.x * factorVisorASalida, salidaSize / 2f + offset.y * factorVisorASalida)

                                    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
                                    canvas.drawBitmap(bmp, matrix, paint)

                                    val archivoDestino = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
                                    FileOutputStream(archivoDestino).use { out ->
                                        cropped.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                    }

                                    withContext(Dispatchers.Main) {
                                        onFotoRecortada(archivoDestino.absolutePath)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        onDismiss()
                                    }
                                } finally {
                                    withContext(Dispatchers.Main) {
                                        procesando = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !procesando && bitmapOriginal != null,
                        colors = ButtonDefaults.buttonColors(containerColor = LimeVolt, contentColor = Color.Black)
                    ) {
                        if (procesando) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                        } else {
                            Text("Guardar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
