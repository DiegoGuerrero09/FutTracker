package com.diegoguerrero.futtracker.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.diegoguerrero.futtracker.ui.theme.DarkCard
import com.diegoguerrero.futtracker.ui.theme.LimeVolt
import com.diegoguerrero.futtracker.ui.theme.TextSecondary

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Alineacion,
        Screen.Sorteos,
        Screen.Jugadores,
        Screen.Partidos,
        Screen.Perfil
    )

    NavigationBar(
        containerColor = DarkCard,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 10.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = LimeVolt,
                    indicatorColor = LimeVolt,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}