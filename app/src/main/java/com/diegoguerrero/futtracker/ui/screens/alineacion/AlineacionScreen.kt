package com.diegoguerrero.futtracker.ui.screens.alineacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.diegoguerrero.futtracker.domain.model.*
import com.diegoguerrero.futtracker.domain.usecase.GenerarAlineacionUseCase
import com.diegoguerrero.futtracker.ui.components.CampoFutbol
import com.diegoguerrero.futtracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlineacionScreen(
    plantillaCompleta: List<Jugador>
) {
    var tipoFutbol by remember { mutableStateOf(TipoFutbol.FUTSAL) }
    
    val formacionesDisponibles = when (tipoFutbol) {
        TipoFutbol.FUTSAL -> FORMACIONES_FUTSAL
        TipoFutbol.FUT_6 -> FORMACIONES_FUT_6
        TipoFutbol.FUT_7 -> FORMACIONES_FUT_7
    }.sortedBy { it.nombre } // Orden lexicográfico (alfabético)

    var formacionSeleccionada by remember { mutableStateOf(formacionesDisponibles.first()) }
    val convocados = remember { mutableStateListOf<Jugador>() }
    var alineacionMapaCampo by remember { mutableStateOf<Map<Pair<Posicion, Pair<Float, Float>>, Jugador?>?>(null) }

    val useCase = remember { GenerarAlineacionUseCase() }

    LaunchedEffect(tipoFutbol) {
        formacionSeleccionada = formacionesDisponibles.first()
        alineacionMapaCampo = null
    }

    LaunchedEffect(formacionSeleccionada) {
        alineacionMapaCampo = null
    }

    val numRequerido = tipoFutbol.nJugadoresCampo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // Seleccion de modalidad: Futsal -> Fut 6 -> Fut 7
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = tipoFutbol == TipoFutbol.FUTSAL,
                onClick = { tipoFutbol = TipoFutbol.FUTSAL },
                label = { Text("Futsal") }
            )
            FilterChip(
                selected = tipoFutbol == TipoFutbol.FUT_6,
                onClick = { tipoFutbol = TipoFutbol.FUT_6 },
                label = { Text("Fut 6") }
            )
            FilterChip(
                selected = tipoFutbol == TipoFutbol.FUT_7,
                onClick = { tipoFutbol = TipoFutbol.FUT_7 },
                label = { Text("Fut 7") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selector Formaciones ordenadas
        Text("Alineación", color = TextSecondary)
        ScrollableTabRow(
            selectedTabIndex = formacionesDisponibles.indexOf(formacionSeleccionada).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = DarkBackground
        ) {
            formacionesDisponibles.forEach { f ->
                Tab(
                    selected = formacionSeleccionada == f,
                    onClick = { formacionSeleccionada = f },
                    text = { Text(f.nombre, color = if (formacionSeleccionada == f) LimeVolt else TextSecondary) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Boton Generar Alineacion (se habilita unicamente con el numero exacto de convocados)
        Button(
            onClick = {
                val alineacionLista: List<Pair<Posicion, Jugador>> = useCase(convocados, formacionSeleccionada)
                val coords = obtenerCoordenadas(formacionSeleccionada)
                
                alineacionMapaCampo = coords.associateWith { (posicionCampo, _) ->
                    alineacionLista.firstOrNull { (posAsignada, _) -> posAsignada == posicionCampo }?.second
                }
            },
            enabled = convocados.size == numRequerido,
            colors = ButtonDefaults.buttonColors(containerColor = LimeVolt, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generar alineación")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Vista de lista + tablero
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            alineacionMapaCampo?.let { mapaAlineacion ->
                item {
                    Text(
                        text = "Alineación Optimizada (${formacionSeleccionada.nombre})",
                        color = LimeVolt,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CampoFutbol(
                        alineacion = mapaAlineacion,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Text(
                    text = "Convocados (${convocados.size}/$numRequerido)",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(
                items = plantillaCompleta,
                key = { it.id }
            ) { jugador ->
                val isSelected = convocados.contains(jugador)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (convocados.size < numRequerido) {
                                    convocados.add(jugador)
                                }
                            } else {
                                convocados.remove(jugador)
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = LimeVolt, checkmarkColor = Color.Black)
                    )
                    Text(jugador.nombre, color = Color.White)
                }
            }
        }
    }
}