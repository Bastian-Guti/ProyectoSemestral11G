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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.model.UbicacionEquipo
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.view.components.CampoDesplegable
import com.atzael.proyectosemestralg11.view.components.CampoFecha
import com.atzael.proyectosemestralg11.view.components.SuccessToast
import com.atzael.proyectosemestralg11.viewmodel.EquipoViewModel

// equipoId == null -> registrar; si no, actualizar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipoFormScreen(
    viewModel: EquipoViewModel,
    equipoId: Int?,
    onBack: () -> Unit
) {
    val locales by viewModel.locales.collectAsState()
    val clientes by viewModel.clientes.collectAsState()
    val localId by viewModel.localId.collectAsState()
    val nombre by viewModel.nombre.collectAsState()
    val marca by viewModel.marca.collectAsState()
    val ubicacion by viewModel.ubicacion.collectAsState()
    val ultimaMantencion by viewModel.ultimaMantencion.collectAsState()
    val nombreError by viewModel.nombreError.collectAsState()
    val isButtonEnabled by viewModel.isButtonEnabled.collectAsState()
    val showToast by viewModel.showToast.collectAsState()

    LaunchedEffect(equipoId) {
        if (equipoId != null) viewModel.cargarEquipo(equipoId)
    }

    fun textoLocal(localId: Int): String {
        val local = locales.firstOrNull { it.id == localId } ?: return ""
        val cliente = clientes.firstOrNull { it.id == local.clienteId }?.nombre ?: "-"
        return "$cliente / ${local.nombre}"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BarraSuperior(
                titulo = if (equipoId == null) "Registrar equipo" else "Actualizar equipo",
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
                label = "Local",
                valorSeleccionado = localId?.let { textoLocal(it) } ?: "",
                opciones = locales,
                textoOpcion = { textoLocal(it.id) },
                onSeleccion = { viewModel.onLocalChange(it.id) },
                modifier = Modifier.fillMaxWidth(),
                error = if (locales.isEmpty()) "Primero registre un local" else null
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { viewModel.onNombreChange(it) },
                label = { Text("Equipo (ej: Split 18000 BTU)") },
                modifier = Modifier.fillMaxWidth(),
                isError = nombreError != null,
                supportingText = { nombreError?.let { Text(it) } }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = marca,
                onValueChange = { viewModel.onMarcaChange(it) },
                label = { Text("Marca / modelo (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            CampoDesplegable(
                label = "Ubicación del equipo",
                valorSeleccionado = ubicacion,
                opciones = UbicacionEquipo.todos,
                textoOpcion = { it },
                onSeleccion = { viewModel.onUbicacionChange(it) },
                modifier = Modifier.fillMaxWidth()
            )

            CampoFecha(
                label = "Fecha de última mantención (opcional)",
                fecha = ultimaMantencion,
                onFechaSeleccionada = { viewModel.onUltimaMantencionChange(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (equipoId == null) {
                        viewModel.registrarEquipo { onBack() }
                    } else {
                        viewModel.actualizarEquipo { onBack() }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isButtonEnabled
            ) {
                Text(if (equipoId == null) "Registrar" else "Actualizar")
            }
        }

        SuccessToast(
            show = showToast,
            message = if (equipoId == null) "Equipo registrado con éxito" else "Equipo actualizado con éxito",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
