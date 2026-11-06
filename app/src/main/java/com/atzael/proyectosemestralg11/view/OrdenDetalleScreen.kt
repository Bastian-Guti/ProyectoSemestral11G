package com.atzael.proyectosemestralg11.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.model.MotivoReagendar
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import com.atzael.proyectosemestralg11.model.TipoFoto
import com.atzael.proyectosemestralg11.model.Usuario
import com.atzael.proyectosemestralg11.view.components.GaleriaFotos
import com.atzael.proyectosemestralg11.util.formatearDuracion
import com.atzael.proyectosemestralg11.util.formatearFecha
import com.atzael.proyectosemestralg11.util.formatearFechaHora
import com.atzael.proyectosemestralg11.view.components.BarraSuperior
import com.atzael.proyectosemestralg11.view.components.CampoDesplegable
import com.atzael.proyectosemestralg11.view.components.CampoFecha
import com.atzael.proyectosemestralg11.view.components.DialogoConfirmacion
import com.atzael.proyectosemestralg11.view.components.SuccessToast
import com.atzael.proyectosemestralg11.viewmodel.OrdenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdenDetalleScreen(
    viewModel: OrdenViewModel,
    ordenId: Int,
    onBack: () -> Unit,
    onEditar: (Int) -> Unit,
    onVerFicha: (Int) -> Unit
) {
    val detalle by viewModel.ordenDetalle.collectAsState()
    val fotos by viewModel.fotos.collectAsState()
    val reprogramaciones by viewModel.reprogramaciones.collectAsState()
    val tecnicos by viewModel.tecnicos.collectAsState()
    val showToast by viewModel.showToast.collectAsState()

    var mostrarReagendar by remember { mutableStateOf(false) }
    var mostrarCancelar by remember { mutableStateOf(false) }

    LaunchedEffect(ordenId) {
        viewModel.seleccionarOrden(ordenId)
    }

    // Evita mostrar un instante la OT anterior mientras carga la nueva
    val item: OrdenConDetalle? = detalle?.takeIf { it.orden.id == ordenId }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { BarraSuperior(titulo = "OT #$ordenId", onBack = onBack) }
    ) { innerPadding ->
        if (item == null) {
            Text(
                text = "Cargando...",
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        } else {
            val orden = item.orden
            val abierta = orden.estado in EstadoOrden.gestionablesPorJefatura

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = orden.estado, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Dato("Cliente", item.clienteNombre)
                        Dato("Local", item.localNombre)
                        Dato("Equipo", "${item.equipoNombre} (${item.equipoUbicacion})")
                        Dato("Tipo", orden.tipo)
                        Dato("Prioridad", orden.prioridad)
                        Dato("Técnico", item.tecnicoNombre)
                        Dato("Fecha de visita", orden.fechaVisita.formatearFecha())
                        Dato("Creada", orden.fechaCreacion.formatearFechaHora())
                        Dato("Creada por (RUT)", orden.creadaPor)
                    }
                }

                OutlinedButton(onClick = { onVerFicha(orden.equipoId) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver ficha e historial del equipo")
                }

                if (orden.estado == EstadoOrden.RECHAZADA_TECNICO) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("El técnico rechazó esta orden", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                            Text("Motivo: ${orden.motivoRechazo ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                            Text("Reasígnela con \"Reagendar / Reasignar\".", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Atención en terreno", style = MaterialTheme.typography.titleMedium)
                        Dato("Check-in", orden.checkIn?.formatearFechaHora() ?: "Sin registrar")
                        orden.checkInLat?.let { lat -> Dato("Ubicación del check-in", "%.5f, %.5f".format(lat, orden.checkInLon ?: 0.0)) }
                        Dato("Check-out", orden.checkOut?.formatearFechaHora() ?: "Sin registrar")
                        orden.checkOutLat?.let { lat -> Dato("Ubicación del check-out", "%.5f, %.5f".format(lat, orden.checkOutLon ?: 0.0)) }
                        if (orden.checkIn != null && orden.checkOut != null && orden.checkOut >= orden.checkIn) {
                            Dato("Tiempo de atención", formatearDuracion((orden.checkOut - orden.checkIn) / 60_000))
                        }
                        Dato("Diagnóstico", orden.diagnostico ?: "Sin registrar")
                        orden.actividades?.let { Dato("Actividades", it) }
                        orden.materiales?.let { Dato("Materiales", it) }
                    }
                }

                Text("Fotos del equipo", style = MaterialTheme.typography.titleMedium)
                TipoFoto.todos.forEach { tipo ->
                    Text(tipo, style = MaterialTheme.typography.labelLarge)
                    GaleriaFotos(fotos = fotos.filter { it.tipo == tipo })
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Conformidad del cliente", style = MaterialTheme.typography.titleMedium)
                        if (orden.conformidad == null) {
                            Dato("Estado", "Sin pronunciamiento del cliente")
                        } else {
                            Dato("Resultado", orden.conformidad)
                            Dato("Fecha", orden.fechaConformidad?.formatearFechaHora() ?: "-")
                            Dato("Validó (RUT)", orden.conformidadPor ?: "-")
                            orden.observacionCliente?.let { Dato("Observaciones", it) }
                        }
                    }
                }

                Text("Historial de reprogramaciones", style = MaterialTheme.typography.titleMedium)
                if (reprogramaciones.isEmpty()) {
                    Text("Esta OT no ha sido reprogramada.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    reprogramaciones.forEach { rep ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "${rep.fechaAnterior.formatearFecha()} -> ${rep.fechaNueva.formatearFecha()}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (rep.tecnicoAnterior != rep.tecnicoNuevo) {
                                    Text(
                                        text = "Técnico: ${nombreTecnico(tecnicos, rep.tecnicoAnterior)} -> ${nombreTecnico(tecnicos, rep.tecnicoNuevo)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    text = "Motivo: ${rep.motivo} (${rep.fechaRegistro.formatearFechaHora()})",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                if (orden.estado == EstadoOrden.PENDIENTE) {
                    OutlinedButton(onClick = { onEditar(orden.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Editar datos de la OT")
                    }
                }
                if (abierta) {
                    Button(onClick = { mostrarReagendar = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Reagendar / Reasignar")
                    }
                    Button(
                        onClick = { mostrarCancelar = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancelar OT")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (mostrarReagendar) {
                DialogoReagendar(
                    tecnicos = tecnicos,
                    fechaActual = orden.fechaVisita,
                    tecnicoActual = orden.tecnicoRut,
                    onCancelar = { mostrarReagendar = false },
                    onConfirmar = { fecha, tecnicoRut, motivo ->
                        viewModel.reagendarOrden(orden, fecha, tecnicoRut, motivo)
                        mostrarReagendar = false
                    }
                )
            }

            if (mostrarCancelar) {
                DialogoConfirmacion(
                    titulo = "Cancelar orden de trabajo",
                    mensaje = "¿Desea cancelar la OT #${orden.id} de '${item.clienteNombre}'? Quedará registrada como cancelada.",
                    textoConfirmar = "Cancelar OT",
                    onConfirmar = {
                        viewModel.cancelarOrden(orden)
                        mostrarCancelar = false
                    },
                    onCancelar = { mostrarCancelar = false }
                )
            }
        }

        SuccessToast(
            show = showToast,
            message = "Orden reagendada con éxito",
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Column {
        Text(text = etiqueta, style = MaterialTheme.typography.labelSmall)
        Text(text = valor, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun nombreTecnico(tecnicos: List<Usuario>, rut: String): String =
    tecnicos.firstOrNull { it.rut == rut }?.let { "${it.nombre} ${it.apellido}" } ?: rut

@Composable
private fun DialogoReagendar(
    tecnicos: List<Usuario>,
    fechaActual: Long,
    tecnicoActual: String,
    onCancelar: () -> Unit,
    onConfirmar: (Long, String, String) -> Unit
) {
    var fecha by remember { mutableStateOf(fechaActual) }
    var tecnicoRut by remember { mutableStateOf(tecnicoActual) }
    var motivo by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Reagendar / Reasignar") },
        text = {
            Column {
                CampoFecha(
                    label = "Nueva fecha de visita",
                    fecha = fecha,
                    onFechaSeleccionada = { fecha = it },
                    modifier = Modifier.fillMaxWidth()
                )
                CampoDesplegable(
                    label = "Técnico",
                    valorSeleccionado = nombreTecnico(tecnicos, tecnicoRut),
                    opciones = tecnicos,
                    textoOpcion = { "${it.nombre} ${it.apellido}" },
                    onSeleccion = { tecnicoRut = it.rut },
                    modifier = Modifier.fillMaxWidth()
                )
                CampoDesplegable(
                    label = "Motivo",
                    valorSeleccionado = motivo ?: "",
                    opciones = MotivoReagendar.todos,
                    textoOpcion = { it },
                    onSeleccion = { motivo = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { motivo?.let { onConfirmar(fecha, tecnicoRut, it) } },
                enabled = motivo != null && (fecha != fechaActual || tecnicoRut != tecnicoActual)
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Volver") }
        }
    )
}
