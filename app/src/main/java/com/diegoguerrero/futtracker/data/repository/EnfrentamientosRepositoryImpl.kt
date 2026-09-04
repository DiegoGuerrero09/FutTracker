package com.diegoguerrero.futtracker.data.repository

import com.diegoguerrero.futtracker.data.local.dao.EnfrentamientosDao
import com.diegoguerrero.futtracker.domain.model.ComparativaCaraACara
import com.diegoguerrero.futtracker.domain.model.DestacadosEnfrentamientos
import com.diegoguerrero.futtracker.domain.model.DuoEstadisticas
import com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorCruzadas
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.domain.repository.EnfrentamientosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EnfrentamientosRepositoryImpl @Inject constructor(
    private val enfrentamientosDao: EnfrentamientosDao
) : EnfrentamientosRepository {

    override fun obtenerHistorialCruzado(): Flow<List<EstadisticasJugadorCruzadas>> {
        return combine(
            enfrentamientosDao.getAllPartidos(),
            enfrentamientosDao.getAllJugadores()
        ) { partidosEntities, jugadoresEntities ->
            val partidos = partidosEntities.map { it.toDomain() }
            val jugadores = jugadoresEntities.map { it.toDomain() }

            val listaSinUsuario = jugadores.filter { !it.esUsuarioPropio && it.id != "usuario_propio_id" }

            listaSinUsuario.map { j ->
                var partComp = 0
                var vicComp = 0
                var empComp = 0
                var derComp = 0
                var gfComp = 0
                var gcComp = 0
                var gmComp = 0

                var partRiv = 0
                var vicRiv = 0
                var empRiv = 0
                var derRiv = 0
                var gfRiv = 0
                var gcRiv = 0
                var gmRiv = 0

                for (p in partidos) {
                    val enMiEquipo = p.jugadoresMiEquipo.contains(j.id)
                    val enRival = p.jugadoresEquipoRival.contains(j.id)

                    if (enMiEquipo) {
                        partComp++
                        if (p.esVictoria) vicComp++
                        else if (p.esEmpate) empComp++
                        else if (p.esDerrota) derComp++
                        gfComp += p.golesAFavor
                        gcComp += p.golesEnContra
                        gmComp += p.goles
                    }

                    if (enRival) {
                        partRiv++
                        // Si p.esVictoria significa que el usuario ganó contra este rival
                        if (p.esVictoria) vicRiv++
                        else if (p.esEmpate) empRiv++
                        else if (p.esDerrota) derRiv++
                        gfRiv += p.golesAFavor
                        gcRiv += p.golesEnContra
                        gmRiv += p.goles
                    }
                }

                EstadisticasJugadorCruzadas(
                    jugador = j,
                    partidosComoCompanero = partComp,
                    victoriasComoCompanero = vicComp,
                    empatesComoCompanero = empComp,
                    derrotasComoCompanero = derComp,
                    golesFavorComoCompanero = gfComp,
                    golesContraComoCompanero = gcComp,
                    golesMarcadosComoCompanero = gmComp,
                    partidosComoRival = partRiv,
                    victoriasComoRival = vicRiv,
                    empatesComoRival = empRiv,
                    derrotasComoRival = derRiv,
                    golesFavorComoRival = gfRiv,
                    golesContraComoRival = gcRiv,
                    golesMarcadosComoRival = gmRiv
                )
            }.sortedWith(compareByDescending<EstadisticasJugadorCruzadas> { it.totalPartidos }.thenBy { it.jugador.nombre.lowercase() })
        }
    }

    override fun obtenerDestacados(): Flow<DestacadosEnfrentamientos> {
        return obtenerHistorialCruzado().map { lista ->
            val conPartidosComp = lista.filter { it.partidosComoCompanero > 0 }
            val conPartidosRiv = lista.filter { it.partidosComoRival > 0 }

            val compMasGana = conPartidosComp
                .filter { it.victoriasComoCompanero > 0 }
                .maxWithOrNull(
                    compareBy<EstadisticasJugadorCruzadas> { it.victoriasComoCompanero }
                        .thenBy { it.porcentajeVictoriasCompanero }
                )

            val compMasPierde = conPartidosComp
                .filter { it.derrotasComoCompanero > 0 }
                .maxWithOrNull(
                    compareBy<EstadisticasJugadorCruzadas> { it.derrotasComoCompanero }
                        .thenByDescending { 100f - it.porcentajeVictoriasCompanero }
                )

            val rivMasGana = conPartidosRiv
                .filter { it.victoriasComoRival > 0 }
                .maxWithOrNull(
                    compareBy<EstadisticasJugadorCruzadas> { it.victoriasComoRival }
                        .thenBy { it.porcentajeVictoriasRival }
                )

            val rivMasPierde = conPartidosRiv
                .filter { it.derrotasComoRival > 0 }
                .maxWithOrNull(
                    compareBy<EstadisticasJugadorCruzadas> { it.derrotasComoRival }
                        .thenByDescending { 100f - it.porcentajeVictoriasRival }
                )

            DestacadosEnfrentamientos(
                companeroMasGana = compMasGana,
                companeroMasPierde = compMasPierde,
                rivalMasGana = rivMasGana,
                rivalMasPierde = rivMasPierde
            )
        }
    }

    override fun obtenerComparativa(
        jugadorIdA: String,
        jugadorIdB: String
    ): Flow<ComparativaCaraACara?> {
        return combine(
            enfrentamientosDao.getAllPartidos(),
            enfrentamientosDao.getAllJugadores()
        ) { partidosEntities, jugadoresEntities ->
            val jugadoresMap = jugadoresEntities.map { it.toDomain() }.associateBy { it.id }
            val jugadorA = jugadoresMap[jugadorIdA] ?: return@combine null
            val jugadorB = jugadoresMap[jugadorIdB] ?: return@combine null

            val partidos = partidosEntities.map { it.toDomain() }

            var partEnfrentados = 0
            var vicA = 0
            var vicB = 0
            var empates = 0
            var golesA = 0
            var golesB = 0

            var partJuntos = 0
            var vicJuntos = 0
            var empJuntos = 0
            var derJuntos = 0

            val historial = mutableListOf<Partido>()

            for (p in partidos) {
                val aEnMiEquipo = p.jugadoresMiEquipo.contains(jugadorIdA) || (jugadorA.esUsuarioPropio && p.jugadoresMiEquipo.contains("usuario_propio_id"))
                val aEnRival = p.jugadoresEquipoRival.contains(jugadorIdA) || (jugadorA.esUsuarioPropio && p.jugadoresEquipoRival.contains("usuario_propio_id"))
                val bEnMiEquipo = p.jugadoresMiEquipo.contains(jugadorIdB) || (jugadorB.esUsuarioPropio && p.jugadoresMiEquipo.contains("usuario_propio_id"))
                val bEnRival = p.jugadoresEquipoRival.contains(jugadorIdB) || (jugadorB.esUsuarioPropio && p.jugadoresEquipoRival.contains("usuario_propio_id"))

                // Enfrentados
                if (aEnMiEquipo && bEnRival) {
                    partEnfrentados++
                    golesA += p.golesAFavor
                    golesB += p.golesEnContra
                    if (p.esVictoria) vicA++
                    else if (p.esDerrota) vicB++
                    else empates++
                    historial.add(p)
                } else if (bEnMiEquipo && aEnRival) {
                    partEnfrentados++
                    golesB += p.golesAFavor
                    golesA += p.golesEnContra
                    if (p.esVictoria) vicB++
                    else if (p.esDerrota) vicA++
                    else empates++
                    historial.add(p)
                }

                // Juntos en el mismo equipo
                if (aEnMiEquipo && bEnMiEquipo) {
                    partJuntos++
                    if (p.esVictoria) vicJuntos++
                    else if (p.esEmpate) empJuntos++
                    else derJuntos++
                } else if (aEnRival && bEnRival) {
                    partJuntos++
                    if (p.esDerrota) vicJuntos++
                    else if (p.esEmpate) empJuntos++
                    else derJuntos++
                }
            }

            ComparativaCaraACara(
                jugadorA = jugadorA,
                jugadorB = jugadorB,
                partidosEnfrentados = partEnfrentados,
                victoriasA = vicA,
                victoriasB = vicB,
                empates = empates,
                golesEquipoA = golesA,
                golesEquipoB = golesB,
                partidosJuntos = partJuntos,
                victoriasJuntos = vicJuntos,
                empatesJuntos = empJuntos,
                derrotasJuntos = derJuntos,
                partidosHistorial = historial
            )
        }
    }

    override fun obtenerDuos(): Flow<List<DuoEstadisticas>> {
        return combine(
            enfrentamientosDao.getAllPartidos(),
            enfrentamientosDao.getAllJugadores()
        ) { partidosEntities, jugadoresEntities ->
            val jugadoresList = jugadoresEntities.map { it.toDomain() }
            val usuarioPropio = jugadoresList.firstOrNull { it.esUsuarioPropio || it.id == "usuario_propio_id" }
            val jugadoresMap = jugadoresList.associateBy { it.id }.toMutableMap()
            if (usuarioPropio != null) {
                jugadoresMap["usuario_propio_id"] = usuarioPropio
            }
            val partidos = partidosEntities.map { it.toDomain() }

            class DuoAccumulator(
                var partidos: Int = 0,
                var victorias: Int = 0,
                var empates: Int = 0,
                var derrotas: Int = 0,
                var golesFavor: Int = 0,
                var golesContra: Int = 0
            )

            val duosMap = mutableMapOf<Pair<String, String>, DuoAccumulator>()

            fun procesarEquipo(jugadoresIds: List<String>, esVictoria: Boolean, esEmpate: Boolean, esDerrota: Boolean, gf: Int, gc: Int) {
                val normalizedIds = if (usuarioPropio != null) {
                    jugadoresIds.map { if (it == "usuario_propio_id") usuarioPropio.id else it }
                } else jugadoresIds
                val ids = normalizedIds.distinct().sorted()
                for (i in ids.indices) {
                    for (j in i + 1 until ids.size) {
                        val key = Pair(ids[i], ids[j])
                        val acc = duosMap.getOrPut(key) { DuoAccumulator() }
                        acc.partidos++
                        if (esVictoria) acc.victorias++
                        else if (esEmpate) acc.empates++
                        else if (esDerrota) acc.derrotas++
                        acc.golesFavor += gf
                        acc.golesContra += gc
                    }
                }
            }

            for (p in partidos) {
                // Mi equipo
                procesarEquipo(
                    jugadoresIds = p.jugadoresMiEquipo,
                    esVictoria = p.esVictoria,
                    esEmpate = p.esEmpate,
                    esDerrota = p.esDerrota,
                    gf = p.golesAFavor,
                    gc = p.golesEnContra
                )
                // Equipo rival
                procesarEquipo(
                    jugadoresIds = p.jugadoresEquipoRival,
                    esVictoria = p.esDerrota,
                    esEmpate = p.esEmpate,
                    esDerrota = p.esVictoria,
                    gf = p.golesEnContra,
                    gc = p.golesAFavor
                )
            }

            duosMap.mapNotNull { (key, acc) ->
                val j1 = jugadoresMap[key.first]
                val j2 = jugadoresMap[key.second]
                if (j1 != null && j2 != null && acc.partidos > 0) {
                    DuoEstadisticas(
                        jugador1 = j1,
                        jugador2 = j2,
                        partidosJuntos = acc.partidos,
                        victorias = acc.victorias,
                        empates = acc.empates,
                        derrotas = acc.derrotas,
                        golesFavor = acc.golesFavor,
                        golesContra = acc.golesContra
                    )
                } else null
            }.sortedWith(
                compareByDescending<DuoEstadisticas> { it.porcentajeVictorias }
                    .thenByDescending { it.partidosJuntos }
            )
        }
    }
}
