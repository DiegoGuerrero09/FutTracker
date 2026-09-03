// data/repository/JugadorRepositoryImpl.kt
package com.diegoguerrero.futtracker.data.repository

import com.diegoguerrero.futtracker.data.local.dao.JugadorDao
import com.diegoguerrero.futtracker.data.local.entity.toDomain
import com.diegoguerrero.futtracker.data.local.entity.toEntity
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.repository.JugadorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class JugadorRepositoryImpl(
    private val dao: JugadorDao
) : JugadorRepository {

    override fun getJugadores(): Flow<List<Jugador>> {
        return dao.getAllJugadores().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertJugador(jugador: Jugador) {
        dao.insertJugador(jugador.toEntity())
    }

    override suspend fun deleteJugador(id: Long) {
        dao.deleteJugadorById(id)
    }
}