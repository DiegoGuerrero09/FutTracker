package com.diegoguerrero.futtracker.ui.screens.rankings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.domain.model.nombreConTu
import com.diegoguerrero.futtracker.ui.components.BadgePosicion
import com.diegoguerrero.futtracker.ui.components.FilaBadgesPosiciones
import com.diegoguerrero.futtracker.ui.components.JugadorAvatar
import com.diegoguerrero.futtracker.ui.screens.estadisticas.CriterioOrdenGeneral
import com.diegoguerrero.futtracker.ui.screens.estadisticas.EstadisticasJugadorGeneral
import com.diegoguerrero.futtracker.ui.screens.estadisticas.EstadisticasViewModel
import com.diegoguerrero.futtracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingsScreen(
    viewModel: EstadisticasViewModel = hiltViewModel()
) {
    val busquedaGeneral by viewModel.busquedaGeneral.collectAsState()
    val soloFavoritosGeneral by viewModel.soloFavoritosGeneral.collectAsState()
    val posicionGeneral by viewModel.posicionGeneral.collectAsState()
    val soloPosicionPrincipalGeneral by viewModel.soloPosicionPrincipalGeneral.collectAsState()
    val criterioOrdenGeneral by viewModel.criterioOrdenGeneral.collectAsState()
    val ordenAscendenteGeneral by viewModel.ordenAscendenteGeneral.collectAsState()
    val jugadoresGeneral by viewModel.jugadoresEstadisticasGeneral.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rankings",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCard)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Buscador centrado
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(DarkCard, RoundedCornerShape(10.dp))
                        .border(1.dp, TextSecondary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (busquedaGeneral.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Buscar jugador...",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    BasicTextField(
                        value = busquedaGeneral,
                        onValueChange = { viewModel.setBusquedaGeneral(it) },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(LimeVolt),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (busquedaGeneral.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.setBusquedaGeneral("") },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Filtros: Favoritos y Posiciones
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = soloFavoritosGeneral,
                            onClick = { viewModel.toggleSoloFavoritosGeneral() },
                            label = { Text("Favoritos", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = posicionGeneral == null,
                            onClick = { viewModel.setPosicionGeneral(null) },
                            label = { Text("Todos", fontSize = 11.sp) }
                        )
                    }
                    items(Posicion.entries.toTypedArray()) { pos ->
                        FilterChip(
                            selected = posicionGeneral == pos,
                            onClick = {
                                viewModel.setPosicionGeneral(if (posicionGeneral == pos) null else pos)
                            },
                            label = { Text(pos.name, fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Subfiltro Ambas posiciones vs Solo principal
            if (posicionGeneral != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = !soloPosicionPrincipalGeneral,
                            onClick = { viewModel.setSoloPosicionPrincipalGeneral(false) },
                            label = { Text("Ambas posiciones", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = soloPosicionPrincipalGeneral,
                            onClick = { viewModel.setSoloPosicionPrincipalGeneral(true) },
                            label = { Text("Solo posición principal", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Criterios de ordenación
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ordenar por:",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            onClick = { viewModel.toggleOrdenAscendenteGeneral() },
                            shape = RoundedCornerShape(8.dp),
                            color = DarkCard,
                            border = BorderStroke(1.dp, (if (ordenAscendenteGeneral) LimeVolt else BlueCompanero).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val flechaColor = if (ordenAscendenteGeneral) LimeVolt else BlueCompanero
                                Icon(
                                    imageVector = if (ordenAscendenteGeneral) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = if (ordenAscendenteGeneral) "Ascendente" else "Descendente",
                                    tint = flechaColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (ordenAscendenteGeneral) "Ascendente" else "Descendente",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val opciones = listOf(
                            CriterioOrdenGeneral.PORCENTAJE to "% Victorias",
                            CriterioOrdenGeneral.NOMBRE to "Nombre",
                            CriterioOrdenGeneral.PARTIDOS to "Partidos",
                            CriterioOrdenGeneral.MINUTOS to "Minutos",
                            CriterioOrdenGeneral.VICTORIAS to "Victorias",
                            CriterioOrdenGeneral.EMPATES to "Empates",
                            CriterioOrdenGeneral.DERROTAS to "Derrotas"
                        )
                        items(opciones) { (criterio, label) ->
                            FilterChip(
                                selected = criterioOrdenGeneral == criterio,
                                onClick = { viewModel.setCriterioOrdenGeneral(criterio) },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // Lista de jugadores con estadísticas
            if (jugadoresGeneral.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No se encontraron jugadores",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(jugadoresGeneral, key = { it.jugador.id }) { item ->
                    CardJugadorGeneral(item = item)
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun CardJugadorGeneral(item: EstadisticasJugadorGeneral) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, LimeVolt.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JugadorAvatar(
                    fotoUri = item.jugador.fotoUri,
                    nombre = item.jugador.nombre,
                    tamano = 44.dp,
                    permitirZoom = true
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.jugador.nombreConTu(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (item.jugador.esFavorito) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorito",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    FilaBadgesPosiciones(
                        primarias = item.jugador.posicionesPrimarias,
                        secundarias = item.jugador.posicionesSecundarias,
                        maxVisibles = 2
                    )
                }

                // Porcentaje de victorias destacado
                val colorPct = obtenerColorPorcentaje(item.porcentajeVictorias.toFloat())
                Surface(
                    color = colorPct.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, colorPct.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${item.porcentajeVictorias}%",
                            color = colorPct,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Victorias",
                            color = colorPct.copy(alpha = 0.85f),
                            fontSize = 9.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = DarkCardBorder, thickness = 0.8.dp)

            // Fila de estadísticas: PJ, Min, V, E, D
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatGeneralPill(
                    label = "PJ",
                    valor = item.partidosJugados.toString(),
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                StatGeneralPill(
                    label = "",
                    valor = "${item.minutosJugados} min",
                    color = LimeVolt,
                    modifier = Modifier.weight(1.3f)
                )

                StatGeneralPill(
                    label = "V",
                    valor = item.victorias.toString(),
                    color = GreenWin,
                    modifier = Modifier.weight(1f)
                )

                StatGeneralPill(
                    label = "E",
                    valor = item.empates.toString(),
                    color = OrangeDraw,
                    modifier = Modifier.weight(1f)
                )

                StatGeneralPill(
                    label = "D",
                    valor = item.derrotas.toString(),
                    color = RedLoss,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatGeneralPill(
    label: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = valor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
