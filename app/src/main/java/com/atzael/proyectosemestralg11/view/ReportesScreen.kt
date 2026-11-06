package com.atzael.proyectosemestralg11.view

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.atzael.proyectosemestralg11.util.uriDeArchivo
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.Local
import com.atzael.proyectosemestralg11.util.formatearDuracion
import com.atzael.proyectosemestralg11.util.formatearFecha
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.view.components.CampoDesplegable
import com.atzael.proyectosemestralg11.viewmodel.FilaReporte
import com.atzael.proyectosemestralg11.viewmodel.ReporteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    viewModel: ReporteViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clientes by viewModel.clientes.collectAsState()
    val locales by viewModel.localesDisponibles.collectAsState()
    val clienteId by viewModel.clienteId.collectAsState()
    val localId by viewModel.localId.collectAsState()
    val periodo by viewModel.periodo.collectAsState()
    val resumen by viewModel.resumen.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { BarraSuperior(titulo = "Reportes y SLA", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            CampoDesplegable(
                label = "Cliente",
                valorSeleccionado = clientes.firstOrNull { it.id == clienteId }?.nombre ?: "Todos",
                opciones = listOf<Cliente?>(null) + clientes,
                textoOpcion = { it?.nombre ?: "Todos" },
                onSeleccion = { viewModel.onClienteChange(it?.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoDesplegable(
                    label = "Local",
                    valorSeleccionado = locales.firstOrNull { it.id == localId }?.nombre ?: "Todos",
                    opciones = listOf<Local?>(null) + locales,
                    textoOpcion = { it?.nombre ?: "Todos" },
                    onSeleccion = { viewModel.onLocalChange(it?.id) },
                    modifier = Modifier.weight(1f),
                    enabled = clienteId != null
                )
                CampoDesplegable(
                    label = "Período",
                    valorSeleccionado = periodo,
                    opciones = ReporteViewModel.PERIODOS,
                    textoOpcion = { it },
                    onSeleccion = { viewModel.onPeriodoChange(it) },
                    modifier = Modifier.weight(1f)
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Resumen", style = MaterialTheme.typography.titleMedium)
                    Text("OT en el período: ${resumen.filas.size}")
                    Text("OT atendidas (con check-in y check-out): ${resumen.atendidas}")
                    Text("Tiempo promedio de atención: ${resumen.promedioMin?.let { formatearDuracion(it) } ?: "sin datos"}")
                    Text(
                        text = "Cumplimiento de SLA: " +
                                (resumen.porcentajeSla?.let { "$it% (${resumen.dentroSla} de ${resumen.atendidas})" } ?: "sin datos"),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        // Evidencias: se adjuntan las fotos de las OT del reporte (máximo 30)
                        val fotos = viewModel.rutasFotosDelReporte().take(30)
                        val envio = Intent(
                            if (fotos.isEmpty()) Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE
                        ).apply {
                            putExtra(Intent.EXTRA_SUBJECT, "Reporte de órdenes de trabajo")
                            putExtra(Intent.EXTRA_TEXT, viewModel.construirTextoReporte())
                            if (fotos.isEmpty()) {
                                type = "text/plain"
                            } else {
                                type = "image/*"
                                putParcelableArrayListExtra(
                                    Intent.EXTRA_STREAM,
                                    ArrayList(fotos.map { uriDeArchivo(context, File(it)) })
                                )
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        }
                        context.startActivity(Intent.createChooser(envio, "Compartir reporte"))
                    }
                },
                enabled = resumen.filas.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Generar y compartir reporte")
            }

            if (resumen.filas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay órdenes para los filtros elegidos.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(resumen.filas, key = { it.orden.orden.id }) { fila ->
                        FilaReporteCard(fila)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaReporteCard(fila: FilaReporte) {
    val item = fila.orden
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "OT #${item.orden.id} - ${item.clienteNombre} / ${item.localNombre}",
                style = MaterialTheme.typography.titleSmall
            )
            Text("Equipo: ${item.equipoNombre} - ${item.orden.tipo}", style = MaterialTheme.typography.bodySmall)
            Text("Visita: ${item.orden.fechaVisita.formatearFecha()} - ${item.orden.estado}", style = MaterialTheme.typography.bodySmall)
            val tiempo = fila.tiempoAtencionMin
            if (tiempo == null) {
                Text("Tiempo de atención: sin registro", style = MaterialTheme.typography.bodySmall)
            } else {
                Text(
                    text = "Tiempo de atención: ${formatearDuracion(tiempo)} (SLA ${item.slaHoras} h) - " +
                            if (fila.dentroSla == true) "dentro del SLA" else "fuera del SLA",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (fila.dentroSla == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
