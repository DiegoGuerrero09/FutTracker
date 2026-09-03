package com.diegoguerrero.futtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SportsSoccer

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Alineacion : Screen("alineacion", "Alineaciones", Icons.Default.SportsSoccer)
    object Jugadores : Screen("jugadores", "Jugadores", Icons.Default.People)
    object Partidos : Screen("partidos", "Partidos", Icons.Default.Event)
    object Estadisticas : Screen("estadisticas", "Estadísticas", Icons.Default.BarChart)
}