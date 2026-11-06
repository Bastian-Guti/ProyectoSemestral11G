package com.atzael.proyectosemestralg11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atzael.proyectosemestralg11.data.ClienteDao
import com.atzael.proyectosemestralg11.data.FotoDao
import com.atzael.proyectosemestralg11.data.LocalDao
import com.atzael.proyectosemestralg11.data.OrdenTrabajoDao
import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.model.Local
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import com.atzael.proyectosemestralg11.util.formatearDuracion
import com.atzael.proyectosemestralg11.util.formatearFecha
import com.atzael.proyectosemestralg11.util.formatearFechaHora
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class FilaReporte(
    val orden: OrdenConDetalle,
    val tiempoAtencionMin: Long?, // check-out - check-in; null si aún no se atiende
    val dentroSla: Boolean?
)

data class ResumenReporte(
    val filas: List<FilaReporte> = emptyList(),
    val atendidas: Int = 0,
    val promedioMin: Long? = null,
    val dentroSla: Int = 0
) {
    val porcentajeSla: Int? get() = if (atendidas == 0) null else dentroSla * 100 / atendidas
}

class ReporteViewModel(
    private val ordenDao: OrdenTrabajoDao,
    clienteDao: ClienteDao,
    localDao: LocalDao,
    private val fotoDao: FotoDao
) : ViewModel() {

    val clientes: StateFlow<List<Cliente>> = clienteDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val todosLosLocales: StateFlow<List<Local>> = localDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _clienteId = MutableStateFlow<Int?>(null)
    val clienteId: StateFlow<Int?> = _clienteId.asStateFlow()

    private val _localId = MutableStateFlow<Int?>(null)
    val localId: StateFlow<Int?> = _localId.asStateFlow()

    private val _periodo = MutableStateFlow(PERIODOS.first())
    val periodo: StateFlow<String> = _periodo.asStateFlow()

    val localesDisponibles: StateFlow<List<Local>> = combine(_clienteId, todosLosLocales) { cliente, locales ->
        if (cliente == null) emptyList() else locales.filter { it.clienteId == cliente }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val resumen: StateFlow<ResumenReporte> = combine(
        ordenDao.getAllConDetalle(), _clienteId, _localId, _periodo
    ) { ordenes, clienteId, localId, periodo ->
        calcularResumen(ordenes, clienteId, localId, periodo, System.currentTimeMillis())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ResumenReporte()
    )

    fun onClienteChange(id: Int?) {
        _clienteId.value = id
        _localId.value = null
    }

    fun onLocalChange(id: Int?) {
        _localId.value = id
    }

    fun onPeriodoChange(periodo: String) {
        _periodo.value = periodo
    }

    // Archivos de las fotos de las OT del reporte, para adjuntarlas como evidencia (HU-21)
    suspend fun rutasFotosDelReporte(): List<String> {
        val ids = resumen.value.filas.map { it.orden.orden.id }
        if (ids.isEmpty()) return emptyList()
        return fotoDao.getByOrdenes(ids).map { it.ruta }.filter { java.io.File(it).exists() }
    }

    // Texto del reporte para compartir con el cliente (HU-21)
    fun construirTextoReporte(): String {
        val resumen = resumen.value
        val cliente = clientes.value.firstOrNull { it.id == _clienteId.value }?.nombre ?: "Todos los clientes"
        return buildString {
            appendLine("Reporte de órdenes de trabajo")
            appendLine("Cliente: $cliente")
            appendLine("Período: ${_periodo.value}")
            appendLine("OT incluidas: ${resumen.filas.size}")
            resumen.promedioMin?.let { appendLine("Tiempo promedio de atención: ${formatearDuracion(it)}") }
            resumen.porcentajeSla?.let {
                appendLine("Cumplimiento de SLA: $it% (${resumen.dentroSla} de ${resumen.atendidas} atendidas)")
            }
            appendLine()
            resumen.filas.forEach { fila ->
                val o = fila.orden.orden
                appendLine("OT #${o.id} - ${fila.orden.localNombre} - ${fila.orden.equipoNombre}")
                appendLine("  Tipo: ${o.tipo} | Estado: ${o.estado} | Visita: ${o.fechaVisita.formatearFecha()}")
                appendLine("  Técnico: ${fila.orden.tecnicoNombre}")
                if (o.checkIn != null && o.checkOut != null) {
                    appendLine("  Check-in: ${o.checkIn.formatearFechaHora()} | Check-out: ${o.checkOut.formatearFechaHora()}")
                    fila.tiempoAtencionMin?.let { appendLine("  Tiempo de atención: ${formatearDuracion(it)}") }
                }
                o.diagnostico?.let { appendLine("  Diagnóstico: $it") }
                o.actividades?.let { appendLine("  Actividades: $it") }
                o.materiales?.let { appendLine("  Materiales: $it") }
                if (fila.orden.cantidadFotos > 0) appendLine("  Evidencias: ${fila.orden.cantidadFotos} foto(s) adjuntas")
            }
        }
    }

    companion object {
        val PERIODOS = listOf("Todo el historial", "Últimos 7 días", "Últimos 30 días", "Últimos 90 días")
        private const val MILIS_DIA = 24L * 60 * 60 * 1000

        fun calcularResumen(
            ordenes: List<OrdenConDetalle>,
            clienteId: Int?,
            localId: Int?,
            periodo: String,
            ahora: Long
        ): ResumenReporte {
            val dias = when (periodo) {
                PERIODOS[1] -> 7
                PERIODOS[2] -> 30
                PERIODOS[3] -> 90
                else -> null
            }
            val desde = dias?.let { ahora - it * MILIS_DIA }

            val filas = ordenes
                .filter { it.orden.estado != EstadoOrden.CANCELADA }
                .filter { clienteId == null || it.orden.clienteId == clienteId }
                .filter { localId == null || it.orden.localId == localId }
                .filter { desde == null || it.orden.fechaVisita >= desde }
                .map { item ->
                    val checkIn = item.orden.checkIn
                    val checkOut = item.orden.checkOut
                    val minutos = if (checkIn != null && checkOut != null && checkOut >= checkIn) {
                        (checkOut - checkIn) / 60_000
                    } else {
                        null
                    }
                    FilaReporte(
                        orden = item,
                        tiempoAtencionMin = minutos,
                        dentroSla = minutos?.let { it <= item.slaHoras * 60L }
                    )
                }

            val atendidas = filas.filter { it.tiempoAtencionMin != null }
            return ResumenReporte(
                filas = filas,
                atendidas = atendidas.size,
                promedioMin = if (atendidas.isEmpty()) null else atendidas.sumOf { it.tiempoAtencionMin!! } / atendidas.size,
                dentroSla = atendidas.count { it.dentroSla == true }
            )
        }
    }
}
