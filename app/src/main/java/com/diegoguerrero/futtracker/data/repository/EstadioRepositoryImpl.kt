package com.diegoguerrero.futtracker.data.repository

import com.diegoguerrero.futtracker.data.local.dao.EstadioDao
import com.diegoguerrero.futtracker.data.local.entity.toEntity
import com.diegoguerrero.futtracker.domain.model.Estadio
import com.diegoguerrero.futtracker.domain.repository.EstadioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EstadioRepositoryImpl @Inject constructor(
    private val estadioDao: EstadioDao
) : EstadioRepository {

    override fun obtenerEstadios(): Flow<List<Estadio>> {
        return estadioDao.obtenerEstadios().map { lista ->
            lista.map { it.toDomain() }
        }
    }

    override suspend fun obtenerEstadioPorId(id: Long): Estadio? {
        return estadioDao.obtenerEstadioPorId(id)?.toDomain()
    }

    override suspend fun insertarEstadio(estadio: Estadio): Long {
        return estadioDao.insertarEstadio(estadio.toEntity())
    }

    override suspend fun actualizarEstadio(estadio: Estadio) {
        estadioDao.actualizarEstadio(estadio.toEntity())
    }

    override suspend fun eliminarEstadio(estadio: Estadio) {
        estadioDao.eliminarEstadio(estadio.toEntity())
    }
}
