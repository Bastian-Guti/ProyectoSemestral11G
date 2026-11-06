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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.model.PrioridadOrden
import com.atzael.proyectosemestralg11.model.Usuario
import com.atzael.proyectosemestralg11.util.formatearFecha
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.view.components.CampoDesplegable
import com.atzael.proyectosemestralg11.view.components.CampoFecha
import com.atzael.proyectosemestralg11.view.components.TarjetaOrden
import com.atzael.proyectosemestralg11.viewmodel.OrdenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdenesScreen(
    viewModel: OrdenViewModel,
    onBack: () -> Unit,
    onNuevaOrden: () -> Unit,
    onOrdenClick: (Int) -> Unit
) {
    val ordenes by viewModel.ordenesFiltradas.collectAsState()
    val filtros by viewModel.filtros.collectAsState()
    val clientes by viewModel.clientes.collectAsState()
    val tecnicos by viewModel.tecnicos.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { BarraSuperior(titulo = "Órdenes de trabajo", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevaOrden) {
                Icon(Icons.Default.Add, contentDescription = "Crear orden de trabajo")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoDesplegable(
                    label = "Estado",
                    valorSeleccionado = filtros.estado ?: "Todos",
                    opciones = listOf<String?>(null) + EstadoOrden.todos,
                    textoOpcion = { it ?: "Todos" },
                    onSeleccion = { viewModel.onFiltroEstadoChange(it) },
                    modifier = Modifier.weight(1f)
                )
                CampoDesplegable(
                    label = "Prioridad",
                    valorSeleccionado = filtros.prioridad ?: "Todas",
                    opciones = listOf<String?>(null) + PrioridadOrden.todos,
                    textoOpcion = { it ?: "Todas" },
                    onSeleccion = { viewModel.onFiltroPrioridadChange(it) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoDesplegable(
                    label = "Técnico",
                    valorSeleccionado = tecnicos.firstOrNull { it.rut == filtros.tecnicoRut }
                        ?.let { "${it.nombre} ${it.apellido}" } ?: "Todos",
                    opciones = listOf<Usuario?>(null) + tecnicos,
                    textoOpcion = { it?.let { u -> "${u.nombre} ${u.apellido}" } ?: "Todos" },
                    onSeleccion = { viewModel.onFiltroTecnicoChange(it?.rut) },
                    modifier = Modifier.weight(1f)
                )
                CampoDesplegable(
                    label = "Cliente",
                    valorSeleccionado = clientes.firstOrNull { it.id == filtros.clienteId }?.nombre ?: "Todos",
                    opciones = listOf<Cliente?>(null) + clientes,
                    textoOpcion = { it?.nombre ?: "Todos" },
                    onSeleccion = { viewModel.onFiltroClienteChange(it?.id) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                CampoFecha(
                    label = "Fecha de visita",
                    fecha = filtros.fecha,
                    onFechaSeleccionada = { viewModel.onFiltroFechaChange(it) },
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { viewModel.limpiarFiltros() },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Limpiar filtros")
                }
            }

            Text(
                text = "${ordenes.size} orden(es)" +
                        (filtros.fecha?.let { " - visita el ${it.formatearFecha()}" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (ordenes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay órdenes que coincidan.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 88.dp)
                ) {
                    items(ordenes, key = { it.orden.id }) { item ->
                        TarjetaOrden(item = item, onClick = { onOrdenClick(item.orden.id) })
                    }
                }
            }
        }
    }
}
