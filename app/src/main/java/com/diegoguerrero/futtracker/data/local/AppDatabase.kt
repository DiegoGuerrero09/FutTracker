package com.diegoguerrero.futtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diegoguerrero.futtracker.data.local.dao.JugadorDao
import com.diegoguerrero.futtracker.data.local.dao.PartidoDao
import com.diegoguerrero.futtracker.data.local.dao.PerfilDao
import com.diegoguerrero.futtracker.data.local.entity.JugadorEntity
import com.diegoguerrero.futtracker.data.local.entity.PartidoEntity
import com.diegoguerrero.futtracker.data.local.entity.PerfilEntity

@Database(
    entities = [JugadorEntity::class, PartidoEntity::class, PerfilEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jugadorDao(): JugadorDao
    abstract fun partidoDao(): PartidoDao
    abstract fun perfilDao(): PerfilDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE jugadores ADD COLUMN esFavorito INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE jugadores ADD COLUMN nivel INTEGER NOT NULL DEFAULT 3")
                db.execSQL("ALTER TABLE jugadores ADD COLUMN esUsuarioPropio INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE TABLE IF NOT EXISTS partidos (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, fecha INTEGER NOT NULL, modoJuego TEXT NOT NULL, golesAFavor INTEGER NOT NULL, golesEnContra INTEGER NOT NULL, posicionJugada TEXT NOT NULL, goles INTEGER NOT NULL, asistencias INTEGER NOT NULL, notas TEXT NOT NULL, jugadoresIds TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS perfil (id INTEGER PRIMARY KEY NOT NULL, nombre TEXT NOT NULL, fotoUri TEXT, posicionFavorita TEXT NOT NULL, nivel INTEGER NOT NULL, sincronizadoConJugadores INTEGER NOT NULL)")
            }
        }
    }
}
