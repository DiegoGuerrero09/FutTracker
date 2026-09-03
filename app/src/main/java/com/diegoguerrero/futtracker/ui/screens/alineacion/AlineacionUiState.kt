package com.diegoguerrero.futtracker.ui.screens.alineacion

import com.diegoguerrero.futtracker.domain.model.Formacion
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.domain.model.TipoFutbol

data class AlineacionUiState(
    val tipoFutbol: TipoFutbol = TipoFutbol.FUT_7,
    val formacionSeleccionada: Formacion? = null,
    val alineacionCalculada: List<Pair<Posicion, Jugador>>? = null,
    val isLoading: Boolean = false
)