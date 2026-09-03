package com.diegoguerrero.futtracker.domain.repository

import com.diegoguerrero.futtracker.domain.model.Jugador
import kotlinx.coroutines.flow.Flow

interface JugadorRepository {
    fun getJugadores(): Flow<List<Jugador>>
    suspend fun insertJugador(jugador: Jugador)
    suspend fun deleteJugador(id: String)
}