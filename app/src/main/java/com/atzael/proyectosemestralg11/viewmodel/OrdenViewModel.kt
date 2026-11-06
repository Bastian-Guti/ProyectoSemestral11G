package com.atzael.proyectosemestralg11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atzael.proyectosemestralg11.data.ClienteDao
import com.atzael.proyectosemestralg11.data.EquipoDao
import com.atzael.proyectosemestralg11.data.FotoDao
import com.atzael.proyectosemestralg11.model.FotoOrden
import com.atzael.proyectosemestralg11.data.LocalDao
import com.atzael.proyectosemestralg11.data.OrdenTrabajoDao
import com.atzael.proyectosemestralg11.data.UsuarioDao
import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.Equipo
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.model.Local
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import com.atzael.proyectosemestralg11.model.OrdenTrabajo
import com.atzael.proyectosemestralg11.model.PrioridadOrden
import com.atzael.proyectosemestralg11.model.Reprogramacion
import com.atzael.proyectosemestralg11.model.TipoOrden
import com.atzael.proyectosemestralg11.model.Usuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FiltrosOrden(
    val estado: String? = null,
    val prioridad: String? = null,
    val tecnicoRut: String? = null,
    val clienteId: Int? = null,
    val fecha: Long? = null // Fecha de visita (medianoche UTC)
)

