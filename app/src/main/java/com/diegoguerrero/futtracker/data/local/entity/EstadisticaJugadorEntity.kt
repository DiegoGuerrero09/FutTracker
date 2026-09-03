package com.diegoguerrero.futtracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "estadisticas_jugador",
    foreignKeys = [
        ForeignKey(
            entity = PartidoEntity::class,
            parentColumns = ["id"],
            childColumns = ["partidoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = JugadorEntity::class,
            parentColumns = ["id"],
            childColumns = ["jugadorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
  data class EstadisticaJugadorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partidoId: Long,
    val jugadorId: Long,
    val goles: Int,
    val asistencias: Int
)