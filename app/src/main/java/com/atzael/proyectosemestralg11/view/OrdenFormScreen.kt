package com.atzael.proyectosemestralg11.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.atzael.proyectosemestralg11.model.PrioridadOrden
import com.atzael.proyectosemestralg11.model.TipoOrden
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.view.components.CampoDesplegable
import com.atzael.proyectosemestralg11.view.components.CampoFecha
import com.atzael.proyectosemestralg11.view.components.SuccessToast
import com.atzael.proyectosemestralg11.viewmodel.OrdenViewModel

// ordenId == null -> crear una OT nueva; si no, editar la existente
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdenFormScreen(
    viewModel: OrdenViewModel,
    rutUsuario: String,
    ordenId: Int?,
    onBack: () -> Unit
) {
    val clientes by viewModel.clientes.collectAsState()
    val locales by viewModel.localesDisponibles.collectAsState()
    val equipos by viewModel.equiposDisponibles.collectAsState()
    val tecnicos by viewModel.tecnicos.collectAsState()

    val clienteId by viewModel.clienteId.collectAsState()
    val localId by viewModel.localId.collectAsState()
    val equipoId by viewModel.equipoId.collectAsState()
    val tipo by viewModel.tipo.collectAsState()
    val prioridad by viewModel.prioridad.collectAsState()
    val tecnicoRut by viewModel.tecnicoRut.collectAsState()
    val fechaVisita by viewModel.fechaVisita.collectAsState()
    val modoEdicion by viewModel.modoEdicion.collectAsState()
    val isButtonEnabled by viewModel.isButtonEnabled.collectAsState()
    val showToast by viewModel.showToast.collectAsState()

    LaunchedEffect(ordenId) {
        if (ordenId != null) viewModel.cargarOrden(ordenId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BarraSuperior(
                titulo = if (ordenId == null) "Crear orden de trabajo" else "Editar OT #$ordenId",
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CampoDesplegable(
                label = "Cliente",
                valorSeleccionado = clientes.firstOrNull { it.id == clienteId }?.nombre ?: "",
                opciones = clientes,
                textoOpcion = { it.nombre },
                onSeleccion = { viewModel.onClienteChange(it.id) },
                modifier = Modifier.fillMaxWidth()
            )

            CampoDesplegable(
                label = "Local",
                valorSeleccionado = locales.firstOrNull { it.id == localId }?.nombre ?: "",
                opciones = locales,
                textoOpcion = { it.nombre },
                onSeleccion = { viewModel.onLocalChange(it.id) },
                modifier = Modifier.fillMaxWidth(),
                enabled = clienteId != null
            )

            CampoDesplegable(
                label = "Equipo",
                valorSeleccionado = equipos.firstOrNull { it.id == equipoId }
                    ?.let { "${it.nombre} (${it.ubicacion})" } ?: "",
                opciones = equipos,
                textoOpcion = { "${it.nombre} (${it.ubicacion})" },
                onSeleccion = { viewModel.onEquipoChange(it.id) },
                modifier = Modifier.fillMaxWidth(),
                enabled = localId != null
            )

            CampoDesplegable(
                label = "Tipo",
                valorSeleccionado = tipo,
                opciones = TipoOrden.todos,
                textoOpcion = { it },
                onSeleccion = { viewModel.onTipoChange(it) },
                modifier = Modifier.fillMaxWidth()
            )

            CampoDesplegable(
                label = "Prioridad",
                valorSeleccionado = prioridad,
                opciones = PrioridadOrden.todos,
                textoOpcion = { it },
                onSeleccion = { viewModel.onPrioridadChange(it) },
                modifier = Modifier.fillMaxWidth()
            )

            CampoDesplegable(
                label = "Técnico asignado",
                valorSeleccionado = tecnicos.firstOrNull { it.rut == tecnicoRut }
                    ?.let { "${it.nombre} ${it.apellido}" } ?: "",
                opciones = tecnicos,
                textoOpcion = { "${it.nombre} ${it.apellido} (${it.rut})" },
                onSeleccion = { viewModel.onTecnicoChange(it.rut) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !modoEdicion,
                error = if (tecnicos.isEmpty()) "No hay técnicos registrados" else null
            )

            CampoFecha(
                label = "Fecha de visita",
                fecha = fechaVisita,
                onFechaSeleccionada = { viewModel.onFechaVisitaChange(it) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !modoEdicion
            )

            if (modoEdicion) {
                Text(
                    text = "El técnico y la fecha se cambian desde el detalle de la OT con \"Reagendar / Reasignar\", para dejar historial.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (ordenId == null) {
                        viewModel.registrarOrden(rutUsuario) { onBack() }
                    } else {
                        viewModel.actualizarOrden { onBack() }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isButtonEnabled
            ) {
                Text(if (ordenId == null) "Crear orden de trabajo" else "Actualizar")
            }
        }

        SuccessToast(
            show = showToast,
            message = if (ordenId == null) "Orden creada con éxito" else "Orden actualizada con éxito",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
