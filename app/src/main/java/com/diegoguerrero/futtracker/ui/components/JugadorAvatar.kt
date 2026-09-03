package com.diegoguerrero.futtracker.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.foundation.layout.padding
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
    bordeAncho: Dp = 0.dp
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

    val modBorde = if (bordeAncho > 0.dp) {
        modifier
            .size(tamano)
            .border(bordeAncho, bordeColor, CircleShape)
            .clip(CircleShape)
    } else {
        modifier
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
}

@Composable
fun BadgePosicion(label: String, esPrimaria: Boolean) {
    val bgColor = if (esPrimaria) {
        LimeVolt.copy(alpha = 0.25f)
    } else {
        Color.White.copy(alpha = 0.08f)
    }
    val textColor = if (esPrimaria) {
        LimeVolt
    } else {
        TextSecondary
    }

    androidx.compose.material3.Surface(
        color = bgColor,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = if (esPrimaria) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            lineHeight = 14.sp
        )
    }
}
