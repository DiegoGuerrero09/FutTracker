package com.diegoguerrero.futtracker.domain.model

data class Coordenada(val x: Float, val y: Float)

fun Formacion.obtenerCoordenadas(): List<Pair<Posicion, Coordenada>> {
    val coords = obtenerCoordenadas(this)
    return coords.map { it.first to Coordenada(it.second.first, it.second.second) }
}