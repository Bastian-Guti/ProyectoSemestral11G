package com.atzael.proyectosemestralg11.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.view.components.CampoDesplegable
import com.atzael.proyectosemestralg11.view.components.SuccessToast
import com.atzael.proyectosemestralg11.viewmodel.LocalViewModel

// localId == null -> registrar; si no, actualizar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalFormScreen(
    viewModel: LocalViewModel,
    localId: Int?,
    onBack: () -> Unit
) {
    val clientes by viewModel.clientes.collectAsState()
    val clienteId by viewModel.clienteId.collectAsState()
    val nombre by viewModel.nombre.collectAsState()
    val direccion by viewModel.direccion.collectAsState()
    val telefono by viewModel.telefono.collectAsState()
    val ciudad by viewModel.ciudad.collectAsState()
    val isButtonEnabled by viewModel.isButtonEnabled.collectAsState()
    val showToast by viewModel.showToast.collectAsState()

    val nombreError by viewModel.nombreError.collectAsState()
    val direccionError by viewModel.direccionError.collectAsState()
    val telefonoError by viewModel.telefonoError.collectAsState()

    LaunchedEffect(localId) {
        if (localId != null) viewModel.cargarLocal(localId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BarraSuperior(
                titulo = if (localId == null) "Registrar local" else "Actualizar local",
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
                modifier = Modifier.fillMaxWidth(),
                error = if (clientes.isEmpty()) "Primero registre un cliente" else null
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { viewModel.onNombreChange(it) },
                label = { Text("Nombre del local") },
                modifier = Modifier.fillMaxWidth(),
                isError = nombreError != null,
                supportingText = { nombreError?.let { Text(it) } }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = direccion,
                onValueChange = { viewModel.onDireccionChange(it) },
                label = { Text("Dirección / ubicación") },
                modifier = Modifier.fillMaxWidth(),
                isError = direccionError != null,
                supportingText = { direccionError?.let { Text(it) } }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { viewModel.onTelefonoChange(it) },
                label = { Text("Teléfono de contacto") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = telefonoError != null,
                supportingText = { telefonoError?.let { Text(it) } }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = ciudad,
                onValueChange = { viewModel.onCiudadChange(it) },
                label = { Text("Ciudad (opcional)") },
                supportingText = { Text("Se usa para las alertas de lluvia y viento en equipos exteriores") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (localId == null) {
                        viewModel.registrarLocal { onBack() }
                    } else {
                        viewModel.actualizarLocal { onBack() }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isButtonEnabled
            ) {
                Text(if (localId == null) "Registrar" else "Actualizar")
            }
        }

        SuccessToast(
            show = showToast,
            message = if (localId == null) "Local registrado con éxito" else "Local actualizado con éxito",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
