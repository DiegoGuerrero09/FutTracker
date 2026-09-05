package com.diegoguerrero.futtracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
        Screen.Rankings,
        Screen.Versus,
        Screen.Perfil
    )

    Surface(
        color = DarkCard,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            items.forEach { screen ->
                val isSelected = currentRoute == screen.route ||
                    (screen == Screen.Datos && (currentRoute == Screen.Jugadores.route || currentRoute == Screen.Partidos.route)) ||
                    (screen == Screen.Versus && currentRoute == Screen.Enfrentamientos.route)

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 28.dp, height = 20.dp)
                                .background(
                                    color = when {
                                        isSelected -> LimeVolt
                                        isPressed -> LimeVolt.copy(alpha = 0.25f)
                                        else -> Color.Transparent
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(17.dp),
                                tint = when {
                                    isSelected -> Color.Black
                                    isPressed -> LimeVolt
                                    else -> TextSecondary
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(1.5.dp))
                        Text(
                            text = screen.title,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) LimeVolt else TextSecondary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            style = androidx.compose.ui.text.TextStyle(
                                platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }
            }
        }
    }
}