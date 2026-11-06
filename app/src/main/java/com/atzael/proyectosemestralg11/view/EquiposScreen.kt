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
import com.atzael.proyectosemestralg11.model.Equipo
import com.atzael.proyectosemestralg11.util.formatearFecha
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.view.components.DialogoConfirmacion
import com.atzael.proyectosemestralg11.view.components.DialogoMensaje
import com.atzael.proyectosemestralg11.viewmodel.EquipoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquiposScreen(
    viewModel: EquipoViewModel,
    onBack: () -> Unit,
    onNuevo: () -> Unit,
    onEditar: (Int) -> Unit
) {
    val equipos by viewModel.equipos.collectAsState()
    val locales by viewModel.locales.collectAsState()
    val clientes by viewModel.clientes.collectAsState()
    val errorEliminar by viewModel.errorEliminar.collectAsState()
    var equipoAEliminar by remember { mutableStateOf<Equipo?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { BarraSuperior(titulo = "Equipos", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo) {
                Icon(Icons.Default.Add, contentDescription = "Agregar equipo")
            }
        }
    ) { innerPadding ->
        if (equipos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (locales.isEmpty()) "Primero registre un local." else "No hay equipos registrados.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(equipos, key = { it.id }) { equipo ->
                    val local = locales.firstOrNull { it.id == equipo.localId }
                    val cliente = clientes.firstOrNull { it.id == local?.clienteId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${cliente?.nombre ?: "-"} / ${local?.nombre ?: "-"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(equipo.nombre, style = MaterialTheme.typography.titleLarge)
                            if (equipo.marca.isNotBlank()) {
                                Text("Marca: ${equipo.marca}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("Ubicación: ${equipo.ubicacion}", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "Última mantención: ${equipo.ultimaMantencion?.formatearFecha() ?: "sin registro"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { onEditar(equipo.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { equipoAEliminar = equipo }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    equipoAEliminar?.let { equipo ->
        DialogoConfirmacion(
            titulo = "Eliminar equipo",
            mensaje = "¿Desea eliminar el equipo '${equipo.nombre}'?",
            textoConfirmar = "Eliminar",
            onConfirmar = {
                viewModel.eliminarEquipo(equipo)
                equipoAEliminar = null
            },
            onCancelar = { equipoAEliminar = null }
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
