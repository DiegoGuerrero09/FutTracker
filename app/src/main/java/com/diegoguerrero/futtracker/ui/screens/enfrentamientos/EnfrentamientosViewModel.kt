package com.diegoguerrero.futtracker.ui.screens.enfrentamientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegoguerrero.futtracker.domain.model.DestacadosEnfrentamientos
import com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorCruzadas
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.domain.repository.EnfrentamientosRepository
import com.diegoguerrero.futtracker.domain.repository.JugadorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class FiltrosGeneralData(
    val busq: String,
    val fav: Boolean,
    val pos: Posicion?,
    val soloPrin: Boolean,
    val idSel: String?
)

private data class TupleCuatro<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

@HiltViewModel
class EnfrentamientosViewModel @Inject constructor(
    private val enfrentamientosRepository: EnfrentamientosRepository,
    private val jugadorRepository: JugadorRepository
) : ViewModel() {

    private val _seccionActual = MutableStateFlow(SeccionEnfrentamientos.INDIVIDUAL)
    private val _filtroTexto = MutableStateFlow("")
    private val _filtroHistorial = MutableStateFlow(FiltroHistorial.TODOS)
    private val _filtroSoloFavoritos = MutableStateFlow(false)
    private val _filtroPosicion = MutableStateFlow<Posicion?>(null)
    private val _filtroSoloPosicionPrincipal = MutableStateFlow(false)
    private val _jugadorDetalle = MutableStateFlow<EstadisticasJugadorCruzadas?>(null)
    private val _jugadorAId = MutableStateFlow<String?>(null)
    private val _jugadorBId = MutableStateFlow<String?>(null)

    // Estado para la pestaña General (ver cabra, lacra, caramelito, bestia de cualquier jugador)
    private val _jugadorSeleccionadoGeneralId = MutableStateFlow<String?>(null)
    private val _busquedaGeneral = MutableStateFlow("")
    private val _soloFavoritosGeneral = MutableStateFlow(false)
    private val _posicionGeneral = MutableStateFlow<Posicion?>(null)
    private val _soloPosicionPrincipalGeneral = MutableStateFlow(false)

    val historial: StateFlow<List<EstadisticasJugadorCruzadas>> = enfrentamientosRepository.obtenerHistorialCruzado()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val destacados = enfrentamientosRepository.obtenerDestacados()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DestacadosEnfrentamientos())

    val duos = enfrentamientosRepository.obtenerDuos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todosJugadores = jugadorRepository.obtenerJugadores()
        .map { lista ->
            lista.sortedWith(
                compareByDescending<Jugador> { it.esUsuarioPropio || it.id == "usuario_propio_id" }
                    .thenByDescending { it.esFavorito }
                    .thenBy { it.nombre.lowercase() }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val destacadosGeneral: StateFlow<DestacadosEnfrentamientos> = _jugadorSeleccionadoGeneralId.flatMapLatest { id ->
        if (id != null) {
            enfrentamientosRepository.obtenerDestacadosParaJugador(id)
        } else {
            flowOf(DestacadosEnfrentamientos())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DestacadosEnfrentamientos())

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

    private val filtrosGeneralFlow = combine(
        _busquedaGeneral,
        _soloFavoritosGeneral,
        _posicionGeneral,
        _soloPosicionPrincipalGeneral,
        _jugadorSeleccionadoGeneralId
    ) { busq, fav, pos, soloPrin, idSel ->
        FiltrosGeneralData(busq, fav, pos, soloPrin, idSel)
    }

    val uiState: StateFlow<EnfrentamientosUiState> = combine(
        combine(_seccionActual, _filtroTexto, _filtroHistorial) { sec, texto, filtro ->
            Triple(sec, texto, filtro)
        },
        combine(_filtroSoloFavoritos, _filtroPosicion, _filtroSoloPosicionPrincipal) { fav, pos, soloPrin ->
            Triple(fav, pos, soloPrin)
        },
        combine(historial, destacados, duos) { hist, dest, d ->
            Triple(hist, dest, d)
        },
        combine(todosJugadores, _jugadorDetalle, comparativaCaraACara) { jug, det, comp ->
            Triple(jug, det, comp)
        },
        combine(_jugadorAId, _jugadorBId, destacadosGeneral, filtrosGeneralFlow) { a, b, destGen, fGen ->
            TupleCuatro(a, b, destGen, fGen)
        }
    ) { (sec, texto, filtro), (soloFav, pos, soloPrin), (hist, dest, d), (jug, det, comp), tuple ->
        val (idA, idB, destGen, fGen) = tuple

        val historialFiltrado = hist.filter { item ->
            val coincideTexto = texto.isBlank() || item.jugador.nombre.contains(texto, ignoreCase = true)
            val coincideFiltro = when (filtro) {
                FiltroHistorial.TODOS -> true
                FiltroHistorial.COMPANEROS -> item.partidosComoCompanero > 0
                FiltroHistorial.RIVALES -> item.partidosComoRival > 0
            }
            val coincideFav = !soloFav || item.jugador.esFavorito
            val coincidePos = when {
                pos == null -> true
                soloPrin -> item.jugador.posicionesPrimarias.contains(pos)
                else -> item.jugador.posicionesPrimarias.contains(pos) || item.jugador.posicionesSecundarias.contains(pos)
            }
            coincideTexto && coincideFiltro && coincideFav && coincidePos
        }

        val jugadoresFiltradosGen = jug.filter { item ->
            val coincideBusq = fGen.busq.isBlank() || item.nombre.contains(fGen.busq, ignoreCase = true)
            val coincideFav = !fGen.fav || item.esFavorito
            val coincidePos = when {
                fGen.pos == null -> true
                fGen.soloPrin -> item.posicionesPrimarias.contains(fGen.pos)
                else -> item.posicionesPrimarias.contains(fGen.pos) || item.posicionesSecundarias.contains(fGen.pos)
            }
            coincideBusq && coincideFav && coincidePos
        }

        // Auto-seleccionar primer y segundo jugador para cara a cara si están vacíos
        val finalA = idA ?: jug.firstOrNull()?.id
        val finalB = idB ?: jug.drop(1).firstOrNull()?.id
        val finalGenId = fGen.idSel ?: jug.firstOrNull()?.id

        EnfrentamientosUiState(
            seccionActual = sec,
            historial = historialFiltrado,
            destacados = dest,
            duos = d,
            todosLosJugadores = jug,
            filtroTexto = texto,
            filtroHistorial = filtro,
            filtroSoloFavoritos = soloFav,
            filtroPosicion = pos,
            filtroSoloPosicionPrincipal = soloPrin,
            jugadorDetalle = det,
            jugadorAId = finalA,
            jugadorBId = finalB,
            comparativaCaraACara = comp,
            jugadorSeleccionadoGeneralId = finalGenId,
            destacadosGeneral = destGen,
            busquedaGeneral = fGen.busq,
            soloFavoritosGeneral = fGen.fav,
            posicionGeneral = fGen.pos,
            soloPosicionPrincipalGeneral = fGen.soloPrin,
            jugadoresFiltradosGeneral = jugadoresFiltradosGen
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
                    if (_jugadorSeleccionadoGeneralId.value == null) {
                        _jugadorSeleccionadoGeneralId.value = jugadores[0].id
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

    fun toggleFiltroSoloFavoritos() {
        _filtroSoloFavoritos.value = !_filtroSoloFavoritos.value
    }

    fun setFiltroPosicion(pos: Posicion?) {
        _filtroPosicion.value = pos
    }

    fun setFiltroSoloPosicionPrincipal(soloPrincipal: Boolean) {
        _filtroSoloPosicionPrincipal.value = soloPrincipal
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

    fun seleccionarJugadorGeneral(id: String) {
        _jugadorSeleccionadoGeneralId.value = id
    }

    fun setBusquedaGeneral(texto: String) {
        _busquedaGeneral.value = texto
    }

    fun toggleSoloFavoritosGeneral() {
        _soloFavoritosGeneral.value = !_soloFavoritosGeneral.value
    }

    fun setPosicionGeneral(pos: Posicion?) {
        _posicionGeneral.value = pos
    }

    fun setSoloPosicionPrincipalGeneral(soloPrincipal: Boolean) {
        _soloPosicionPrincipalGeneral.value = soloPrincipal
    }
}
