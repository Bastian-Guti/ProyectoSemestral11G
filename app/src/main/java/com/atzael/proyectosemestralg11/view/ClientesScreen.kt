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
import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.view.components.DialogoConfirmacion
import com.atzael.proyectosemestralg11.view.components.DialogoMensaje
import com.atzael.proyectosemestralg11.viewmodel.ClienteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(
    viewModel: ClienteViewModel,
    onBack: () -> Unit,
    onNuevo: () -> Unit,
    onEditar: (Int) -> Unit
) {
    val clientes by viewModel.clientes.collectAsState()
    val errorEliminar by viewModel.errorEliminar.collectAsState()
    var clienteAEliminar by remember { mutableStateOf<Cliente?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { BarraSuperior(titulo = "Clientes", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo) {
                Icon(Icons.Default.Add, contentDescription = "Agregar cliente")
            }
        }
    ) { innerPadding ->
        if (clientes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay clientes registrados.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(clientes, key = { it.id }) { cliente ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cliente.nombre, style = MaterialTheme.typography.titleLarge)
                            Text("Contacto: ${cliente.contacto}", style = MaterialTheme.typography.bodySmall)
                            Text("Teléfono: ${cliente.telefono}", style = MaterialTheme.typography.bodySmall)
                            Text("SLA: ${cliente.slaHoras} h por atención", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onEditar(cliente.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { clienteAEliminar = cliente }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    clienteAEliminar?.let { cliente ->
        DialogoConfirmacion(
            titulo = "Eliminar cliente",
            mensaje = "¿Desea eliminar a '${cliente.nombre}'?",
            textoConfirmar = "Eliminar",
            onConfirmar = {
                viewModel.eliminarCliente(cliente)
                clienteAEliminar = null
            },
            onCancelar = { clienteAEliminar = null }
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
