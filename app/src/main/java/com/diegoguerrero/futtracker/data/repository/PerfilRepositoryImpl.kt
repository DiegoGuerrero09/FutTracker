package com.diegoguerrero.futtracker.data.repository

import com.diegoguerrero.futtracker.data.local.dao.PerfilDao
import com.diegoguerrero.futtracker.data.local.entity.toEntity
import com.diegoguerrero.futtracker.domain.model.Perfil
import com.diegoguerrero.futtracker.domain.repository.PerfilRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PerfilRepositoryImpl @Inject constructor(
    private val perfilDao: PerfilDao
) : PerfilRepository {

    override fun obtenerPerfil(): Flow<Perfil?> {
        return perfilDao.getPerfil().map { it?.toDomain() }
    }

    override suspend fun guardarPerfil(perfil: Perfil) {
        perfilDao.savePerfil(perfil.toEntity())
    }
}
