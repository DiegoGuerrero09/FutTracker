package com.diegoguerrero.futtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diegoguerrero.futtracker.domain.model.Perfil
import com.diegoguerrero.futtracker.domain.model.Posicion

@Entity(tableName = "perfil")
data class PerfilEntity(
    @PrimaryKey val id: Long = 1,
    val nombre: String,
    val fotoUri: String?,
    val posicionFavorita: String,
    val posiciones: String = "",
    val nivel: Int,
    val sincronizadoConJugadores: Boolean
) {
    fun toDomain(): Perfil {
        val posFav = runCatching { Posicion.valueOf(posicionFavorita) }.getOrDefault(Posicion.DC)
        val posSet = if (posiciones.isBlank()) {
            setOf(posFav)
        } else {
            posiciones.split(",")
                .mapNotNull { name -> runCatching { Posicion.valueOf(name.trim()) }.getOrNull() }
                .toSet().ifEmpty { setOf(posFav) }
        }

        return Perfil(
            id = id,
            nombre = nombre,
            fotoUri = fotoUri,
            posicionFavorita = posFav,
            posiciones = posSet,
            nivel = nivel,
            sincronizadoConJugadores = sincronizadoConJugadores
        )
    }
}

fun Perfil.toEntity(): PerfilEntity {
    val posFav = posiciones.firstOrNull() ?: posicionFavorita
    return PerfilEntity(
        id = id,
        nombre = nombre,
        fotoUri = fotoUri,
        posicionFavorita = posFav.name,
        posiciones = posiciones.joinToString(",") { it.name },
        nivel = nivel,
        sincronizadoConJugadores = sincronizadoConJugadores
    )
}
