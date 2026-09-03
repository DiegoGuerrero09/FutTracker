package com.diegoguerrero.futtracker.domain.usecase

import com.diegoguerrero.futtracker.domain.model.Formacion
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion

class GenerarAlineacionUseCase {

    operator fun invoke(convocados: List<Jugador>, formacion: Formacion): List<Pair<Posicion, Jugador>> {
        if (convocados.isEmpty()) return emptyList()

        val posicionesRequeridas = mutableListOf(Posicion.POR).apply {
            addAll(formacion.posicionesRequeridas)
        }

        // Si el número de convocados difiere del de la formación, ajustamos
        val n = minOf(convocados.size, posicionesRequeridas.size)
        val jugadoresAlineables = convocados.take(n)
        val posicionesAlineables = posicionesRequeridas.take(n)

        // Asignación de mínimo coste táctico (branch & bound sobre permutaciones de jugadores)
        val mejorAsignacion = resolverAsignacionOptima(jugadoresAlineables, posicionesAlineables)

        return mejorAsignacion
    }

    fun sugerirMejorFormacion(convocados: List<Jugador>, formaciones: List<Formacion>): Formacion {
        if (formaciones.isEmpty()) throw IllegalArgumentException("La lista de formaciones no puede estar vacía")
        if (convocados.isEmpty()) return formaciones.first()

        return formaciones.minByOrNull { formacion ->
            evaluarPenalizacionTotal(convocados, formacion)
        } ?: formaciones.first()
    }

    fun evaluarAfinidadTotal(convocados: List<Jugador>, formacion: Formacion): Int {
        val penalizacion = evaluarPenalizacionTotal(convocados, formacion)
        return (1000 - penalizacion).coerceAtLeast(0)
    }

    private fun evaluarPenalizacionTotal(convocados: List<Jugador>, formacion: Formacion): Int {
        val posiciones = listOf(Posicion.POR) + formacion.posicionesRequeridas
        val n = minOf(convocados.size, posiciones.size)
        val asignacion = resolverAsignacionOptima(convocados.take(n), posiciones.take(n))
        return asignacion.sumOf { (pos, jug) -> calcularPenalizacion(jug, pos) }
    }

    /**
     * Busca la asignación 1 a 1 de menor coste entre jugadores y posiciones.
     * Al ser N <= 7, la exploración con branch and bound es instantánea (< 1ms).
     */
    private fun resolverAsignacionOptima(
        jugadores: List<Jugador>,
        posiciones: List<Posicion>
    ): List<Pair<Posicion, Jugador>> {
        val n = jugadores.size
        if (n == 0) return emptyList()

        // Matriz de costes: costeMatriz[jugadorIndex][posicionIndex]
        val costeMatriz = Array(n) { jIdx ->
            IntArray(n) { pIdx ->
                calcularPenalizacion(jugadores[jIdx], posiciones[pIdx])
            }
        }

        var mejorCoste = Int.MAX_VALUE
        var mejorPermutacion = IntArray(n) { it }

        val actualPermutacion = IntArray(n)
        val usado = BooleanArray(n)

        fun buscar(idxPos: Int, costeAcumulado: Int) {
            if (costeAcumulado >= mejorCoste) return

            if (idxPos == n) {
                mejorCoste = costeAcumulado
                mejorPermutacion = actualPermutacion.clone()
                return
            }

            for (jIdx in 0 until n) {
                if (!usado[jIdx]) {
                    usado[jIdx] = true
                    actualPermutacion[idxPos] = jIdx
                    buscar(idxPos + 1, costeAcumulado + costeMatriz[jIdx][idxPos])
                    usado[jIdx] = false
                }
            }
        }

        buscar(0, 0)

        return posiciones.indices.map { pIdx ->
            val jIdx = mejorPermutacion[pIdx]
            posiciones[pIdx] to jugadores[jIdx]
        }
    }

