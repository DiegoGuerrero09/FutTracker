package com.diegoguerrero.futtracker.data.repository

import com.diegoguerrero.futtracker.data.local.dao.JugadorDao
import com.diegoguerrero.futtracker.data.local.entity.toEntity
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.repository.JugadorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class JugadorRepositoryImpl(
    private val jugadorDao: JugadorDao
) : JugadorRepository {

    override fun getJugadores(): Flow<List<Jugador>> {
        return jugadorDao.getAll().map { entidades ->
            entidades.map { it.toDomain() }
        }
    }

    override suspend fun insertJugador(jugador: Jugador) {
        jugadorDao.insert(jugador.toEntity())
    }

    override suspend fun deleteJugador(id: String) {
        jugadorDao.deleteById(id)
    }
}