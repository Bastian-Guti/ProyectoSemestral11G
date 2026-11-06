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
import androidx.compose.material3.MaterialTheme
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
import com.atzael.proyectosemestralg11.view.components.SuccessToast
import com.atzael.proyectosemestralg11.viewmodel.ClienteViewModel

// clienteId == null -> registrar; si no, actualizar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteFormScreen(
    viewModel: ClienteViewModel,
    clienteId: Int?,
    onBack: () -> Unit
) {
    val nombre by viewModel.nombre.collectAsState()
    val contacto by viewModel.contacto.collectAsState()
    val telefono by viewModel.telefono.collectAsState()
    val slaHoras by viewModel.slaHoras.collectAsState()
    val isButtonEnabled by viewModel.isButtonEnabled.collectAsState()
    val showToast by viewModel.showToast.collectAsState()

    val nombreError by viewModel.nombreError.collectAsState()
    val contactoError by viewModel.contactoError.collectAsState()
    val telefonoError by viewModel.telefonoError.collectAsState()
    val slaError by viewModel.slaError.collectAsState()

    LaunchedEffect(clienteId) {
        if (clienteId != null) viewModel.cargarCliente(clienteId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BarraSuperior(
                titulo = if (clienteId == null) "Registrar cliente" else "Actualizar cliente",
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
            OutlinedTextField(
                value = nombre,
                onValueChange = { viewModel.onNombreChange(it) },
                label = { Text("Nombre del cliente / empresa") },
                modifier = Modifier.fillMaxWidth(),
                isError = nombreError != null,
                supportingText = { nombreError?.let { Text(it) } }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = contacto,
                onValueChange = { viewModel.onContactoChange(it) },
                label = { Text("Persona de contacto") },
                modifier = Modifier.fillMaxWidth(),
                isError = contactoError != null,
                supportingText = { contactoError?.let { Text(it) } }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { viewModel.onTelefonoChange(it) },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = telefonoError != null,
                supportingText = { telefonoError?.let { Text(it) } }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = slaHoras,
                onValueChange = { viewModel.onSlaChange(it) },
                label = { Text("SLA: horas máximas de atención por OT") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = slaError != null,
                supportingText = {
                    Text(slaError ?: "Se compara con el tiempo entre check-in y check-out", style = MaterialTheme.typography.bodySmall)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (clienteId == null) {
                        viewModel.registrarCliente { onBack() }
                    } else {
                        viewModel.actualizarCliente { onBack() }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isButtonEnabled
            ) {
                Text(if (clienteId == null) "Registrar" else "Actualizar")
            }
        }

        SuccessToast(
            show = showToast,
            message = if (clienteId == null) "Cliente registrado con éxito" else "Cliente actualizado con éxito",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
