package com.atzael.proyectosemestralg11.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.util.formatearFecha
import com.atzael.proyectosemestralg11.util.formatearFechaHora
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.viewmodel.FichaEquipoViewModel

// HU-04: ficha del equipo con el historial de intervenciones anteriores
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaEquipoScreen(
    viewModel: FichaEquipoViewModel,
    equipoId: Int,
    onBack: () -> Unit
) {
    val equipo by viewModel.equipo.collectAsState()
    val ubicacionTexto by viewModel.ubicacionTexto.collectAsState()
    val historial by viewModel.historial.collectAsState()

    LaunchedEffect(equipoId) {
        viewModel.cargarEquipo(equipoId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { BarraSuperior(titulo = "Ficha del equipo", onBack = onBack) }
    ) { innerPadding ->
        val ficha = equipo
        if (ficha == null) {
            Text(
                text = "Cargando...",
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(ficha.nombre, style = MaterialTheme.typography.titleLarge)
                        if (ficha.marca.isNotBlank()) Text("Marca: ${ficha.marca}", style = MaterialTheme.typography.bodyMedium)
                        Text("Ubicación del equipo: ${ficha.ubicacion}", style = MaterialTheme.typography.bodyMedium)
                        Text("Local: $ubicacionTexto", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Última mantención: ${ficha.ultimaMantencion?.formatearFecha() ?: "sin registro"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Text("Historial de intervenciones", style = MaterialTheme.typography.titleMedium)
                if (historial.isEmpty()) {
                    Text("Este equipo aún no tiene intervenciones finalizadas.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    historial.forEach { item ->
                        val orden = item.orden
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = (orden.checkOut ?: orden.fechaCreacion).formatearFechaHora(),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text("Tipo: ${orden.tipo}", style = MaterialTheme.typography.bodyMedium)
                                Text("Técnico: ${item.tecnicoNombre}", style = MaterialTheme.typography.bodySmall)
                                Text("Diagnóstico: ${orden.diagnostico ?: "sin registro"}", style = MaterialTheme.typography.bodyMedium)
                                orden.actividades?.let { Text("Actividades: $it", style = MaterialTheme.typography.bodySmall) }
                                orden.materiales?.let { Text("Materiales: $it", style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
            }
        }
    }
}
