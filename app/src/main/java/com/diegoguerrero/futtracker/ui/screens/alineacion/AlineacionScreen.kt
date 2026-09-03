package com.diegoguerrero.futtracker.ui.screens.alineacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegoguerrero.futtracker.domain.model.*
import com.diegoguerrero.futtracker.domain.usecase.GenerarAlineacionUseCase
import com.diegoguerrero.futtracker.ui.components.CampoFutbol
import com.diegoguerrero.futtracker.ui.theme.*

@Composable
fun AlineacionScreen() {
    var formacionSeleccionada by remember { mutableStateOf<Formacion>(Formacion.Fut7_231) }
    val generator = remember { GenerarAlineacionUseCase() }

    // Lista de plantilla ficticia para prueba inicial
    val plantillaPrueba = remember {
        listOf(
            Jugador(1, "Diego", posPrincipal = TipoPosicion.DEL, posSecundaria = TipoPosicion.MED),
            Jugador(2, "Carlos", posPrincipal = TipoPosicion.POR),
            Jugador(3, "Alex", posPrincipal = TipoPosicion.DEF, posSecundaria = TipoPosicion.MED),
            Jugador(4, "Hugo", posPrincipal = TipoPosicion.DEF),
            Jugador(5, "Lauri", posPrincipal = TipoPosicion.MED, posSecundaria = TipoPosicion.DEL),
            Jugador(6, "Dani", posPrincipal = TipoPosicion.MED),
            Jugador(7, "Rubén", posPrincipal = TipoPosicion.DEL, posSecundaria = TipoPosicion.DEF)
        )
    }

    var alineacionActual by remember {
        mutableStateOf(generator.ejecutar(plantillaPrueba, formacionSeleccionada))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pizarra Táctica",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { alineacionActual = generator.ejecutar(plantillaPrueba, formacionSeleccionada) },
                colors = ButtonDefaults.buttonColors(containerColor = LimeVolt)
            ) {
                Text("⚡ Autogenerar", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CampoFutbol(alineacion = alineacionActual)
    }
}