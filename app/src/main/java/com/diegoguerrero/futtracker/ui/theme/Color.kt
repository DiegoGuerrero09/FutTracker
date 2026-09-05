package com.diegoguerrero.futtracker.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta Negra & Lima (Volt Dark)
val DarkBackground = Color(0xFF090A0F)
val DarkCard = Color(0xFF13151E)
val DarkCardBorder = Color(0xFF222634)
val PitchGreen = Color(0xFF101D16)
val PitchLines = Color(0xFF1E3A2B)

val LimeVolt = Color(0xD9BCE324)
val LimeVoltSolid = Color(0xFFBCE324)
val LimeVoltHover = Color(0xFF9FC41C)
val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)

val PosPortero = Color(0xFFEAB308)
val PosDefensa = Color(0xFF3B82F6)
val PosMedio = Color(0xFF10B981)
val PosDelantero = Color(0xFFEF4444)

val GreenWin = Color(0xFF10B981)
val OrangeDraw = Color(0xFFF59E0B)
val RedLoss = Color(0xFFEF4444)
val BlueAssist = Color(0xFF29B6F6)
val BlueCompanero = Color(0xFF38BDF8)

fun obtenerColorPorcentaje(porcentaje: Float): Color {
    val p = (porcentaje / 100f).coerceIn(0f, 1f)
    return if (p < 0.5f) {
        val factor = p / 0.5f
        androidx.compose.ui.graphics.lerp(RedLoss, OrangeDraw, factor)
    } else {
        val factor = (p - 0.5f) / 0.5f
        androidx.compose.ui.graphics.lerp(OrangeDraw, GreenWin, factor)
    }
}