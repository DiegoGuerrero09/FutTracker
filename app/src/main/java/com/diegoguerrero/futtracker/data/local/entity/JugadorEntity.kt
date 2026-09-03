package com.diegoguerrero.futtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion

@Entity(tableName = "jugadores")
data class JugadorEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val posicionesPrimarias: String,
    val posicionesSecundarias: String
) {
    fun toDomain(): Jugador {
        return Jugador(
            id = id,
            nombre = nombre,
            posicionesPrimarias = posicionesPrimarias.toPosicionSet(),
            posicionesSecundarias = posicionesSecundarias.toPosicionSet()
        )
    }
}

fun Jugador.toEntity(): JugadorEntity {
    return JugadorEntity(
        id = id,
        nombre = nombre,
        posicionesPrimarias = posicionesPrimarias.joinToString(",") { it.name },
        posicionesSecundarias = posicionesSecundarias.joinToString(",") { it.name }
    )
}

private fun String.toPosicionSet(): Set<Posicion> {
    if (this.isBlank()) return emptySet()
    return this.split(",")
        .mapNotNull { name -> runCatching { Posicion.valueOf(name) }.getOrNull() }
        .toSet()
}