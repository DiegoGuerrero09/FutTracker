package com.diegoguerrero.futtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diegoguerrero.futtracker.domain.model.ModoJuego

@Entity(tableName = "partidos")
data class PartidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fechaEpochDay: Long, // LocalDate.toEpochDay()
    val modo: ModoJuego,     // FUT_6 o FUT_7
    val golesFavor: Int,
    val golesRival: Int,
    val rival: String
)