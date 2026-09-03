package com.diegoguerrero.futtracker.data.local.dao

import androidx.room.*
import com.diegoguerrero.futtracker.data.local.entity.JugadorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JugadorDao {
    @Query("SELECT * FROM jugadores ORDER BY nombre ASC")
    fun getTodosLosJugadores(): Flow<List<JugadorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarJugador(jugador: JugadorEntity)

    @Delete
    suspend fun eliminarJugador(jugador: JugadorEntity)
}