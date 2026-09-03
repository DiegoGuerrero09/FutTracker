package com.diegoguerrero.futtracker.ui.screens.jugadores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.repository.JugadorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JugadoresViewModel @Inject constructor(
    private val repository: JugadorRepository
) : ViewModel() {

    val jugadores: StateFlow<List<Jugador>> = repository.obtenerJugadores()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agregarJugador(jugador: Jugador) {
        viewModelScope.launch {
            repository.insertarJugador(jugador)
        }
    }

    fun actualizarJugador(jugador: Jugador) {
        viewModelScope.launch {
            repository.actualizarJugador(jugador)
        }
    }

    fun eliminarJugador(jugador: Jugador) {
        viewModelScope.launch {
            repository.eliminarJugador(jugador)
        }
    }

    fun toggleFavorito(jugador: Jugador) {
        viewModelScope.launch {
            val jugadorActualizado = jugador.copy(esFavorito = !jugador.esFavorito)
            repository.actualizarJugador(jugadorActualizado)
        }
    }
}