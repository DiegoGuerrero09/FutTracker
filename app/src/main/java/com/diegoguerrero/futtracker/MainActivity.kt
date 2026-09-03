package com.diegoguerrero.futtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.ui.navigation.BottomNavigationBar
import com.diegoguerrero.futtracker.ui.navigation.Screen
import com.diegoguerrero.futtracker.ui.screens.alineacion.AlineacionScreen
import com.diegoguerrero.futtracker.ui.screens.jugadores.JugadoresScreen
import com.diegoguerrero.futtracker.ui.theme.DarkBackground
import com.diegoguerrero.futtracker.ui.theme.FutTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FutTrackerTheme {
                MainAppLayout()
            }
        }
    }
}

@Composable
fun MainAppLayout() {
    val navController = rememberNavController()
    val jugadores = remember { mutableStateListOf<Jugador>() }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Alineacion.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Alineacion.route) {
                AlineacionScreen(plantillaCompleta = jugadores)
            }
            composable(Screen.Jugadores.route) {
                JugadoresScreen(
                    jugadores = jugadores,
                    onAgregarJugador = { nuevoJugador ->
                        jugadores.add(nuevoJugador)
                    },
                    onEliminarJugador = { jugadorAEliminar ->
                        jugadores.remove(jugadorAEliminar)
                    },
                    onToggleFavorito = { jugadorAAlternar ->
                        val index = jugadores.indexOfFirst { it.id == jugadorAAlternar.id }
                        if (index != -1) {
                            jugadores[index] = jugadores[index].copy(
                                esFavorito = !jugadores[index].esFavorito
                            )
                        }
                    }
                )
            }
            composable(Screen.Partidos.route) {
                // PartidosScreen()
            }
            composable(Screen.Estadisticas.route) {
                // EstadisticasScreen()
            }
        }
    }
}