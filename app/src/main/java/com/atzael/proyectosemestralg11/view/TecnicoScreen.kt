package com.atzael.proyectosemestralg11.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.view.components.SuccessToast
import com.atzael.proyectosemestralg11.view.components.TarjetaOrden
import com.atzael.proyectosemestralg11.viewmodel.TecnicoViewModel

// HU-08: mi jornada de trabajo
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TecnicoScreen(
    rut: String,
    viewModel: TecnicoViewModel,
    onOrdenClick: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val nombre by viewModel.nombre.collectAsState()
    val ordenes by viewModel.ordenes.collectAsState()
    val verCerradas by viewModel.verCerradas.collectAsState()
    val avisoAceptado by viewModel.avisoAceptado.collectAsState()
    val mensajeExito by viewModel.mensajeExito.collectAsState()

    LaunchedEffect(rut) {
        viewModel.cargarUsuario(rut)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BarraSuperior(
                titulo = "Mi jornada",
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
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Bienvenido, $nombre",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Órdenes asignadas, ordenadas por prioridad y fecha de visita.",
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ver también finalizadas y canceladas", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = verCerradas, onCheckedChange = { viewModel.onVerCerradasChange(it) })
            }

            if (ordenes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tiene órdenes de trabajo asignadas.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ordenes, key = { it.orden.id }) { item ->
                        Column {
                            TarjetaOrden(item = item, onClick = { onOrdenClick(item.orden.id) })
                        }
                    }
                }
            }
        }

        SuccessToast(
            show = mensajeExito != null,
            message = mensajeExito ?: "",
            modifier = Modifier.padding(innerPadding)
        )
    }

    // HU-22: el técnico debe conocer qué datos se recopilan antes de usar ubicación y fotos
    if (avisoAceptado == false) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Aviso sobre sus datos personales") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Para registrar su trabajo la aplicación recopila:")
                    Text("• Su ubicación, solo en el momento del check-in y del check-out. No hay seguimiento continuo.")
                    Text("• Fotografías de los equipos que usted tome o elija, asociadas a la orden de trabajo.")
                    Text("Se usan solo para dejar evidencia del servicio y calcular tiempos de atención. Únicamente la jefatura y usted pueden verlas.")
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.aceptarAviso() }) { Text("Entiendo y acepto") }
            },
            dismissButton = {
                TextButton(onClick = onLogout) { Text("No acepto (cerrar sesión)") }
            }
        )
    }
}
