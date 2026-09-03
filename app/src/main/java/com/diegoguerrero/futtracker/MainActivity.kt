package com.diegoguerrero.futtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.diegoguerrero.futtracker.ui.navigation.BottomNavigationBar
import com.diegoguerrero.futtracker.ui.navigation.Screen
import com.diegoguerrero.futtracker.ui.screens.alineacion.AlineacionScreen
import com.diegoguerrero.futtracker.ui.screens.jugadores.JugadoresScreen
import com.diegoguerrero.futtracker.ui.screens.jugadores.JugadoresViewModel
import com.diegoguerrero.futtracker.ui.theme.DarkBackground
import com.diegoguerrero.futtracker.ui.theme.FutTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val jugadoresViewModel: JugadoresViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FutTrackerTheme {
                MainAppLayout(viewModel = jugadoresViewModel)
            }
        }
    }
}

@Composable
fun MainAppLayout(viewModel: JugadoresViewModel) {
    val navController = rememberNavController()
    val jugadores by viewModel.jugadores.collectAsStateWithLifecycle()

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
                        viewModel.agregarJugador(nuevoJugador)
                    },
                    onActualizarJugador = { jugadorActualizado ->
                        viewModel.actualizarJugador(jugadorActualizado)
                    },
                    onEliminarJugador = { jugadorAEliminar ->
                        viewModel.eliminarJugador(jugadorAEliminar)
                    },
                    onToggleFavorito = { jugadorAAlternar ->
                        viewModel.toggleFavorito(jugadorAAlternar)
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