package com.diegoguerrero.futtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Jugadores : Screen("jugadores", "Jugadores", Icons.Default.People)
    object Alineacion : Screen("alineacion", "Alineaciones", Icons.Default.SportsSoccer)
    object Sorteos : Screen("sorteos", "Sorteos", Icons.Default.Casino)
    object Estadisticas : Screen("estadisticas", "Estadísticas", Icons.Default.BarChart)
    object Partidos : Screen("partidos", "Partidos", Icons.Default.Event)
    object Enfrentamientos : Screen("enfrentamientos", "Enfrentamientos", Icons.AutoMirrored.Filled.CompareArrows)
    object Perfil : Screen("perfil", "Perfil", Icons.Default.Person)
}