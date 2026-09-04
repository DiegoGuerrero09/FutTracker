package com.diegoguerrero.futtracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
        Screen.Datos,
        Screen.Alineacion,
        Screen.Sorteos,
        Screen.Estadisticas,
        Screen.Versus,
        Screen.Perfil
    )

    NavigationBar(
        containerColor = DarkCard,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            val isSelected = currentRoute == screen.route ||
                (screen == Screen.Datos && (currentRoute == Screen.Jugadores.route || currentRoute == Screen.Partidos.route)) ||
                (screen == Screen.Versus && currentRoute == Screen.Enfrentamientos.route)

            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 24.dp)
                            .background(
                                color = if (isSelected) LimeVolt else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            modifier = Modifier.size(19.dp),
                            tint = if (isSelected) Color.Black else TextSecondary
                        )
                    }
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = LimeVolt,
                    indicatorColor = Color.Transparent,
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