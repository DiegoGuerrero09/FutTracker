package com.diegoguerrero.futtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.diegoguerrero.futtracker.data.local.dao.JugadorDao
import com.diegoguerrero.futtracker.data.local.entity.JugadorEntity

@Database(entities = [JugadorEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jugadorDao(): JugadorDao
}