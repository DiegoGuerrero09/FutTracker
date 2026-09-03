package com.diegoguerrero.futtracker.domain.repository

import com.diegoguerrero.futtracker.domain.model.Partido
import kotlinx.coroutines.flow.Flow

interface PartidoRepository {
    fun obtenerPartidos(): Flow<List<Partido>>
    suspend fun insertarPartido(partido: Partido): Long
    suspend fun actualizarPartido(partido: Partido)
    suspend fun eliminarPartido(partido: Partido)
}
