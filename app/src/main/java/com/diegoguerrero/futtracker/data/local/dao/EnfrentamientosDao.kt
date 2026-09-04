package com.diegoguerrero.futtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.diegoguerrero.futtracker.data.local.entity.JugadorEntity
import com.diegoguerrero.futtracker.data.local.entity.PartidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnfrentamientosDao {

    @Query("SELECT * FROM partidos ORDER BY fecha DESC")
    fun getAllPartidos(): Flow<List<PartidoEntity>>

    @Query("SELECT * FROM jugadores ORDER BY nombre ASC")
    fun getAllJugadores(): Flow<List<JugadorEntity>>

    @Query("SELECT * FROM partidos WHERE (jugadoresMiEquipo LIKE '%' || :jugadorId || '%' OR jugadoresEquipoRival LIKE '%' || :jugadorId || '%') ORDER BY fecha DESC")
    fun getPartidosConJugador(jugadorId: String): Flow<List<PartidoEntity>>
}
