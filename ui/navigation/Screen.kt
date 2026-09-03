package com.diegoguerrero.futtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Alineacion : Screen("alineacion", "Pizarra", Icons.Default.List)
    object Partidos : Screen("partidos", "Partidos", Icons.Default.DateRange)
    object Estadisticas : Screen("estadisticas", "Stats", Icons.Default.Person)
}