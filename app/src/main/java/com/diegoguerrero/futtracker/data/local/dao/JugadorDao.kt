// data/local/dao/JugadorDao.kt
package com.diegoguerrero.futtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diegoguerrero.futtracker.data.local.entity.JugadorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JugadorDao {
    @Query("SELECT * FROM jugadores")
    fun getAllJugadores(): Flow<List<JugadorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJugador(jugador: JugadorEntity)

    @Query("DELETE FROM jugadores WHERE id = :id")
    suspend fun deleteJugadorById(id: Long)
}