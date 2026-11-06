package com.atzael.proyectosemestralg11.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.model.Local
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.view.components.DialogoConfirmacion
import com.atzael.proyectosemestralg11.view.components.DialogoMensaje
import com.atzael.proyectosemestralg11.viewmodel.LocalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalesScreen(
    viewModel: LocalViewModel,
    onBack: () -> Unit,
    onNuevo: () -> Unit,
    onEditar: (Int) -> Unit
) {
    val locales by viewModel.locales.collectAsState()
    val clientes by viewModel.clientes.collectAsState()
    val errorEliminar by viewModel.errorEliminar.collectAsState()
    var localAEliminar by remember { mutableStateOf<Local?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { BarraSuperior(titulo = "Locales", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo) {
                Icon(Icons.Default.Add, contentDescription = "Agregar local")
            }
        }
    ) { innerPadding ->
        if (locales.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (clientes.isEmpty()) "Primero registre un cliente." else "No hay locales registrados.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(locales, key = { it.id }) { local ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = clientes.firstOrNull { it.id == local.clienteId }?.nombre ?: "Cliente #${local.clienteId}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(local.nombre, style = MaterialTheme.typography.titleLarge)
                            Text("Dirección: ${local.direccion}", style = MaterialTheme.typography.bodySmall)
                            Text("Contacto: ${local.telefono}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onEditar(local.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { localAEliminar = local }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    localAEliminar?.let { local ->
        DialogoConfirmacion(
            titulo = "Eliminar local",
            mensaje = "¿Desea eliminar el local '${local.nombre}'?",
            textoConfirmar = "Eliminar",
            onConfirmar = {
                viewModel.eliminarLocal(local)
                localAEliminar = null
            },
            onCancelar = { localAEliminar = null }
        )
    }

    errorEliminar?.let { mensaje ->
        DialogoMensaje(
            titulo = "No se pudo eliminar",
            mensaje = mensaje,
            onCerrar = { viewModel.descartarErrorEliminar() }
        )
    }
}
