package com.diegoguerrero.futtracker.ui.screens.enfrentamientos

import com.diegoguerrero.futtracker.domain.model.ComparativaCaraACara
import com.diegoguerrero.futtracker.domain.model.DestacadosEnfrentamientos
import com.diegoguerrero.futtracker.domain.model.DuoEstadisticas
import com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorCruzadas
import com.diegoguerrero.futtracker.domain.model.Jugador

enum class SeccionEnfrentamientos(val titulo: String) {
    HISTORIAL("Compañeros / rivales"),
    CARA_A_CARA("Cara a cara"),
    DUOS("Dúos")
}

enum class FiltroHistorial(val label: String) {
    TODOS("Todos"),
    COMPANEROS("Compañeros"),
    RIVALES("Rivales")
}

data class EnfrentamientosUiState(
    val seccionActual: SeccionEnfrentamientos = SeccionEnfrentamientos.HISTORIAL,
    val historial: List<EstadisticasJugadorCruzadas> = emptyList(),
    val destacados: DestacadosEnfrentamientos = DestacadosEnfrentamientos(),
    val duos: List<DuoEstadisticas> = emptyList(),
    val todosLosJugadores: List<Jugador> = emptyList(),
    val filtroTexto: String = "",
    val filtroHistorial: FiltroHistorial = FiltroHistorial.TODOS,
    val jugadorDetalle: EstadisticasJugadorCruzadas? = null,
    val jugadorAId: String? = null,
    val jugadorBId: String? = null,
    val comparativaCaraACara: ComparativaCaraACara? = null,
    val cargando: Boolean = false
)
