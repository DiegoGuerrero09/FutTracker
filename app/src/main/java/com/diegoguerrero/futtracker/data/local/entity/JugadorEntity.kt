// data/local/entity/JugadorEntity.kt
package com.diegoguerrero.futtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion

@Entity(tableName = "jugadores")
data class JugadorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val fotoUrl: String? = null,
    val posPrincipal: Posicion,
    val posSecundaria: Posicion? = null,
    val posTercera: Posicion? = null
)

fun JugadorEntity.toDomain(): Jugador = Jugador(
    id = id,
    nombre = nombre,
    posPrincipal = posPrincipal,
    posSecundaria = posSecundaria,
    posTercera = posTercera
)

fun Jugador.toEntity(): JugadorEntity = JugadorEntity(
    id = id,
    nombre = nombre,
    posPrincipal = posPrincipal,
    posSecundaria = posSecundaria,
    posTercera = posTercera
)