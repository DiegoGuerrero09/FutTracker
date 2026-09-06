package com.diegoguerrero.futtracker.data.repository

import com.diegoguerrero.futtracker.data.local.dao.EnfrentamientosDao
import com.diegoguerrero.futtracker.domain.model.ComparativaCaraACara
import com.diegoguerrero.futtracker.domain.model.DestacadosEnfrentamientos
import com.diegoguerrero.futtracker.domain.model.DuoEstadisticas
import com.diegoguerrero.futtracker.domain.model.EstadisticasJugadorCruzadas
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Partido
import com.diegoguerrero.futtracker.domain.repository.EnfrentamientosRepository
import com.diegoguerrero.futtracker.domain.model.Posicion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EnfrentamientosRepositoryImpl @Inject constructor(
    private val enfrentamientosDao: EnfrentamientosDao
) : EnfrentamientosRepository {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun obtenerHistorialCruzado(fechaInicio: Long?, fechaFin: Long?): Flow<List<EstadisticasJugadorCruzadas>> {
        return enfrentamientosDao.getAllJugadores().flatMapLatest { jugadoresEntities ->
            val jugadores = jugadoresEntities.map { it.toDomain() }
            val usuario = jugadores.firstOrNull { it.esUsuarioPropio || it.id == "usuario_propio_id" }
            val userId = usuario?.id ?: "usuario_propio_id"
            obtenerHistorialCruzadoParaJugador(userId, fechaInicio, fechaFin)
        }
    }

    override fun obtenerDestacados(fechaInicio: Long?, fechaFin: Long?): Flow<DestacadosEnfrentamientos> {
        return obtenerHistorialCruzado(fechaInicio, fechaFin).map { lista ->
            calcularDestacados(lista)
        }
    }

    override fun obtenerHistorialCruzadoParaJugador(jugadorId: String, fechaInicio: Long?, fechaFin: Long?): Flow<List<EstadisticasJugadorCruzadas>> {
        return combine(
            enfrentamientosDao.getAllPartidos(),
            enfrentamientosDao.getAllJugadores()
        ) { partidosEntities, jugadoresEntities ->
            val partidos = partidosEntities.map { it.toDomain() }.filter { p ->
                (fechaInicio == null || p.fecha >= fechaInicio) && (fechaFin == null || p.fecha <= fechaFin)
            }
            val jugadores = jugadoresEntities.map { it.toDomain() }
            val targetJugador = jugadores.firstOrNull { it.id == jugadorId || (it.esUsuarioPropio && jugadorId == "usuario_propio_id") } ?: return@combine emptyList()

            val listaSinTarget = jugadores.filter { it.id != targetJugador.id }

            listaSinTarget.map { j ->
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

                var golesJ = 0
                var asistenciasJ = 0
                var tirosAlPaloJ = 0
                var fueraAreaJ = 0
                var chilenaJ = 0
                var taconJ = 0
                var golesEncajadosJ = 0
                var paradasJ = 0
                var haJugadoPorteroJ = false

                var golesJComp = 0
                var asistenciasJComp = 0
                var tirosAlPaloJComp = 0
                var fueraAreaJComp = 0
                var chilenaJComp = 0
                var taconJComp = 0
                var golesEncajadosJComp = 0
                var paradasJComp = 0
                var haJugadoPorteroJComp = false

                var golesJRiv = 0
                var asistenciasJRiv = 0
                var tirosAlPaloJRiv = 0
                var fueraAreaJRiv = 0
                var chilenaJRiv = 0
                var taconJRiv = 0
                var golesEncajadosJRiv = 0
                var paradasJRiv = 0
                var haJugadoPorteroJRiv = false

                var golesTarget = 0
                var asistenciasTarget = 0
                var tirosAlPaloTarget = 0
                var fueraAreaTarget = 0
                var chilenaTarget = 0
                var taconTarget = 0
                var golesEncajadosTarget = 0
                var paradasTarget = 0
                var haJugadoPorteroTarget = false

                var golesTargetComp = 0
                var asistenciasTargetComp = 0
                var tirosAlPaloTargetComp = 0
                var fueraAreaTargetComp = 0
                var chilenaTargetComp = 0
                var taconTargetComp = 0
                var golesEncajadosTargetComp = 0
                var paradasTargetComp = 0
                var haJugadoPorteroTargetComp = false

                var golesTargetRiv = 0
                var asistenciasTargetRiv = 0
                var tirosAlPaloTargetRiv = 0
                var fueraAreaTargetRiv = 0
                var chilenaTargetRiv = 0
                var taconTargetRiv = 0
                var golesEncajadosTargetRiv = 0
                var paradasTargetRiv = 0
                var haJugadoPorteroTargetRiv = false

                for (p in partidos) {
                    val targetEsUser = targetJugador.esUsuarioPropio || targetJugador.id == "usuario_propio_id"
                    val jEsUser = j.esUsuarioPropio || j.id == "usuario_propio_id"
                    if (!p.jugadoPorMi && (targetEsUser || jEsUser)) continue

                    val targetEnMiEquipo = p.jugadoresMiEquipo.contains(targetJugador.id) || (targetJugador.esUsuarioPropio && p.jugadoresMiEquipo.contains("usuario_propio_id"))
                    val targetEnRival = p.jugadoresEquipoRival.contains(targetJugador.id) || (targetJugador.esUsuarioPropio && p.jugadoresEquipoRival.contains("usuario_propio_id"))

                    val jEnMiEquipo = p.jugadoresMiEquipo.contains(j.id) || (j.esUsuarioPropio && p.jugadoresMiEquipo.contains("usuario_propio_id"))
                    val jEnRival = p.jugadoresEquipoRival.contains(j.id) || (j.esUsuarioPropio && p.jugadoresEquipoRival.contains("usuario_propio_id"))

                    val sonCompaneros = (targetEnMiEquipo && jEnMiEquipo) || (targetEnRival && jEnRival)
                    val sonRivales = (targetEnMiEquipo && jEnRival) || (targetEnRival && jEnMiEquipo)

                    if (!sonCompaneros && !sonRivales) continue

                    // Stats in match for target
                    val detTarget = p.jugadoresDetalle.firstOrNull { it.jugadorId == targetJugador.id || (targetJugador.esUsuarioPropio && it.jugadorId == "usuario_propio_id") }
                    val gTarget = detTarget?.goles ?: if (targetJugador.esUsuarioPropio && p.jugadoPorMi) p.goles else 0
                    val asisTarget = detTarget?.asistencias ?: if (targetJugador.esUsuarioPropio && p.jugadoPorMi) p.asistencias else 0
                    val paloTarget = detTarget?.tirosAlPalo ?: if (targetJugador.esUsuarioPropio && p.jugadoPorMi) p.tirosAlPalo else 0
                    val faTarget = detTarget?.golesFueraArea ?: if (targetJugador.esUsuarioPropio && p.jugadoPorMi) p.golesFueraArea else 0
                    val chilTarget = detTarget?.golesChilena ?: if (targetJugador.esUsuarioPropio && p.jugadoPorMi) p.golesChilena else 0
                    val tacTarget = detTarget?.golesTacon ?: if (targetJugador.esUsuarioPropio && p.jugadoPorMi) p.golesTacon else 0
                    val esPorteroTarget = detTarget?.let { it.posicionPrincipal == Posicion.POR || it.posicionesSecundarias.contains(Posicion.POR) }
                        ?: (targetJugador.esUsuarioPropio && p.jugadoPorMi && (p.posicionJugada == Posicion.POR || p.posicionesJugadas.contains(Posicion.POR) || p.posicionesSecundarias.contains(Posicion.POR)))
                    val parTarget = if (esPorteroTarget) (detTarget?.paradas ?: if (targetJugador.esUsuarioPropio && p.jugadoPorMi) p.paradas else 0) else 0

                    golesTarget += gTarget
                    asistenciasTarget += asisTarget
                    tirosAlPaloTarget += paloTarget
                    fueraAreaTarget += faTarget
                    chilenaTarget += chilTarget
                    taconTarget += tacTarget
                    paradasTarget += parTarget

                    val encTarget = if (esPorteroTarget) {
                        haJugadoPorteroTarget = true
                        val enc = if (targetEnMiEquipo) p.golesEnContra else p.golesAFavor
                        golesEncajadosTarget += enc
                        enc
                    } else 0

                    // Stats in match for j
                    val detJ = p.jugadoresDetalle.firstOrNull { it.jugadorId == j.id || (j.esUsuarioPropio && it.jugadorId == "usuario_propio_id") }
                    val gJ = detJ?.goles ?: if (j.esUsuarioPropio && p.jugadoPorMi) p.goles else 0
                    val asisJ = detJ?.asistencias ?: if (j.esUsuarioPropio && p.jugadoPorMi) p.asistencias else 0
                    val paloJ = detJ?.tirosAlPalo ?: if (j.esUsuarioPropio && p.jugadoPorMi) p.tirosAlPalo else 0
                    val faJ = detJ?.golesFueraArea ?: if (j.esUsuarioPropio && p.jugadoPorMi) p.golesFueraArea else 0
                    val chilJ = detJ?.golesChilena ?: if (j.esUsuarioPropio && p.jugadoPorMi) p.golesChilena else 0
                    val tacJ = detJ?.golesTacon ?: if (j.esUsuarioPropio && p.jugadoPorMi) p.golesTacon else 0

                    val esPorteroJ = detJ?.let { it.posicionPrincipal == Posicion.POR || it.posicionesSecundarias.contains(Posicion.POR) }
                        ?: (j.esUsuarioPropio && p.jugadoPorMi && (p.posicionJugada == Posicion.POR || p.posicionesJugadas.contains(Posicion.POR) || p.posicionesSecundarias.contains(Posicion.POR)))
                    val parJ = if (esPorteroJ) (detJ?.paradas ?: if (j.esUsuarioPropio && p.jugadoPorMi) p.paradas else 0) else 0

                    golesJ += gJ
                    asistenciasJ += asisJ
                    tirosAlPaloJ += paloJ
                    fueraAreaJ += faJ
                    chilenaJ += chilJ
                    taconJ += tacJ
                    paradasJ += parJ

                    val encJ = if (esPorteroJ) {
                        haJugadoPorteroJ = true
                        val enc = if (jEnMiEquipo) p.golesEnContra else p.golesAFavor
                        golesEncajadosJ += enc
                        enc
                    } else 0

                    if (sonCompaneros) {
                        golesTargetComp += gTarget
                        asistenciasTargetComp += asisTarget
                        tirosAlPaloTargetComp += paloTarget
                        fueraAreaTargetComp += faTarget
                        chilenaTargetComp += chilTarget
                        taconTargetComp += tacTarget
                        paradasTargetComp += parTarget
                        if (esPorteroTarget) {
                            haJugadoPorteroTargetComp = true
                            golesEncajadosTargetComp += encTarget
                        }

                        golesJComp += gJ
                        asistenciasJComp += asisJ
                        tirosAlPaloJComp += paloJ
                        fueraAreaJComp += faJ
                        chilenaJComp += chilJ
                        taconJComp += tacJ
                        paradasJComp += parJ
                        if (esPorteroJ) {
                            haJugadoPorteroJComp = true
                            golesEncajadosJComp += encJ
                        }
                    } else if (sonRivales) {
                        golesTargetRiv += gTarget
                        asistenciasTargetRiv += asisTarget
                        tirosAlPaloTargetRiv += paloTarget
                        fueraAreaTargetRiv += faTarget
                        chilenaTargetRiv += chilTarget
                        taconTargetRiv += tacTarget
                        paradasTargetRiv += parTarget
                        if (esPorteroTarget) {
                            haJugadoPorteroTargetRiv = true
                            golesEncajadosTargetRiv += encTarget
                        }

                        golesJRiv += gJ
                        asistenciasJRiv += asisJ
                        tirosAlPaloJRiv += paloJ
                        fueraAreaJRiv += faJ
                        chilenaJRiv += chilJ
                        taconJRiv += tacJ
                        paradasJRiv += parJ
                        if (esPorteroJ) {
                            haJugadoPorteroJRiv = true
                            golesEncajadosJRiv += encJ
                        }
                    }

                    if (targetEnMiEquipo) {
                        if (jEnMiEquipo) {
                            partComp++
                            if (p.esVictoria) vicComp++
                            else if (p.esEmpate) empComp++
                            else if (p.esDerrota) derComp++
                            gfComp += p.golesAFavor
                            gcComp += p.golesEnContra
                            gmComp += gTarget
                        }
                        if (jEnRival) {
                            partRiv++
                            if (p.esVictoria) vicRiv++
                            else if (p.esEmpate) empRiv++
                            else if (p.esDerrota) derRiv++
                            gfRiv += p.golesAFavor
                            gcRiv += p.golesEnContra
                            gmRiv += gTarget
                        }
                    } else if (targetEnRival) {
                        if (jEnRival) {
                            partComp++
                            if (p.esDerrota) vicComp++
                            else if (p.esEmpate) empComp++
                            else if (p.esVictoria) derComp++
                            gfComp += p.golesEnContra
                            gcComp += p.golesAFavor
                            gmComp += gTarget
                        }
                        if (jEnMiEquipo) {
                            partRiv++
                            if (p.esDerrota) vicRiv++
                            else if (p.esEmpate) empRiv++
                            else if (p.esVictoria) derRiv++
                            gfRiv += p.golesEnContra
                            gcRiv += p.golesAFavor
                            gmRiv += gTarget
                        }
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
                    golesMarcadosComoRival = gmRiv,
                    goles = golesJ,
                    asistencias = asistenciasJ,
                    tirosAlPalo = tirosAlPaloJ,
                    fueraArea = fueraAreaJ,
                    chilena = chilenaJ,
                    tacon = taconJ,
                    golesEncajados = golesEncajadosJ,
                    paradas = paradasJ,
                    haJugadoPortero = haJugadoPorteroJ,
                    golesComoCompaneroInd = golesJComp,
                    asistenciasComoCompaneroInd = asistenciasJComp,
                    tirosAlPaloComoCompaneroInd = tirosAlPaloJComp,
                    fueraAreaComoCompaneroInd = fueraAreaJComp,
                    chilenaComoCompaneroInd = chilenaJComp,
                    taconComoCompaneroInd = taconJComp,
                    golesEncajadosComoCompaneroInd = golesEncajadosJComp,
                    paradasComoCompaneroInd = paradasJComp,
                    haJugadoPorteroComoCompaneroInd = haJugadoPorteroJComp,
                    golesComoRivalInd = golesJRiv,
                    asistenciasComoRivalInd = asistenciasJRiv,
                    tirosAlPaloComoRivalInd = tirosAlPaloJRiv,
                    fueraAreaComoRivalInd = fueraAreaJRiv,
                    chilenaComoRivalInd = chilenaJRiv,
                    taconComoRivalInd = taconJRiv,
                    golesEncajadosComoRivalInd = golesEncajadosJRiv,
                    paradasComoRivalInd = paradasJRiv,
                    haJugadoPorteroComoRivalInd = haJugadoPorteroJRiv,
                    golesTarget = golesTarget,
                    asistenciasTarget = asistenciasTarget,
                    tirosAlPaloTarget = tirosAlPaloTarget,
                    fueraAreaTarget = fueraAreaTarget,
                    chilenaTarget = chilenaTarget,
                    taconTarget = taconTarget,
                    golesEncajadosTarget = golesEncajadosTarget,
                    paradasTarget = paradasTarget,
                    haJugadoPorteroTarget = haJugadoPorteroTarget,
                    golesTargetComoCompaneroInd = golesTargetComp,
                    asistenciasTargetComoCompaneroInd = asistenciasTargetComp,
                    tirosAlPaloTargetComoCompaneroInd = tirosAlPaloTargetComp,
                    fueraAreaTargetComoCompaneroInd = fueraAreaTargetComp,
                    chilenaTargetComoCompaneroInd = chilenaTargetComp,
                    taconTargetComoCompaneroInd = taconTargetComp,
                    golesEncajadosTargetComoCompaneroInd = golesEncajadosTargetComp,
                    paradasTargetComoCompaneroInd = paradasTargetComp,
                    haJugadoPorteroTargetComoCompaneroInd = haJugadoPorteroTargetComp,
                    golesTargetComoRivalInd = golesTargetRiv,
                    asistenciasTargetComoRivalInd = asistenciasTargetRiv,
                    tirosAlPaloTargetComoRivalInd = tirosAlPaloTargetRiv,
                    fueraAreaTargetComoRivalInd = fueraAreaTargetRiv,
                    chilenaTargetComoRivalInd = chilenaTargetRiv,
                    taconTargetComoRivalInd = taconTargetRiv,
                    golesEncajadosTargetComoRivalInd = golesEncajadosTargetRiv,
                    paradasTargetComoRivalInd = paradasTargetRiv,
                    haJugadoPorteroTargetComoRivalInd = haJugadoPorteroTargetRiv
                )
            }.sortedWith(compareByDescending<EstadisticasJugadorCruzadas> { it.totalPartidos }.thenBy { it.jugador.nombre.lowercase() })
        }
    }

    override fun obtenerDestacadosParaJugador(jugadorId: String, fechaInicio: Long?, fechaFin: Long?): Flow<DestacadosEnfrentamientos> {
        return obtenerHistorialCruzadoParaJugador(jugadorId, fechaInicio, fechaFin).map { lista ->
            calcularDestacados(lista)
        }
    }

    private fun calcularDestacados(lista: List<EstadisticasJugadorCruzadas>): DestacadosEnfrentamientos {
        val conPartidosComp = lista.filter { it.partidosComoCompanero > 0 }
        val conPartidosRiv = lista.filter { it.partidosComoRival > 0 }

        // Compañero más gana (La cabra / Talismán)
        val compGanaCands = conPartidosComp.filter { it.victoriasComoCompanero > 0 }
        val maxVicComp = compGanaCands.maxOfOrNull { it.victoriasComoCompanero }
        val compMasGanan = if (maxVicComp != null && maxVicComp > 0) {
            val topVic = compGanaCands.filter { it.victoriasComoCompanero == maxVicComp }
            val maxPct = topVic.maxOfOrNull { it.porcentajeVictoriasCompanero } ?: 0f
            topVic.filter { it.porcentajeVictoriasCompanero == maxPct }
        } else emptyList()

        // Compañero más pierde (La lacra)
        val compPierdeCands = conPartidosComp.filter { it.derrotasComoCompanero > 0 }
        val maxDerComp = compPierdeCands.maxOfOrNull { it.derrotasComoCompanero }
        val compMasPierden = if (maxDerComp != null && maxDerComp > 0) {
            val topDer = compPierdeCands.filter { it.derrotasComoCompanero == maxDerComp }
            val minPct = topDer.minOfOrNull { it.porcentajeVictoriasCompanero } ?: 100f
            topDer.filter { it.porcentajeVictoriasCompanero == minPct }
        } else emptyList()

        // Rival más gana (Rival favorito / Caramelito)
        val rivGanaCands = conPartidosRiv.filter { it.victoriasComoRival > 0 }
        val maxVicRiv = rivGanaCands.maxOfOrNull { it.victoriasComoRival }
        val rivMasGanan = if (maxVicRiv != null && maxVicRiv > 0) {
            val topVic = rivGanaCands.filter { it.victoriasComoRival == maxVicRiv }
            val maxPct = topVic.maxOfOrNull { it.porcentajeVictoriasRival } ?: 0f
            topVic.filter { it.porcentajeVictoriasRival == maxPct }
        } else emptyList()

        // Rival más pierde (La bestia negra)
        val rivPierdeCands = conPartidosRiv.filter { it.derrotasComoRival > 0 }
        val maxDerRiv = rivPierdeCands.maxOfOrNull { it.derrotasComoRival }
        val rivMasPierden = if (maxDerRiv != null && maxDerRiv > 0) {
            val topDer = rivPierdeCands.filter { it.derrotasComoRival == maxDerRiv }
            val minPct = topDer.minOfOrNull { it.porcentajeVictoriasRival } ?: 100f
            topDer.filter { it.porcentajeVictoriasRival == minPct }
        } else emptyList()

        return DestacadosEnfrentamientos(
            companerosMasGanan = compMasGanan,
            companerosMasPierden = compMasPierden,
            rivalesMasGanan = rivMasGanan,
            rivalesMasPierden = rivMasPierden
        )
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
