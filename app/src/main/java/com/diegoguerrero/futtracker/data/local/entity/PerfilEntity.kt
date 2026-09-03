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
    val nivel: Int,
    val sincronizadoConJugadores: Boolean
) {
    fun toDomain(): Perfil {
        return Perfil(
            id = id,
            nombre = nombre,
            fotoUri = fotoUri,
            posicionFavorita = runCatching { Posicion.valueOf(posicionFavorita) }.getOrDefault(Posicion.DC),
            nivel = nivel,
            sincronizadoConJugadores = sincronizadoConJugadores
        )
    }
}

fun Perfil.toEntity(): PerfilEntity {
    return PerfilEntity(
        id = id,
        nombre = nombre,
        fotoUri = fotoUri,
        posicionFavorita = posicionFavorita.name,
        nivel = nivel,
        sincronizadoConJugadores = sincronizadoConJugadores
    )
}
