package com.diegoguerrero.futtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val CoronaIcon: ImageVector = ImageVector.Builder(
    name = "Corona",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).path(
    fill = SolidColor(Color.White)
) {
    moveTo(5f, 16f)
    lineTo(3f, 5f)
    lineTo(8.5f, 10f)
    lineTo(12f, 4f)
    lineTo(15.5f, 10f)
    lineTo(21f, 5f)
    lineTo(19f, 16f)
    close()
    moveTo(19f, 19f)
    curveTo(19f, 19.55f, 18.55f, 20f, 18f, 20f)
    lineTo(6f, 20f)
    curveTo(5.45f, 20f, 5f, 19.55f, 5f, 19f)
    curveTo(5f, 18.45f, 5.45f, 18f, 6f, 18f)
    lineTo(18f, 18f)
    curveTo(18.55f, 18f, 19f, 18.45f, 19f, 19f)
    close()
}.build()

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Datos : Screen("datos", "Datos", Icons.Default.Storage)
    object Alineacion : Screen("alineacion", "Pizarra", Icons.Default.SportsSoccer)
    object Sorteos : Screen("sorteos", "Sorteos", Icons.Default.Casino)
    object Estadisticas : Screen("estadisticas", "Stats", Icons.Default.BarChart)
    object Rankings : Screen("rankings", "Rankings", CoronaIcon)
    object Versus : Screen("versus", "Versus", Icons.Default.SportsMma)
    object Perfil : Screen("perfil", "Perfil", Icons.Default.Person)

    // Alias heredados para compatibilidad
    object Jugadores : Screen("jugadores", "Jugadores", Icons.Default.People)
    object Partidos : Screen("partidos", "Partidos", Icons.Default.Event)
    object Enfrentamientos : Screen("enfrentamientos", "Versus", Icons.Default.SportsMma)
}