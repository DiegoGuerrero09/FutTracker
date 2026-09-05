package com.diegoguerrero.futtracker.domain.model

data class EstadisticasJugadorCruzadas(
    val jugador: Jugador,
    // Como compañero en mi equipo
    val partidosComoCompanero: Int = 0,
    val victoriasComoCompanero: Int = 0,
    val empatesComoCompanero: Int = 0,
    val derrotasComoCompanero: Int = 0,
    val golesFavorComoCompanero: Int = 0,
    val golesContraComoCompanero: Int = 0,
    val golesMarcadosComoCompanero: Int = 0,
    // Como rival en el equipo contrario
    val partidosComoRival: Int = 0,
    val victoriasComoRival: Int = 0,
    val empatesComoRival: Int = 0,
    val derrotasComoRival: Int = 0,
    val golesFavorComoRival: Int = 0,
    val golesContraComoRival: Int = 0,
    val golesMarcadosComoRival: Int = 0
) {
    val totalPartidos: Int
        get() = partidosComoCompanero + partidosComoRival

    val porcentajeVictoriasCompanero: Float
        get() = if (partidosComoCompanero > 0) (victoriasComoCompanero.toFloat() / partidosComoCompanero) * 100f else 0f

    val porcentajeVictoriasRival: Float
        get() = if (partidosComoRival > 0) (victoriasComoRival.toFloat() / partidosComoRival) * 100f else 0f

    val diferenciaGolesCompanero: Int
        get() = golesFavorComoCompanero - golesContraComoCompanero

    val diferenciaGolesRival: Int
        get() = golesFavorComoRival - golesContraComoRival
}

data class DuoEstadisticas(
    val jugador1: Jugador,
    val jugador2: Jugador,
    val partidosJuntos: Int = 0,
    val victorias: Int = 0,
    val empates: Int = 0,
    val derrotas: Int = 0,
    val golesFavor: Int = 0,
    val golesContra: Int = 0
) {
    val porcentajeVictorias: Float
        get() = if (partidosJuntos > 0) (victorias.toFloat() / partidosJuntos) * 100f else 0f

    val diferenciaGoles: Int
        get() = golesFavor - golesContra
}

data class ComparativaCaraACara(
    val jugadorA: Jugador,
    val jugadorB: Jugador,
    val partidosEnfrentados: Int = 0,
    val victoriasA: Int = 0,
    val victoriasB: Int = 0,
    val empates: Int = 0,
    val golesEquipoA: Int = 0,
    val golesEquipoB: Int = 0,
    val partidosJuntos: Int = 0,
    val victoriasJuntos: Int = 0,
    val empatesJuntos: Int = 0,
    val derrotasJuntos: Int = 0,
    val partidosHistorial: List<Partido> = emptyList()
) {
    val porcentajeVictoriasA: Float
        get() = if (partidosEnfrentados > 0) (victoriasA.toFloat() / partidosEnfrentados) * 100f else 0f

    val porcentajeVictoriasB: Float
        get() = if (partidosEnfrentados > 0) (victoriasB.toFloat() / partidosEnfrentados) * 100f else 0f
}

data class DestacadosEnfrentamientos(
    val companerosMasGanan: List<EstadisticasJugadorCruzadas> = emptyList(),
    val companerosMasPierden: List<EstadisticasJugadorCruzadas> = emptyList(),
    val rivalesMasGanan: List<EstadisticasJugadorCruzadas> = emptyList(),
    val rivalesMasPierden: List<EstadisticasJugadorCruzadas> = emptyList()
) {
    val companeroMasGana: EstadisticasJugadorCruzadas? get() = companerosMasGanan.firstOrNull()
    val companeroMasPierde: EstadisticasJugadorCruzadas? get() = companerosMasPierden.firstOrNull()
    val rivalMasGana: EstadisticasJugadorCruzadas? get() = rivalesMasGanan.firstOrNull()
    val rivalMasPierde: EstadisticasJugadorCruzadas? get() = rivalesMasPierden.firstOrNull()
}
