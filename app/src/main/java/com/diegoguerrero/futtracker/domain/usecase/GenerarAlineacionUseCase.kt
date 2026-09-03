package com.diegoguerrero.futtracker.domain.usecase

import com.diegoguerrero.futtracker.domain.model.Formacion
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion

class GenerarAlineacionUseCase {
    operator fun invoke(convocados: List<Jugador>, formacion: Formacion): List<Pair<Posicion, Jugador>> {
        val requeridas = mutableListOf(Posicion.POR).apply { addAll(formacion.posicionesRequeridas) }
        val asignaciones = mutableListOf<Pair<Posicion, Jugador>>()
        val sinAsignar = convocados.toMutableList()

        for (pos in requeridas) {
            // Evaluamos la afinidad con el Set de posiciones
            val candidatosConPuntuacion = sinAsignar.mapNotNull { jugador ->
                val puntos = when {
                    pos in jugador.posicionesPrimarias -> 2
                    pos in jugador.posicionesSecundarias -> 1
                    else -> 0
                }
                // Solo consideramos candidatos si tienen puntos > 0
                if (puntos > 0) jugador to puntos else null
            }

            // Seleccionamos al jugador con mayor puntuación
            val mejorCandidato = candidatosConPuntuacion.maxByOrNull { it.second }?.first

            if (mejorCandidato != null) {
                asignaciones.add(pos to mejorCandidato)
                sinAsignar.remove(mejorCandidato)
            }
        }
        return asignaciones
    }
}