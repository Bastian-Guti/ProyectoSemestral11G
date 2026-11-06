package com.atzael.proyectosemestralg11.view

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import com.atzael.proyectosemestralg11.model.TipoFoto
import com.atzael.proyectosemestralg11.util.Ubicacion
import com.atzael.proyectosemestralg11.util.formatearDuracion
import com.atzael.proyectosemestralg11.util.formatearFecha
import com.atzael.proyectosemestralg11.util.formatearFechaHora
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.view.components.DialogoMensaje
import com.atzael.proyectosemestralg11.view.components.GaleriaFotos
import com.atzael.proyectosemestralg11.view.components.SeccionFotos
import com.atzael.proyectosemestralg11.view.components.SuccessToast
import com.atzael.proyectosemestralg11.viewmodel.TecnicoViewModel

// HU-09 a HU-13: aceptar o rechazar, check-in, informe, fotos, check-out y cierre
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TecnicoOrdenScreen(
    viewModel: TecnicoViewModel,
    ordenId: Int,
    onBack: () -> Unit,
    onVerFicha: (Int) -> Unit
) {
    val context = LocalContext.current
    val ordenes by viewModel.todasLasOrdenes.collectAsState()
    val fotos by viewModel.fotos.collectAsState()
    val avisoAceptado by viewModel.avisoAceptado.collectAsState()
    val diagnostico by viewModel.diagnostico.collectAsState()
    val actividades by viewModel.actividades.collectAsState()
    val materiales by viewModel.materiales.collectAsState()
    val mensajeExito by viewModel.mensajeExito.collectAsState()
    val error by viewModel.error.collectAsState()

    var mostrarRechazo by remember { mutableStateOf(false) }
    var obteniendoUbicacion by remember { mutableStateOf(false) }
    var alResponderPermiso by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }

    val permisoUbicacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        alResponderPermiso?.invoke(concedido)
        alResponderPermiso = null
    }

    // Pide la ubicación solo en este momento; si se niega o no hay señal, queda registrada solo la hora
    fun conUbicacion(accion: (Pair<Double, Double>?) -> Unit) {
        obteniendoUbicacion = true
        val terminar: (Pair<Double, Double>?) -> Unit = {
            obteniendoUbicacion = false
            accion(it)
        }
        if (Ubicacion.tienePermiso(context)) {
            Ubicacion.obtenerUnaVez(context, terminar)
        } else {
            alResponderPermiso = { concedido ->
                if (concedido) Ubicacion.obtenerUnaVez(context, terminar) else terminar(null)
            }
            permisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(ordenId) {
        viewModel.seleccionarOrden(ordenId)
    }

    // Solo se busca entre las OT asignadas a este técnico
    val item: OrdenConDetalle? = ordenes.firstOrNull { it.orden.id == ordenId }

    LaunchedEffect(item?.orden?.id) {
        item?.let { viewModel.cargarInforme(it.orden) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { BarraSuperior(titulo = "OT #$ordenId", onBack = onBack) }
    ) { innerPadding ->
        if (item == null) {
            Text(
                text = "No se encontró la orden de trabajo.",
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        } else {
            val orden = item.orden
            val datosPermitidos = avisoAceptado == true

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(orden.estado, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Dato("Cliente", item.clienteNombre)
                        Dato("Local", item.localNombre)
                        Dato("Equipo", "${item.equipoNombre} (${item.equipoUbicacion})")
                        Dato("Tipo", orden.tipo)
                        Dato("Prioridad", orden.prioridad)
                        Dato("Fecha de visita", orden.fechaVisita.formatearFecha())
                    }
                }

                OutlinedButton(onClick = { onVerFicha(orden.equipoId) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver ficha e historial del equipo")
                }

                if (!datosPermitidos && orden.estado in listOf(EstadoOrden.ACEPTADA, EstadoOrden.EN_CURSO)) {
                    Text(
                        text = "Acepte el aviso de datos personales en su jornada para registrar ubicación y fotos.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                when (orden.estado) {
                    EstadoOrden.PENDIENTE -> {
                        Text("Esta orden espera su respuesta.", style = MaterialTheme.typography.bodyMedium)
                        Button(onClick = { viewModel.aceptarOrden(orden) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Aceptar orden")
                        }
                        Button(
                            onClick = { mostrarRechazo = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("Rechazar orden") }
                    }

                    EstadoOrden.ACEPTADA -> {
                        Text("Al llegar al local registre su check-in.", style = MaterialTheme.typography.bodyMedium)
                        Button(
                            onClick = { conUbicacion { viewModel.registrarCheckIn(orden, it) } },
                            enabled = datosPermitidos && !obteniendoUbicacion,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (obteniendoUbicacion) "Obteniendo ubicación..." else "Registrar check-in (llegada)") }
                        OutlinedButton(onClick = { mostrarRechazo = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Rechazar orden")
                        }
                    }

                    EstadoOrden.EN_CURSO -> {
                        Resumen(item)

                        Text("Informe del trabajo", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = diagnostico,
                            onValueChange = { viewModel.onDiagnosticoChange(it) },
                            label = { Text("Diagnóstico (obligatorio)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        OutlinedTextField(
                            value = actividades,
                            onValueChange = { viewModel.onActividadesChange(it) },
                            label = { Text("Actividades realizadas") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        OutlinedTextField(
                            value = materiales,
                            onValueChange = { viewModel.onMaterialesChange(it) },
                            label = { Text("Materiales usados") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        OutlinedButton(onClick = { viewModel.guardarInforme(orden) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Guardar informe")
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        TipoFoto.todos.forEach { tipo ->
                            SeccionFotos(
                                titulo = "Fotos ($tipo)",
                                ordenId = orden.id,
                                fotos = fotos.filter { it.tipo == tipo },
                                habilitado = datosPermitidos,
                                onFotoNueva = { archivo -> viewModel.registrarFoto(orden, archivo.absolutePath, tipo) },
                                onEliminar = { viewModel.eliminarFoto(it) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        if (orden.checkOut == null) {
                            Button(
                                onClick = { conUbicacion { viewModel.registrarCheckOut(orden, it) } },
                                enabled = datosPermitidos && !obteniendoUbicacion,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(if (obteniendoUbicacion) "Obteniendo ubicación..." else "Registrar check-out (salida)") }
                        }
                        Button(
                            onClick = { viewModel.cerrarOrden(orden) { onBack() } },
                            enabled = orden.checkOut != null && diagnostico.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Cerrar orden de trabajo") }
                        if (orden.checkOut == null || diagnostico.isBlank()) {
                            Text(
                                text = "Para cerrar la OT necesita el check-out y el diagnóstico.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    else -> {
                        if (orden.estado == EstadoOrden.RECHAZADA_TECNICO) {
                            Text("Motivo del rechazo: ${orden.motivoRechazo ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Resumen(item)
                        if (fotos.isNotEmpty()) {
                            Text("Fotos", style = MaterialTheme.typography.titleSmall)
                            GaleriaFotos(fotos = fotos)
                        }
                    }
                }
            }

            if (mostrarRechazo) {
                DialogoRechazo(
                    onCancelar = { mostrarRechazo = false },
                    onConfirmar = { motivo ->
                        viewModel.rechazarOrden(orden, motivo) { onBack() }
                        mostrarRechazo = false
                    }
                )
            }
        }

        SuccessToast(
            show = mensajeExito != null,
            message = mensajeExito ?: "",
            modifier = Modifier.padding(innerPadding)
        )
    }

    error?.let { mensaje ->
        DialogoMensaje(titulo = "No se pudo continuar", mensaje = mensaje, onCerrar = { viewModel.descartarError() })
    }
}

@Composable
private fun Resumen(item: OrdenConDetalle) {
    val orden = item.orden
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Registro en terreno", style = MaterialTheme.typography.titleMedium)
            Dato("Check-in", orden.checkIn?.formatearFechaHora() ?: "Sin registrar")
            Dato("Check-out", orden.checkOut?.formatearFechaHora() ?: "Sin registrar")
            if (orden.checkIn != null && orden.checkOut != null && orden.checkOut >= orden.checkIn) {
                Dato("Tiempo de atención", formatearDuracion((orden.checkOut - orden.checkIn) / 60_000))
            }
            orden.diagnostico?.let { Dato("Diagnóstico", it) }
            orden.actividades?.let { Dato("Actividades", it) }
            orden.materiales?.let { Dato("Materiales", it) }
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Column {
        Text(text = etiqueta, style = MaterialTheme.typography.labelSmall)
        Text(text = valor, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DialogoRechazo(onCancelar: () -> Unit, onConfirmar: (String) -> Unit) {
    var motivo by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Rechazar orden") },
        text = {
            OutlinedTextField(
                value = motivo,
                onValueChange = { motivo = it },
                label = { Text("Motivo (obligatorio)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(motivo) },
                enabled = motivo.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Rechazar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Volver") } }
    )
}
