package com.diegoguerrero.futtracker.data.local.dao

import androidx.room.*
import com.diegoguerrero.futtracker.data.local.entity.PartidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartidoDao {

    @Query("SELECT * FROM partidos ORDER BY fecha DESC")
    fun getAll(): Flow<List<PartidoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(partido: PartidoEntity): Long

    @Update
    suspend fun update(partido: PartidoEntity)

    @Delete
    suspend fun delete(partido: PartidoEntity)

    @Query("DELETE FROM partidos WHERE id = :id")
    suspend fun deleteById(id: Long)
}
