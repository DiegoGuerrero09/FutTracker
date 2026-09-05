package com.diegoguerrero.futtracker.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.style.TextAlign
import com.diegoguerrero.futtracker.ui.screens.jugadores.obtenerIniciales
import com.diegoguerrero.futtracker.ui.theme.LimeVolt
import com.diegoguerrero.futtracker.ui.theme.TextSecondary
import java.io.File

@Composable
fun JugadorAvatar(
    fotoUri: String?,
    nombre: String,
    modifier: Modifier = Modifier,
    tamano: Dp = 40.dp,
    fontSize: TextUnit = 14.sp,
    bordeColor: Color = Color.Transparent,
    bordeAncho: Dp = 0.dp,
    permitirZoom: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    var mostrarZoom by remember { mutableStateOf(false) }

    val bitmap = remember(fotoUri) {
        fotoUri?.let { path ->
            runCatching {
                val file = File(path)
                if (file.exists() && file.length() > 0) {
                    BitmapFactory.decodeFile(path)?.asImageBitmap()
                } else null
            }.getOrNull()
        }
    }

    val puedeClick = onClick != null || (permitirZoom && bitmap != null)

    val modClickable = if (puedeClick) {
        modifier.clickable {
            if (onClick != null) {
                onClick()
            } else if (permitirZoom && bitmap != null) {
                mostrarZoom = true
            }
        }
    } else {
        modifier
    }

    val modBorde = if (bordeAncho > 0.dp) {
        modClickable
            .size(tamano)
            .border(bordeAncho, bordeColor, CircleShape)
            .clip(CircleShape)
    } else {
        modClickable
            .size(tamano)
            .clip(CircleShape)
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Foto de $nombre",
            modifier = modBorde,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modBorde
                .background(LimeVolt),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = obtenerIniciales(nombre),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            )
        }
    }

    if (mostrarZoom && fotoUri != null) {
        DialogoVisorFotoConZoom(
            fotoUri = fotoUri,
            nombre = nombre,
            onDismiss = { mostrarZoom = false }
        )
    }
}

@Composable
fun BadgePosicion(label: String, esPrimaria: Boolean) {
    val bgColor = if (esPrimaria) {
        LimeVolt.copy(alpha = 0.25f)
    } else {
        Color(0xFF334155)
    }
    val textColor = if (esPrimaria) {
        LimeVolt
    } else {
        Color(0xFFCBD5E1)
    }

    androidx.compose.material3.Surface(
        color = bgColor,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 34.dp)
                .height(20.dp)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = if (esPrimaria) FontWeight.Bold else FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
    }
}

@Composable
fun ImagenLocal(
    fotoUri: String?,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmap = remember(fotoUri) {
        fotoUri?.let { path ->
            runCatching {
                val file = File(path)
                if (file.exists() && file.length() > 0) {
                    BitmapFactory.decodeFile(path)?.asImageBitmap()
                } else null
            }.getOrNull()
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}
