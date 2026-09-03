package com.diegoguerrero.futtracker.data.repository

import com.diegoguerrero.futtracker.data.local.dao.JugadorDao
import com.diegoguerrero.futtracker.data.local.entity.toEntity
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.repository.JugadorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class JugadorRepositoryImpl @Inject constructor(
    private val jugadorDao: JugadorDao
) : JugadorRepository {

    override fun obtenerJugadores(): Flow<List<Jugador>> {
        return jugadorDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertarJugador(jugador: Jugador) {
        jugadorDao.insert(jugador.toEntity())
    }

    override suspend fun actualizarJugador(jugador: Jugador) {
        jugadorDao.update(jugador.toEntity())
    }

    override suspend fun eliminarJugador(jugador: Jugador) {
        jugadorDao.deleteById(jugador.id)
    }
}