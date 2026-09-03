package com.diegoguerrero.futtracker.data.local.dao

import androidx.room.*
import com.diegoguerrero.futtracker.data.local.entity.PerfilEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilDao {

    @Query("SELECT * FROM perfil WHERE id = 1 LIMIT 1")
    fun getPerfil(): Flow<PerfilEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePerfil(perfil: PerfilEntity)
}
