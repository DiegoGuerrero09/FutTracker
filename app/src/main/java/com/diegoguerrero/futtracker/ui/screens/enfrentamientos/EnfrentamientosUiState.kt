package com.diegoguerrero.futtracker.ui.screens.enfrentamientos

import com.diegoguerrero.futtracker.domain.model.DestacadosEnfrentamientos
import com.diegoguerrero.futtracker.domain.model.DuoEstadisticas
import com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorCruzadas
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.ui.screens.partidos.PeriodoPartidos
import java.util.Calendar

enum class SeccionEnfrentamientos(val titulo: String) {
    INDIVIDUAL("Individual"),
    DUOS("Dúos")
}

enum class FiltroHistorial(val label: String) {
    TODOS("Todos"),
    COMPANEROS("Compañeros"),
    RIVALES("Rivales")
}

data class EnfrentamientosUiState(
    val seccionActual: SeccionEnfrentamientos = SeccionEnfrentamientos.INDIVIDUAL,

    // Jugador inspeccionado (arriba)
    val jugadorSeleccionadoId: String? = null,
    val busquedaJugadorInspeccionado: String = "",
    val soloFavoritosInspeccionado: Boolean = false,
    val posicionInspeccionado: Posicion? = null,
    val soloPosicionPrincipalInspeccionado: Boolean = false,
    val jugadoresFiltradosInspeccionados: List<Jugador> = emptyList(),

    // Período de tiempo
    val filtroPeriodo: PeriodoPartidos = PeriodoPartidos.TOTAL,
    val anioSeleccionado: Int = Calendar.getInstance().get(Calendar.YEAR),
    val temporadaSeleccionada: String = "",
    val fechaInicio: Long = System.currentTimeMillis() - 30L * 86400000L,
    val fechaFin: Long = System.currentTimeMillis(),
    val aniosDisponibles: List<Int> = listOf(Calendar.getInstance().get(Calendar.YEAR)),
    val temporadasDisponibles: List<String> = emptyList(),

    // Destacados para el jugador seleccionado
    val destacados: DestacadosEnfrentamientos = DestacadosEnfrentamientos(),

    // Historial cruzado contra los demás jugadores (excluyendo el seleccionado)
    val historial: List<EstadisticasJugadorCruzadas> = emptyList(),
    val filtroTexto: String = "",
    val filtroHistorial: FiltroHistorial = FiltroHistorial.TODOS,
    val filtroSoloFavoritos: Boolean = false,
    val filtroPosicion: Posicion? = null,
    val filtroSoloPosicionPrincipal: Boolean = false,

    // Modal de detalle cruzado
    val jugadorDetalle: EstadisticasJugadorCruzadas? = null,

    // Dúos y listado completo
    val duos: List<DuoEstadisticas> = emptyList(),
    val todosLosJugadores: List<Jugador> = emptyList(),
    val cargando: Boolean = false
)
