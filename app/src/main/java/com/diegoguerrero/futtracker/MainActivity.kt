package com.diegoguerrero.futtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
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
import com.diegoguerrero.futtracker.ui.screens.partidos.PartidosScreen
import com.diegoguerrero.futtracker.ui.screens.partidos.PartidosViewModel
import com.diegoguerrero.futtracker.ui.screens.perfil.PerfilScreen
import com.diegoguerrero.futtracker.ui.screens.perfil.PerfilViewModel
import com.diegoguerrero.futtracker.ui.screens.sorteos.SorteosScreen
import com.diegoguerrero.futtracker.ui.screens.splash.SplashScreen
import com.diegoguerrero.futtracker.ui.theme.DarkBackground
import com.diegoguerrero.futtracker.ui.theme.FutTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val jugadoresViewModel: JugadoresViewModel by viewModels()
    private val partidosViewModel: PartidosViewModel by viewModels()
    private val perfilViewModel: PerfilViewModel by viewModels()
    private val enfrentamientosViewModel: com.diegoguerrero.futtracker.ui.screens.enfrentamientos.EnfrentamientosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FutTrackerTheme {
                var showSplash by remember { mutableStateOf(true) }

                Crossfade(targetState = showSplash, label = "splashTransition") { isSplash ->
                    if (isSplash) {
                        SplashScreen(onSplashFinished = { showSplash = false })
                    } else {
                        MainAppLayout(
                            jugadoresViewModel = jugadoresViewModel,
                            partidosViewModel = partidosViewModel,
                            perfilViewModel = perfilViewModel,
                            enfrentamientosViewModel = enfrentamientosViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppLayout(
    jugadoresViewModel: JugadoresViewModel,
    partidosViewModel: PartidosViewModel,
    perfilViewModel: PerfilViewModel,
    enfrentamientosViewModel: com.diegoguerrero.futtracker.ui.screens.enfrentamientos.EnfrentamientosViewModel
) {
    val navController = rememberNavController()
    val jugadores by jugadoresViewModel.jugadores.collectAsStateWithLifecycle()

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
            composable(Screen.Sorteos.route) {
                SorteosScreen(jugadores = jugadores)
            }
            composable(Screen.Jugadores.route) {
                JugadoresScreen(
                    jugadores = jugadores,
                    onAgregarJugador = { nuevoJugador ->
                        jugadoresViewModel.agregarJugador(nuevoJugador)
                    },
                    onActualizarJugador = { jugadorActualizado ->
                        jugadoresViewModel.actualizarJugador(jugadorActualizado)
                    },
                    onEliminarJugador = { jugadorAEliminar ->
                        jugadoresViewModel.eliminarJugador(jugadorAEliminar)
                    },
                    onToggleFavorito = { jugadorAAlternar ->
                        jugadoresViewModel.toggleFavorito(jugadorAAlternar)
                    }
                )
            }
            composable(Screen.Partidos.route) {
                val partidos by partidosViewModel.partidos.collectAsStateWithLifecycle()
                PartidosScreen(
                    partidos = partidos,
                    jugadores = jugadores,
                    onAgregarPartido = { partidosViewModel.agregarPartido(it) },
                    onActualizarPartido = { partidosViewModel.actualizarPartido(it) },
                    onEliminarPartido = { partidosViewModel.eliminarPartido(it) }
                )
            }
            composable(Screen.Estadisticas.route) {
                com.diegoguerrero.futtracker.ui.screens.estadisticas.EstadisticasScreen()
            }
            composable(Screen.Enfrentamientos.route) {
                com.diegoguerrero.futtracker.ui.screens.enfrentamientos.EnfrentamientosScreen(
                    viewModel = enfrentamientosViewModel
                )
            }
            composable(Screen.Perfil.route) {
                val perfil by perfilViewModel.perfil.collectAsStateWithLifecycle()
                PerfilScreen(
                    perfil = perfil,
                    onGuardarPerfil = { perfilViewModel.guardarPerfil(it) },
                    onExportarDatos = { perfilViewModel.exportarDatosJson() }
                )
            }
        }
    }
}
