package com.diegoguerrero.futtracker.ui.screens.alineacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegoguerrero.futtracker.domain.model.Formacion
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.TipoFutbol
import com.diegoguerrero.futtracker.domain.usecase.GenerarAlineacionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlineacionViewModel(
    private val generarAlineacionUseCase: GenerarAlineacionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlineacionUiState())
    val uiState: StateFlow<AlineacionUiState> = _uiState.asStateFlow()

    fun seleccionarTipoFutbol(tipo: TipoFutbol) {
        _uiState.update { it.copy(tipoFutbol = tipo, formacionSeleccionada = null, alineacionCalculada = null) }
    }

    fun seleccionarFormacion(formacion: Formacion) {
        _uiState.update { it.copy(formacionSeleccionada = formacion) }
    }

    fun calcularAlineacion(jugadoresDisponibles: List<Jugador>) {
        val formacionActual = _uiState.value.formacionSeleccionada ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val resultado = generarAlineacionUseCase(jugadoresDisponibles, formacionActual)
            _uiState.update { 
                it.copy(
                    alineacionCalculada = resultado,
                    isLoading = false
                ) 
            }
        }
    }
}