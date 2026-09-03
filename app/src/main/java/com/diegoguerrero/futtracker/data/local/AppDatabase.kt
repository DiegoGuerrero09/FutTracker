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
    version = 5,
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE jugadores ADD COLUMN fotoUri TEXT")
                db.execSQL("ALTER TABLE partidos ADD COLUMN posicionesJugadas TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE partidos ADD COLUMN jugadoresMiEquipo TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE partidos ADD COLUMN jugadoresEquipoRival TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE perfil ADD COLUMN posiciones TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE partidos ADD COLUMN golesZurda INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE partidos ADD COLUMN golesDiestra INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE partidos ADD COLUMN golesCabeza INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE partidos ADD COLUMN golesOtro INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE partidos ADD COLUMN golesChilena INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE partidos ADD COLUMN golesTacon INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
