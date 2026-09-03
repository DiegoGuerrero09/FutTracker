package com.diegoguerrero.futtracker.di

import android.content.Context
import androidx.room.Room
import com.diegoguerrero.futtracker.data.local.AppDatabase
import com.diegoguerrero.futtracker.data.local.dao.JugadorDao
import com.diegoguerrero.futtracker.data.repository.JugadorRepositoryImpl
import com.diegoguerrero.futtracker.domain.repository.JugadorRepository
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
            .addMigrations(AppDatabase.MIGRATION_1_2)
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
}