package com.diegoguerrero.futtracker.domain.repository

import com.diegoguerrero.futtracker.domain.model.Jugador
import kotlinx.coroutines.flow.Flow

interface JugadorRepository {
    fun obtenerJugadores(): Flow<List<Jugador>>
    suspend fun insertarJugador(jugador: Jugador)
    suspend fun actualizarJugador(jugador: Jugador)
    suspend fun eliminarJugador(jugador: Jugador)
}