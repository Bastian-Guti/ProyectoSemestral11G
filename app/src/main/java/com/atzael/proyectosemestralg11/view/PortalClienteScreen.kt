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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.util.formatearFecha
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.view.components.CampoDesplegable
import com.atzael.proyectosemestralg11.view.components.TarjetaOrden
import com.atzael.proyectosemestralg11.viewmodel.PortalClienteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalClienteScreen(
    rut: String,
    nombre: String,
    viewModel: PortalClienteViewModel,
    onOrdenClick: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val empresa by viewModel.nombreEmpresa.collectAsState()
    val ordenes by viewModel.ordenes.collectAsState()
    val filtroEstado by viewModel.filtroEstado.collectAsState()
    val pendientes by viewModel.pendientesDeValidar.collectAsState()

    LaunchedEffect(rut) {
        viewModel.cargarUsuario(rut)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BarraSuperior(
                titulo = "Mis órdenes de trabajo",
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
                text = if (empresa.isNotBlank()) "Empresa: $empresa" else "Sin empresa asociada",
                style = MaterialTheme.typography.bodyMedium
            )

            if (pendientes > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text(
                        text = "Tiene $pendientes trabajo(s) finalizado(s) esperando su conformidad.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            CampoDesplegable(
                label = "Estado",
                valorSeleccionado = filtroEstado ?: "Todos",
                opciones = listOf<String?>(null) + EstadoOrden.todos,
                textoOpcion = { it ?: "Todos" },
                onSeleccion = { viewModel.onFiltroEstadoChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            if (ordenes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay órdenes de trabajo para mostrar.",
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
                            val etiqueta = when {
                                PortalClienteViewModel.requiereConformidad(item) -> "Pendiente de su conformidad"
                                item.orden.conformidad != null ->
                                    "${item.orden.conformidad} el ${item.orden.fechaConformidad?.formatearFecha() ?: ""}"
                                else -> null
                            }
                            etiqueta?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
