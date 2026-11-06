package com.atzael.proyectosemestralg11.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.viewmodel.OrdenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JefaturaScreen(
    nombre: String,
    viewModel: OrdenViewModel,
    onOrdenes: () -> Unit,
    onNuevaOrden: () -> Unit,
    onClientes: () -> Unit,
    onLocales: () -> Unit,
    onEquipos: () -> Unit,
    onReportes: () -> Unit,
    onAlertasClima: () -> Unit,
    cantidadAlertasClima: Int,
    onLogout: () -> Unit
) {
    val ordenes by viewModel.ordenes.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BarraSuperior(
                titulo = "Jefatura",
                acciones = {
                    TextButton(onClick = onLogout) {
                        Text("Cerrar sesión", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Bienvenido, $nombre",
                style = MaterialTheme.typography.headlineSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResumenEstado("Pendientes", ordenes.count { it.orden.estado == EstadoOrden.PENDIENTE }, Modifier.weight(1f))
                ResumenEstado("Aceptadas", ordenes.count { it.orden.estado == EstadoOrden.ACEPTADA }, Modifier.weight(1f))
                ResumenEstado("Finalizadas", ordenes.count { it.orden.estado == EstadoOrden.FINALIZADA }, Modifier.weight(1f))
            }

            Button(
                onClick = onNuevaOrden,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Crear orden de trabajo", style = MaterialTheme.typography.bodyLarge)
            }

            OpcionMenu(
                titulo = if (cantidadAlertasClima > 0) "Alertas de clima ($cantidadAlertasClima)" else "Alertas de clima",
                descripcion = "Lluvia o viento fuerte antes de mantenciones en equipos exteriores",
                onClick = onAlertasClima
            )
            OpcionMenu("Órdenes de trabajo", "Supervisar, filtrar, reagendar y cancelar", onOrdenes)
            OpcionMenu("Clientes", "Registrar y mantener el catastro de clientes y su SLA", onClientes)
            OpcionMenu("Locales", "Locales comerciales de cada cliente", onLocales)
            OpcionMenu("Equipos", "Equipos de climatización y refrigeración", onEquipos)
            OpcionMenu("Reportes y SLA", "Tiempos de atención y cumplimiento por cliente", onReportes)
        }
    }
}

@Composable
private fun ResumenEstado(titulo: String, cantidad: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = cantidad.toString(), style = MaterialTheme.typography.headlineMedium)
            Text(text = titulo, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun OpcionMenu(titulo: String, descripcion: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium)
            Text(text = descripcion, style = MaterialTheme.typography.bodySmall)
        }
    }
}
