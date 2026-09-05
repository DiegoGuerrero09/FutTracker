package com.diegoguerrero.futtracker.di

import android.content.Context
import androidx.room.Room
import com.diegoguerrero.futtracker.data.local.AppDatabase
import com.diegoguerrero.futtracker.data.local.dao.JugadorDao
import com.diegoguerrero.futtracker.data.local.dao.PartidoDao
import com.diegoguerrero.futtracker.data.local.dao.PerfilDao
import com.diegoguerrero.futtracker.data.repository.JugadorRepositoryImpl
import com.diegoguerrero.futtracker.data.repository.PartidoRepositoryImpl
import com.diegoguerrero.futtracker.data.repository.PerfilRepositoryImpl
import com.diegoguerrero.futtracker.domain.repository.JugadorRepository
import com.diegoguerrero.futtracker.domain.repository.PartidoRepository
import com.diegoguerrero.futtracker.domain.repository.PerfilRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "futtracker_db"
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideJugadorDao(database: AppDatabase): JugadorDao {
        return database.jugadorDao()
    }

    @Provides
    @Singleton
    fun provideJugadorRepository(jugadorDao: JugadorDao): JugadorRepository {
        return JugadorRepositoryImpl(jugadorDao)
    }

    @Provides
    @Singleton
    fun providePartidoDao(database: AppDatabase): PartidoDao {
        return database.partidoDao()
    }

    @Provides
    @Singleton
    fun providePartidoRepository(partidoDao: PartidoDao): PartidoRepository {
        return PartidoRepositoryImpl(partidoDao)
    }

    @Provides
    @Singleton
    fun providePerfilDao(database: AppDatabase): PerfilDao {
        return database.perfilDao()
    }

    @Provides
    @Singleton
    fun providePerfilRepository(perfilDao: PerfilDao): PerfilRepository {
        return PerfilRepositoryImpl(perfilDao)
    }

    @Provides
    @Singleton
    fun provideEnfrentamientosDao(database: AppDatabase): com.diegoguerrero.futtracker.data.local.dao.EnfrentamientosDao {
        return database.enfrentamientosDao()
    }

    @Provides
    @Singleton
    fun provideEnfrentamientosRepository(
        enfrentamientosDao: com.diegoguerrero.futtracker.data.local.dao.EnfrentamientosDao
    ): com.diegoguerrero.futtracker.domain.repository.EnfrentamientosRepository {
        return com.diegoguerrero.futtracker.data.repository.EnfrentamientosRepositoryImpl(enfrentamientosDao)
    }
}
