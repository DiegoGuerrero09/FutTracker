package com.diegoguerrero.futtracker.domain.repository

import com.diegoguerrero.futtracker.domain.model.Estadio
import kotlinx.coroutines.flow.Flow

interface EstadioRepository {
    fun obtenerEstadios(): Flow<List<Estadio>>
    suspend fun obtenerEstadioPorId(id: Long): Estadio?
    suspend fun insertarEstadio(estadio: Estadio): Long
    suspend fun actualizarEstadio(estadio: Estadio)
    suspend fun eliminarEstadio(estadio: Estadio)
}
