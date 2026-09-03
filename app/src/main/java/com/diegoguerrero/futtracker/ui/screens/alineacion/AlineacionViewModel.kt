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
        _uiState.update { 
            it.copy(
                tipoFutbol = tipo, 
                formacionSeleccionada = null, 
                alineacionCalculada = null,
                convocados = emptyList(),
                error = null
            ) 
        }
    }

    fun seleccionarFormacion(formacion: Formacion) {
        _uiState.update { it.copy(formacionSeleccionada = formacion) }
    }

    fun toggleConvocado(jugador: Jugador) {
        _uiState.update { state ->
            val listaActual = state.convocados.toMutableList()
            val maxRequerido = state.tipoFutbol.nJugadoresCampo

            if (listaActual.contains(jugador)) {
                listaActual.remove(jugador)
            } else if (listaActual.size < maxRequerido) {
                listaActual.add(jugador)
            }

            state.copy(
                convocados = listaActual,
                error = null
            )
        }
    }

    fun calcularAlineacion() {
        val currentState = _uiState.value
        val formacionActual = currentState.formacionSeleccionada ?: return
        val convocados = currentState.convocados
        val numRequerido = currentState.tipoFutbol.nJugadoresCampo

        // Validación de número exacto de convocados
        if (convocados.size != numRequerido) {
            _uiState.update { 
                it.copy(error = "Se requieren exactamente $numRequerido jugadores para la alineación.") 
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val resultado = generarAlineacionUseCase(convocados, formacionActual)
            _uiState.update { 
                it.copy(
                    alineacionCalculada = resultado,
                    isLoading = false
                ) 
            }
        }
    }
}