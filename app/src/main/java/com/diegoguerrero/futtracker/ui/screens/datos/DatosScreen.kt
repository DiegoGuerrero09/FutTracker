package com.diegoguerrero.futtracker.ui.screens.datos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.ui.screens.jugadores.JugadoresScreen
import com.diegoguerrero.futtracker.ui.screens.partidos.PartidosScreen
import com.diegoguerrero.futtracker.ui.theme.DarkBackground
import com.diegoguerrero.futtracker.ui.theme.DarkCard
import com.diegoguerrero.futtracker.ui.theme.LimeVolt
import com.diegoguerrero.futtracker.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatosScreen(
    jugadores: List<Jugador>,
    partidos: List<Partido>,
    onAgregarJugador: (Jugador) -> Unit,
    onActualizarJugador: (Jugador) -> Unit,
    onEliminarJugador: (Jugador) -> Unit,
    onToggleFavorito: (Jugador) -> Unit,
    onAgregarPartido: (Partido) -> Unit,
    onActualizarPartido: (Partido) -> Unit,
    onEliminarPartido: (Partido) -> Unit
) {
    var tabSeleccionada by remember { mutableStateOf(0) }
    val tabs = listOf("Jugadores", "Partidos")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Datos",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkCard
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = tabSeleccionada,
                containerColor = DarkCard,
                contentColor = LimeVolt
            ) {
                tabs.forEachIndexed { index, titulo ->
                    Tab(
                        selected = tabSeleccionada == index,
                        onClick = { tabSeleccionada = index },
                        text = {
                            Text(
                                text = titulo,
                                fontSize = 13.sp,
                                fontWeight = if (tabSeleccionada == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selectedContentColor = LimeVolt,
                        unselectedContentColor = TextSecondary
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (tabSeleccionada) {
                    0 -> JugadoresScreen(
                        jugadores = jugadores,
                        onAgregarJugador = onAgregarJugador,
                        onActualizarJugador = onActualizarJugador,
                        onEliminarJugador = onEliminarJugador,
                        onToggleFavorito = onToggleFavorito,
                        mostrarTopBar = false
                    )
                    1 -> PartidosScreen(
                        partidos = partidos,
                        jugadores = jugadores,
                        onAgregarPartido = onAgregarPartido,
                        onActualizarPartido = onActualizarPartido,
                        onEliminarPartido = onEliminarPartido,
                        mostrarTopBar = false
                    )
                }
            }
        }
    }
}
