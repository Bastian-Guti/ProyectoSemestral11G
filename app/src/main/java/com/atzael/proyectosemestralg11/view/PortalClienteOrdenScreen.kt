package com.atzael.proyectosemestralg11.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.util.formatearDuracion
import com.atzael.proyectosemestralg11.util.formatearFecha
import com.atzael.proyectosemestralg11.util.formatearFechaHora
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.view.components.SuccessToast
import com.atzael.proyectosemestralg11.viewmodel.PortalClienteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalClienteOrdenScreen(
    viewModel: PortalClienteViewModel,
    rutUsuario: String,
    ordenId: Int,
    onBack: () -> Unit
) {
    val ordenes by viewModel.todasLasOrdenes.collectAsState()
    val observaciones by viewModel.observaciones.collectAsState()
    val observacionesError by viewModel.observacionesError.collectAsState()
    val showToast by viewModel.showToast.collectAsState()

    // Limpia el texto escrito al salir de la pantalla
    DisposableEffect(ordenId) {
        viewModel.resetFormulario()
        onDispose { viewModel.resetFormulario() }
    }

    // Solo se busca entre las OT de la empresa del usuario
    val item = ordenes.firstOrNull { it.orden.id == ordenId }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { BarraSuperior(titulo = "OT #$ordenId", onBack = onBack) }
    ) { innerPadding ->
        if (item == null) {
            Text(
                text = "No se encontró la orden de trabajo.",
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        } else {
            val orden = item.orden
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = orden.estado,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Campo("Local", item.localNombre)
                        Campo("Equipo", "${item.equipoNombre} (${item.equipoUbicacion})")
                        Campo("Tipo de trabajo", orden.tipo)
                        Campo("Técnico asignado", item.tecnicoNombre)
                        Campo("Fecha de visita", orden.fechaVisita.formatearFecha())
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Trabajo realizado", style = MaterialTheme.typography.titleMedium)
                        Campo("Llegada del técnico", orden.checkIn?.formatearFechaHora() ?: "Aún sin registrar")
                        Campo("Salida del técnico", orden.checkOut?.formatearFechaHora() ?: "Aún sin registrar")
                        if (orden.checkIn != null && orden.checkOut != null && orden.checkOut >= orden.checkIn) {
                            Campo("Duración", formatearDuracion((orden.checkOut - orden.checkIn) / 60_000))
                        }
                        Campo("Diagnóstico", orden.diagnostico ?: "Aún sin registrar")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                when {
                    PortalClienteViewModel.requiereConformidad(item) -> {
                        Text("Conformidad del trabajo", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Confirme si el trabajo quedó conforme, o recházelo indicando las observaciones.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            value = observaciones,
                            onValueChange = { viewModel.onObservacionesChange(it) },
                            label = { Text("Observaciones") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            isError = observacionesError != null,
                            supportingText = { observacionesError?.let { Text(it) } }
                        )
                        Button(
                            onClick = { viewModel.validarConformidad(item, true, rutUsuario) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Confirmar conformidad") }
                        Button(
                            onClick = { viewModel.validarConformidad(item, false, rutUsuario) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("Rechazar con observaciones") }
                    }

                    orden.conformidad != null -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Su respuesta", style = MaterialTheme.typography.titleMedium)
                                Campo("Resultado", orden.conformidad)
                                Campo("Fecha", orden.fechaConformidad?.formatearFechaHora() ?: "-")
                                Campo("Validó (RUT)", orden.conformidadPor ?: "-")
                                orden.observacionCliente?.let { Campo("Observaciones", it) }
                            }
                        }
                    }

                    else -> {
                        Text(
                            text = "Podrá validar la conformidad cuando el trabajo esté finalizado.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        SuccessToast(
            show = showToast,
            message = "Su respuesta quedó registrada",
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun Campo(etiqueta: String, valor: String) {
    Column {
        Text(text = etiqueta, style = MaterialTheme.typography.labelSmall)
        Text(text = valor, style = MaterialTheme.typography.bodyMedium)
    }
}