    /**
     * Calcula la penalización de un jugador jugando en una posición específica.
     * Menor penalización = mejor adaptación táctica.
     * - Posición primaria: 0
     * - Posición secundaria: 3
     * - Posición cercana (p.ej. DFC jugando de LI/LD, LI jugando de EI): penalización moderada (~12)
     * - Posición lejana (cambio brusco, p.ej. DFC jugando de DC): penalización severa (55+)
     * - De campo a Portero (o viceversa): penalización máxima (250)
     */
    private fun calcularPenalizacion(jugador: Jugador, posicionRequerida: Posicion): Int {
        if (posicionRequerida == Posicion.POR) {
            return when {
                Posicion.POR in jugador.posicionesPrimarias -> 0
                Posicion.POR in jugador.posicionesSecundarias -> 15
                else -> 250 // Evita poner jugadores de campo en portería salvo necesidad absoluta
            }
        }

        // Para posiciones de campo
        if (posicionRequerida in jugador.posicionesPrimarias) return 0
        if (posicionRequerida in jugador.posicionesSecundarias) return 3

        val posicionesDelJugador = jugador.posicionesPrimarias + jugador.posicionesSecundarias
        if (posicionesDelJugador.isEmpty()) {
            return 18 // Sin posiciones asignadas: penalización base moderada
        }

        val distanciaMinima = posicionesDelJugador.minOf { posJugador ->
            distanciaEntrePosiciones(posJugador, posicionRequerida)
        }

        // Sumamos un offset de 6 para asegurar que una posición secundaria natural (coste 3)
        // siempre prevalezca sobre una posición vecina no listada (mínimo 6 + 6 = 12)
        return distanciaMinima + 6
    }

    /**
     * Matriz de distancias tácticas en el terreno de juego.
     * Favorece transiciones lógicas (ej: Central a Lateral antes que a DC, Lateral a Extremo).
     */
    private fun distanciaEntrePosiciones(from: Posicion, to: Posicion): Int {
        if (from == to) return 0
        if (from == Posicion.POR || to == Posicion.POR) return 200

        return when (from) {
            Posicion.DFC -> when (to) {
                Posicion.LI, Posicion.LD -> 6   // Vecino directo en defensa
                Posicion.MC -> 8                // Pivote / medio defensivo
                Posicion.EI, Posicion.ED -> 22  // Banda ofensiva
                Posicion.DC -> 50               // CAMBIO BRUSCO: Central a delantero centro
                else -> 20
            }

            Posicion.LI -> when (to) {
                Posicion.DFC -> 6               // Cierre hacia el centro
                Posicion.EI -> 6                // Misma banda, sube a extremo
                Posicion.LD -> 10               // Lateral cambiado
                Posicion.MC -> 9                // Interior / medio
                Posicion.ED -> 20               // Banda opuesta arriba
                Posicion.DC -> 28               // Delantero centro
                else -> 20
            }

            Posicion.LD -> when (to) {
                Posicion.DFC -> 6               // Cierre hacia el centro
                Posicion.ED -> 6                // Misma banda, sube a extremo
                Posicion.LI -> 10               // Lateral cambiado
                Posicion.MC -> 9                // Interior / medio
                Posicion.EI -> 20               // Banda opuesta arriba
                Posicion.DC -> 28               // Delantero centro
                else -> 20
            }

            Posicion.MC -> when (to) {
                Posicion.EI, Posicion.ED -> 7   // De interior a extremo
                Posicion.DFC -> 8               // Cierre a central
                Posicion.DC -> 8                // Llegador / mediapunta a delantero
                Posicion.LI, Posicion.LD -> 9   // Apoyo a banda
                else -> 15
            }

            Posicion.EI -> when (to) {
                Posicion.LI -> 6                // Misma banda, baja a lateral
                Posicion.MC -> 7                // Interior ofensivo
                Posicion.DC -> 8                // Ataque por dentro
                Posicion.ED -> 10               // Extremo cambiado
                Posicion.DFC -> 35              // Extremo a central
                Posicion.LD -> 20
                else -> 20
            }

            Posicion.ED -> when (to) {
                Posicion.LD -> 6                // Misma banda, baja a lateral
                Posicion.MC -> 7                // Interior ofensivo
                Posicion.DC -> 8                // Ataque por dentro
                Posicion.EI -> 10               // Extremo cambiado
                Posicion.DFC -> 35              // Extremo a central
                Posicion.LI -> 20
                else -> 20
            }

            Posicion.DC -> when (to) {
                Posicion.EI, Posicion.ED -> 8   // Delantero cayendo a banda
                Posicion.MC -> 9                // Bajando al centro del campo
                Posicion.LI, Posicion.LD -> 28  // A lateral
                Posicion.DFC -> 50              // CAMBIO BRUSCO: Delantero a central
                else -> 25
            }

            Posicion.POR -> 200
        }
    }
}