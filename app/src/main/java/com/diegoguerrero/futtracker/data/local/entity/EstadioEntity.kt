package com.diegoguerrero.futtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diegoguerrero.futtracker.domain.model.Estadio
import com.diegoguerrero.futtracker.domain.model.TipoFutbol

@Entity(tableName = "estadios")
data class EstadioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val modalidades: String,
    val fotoUri: String? = null,
    val esFavorito: Boolean = false,
    val fechaCreacion: Long = 0
) {
    fun toDomain(): Estadio {
        val mods = if (modalidades.isBlank()) {
            setOf(TipoFutbol.FUTSAL)
        } else {
            modalidades.split(",")
                .mapNotNull { runCatching { TipoFutbol.valueOf(it.trim()) }.getOrNull() }
                .toSet()
                .ifEmpty { setOf(TipoFutbol.FUTSAL) }
        }
        return Estadio(
            id = id,
            nombre = nombre,
            modalidades = mods,
            fotoUri = fotoUri,
            esFavorito = esFavorito,
            fechaCreacion = if (fechaCreacion > 0) fechaCreacion else id * 1000
        )
    }
}

fun Estadio.toEntity(): EstadioEntity {
    return EstadioEntity(
        id = id,
        nombre = nombre,
        modalidades = modalidades.joinToString(",") { it.name },
        fotoUri = fotoUri,
        esFavorito = esFavorito,
        fechaCreacion = fechaCreacion
    )
}
