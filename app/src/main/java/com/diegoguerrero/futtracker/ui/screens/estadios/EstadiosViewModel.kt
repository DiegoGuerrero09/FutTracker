package com.diegoguerrero.futtracker.ui.screens.estadios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegoguerrero.futtracker.domain.model.Estadio
import com.diegoguerrero.futtracker.domain.repository.EstadioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EstadiosViewModel @Inject constructor(
    private val estadioRepository: EstadioRepository
) : ViewModel() {

    val estadios: StateFlow<List<Estadio>> = estadioRepository.obtenerEstadios()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agregarEstadio(estadio: Estadio) {
        viewModelScope.launch {
            estadioRepository.insertarEstadio(estadio)
        }
    }

    fun actualizarEstadio(estadio: Estadio) {
        viewModelScope.launch {
            estadioRepository.actualizarEstadio(estadio)
        }
    }

    fun eliminarEstadio(estadio: Estadio) {
        viewModelScope.launch {
            estadioRepository.eliminarEstadio(estadio)
        }
    }

    fun toggleFavorito(estadio: Estadio) {
        viewModelScope.launch {
            estadioRepository.actualizarEstadio(estadio.copy(esFavorito = !estadio.esFavorito))
        }
    }
}
