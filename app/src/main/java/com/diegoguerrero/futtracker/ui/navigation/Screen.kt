package com.diegoguerrero.futtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Datos : Screen("datos", "Datos", Icons.Default.Storage)
    object Alineacion : Screen("alineacion", "Pizarra", Icons.Default.SportsSoccer)
    object Sorteos : Screen("sorteos", "Sorteos", Icons.Default.Casino)
    object Estadisticas : Screen("estadisticas", "Stats", Icons.Default.BarChart)
    object Versus : Screen("versus", "Versus", Icons.Default.SportsMma)
    object Perfil : Screen("perfil", "Perfil", Icons.Default.Person)

    // Alias heredados para compatibilidad
    object Jugadores : Screen("jugadores", "Jugadores", Icons.Default.People)
    object Partidos : Screen("partidos", "Partidos", Icons.Default.Event)
    object Enfrentamientos : Screen("enfrentamientos", "Versus", Icons.Default.SportsMma)
}