package com.diegoguerrero.futtracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = LimeVolt,
    onPrimary = DarkBackground,
    primaryContainer = LimeVolt,
    onPrimaryContainer = DarkBackground,
    background = DarkBackground,
    surface = DarkCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = DarkCardBorder
)

private val DarkTypography = androidx.compose.material3.Typography().run {
    copy(
        titleLarge = titleLarge.copy(color = LimeVolt),
        titleMedium = titleMedium.copy(color = LimeVolt),
        titleSmall = titleSmall.copy(color = LimeVolt),
        headlineLarge = headlineLarge.copy(color = LimeVolt),
        headlineMedium = headlineMedium.copy(color = LimeVolt),
        headlineSmall = headlineSmall.copy(color = LimeVolt)
    )
}

@Composable
fun FutTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = DarkTypography,
        content = content
    )
}