@OptIn(ExperimentalCoroutinesApi::class)
class OrdenViewModel(
    private val ordenDao: OrdenTrabajoDao,
    clienteDao: ClienteDao,
    localDao: LocalDao,
    equipoDao: EquipoDao,
    usuarioDao: UsuarioDao,
    fotoDao: FotoDao
) : ViewModel() {

    // ---------- Listado y filtros (HU-07) ----------

    val ordenes: StateFlow<List<OrdenConDetalle>> = ordenDao.getAllConDetalle()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _filtros = MutableStateFlow(FiltrosOrden())
    val filtros: StateFlow<FiltrosOrden> = _filtros.asStateFlow()

    val ordenesFiltradas: StateFlow<List<OrdenConDetalle>> = combine(ordenes, _filtros) { lista, f ->
        lista.filter { item ->
            (f.estado == null || item.orden.estado == f.estado) &&
                    (f.prioridad == null || item.orden.prioridad == f.prioridad) &&
                    (f.tecnicoRut == null || item.orden.tecnicoRut == f.tecnicoRut) &&
                    (f.clienteId == null || item.orden.clienteId == f.clienteId) &&
                    (f.fecha == null || item.orden.fechaVisita == f.fecha)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onFiltroEstadoChange(estado: String?) = _filtros.update { it.copy(estado = estado) }
    fun onFiltroPrioridadChange(prioridad: String?) = _filtros.update { it.copy(prioridad = prioridad) }
    fun onFiltroTecnicoChange(rut: String?) = _filtros.update { it.copy(tecnicoRut = rut) }
    fun onFiltroClienteChange(id: Int?) = _filtros.update { it.copy(clienteId = id) }
    fun onFiltroFechaChange(fecha: Long?) = _filtros.update { it.copy(fecha = fecha) }
    fun limpiarFiltros() {
        _filtros.value = FiltrosOrden()
    }

    // ---------- Datos de apoyo para los formularios ----------

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

    private val todosLosEquipos: StateFlow<List<Equipo>> = equipoDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val tecnicos: StateFlow<List<Usuario>> = usuarioDao.getTecnicos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ---------- Formulario de OT (HU-05 y HU-06) ----------

    private var ordenOriginal: OrdenTrabajo? = null

    private val _modoEdicion = MutableStateFlow(false)
    val modoEdicion: StateFlow<Boolean> = _modoEdicion.asStateFlow()

    private val _clienteId = MutableStateFlow<Int?>(null)
    val clienteId: StateFlow<Int?> = _clienteId.asStateFlow()

    private val _localId = MutableStateFlow<Int?>(null)
    val localId: StateFlow<Int?> = _localId.asStateFlow()

    private val _equipoId = MutableStateFlow<Int?>(null)
    val equipoId: StateFlow<Int?> = _equipoId.asStateFlow()

    private val _tipo = MutableStateFlow(TipoOrden.PREVENTIVO)
    val tipo: StateFlow<String> = _tipo.asStateFlow()

    private val _prioridad = MutableStateFlow(PrioridadOrden.MEDIA)
    val prioridad: StateFlow<String> = _prioridad.asStateFlow()

    private val _tecnicoRut = MutableStateFlow<String?>(null)
    val tecnicoRut: StateFlow<String?> = _tecnicoRut.asStateFlow()

    private val _fechaVisita = MutableStateFlow<Long?>(null)
    val fechaVisita: StateFlow<Long?> = _fechaVisita.asStateFlow()

    val localesDisponibles: StateFlow<List<Local>> = combine(_clienteId, todosLosLocales) { cliente, locales ->
        locales.filter { it.clienteId == cliente }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val equiposDisponibles: StateFlow<List<Equipo>> = combine(_localId, todosLosEquipos) { local, equipos ->
        equipos.filter { it.localId == local }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isButtonEnabled = MutableStateFlow(false)
    val isButtonEnabled: StateFlow<Boolean> = _isButtonEnabled.asStateFlow()

    private val _showToast = MutableStateFlow(false)
    val showToast: StateFlow<Boolean> = _showToast.asStateFlow()

    fun onClienteChange(id: Int) {
        if (_clienteId.value != id) {
            _clienteId.value = id
            _localId.value = null
            _equipoId.value = null
        }
        validate()
    }

    fun onLocalChange(id: Int) {
        if (_localId.value != id) {
            _localId.value = id
            _equipoId.value = null
        }
        validate()
    }

    fun onEquipoChange(id: Int) {
        _equipoId.value = id
        validate()
    }

    fun onTipoChange(newValue: String) {
        _tipo.value = newValue
    }

    fun onPrioridadChange(newValue: String) {
        _prioridad.value = newValue
    }

    fun onTecnicoChange(rut: String) {
        _tecnicoRut.value = rut
        validate()
    }

    fun onFechaVisitaChange(fecha: Long) {
        _fechaVisita.value = fecha
        validate()
    }

    private fun validate() {
        _isButtonEnabled.value = _clienteId.value != null &&
                _localId.value != null &&
                _equipoId.value != null &&
                _tecnicoRut.value != null &&
                _fechaVisita.value != null
    }

    fun registrarOrden(creadaPor: String, onSuccess: () -> Unit) {
        if (_isButtonEnabled.value) {
            viewModelScope.launch {
                ordenDao.insert(
                    OrdenTrabajo(
                        clienteId = _clienteId.value!!,
                        localId = _localId.value!!,
                        equipoId = _equipoId.value!!,
                        tipo = _tipo.value,
                        prioridad = _prioridad.value,
                        tecnicoRut = _tecnicoRut.value!!,
                        fechaVisita = _fechaVisita.value!!,
                        creadaPor = creadaPor
                    )
                )
                mostrarToast()
                resetForm()
                onSuccess()
            }
        }
    }

    fun cargarOrden(id: Int) {
        viewModelScope.launch {
            ordenDao.getById(id)?.let { orden ->
                ordenOriginal = orden
                _modoEdicion.value = true
                _clienteId.value = orden.clienteId
                _localId.value = orden.localId
                _equipoId.value = orden.equipoId
                _tipo.value = orden.tipo
                _prioridad.value = orden.prioridad
                _tecnicoRut.value = orden.tecnicoRut
                _fechaVisita.value = orden.fechaVisita
                validate()
            }
        }
    }

    // Fecha y técnico solo cambian por "reagendar", para dejar historial
    fun actualizarOrden(onSuccess: () -> Unit) {
        val original = ordenOriginal ?: return
        if (_isButtonEnabled.value) {
            viewModelScope.launch {
                ordenDao.update(
                    original.copy(
                        clienteId = _clienteId.value!!,
                        localId = _localId.value!!,
                        equipoId = _equipoId.value!!,
                        tipo = _tipo.value,
                        prioridad = _prioridad.value
                    )
                )
                mostrarToast()
                resetForm()
                onSuccess()
            }
        }
    }

    fun resetForm() {
        ordenOriginal = null
        _modoEdicion.value = false
        _clienteId.value = null
        _localId.value = null
        _equipoId.value = null
        _tipo.value = TipoOrden.PREVENTIVO
        _prioridad.value = PrioridadOrden.MEDIA
        _tecnicoRut.value = null
        _fechaVisita.value = null
        _isButtonEnabled.value = false
    }

    private suspend fun mostrarToast() {
        _showToast.value = true
        delay(2000)
        _showToast.value = false
    }

    // ---------- Detalle, reagendar y cancelar (HU-06 y HU-16) ----------

    private val _ordenSeleccionadaId = MutableStateFlow<Int?>(null)

    val ordenDetalle: StateFlow<OrdenConDetalle?> = _ordenSeleccionadaId
        .flatMapLatest { id -> if (id == null) flowOf(null) else ordenDao.getConDetalle(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val reprogramaciones: StateFlow<List<Reprogramacion>> = _ordenSeleccionadaId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else ordenDao.getReprogramaciones(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Evidencias de la OT: las ve la jefatura (HU-22: solo roles autorizados)
    val fotos: StateFlow<List<FotoOrden>> = _ordenSeleccionadaId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else fotoDao.getByOrden(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun seleccionarOrden(id: Int) {
        _ordenSeleccionadaId.value = id
    }

    fun reagendarOrden(
        orden: OrdenTrabajo,
        nuevaFecha: Long,
        nuevoTecnicoRut: String,
        motivo: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            // Una OT rechazada por el técnico vuelve a quedar pendiente al reasignarla
            val estado = if (orden.estado == EstadoOrden.RECHAZADA_TECNICO) EstadoOrden.PENDIENTE else orden.estado
            ordenDao.reprogramar(
                orden.copy(
                    fechaVisita = nuevaFecha,
                    tecnicoRut = nuevoTecnicoRut,
                    estado = estado,
                    motivoRechazo = if (estado == EstadoOrden.PENDIENTE) null else orden.motivoRechazo
                ),
                Reprogramacion(
                    ordenId = orden.id,
                    fechaAnterior = orden.fechaVisita,
                    fechaNueva = nuevaFecha,
                    tecnicoAnterior = orden.tecnicoRut,
                    tecnicoNuevo = nuevoTecnicoRut,
                    motivo = motivo
                )
            )
            mostrarToast()
            onSuccess()
        }
    }

    // La OT no se borra: queda registrada como cancelada
    fun cancelarOrden(orden: OrdenTrabajo, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            ordenDao.update(orden.copy(estado = EstadoOrden.CANCELADA))
            onSuccess()
        }
    }
}
