package com.diegoguerrero.futtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diegoguerrero.futtracker.data.local.dao.JugadorDao
import com.diegoguerrero.futtracker.data.local.entity.JugadorEntity

@Database(entities = [JugadorEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jugadorDao(): JugadorDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE jugadores ADD COLUMN esFavorito INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}