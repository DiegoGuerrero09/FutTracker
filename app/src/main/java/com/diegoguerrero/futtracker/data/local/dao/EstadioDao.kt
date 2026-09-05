package com.diegoguerrero.futtracker.data.local.dao

import androidx.room.*
import com.diegoguerrero.futtracker.data.local.entity.EstadioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EstadioDao {
    @Query("SELECT * FROM estadios ORDER BY nombre ASC")
    fun obtenerEstadios(): Flow<List<EstadioEntity>>

    @Query("SELECT * FROM estadios WHERE id = :id")
    suspend fun obtenerEstadioPorId(id: Long): EstadioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEstadio(estadio: EstadioEntity): Long

    @Update
    suspend fun actualizarEstadio(estadio: EstadioEntity)

    @Delete
    suspend fun eliminarEstadio(estadio: EstadioEntity)
}
