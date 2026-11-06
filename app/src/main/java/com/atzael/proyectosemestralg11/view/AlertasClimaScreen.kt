package com.atzael.proyectosemestralg11.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.util.formatearFecha
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.viewmodel.ClimaViewModel

// HU-15: alertas de lluvia o viento fuerte antes de mantenciones de equipos exteriores
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasClimaScreen(
    viewModel: ClimaViewModel,
    onBack: () -> Unit,
    onOrdenClick: (Int) -> Unit
) {
    val resultado by viewModel.resultado.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { BarraSuperior(titulo = "Alertas de clima", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Órdenes pendientes o aceptadas de equipos exteriores, con visita en los próximos 15 días. " +
                        "Fuente: Open-Meteo (datos meteorológicos públicos).",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (cargando) Text("Consultando el pronóstico...", style = MaterialTheme.typography.bodyMedium)

            if (resultado.sinCiudad > 0) {
                Text(
                    text = "${resultado.sinCiudad} orden(es) no se pudieron revisar porque su local no tiene ciudad. " +
                            "Agréguela en Locales.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (resultado.sinPronostico > 0) {
                Text(
                    text = "No se pudo consultar el pronóstico de ${resultado.sinPronostico} orden(es). " +
                            "Revise la conexión a internet o el nombre de la ciudad.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (resultado.alertas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (cargando) "" else "No hay alertas de clima.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(resultado.alertas, key = { it.orden.orden.id }) { alerta ->
                        val item = alerta.orden
                        Card(
                            onClick = { onOrdenClick(item.orden.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "OT #${item.orden.id} - ${item.clienteNombre} / ${item.localNombre}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Visita: ${item.orden.fechaVisita.formatearFecha()} - ${item.localCiudad}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text("Equipo: ${item.equipoNombre} (${item.equipoUbicacion})", style = MaterialTheme.typography.bodyMedium)
                                Text(alerta.mensaje, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "Toque para reagendar con motivo \"Clima\".",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
