package com.atzael.proyectosemestralg11.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.view.components.BarraSuperior

// HU-15 (alertas de lluvia o viento fuerte):
// Pendiente: definir de dónde se obtendrá el pronóstico del clima, por ahora no se conecta a apis
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasClimaScreen(
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { BarraSuperior(titulo = "Alertas de clima", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Próximamente",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text =
                        "todavía no está disponible.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
