package com.diegoguerrero.futtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diegoguerrero.futtracker.domain.model.TipoPosicion

@Entity(tableName = "jugadores")
data class JugadorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val fotoUrl: String?,
    val posPrincipal: TipoPosicion,
    val posSecundaria: TipoPosicion?,
    val posTercera: TipoPosicion?
)