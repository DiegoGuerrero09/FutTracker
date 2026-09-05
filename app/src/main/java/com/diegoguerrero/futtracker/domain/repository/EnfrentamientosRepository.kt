package com.diegoguerrero.futtracker.domain.repository

import com.diegoguerrero.futtracker.domain.model.ComparativaCaraACara
import com.diegoguerrero.futtracker.domain.model.DestacadosEnfrentamientos
import com.diegoguerrero.futtracker.domain.model.DuoEstadisticas
import com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorCruzadas
import kotlinx.coroutines.flow.Flow

interface EnfrentamientosRepository {
    fun obtenerHistorialCruzado(): Flow<List<EstadisticasJugadorCruzadas>>
    fun obtenerDestacados(): Flow<DestacadosEnfrentamientos>
    fun obtenerHistorialCruzadoParaJugador(jugadorId: String): Flow<List<EstadisticasJugadorCruzadas>>
    fun obtenerDestacadosParaJugador(jugadorId: String): Flow<DestacadosEnfrentamientos>
    fun obtenerComparativa(jugadorIdA: String, jugadorIdB: String): Flow<ComparativaCaraACara?>
    fun obtenerDuos(): Flow<List<DuoEstadisticas>>
}
