package com.diegoguerrero.futtracker.ui.screens.enfrentamientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorCruzadas
import com.diegoguerrero.futtracker.domain.repository.EnfrentamientosRepository
import com.diegoguerrero.futtracker.domain.repository.JugadorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnfrentamientosViewModel @Inject constructor(
    private val enfrentamientosRepository: EnfrentamientosRepository,
    private val jugadorRepository: JugadorRepository
) : ViewModel() {

    private val _seccionActual = MutableStateFlow(SeccionEnfrentamientos.HISTORIAL)
    private val _filtroTexto = MutableStateFlow("")
    private val _filtroHistorial = MutableStateFlow(FiltroHistorial.TODOS)
    private val _jugadorDetalle = MutableStateFlow<EstadisticasJugadorCruzadas?>(null)
    private val _jugadorAId = MutableStateFlow<String?>(null)
    private val _jugadorBId = MutableStateFlow<String?>(null)

    val historial: StateFlow<List<EstadisticasJugadorCruzadas>> = enfrentamientosRepository.obtenerHistorialCruzado()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val destacados = enfrentamientosRepository.obtenerDestacados()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.diegoguerrero.futtracker.domain.model.DestacadosEnfrentamientos())

    val duos = enfrentamientosRepository.obtenerDuos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todosJugadores = jugadorRepository.obtenerJugadores()
        .map { lista -> lista.filter { !it.esUsuarioPropio && it.id != "usuario_propio_id" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val comparativaCaraACara = combine(_jugadorAId, _jugadorBId) { idA, idB ->
        Pair(idA, idB)
    }.flatMapLatest { (idA, idB) ->
        if (idA != null && idB != null && idA != idB) {
            enfrentamientosRepository.obtenerComparativa(idA, idB)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val uiState: StateFlow<EnfrentamientosUiState> = combine(
        combine(_seccionActual, _filtroTexto, _filtroHistorial) { sec, texto, filtro ->
            Triple(sec, texto, filtro)
        },
        combine(historial, destacados, duos) { hist, dest, d ->
            Triple(hist, dest, d)
        },
        combine(todosJugadores, _jugadorDetalle, comparativaCaraACara) { jug, det, comp ->
            Triple(jug, det, comp)
        },
        combine(_jugadorAId, _jugadorBId) { a, b ->
            Pair(a, b)
        }
    ) { (sec, texto, filtro), (hist, dest, d), (jug, det, comp), (idA, idB) ->
        val historialFiltrado = hist.filter { item ->
            val coincideTexto = texto.isBlank() || item.jugador.nombre.contains(texto, ignoreCase = true)
            val coincideFiltro = when (filtro) {
                FiltroHistorial.TODOS -> true
                FiltroHistorial.COMPANEROS -> item.partidosComoCompanero > 0
                FiltroHistorial.RIVALES -> item.partidosComoRival > 0
            }
            coincideTexto && coincideFiltro
        }

        // Auto-seleccionar primer y segundo jugador para cara a cara si están vacíos
        val finalA = idA ?: jug.firstOrNull()?.id
        val finalB = idB ?: jug.drop(1).firstOrNull()?.id

        EnfrentamientosUiState(
            seccionActual = sec,
            historial = historialFiltrado,
            destacados = dest,
            duos = d,
            todosLosJugadores = jug,
            filtroTexto = texto,
            filtroHistorial = filtro,
            jugadorDetalle = det,
            jugadorAId = finalA,
            jugadorBId = finalB,
            comparativaCaraACara = comp
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EnfrentamientosUiState())

    init {
        viewModelScope.launch {
            todosJugadores.collect { jugadores ->
                if (jugadores.isNotEmpty()) {
                    if (_jugadorAId.value == null) {
                        _jugadorAId.value = jugadores[0].id
                    }
                    if (_jugadorBId.value == null && jugadores.size > 1) {
                        _jugadorBId.value = jugadores[1].id
                    }
                }
            }
        }
    }

    fun setSeccion(seccion: SeccionEnfrentamientos) {
        _seccionActual.value = seccion
    }

    fun setFiltroTexto(texto: String) {
        _filtroTexto.value = texto
    }

    fun setFiltroHistorial(filtro: FiltroHistorial) {
        _filtroHistorial.value = filtro
    }

    fun seleccionarJugadorDetalle(item: EstadisticasJugadorCruzadas?) {
        _jugadorDetalle.value = item
    }

    fun seleccionarJugadorA(id: String) {
        _jugadorAId.value = id
    }

    fun seleccionarJugadorB(id: String) {
        _jugadorBId.value = id
    }
}
