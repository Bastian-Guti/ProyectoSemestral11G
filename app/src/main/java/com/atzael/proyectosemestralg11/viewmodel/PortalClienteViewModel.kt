package com.atzael.proyectosemestralg11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atzael.proyectosemestralg11.data.ClienteDao
import com.atzael.proyectosemestralg11.data.OrdenTrabajoDao
import com.atzael.proyectosemestralg11.data.UsuarioDao
import com.atzael.proyectosemestralg11.model.EstadoConformidad
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Portal del usuario con rol Cliente: ve solo las OT de su empresa (HU-17) y
// valida la conformidad del trabajo finalizado (HU-18)
@OptIn(ExperimentalCoroutinesApi::class)
class PortalClienteViewModel(
    private val ordenDao: OrdenTrabajoDao,
    private val usuarioDao: UsuarioDao,
    private val clienteDao: ClienteDao
) : ViewModel() {

    private val _clienteId = MutableStateFlow<Int?>(null)

    private val _nombreEmpresa = MutableStateFlow("")
    val nombreEmpresa: StateFlow<String> = _nombreEmpresa.asStateFlow()

    // Todas las consultas parten del clienteId del usuario: nunca se piden OT de otra empresa
    val todasLasOrdenes: StateFlow<List<OrdenConDetalle>> = _clienteId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else ordenDao.getByCliente(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _filtroEstado = MutableStateFlow<String?>(null)
    val filtroEstado: StateFlow<String?> = _filtroEstado.asStateFlow()

    val ordenes: StateFlow<List<OrdenConDetalle>> = combine(todasLasOrdenes, _filtroEstado) { lista, estado ->
        if (estado == null) lista else lista.filter { it.orden.estado == estado }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pendientesDeValidar: StateFlow<Int> = todasLasOrdenes
        .map { lista -> lista.count { requiereConformidad(it) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _observaciones = MutableStateFlow("")
    val observaciones: StateFlow<String> = _observaciones.asStateFlow()

    private val _observacionesError = MutableStateFlow<String?>(null)
    val observacionesError: StateFlow<String?> = _observacionesError.asStateFlow()

    private val _showToast = MutableStateFlow(false)
    val showToast: StateFlow<Boolean> = _showToast.asStateFlow()

    fun cargarUsuario(rut: String) {
        viewModelScope.launch {
            val usuario = usuarioDao.getUsuarioByRut(rut)
            val clienteId = usuario?.clienteId
            _clienteId.value = clienteId
            _nombreEmpresa.value = clienteId?.let { clienteDao.getById(it)?.nombre } ?: ""
        }
    }

    fun onFiltroEstadoChange(estado: String?) {
        _filtroEstado.value = estado
    }

    fun onObservacionesChange(newValue: String) {
        _observaciones.value = newValue
        _observacionesError.value = null
    }

    fun resetFormulario() {
        _observaciones.value = ""
        _observacionesError.value = null
    }

    // Confirma el trabajo, o lo rechaza con observaciones obligatorias
    fun validarConformidad(item: OrdenConDetalle, conforme: Boolean, rutUsuario: String, onSuccess: () -> Unit = {}) {
        if (!requiereConformidad(item)) return
        val texto = _observaciones.value.trim()
        if (!conforme && texto.isBlank()) {
            _observacionesError.value = "Indique las observaciones para rechazar el trabajo"
            return
        }
        viewModelScope.launch {
            ordenDao.update(
                item.orden.copy(
                    conformidad = if (conforme) EstadoConformidad.CONFORME else EstadoConformidad.RECHAZADA,
                    observacionCliente = texto.ifBlank { null },
                    fechaConformidad = System.currentTimeMillis(),
                    conformidadPor = rutUsuario
                )
            )
            _showToast.value = true
            delay(2000)
            _showToast.value = false
            resetFormulario()
            onSuccess()
        }
    }

    companion object {
        // Solo se valida una OT finalizada que el cliente aún no ha resuelto
        fun requiereConformidad(item: OrdenConDetalle): Boolean =
            item.orden.estado == EstadoOrden.FINALIZADA && item.orden.conformidad == null
    }
}
