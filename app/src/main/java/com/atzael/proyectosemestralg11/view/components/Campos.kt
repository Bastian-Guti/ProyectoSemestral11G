package com.atzael.proyectosemestralg11.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.atzael.proyectosemestralg11.util.formatearFecha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CampoDesplegable(
    label: String,
    valorSeleccionado: String,
    opciones: List<T>,
    textoOpcion: (T) -> String,
    onSeleccion: (T) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido && enabled,
        onExpandedChange = { if (enabled) expandido = !expandido },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = valorSeleccionado,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            isError = error != null,
            supportingText = { error?.let { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = enabled)
        )
        ExposedDropdownMenu(
            expanded = expandido && enabled,
            onDismissRequest = { expandido = false }
        ) {
            if (opciones.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Sin opciones disponibles") },
                    onClick = { expandido = false },
                    enabled = false
                )
            }
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(textoOpcion(opcion)) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    label: String,
    fecha: Long?,
    onFechaSeleccionada: (Long) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true
) {
    var mostrarSelector by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = fecha?.formatearFecha() ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            isError = error != null,
            supportingText = { error?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        if (enabled) {
            // Captura el toque sobre todo el campo para abrir el calendario
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { mostrarSelector = true }
            )
        }
    }

    if (mostrarSelector) {
        val estado = rememberDatePickerState(initialSelectedDateMillis = fecha)
        DatePickerDialog(
            onDismissRequest = { mostrarSelector = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estado.selectedDateMillis?.let(onFechaSeleccionada)
                        mostrarSelector = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSelector = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estado)
        }
    }
}
