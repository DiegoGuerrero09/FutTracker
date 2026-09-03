package com.diegoguerrero.futtracker.domain.repository

import com.diegoguerrero.futtracker.domain.model.Perfil
import kotlinx.coroutines.flow.Flow

interface PerfilRepository {
    fun obtenerPerfil(): Flow<Perfil?>
    suspend fun guardarPerfil(perfil: Perfil)
}
