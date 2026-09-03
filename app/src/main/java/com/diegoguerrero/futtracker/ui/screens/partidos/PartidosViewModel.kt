package com.diegoguerrero.futtracker.ui.screens.partidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.domain.repository.JugadorRepository
import com.diegoguerrero.futtracker.domain.repository.PartidoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartidosViewModel @Inject constructor(
    private val partidoRepository: PartidoRepository,
    private val jugadorRepository: JugadorRepository
) : ViewModel() {

    val partidos: StateFlow<List<Partido>> = partidoRepository.obtenerPartidos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val jugadores: StateFlow<List<Jugador>> = jugadorRepository.obtenerJugadores()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agregarPartido(partido: Partido) {
        viewModelScope.launch {
            partidoRepository.insertarPartido(partido)
        }
    }

    fun actualizarPartido(partido: Partido) {
        viewModelScope.launch {
            partidoRepository.actualizarPartido(partido)
        }
    }

    fun eliminarPartido(partido: Partido) {
        viewModelScope.launch {
            partidoRepository.eliminarPartido(partido)
        }
    }
}
