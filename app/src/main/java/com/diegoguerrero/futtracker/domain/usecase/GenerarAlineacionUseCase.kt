package com.diegoguerrero.futtracker.domain.usecase

import com.diegoguerrero.futtracker.domain.model.Formacion
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion

class GenerarAlineacionUseCase {
    operator fun invoke(disponibles: List<Jugador>, formacion: Formacion): List<Pair<Posicion, Jugador>> {
        val requeridas = mutableListOf(Posicion.POR).apply { addAll(formacion.posicionesRequeridas) }
        val asignaciones = mutableListOf<Pair<Posicion, Jugador>>()
        val sinAsignar = disponibles.toMutableList()

        for (pos in requeridas) {
            val mejorCandidato = sinAsignar.maxByOrNull { j ->
                when (pos) {
                    j.posPrincipal -> 3
                    j.posSecundaria -> 2
                    j.posTercera -> 1
                    else -> 0
                }
            }
            if (mejorCandidato != null) {
                asignaciones.add(pos to mejorCandidato)
                sinAsignar.remove(mejorCandidato)
            }
        }
        return asignaciones
    }
}