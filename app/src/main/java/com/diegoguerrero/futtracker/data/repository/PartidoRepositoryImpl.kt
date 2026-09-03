package com.diegoguerrero.futtracker.data.repository

import com.diegoguerrero.futtracker.data.local.dao.PartidoDao
import com.diegoguerrero.futtracker.data.local.entity.toEntity
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.domain.repository.PartidoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PartidoRepositoryImpl @Inject constructor(
    private val partidoDao: PartidoDao
) : PartidoRepository {

    override fun obtenerPartidos(): Flow<List<Partido>> {
        return partidoDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertarPartido(partido: Partido): Long {
        return partidoDao.insert(partido.toEntity())
    }

    override suspend fun actualizarPartido(partido: Partido) {
        partidoDao.update(partido.toEntity())
    }

    override suspend fun eliminarPartido(partido: Partido) {
        partidoDao.delete(partido.toEntity())
    }
}
