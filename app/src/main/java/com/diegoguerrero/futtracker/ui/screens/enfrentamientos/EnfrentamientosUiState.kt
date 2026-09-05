package com.diegoguerrero.futtracker.ui.screens.enfrentamientos

import com.diegoguerrero.futtracker.domain.model.ComparativaCaraACara
import com.diegoguerrero.futtracker.domain.model.DestacadosEnfrentamientos
import com.diegoguerrero.futtracker.domain.model.DuoEstadisticas
import com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorCruzadas
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion
import com.diegoguerrero.futtracker.ui.screens.partidos.PeriodoPartidos
import java.util.Calendar

enum class SeccionEnfrentamientos(val titulo: String) {
    INDIVIDUAL("Individual"),
    GENERAL("General"),
    DUOS("Dúos"),
    H2H("H2H")
}

enum class FiltroHistorial(val label: String) {
    TODOS("Todos"),
    COMPANEROS("Compañeros"),
    RIVALES("Rivales")
}

data class EnfrentamientosUiState(
    val seccionActual: SeccionEnfrentamientos = SeccionEnfrentamientos.INDIVIDUAL,
    val historial: List<EstadisticasJugadorCruzadas> = emptyList(),
    val destacados: DestacadosEnfrentamientos = DestacadosEnfrentamientos(),
    val duos: List<DuoEstadisticas> = emptyList(),
    val todosLosJugadores: List<Jugador> = emptyList(),
    val filtroTexto: String = "",
    val filtroHistorial: FiltroHistorial = FiltroHistorial.TODOS,
    val filtroSoloFavoritos: Boolean = false,
    val filtroPosicion: Posicion? = null,
    val filtroSoloPosicionPrincipal: Boolean = false,
    val filtroPeriodoIndividual: PeriodoPartidos = PeriodoPartidos.TOTAL,
    val anioSeleccionadoIndividual: Int = Calendar.getInstance().get(Calendar.YEAR),
    val temporadaSeleccionadaIndividual: String = "",
    val fechaInicioIndividual: Long = System.currentTimeMillis() - 30L * 86400000L,
    val fechaFinIndividual: Long = System.currentTimeMillis(),
    val jugadorDetalle: EstadisticasJugadorCruzadas? = null,
    val jugadorAId: String? = null,
    val jugadorBId: String? = null,
    val comparativaCaraACara: ComparativaCaraACara? = null,
    val jugadorSeleccionadoGeneralId: String? = null,
    val destacadosGeneral: DestacadosEnfrentamientos = DestacadosEnfrentamientos(),
    val busquedaGeneral: String = "",
    val soloFavoritosGeneral: Boolean = false,
    val posicionGeneral: Posicion? = null,
    val soloPosicionPrincipalGeneral: Boolean = false,
    val filtroPeriodoGeneral: PeriodoPartidos = PeriodoPartidos.TOTAL,
    val anioSeleccionadoGeneral: Int = Calendar.getInstance().get(Calendar.YEAR),
    val temporadaSeleccionadaGeneral: String = "",
    val fechaInicioGeneral: Long = System.currentTimeMillis() - 30L * 86400000L,
    val fechaFinGeneral: Long = System.currentTimeMillis(),
    val aniosDisponibles: List<Int> = listOf(Calendar.getInstance().get(Calendar.YEAR)),
    val temporadasDisponibles: List<String> = emptyList(),
    val jugadoresFiltradosGeneral: List<Jugador> = emptyList(),
    val cargando: Boolean = false
)
