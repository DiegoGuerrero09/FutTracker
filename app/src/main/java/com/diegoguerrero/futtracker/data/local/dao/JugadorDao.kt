package com.diegoguerrero.futtracker.data.local.dao

import androidx.room.*
import com.diegoguerrero.futtracker.data.local.entity.JugadorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JugadorDao {

    @Query("SELECT * FROM jugadores ORDER BY esFavorito DESC, nombre ASC")
    fun getAll(): Flow<List<JugadorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jugador: JugadorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jugadores: List<JugadorEntity>)

    @Update
    suspend fun update(jugador: JugadorEntity)

    @Query("DELETE FROM jugadores WHERE id = :id")
    suspend fun deleteById(id: String)
}