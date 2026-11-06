package com.atzael.proyectosemestralg11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atzael.proyectosemestralg11.data.ClimaRepository
import com.atzael.proyectosemestralg11.data.EquipoDao
import com.atzael.proyectosemestralg11.data.FotoDao
import com.atzael.proyectosemestralg11.data.OrdenTrabajoDao
import com.atzael.proyectosemestralg11.data.UsuarioDao
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.model.FotoOrden
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import com.atzael.proyectosemestralg11.model.OrdenTrabajo
import com.atzael.proyectosemestralg11.model.PrioridadOrden
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.ZoneOffset

// Flujo del técnico en terreno: jornada (HU-08), aceptar/rechazar (HU-09), check-in/out (HU-10),
// informe (HU-11), fotos (HU-12) y cierre (HU-13). Todo se guarda en el teléfono.
@OptIn(ExperimentalCoroutinesApi::class)
class TecnicoViewModel(
    private val ordenDao: OrdenTrabajoDao,
    private val fotoDao: FotoDao,
    private val equipoDao: EquipoDao,
    private val usuarioDao: UsuarioDao,
    private val clima: ClimaRepository
) : ViewModel() {

    private val _rut = MutableStateFlow<String?>(null)

    // Solo las OT asignadas a este técnico
    val todasLasOrdenes: StateFlow<List<OrdenConDetalle>> = _rut
        .flatMapLatest { rut -> if (rut == null) flowOf(emptyList()) else ordenDao.getByTecnico(rut) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _verCerradas = MutableStateFlow(false)
    val verCerradas: StateFlow<Boolean> = _verCerradas.asStateFlow()

    // Ordenadas por prioridad y luego por fecha de visita
    val ordenes: StateFlow<List<OrdenConDetalle>> = combine(todasLasOrdenes, _verCerradas) { lista, cerradas ->
        lista
            .filter { cerradas || it.orden.estado in ESTADOS_ABIERTOS }
            .sortedWith(compareBy({ rangoPrioridad(it.orden.prioridad) }, { it.orden.fechaVisita }))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // null mientras se consulta; true/false según haya aceptado el aviso de datos (HU-22)
    private val _avisoAceptado = MutableStateFlow<Boolean?>(null)
    val avisoAceptado: StateFlow<Boolean?> = _avisoAceptado.asStateFlow()

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    // Alertas de clima por id de OT (HU-15)
    private val _alertas = MutableStateFlow<Map<Int, String>>(emptyMap())
    val alertas: StateFlow<Map<Int, String>> = _alertas.asStateFlow()

    // ---------- Fotos de la OT abierta en pantalla ----------

    private val _ordenSeleccionadaId = MutableStateFlow<Int?>(null)

    val fotos: StateFlow<List<FotoOrden>> = _ordenSeleccionadaId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else fotoDao.getByOrden(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ---------- Informe (HU-11) ----------

    private var informeCargadoDe: Int? = null

    private val _diagnostico = MutableStateFlow("")
    val diagnostico: StateFlow<String> = _diagnostico.asStateFlow()

    private val _actividades = MutableStateFlow("")
    val actividades: StateFlow<String> = _actividades.asStateFlow()

    private val _materiales = MutableStateFlow("")
    val materiales: StateFlow<String> = _materiales.asStateFlow()

    private val _mensajeExito = MutableStateFlow<String?>(null)
    val mensajeExito: StateFlow<String?> = _mensajeExito.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            todasLasOrdenes.collectLatest { lista ->
                _alertas.value = AlertasClima.evaluarOrdenes(lista, clima).alertas
                    .associate { it.orden.orden.id to it.mensaje }
            }
        }
    }

    fun cargarUsuario(rut: String) {
        _rut.value = rut
        viewModelScope.launch {
            val usuario = usuarioDao.getUsuarioByRut(rut)
            _nombre.value = usuario?.let { "${it.nombre} ${it.apellido}" } ?: ""
            _avisoAceptado.value = usuario?.avisoDatosAceptado ?: false
        }
    }

    fun aceptarAviso() {
        val rut = _rut.value ?: return
        viewModelScope.launch {
            usuarioDao.aceptarAvisoDatos(rut)
            _avisoAceptado.value = true
        }
    }

    fun onVerCerradasChange(valor: Boolean) {
        _verCerradas.value = valor
    }

    fun seleccionarOrden(id: Int) {
        _ordenSeleccionadaId.value = id
    }

    fun cargarInforme(orden: OrdenTrabajo) {
        if (informeCargadoDe == orden.id) return
        informeCargadoDe = orden.id
        _diagnostico.value = orden.diagnostico ?: ""
        _actividades.value = orden.actividades ?: ""
        _materiales.value = orden.materiales ?: ""
    }

    fun onDiagnosticoChange(valor: String) {
        _diagnostico.value = valor
    }

    fun onActividadesChange(valor: String) {
        _actividades.value = valor
    }

    fun onMaterialesChange(valor: String) {
        _materiales.value = valor
    }

    fun descartarError() {
        _error.value = null
    }

    // ---------- Acciones sobre la OT ----------

    fun aceptarOrden(orden: OrdenTrabajo) {
        if (orden.estado != EstadoOrden.PENDIENTE) return
        viewModelScope.launch {
            ordenDao.update(orden.copy(estado = EstadoOrden.ACEPTADA, motivoRechazo = null))
            anunciar("Orden aceptada")
        }
    }

    // El motivo es obligatorio (HU-09)
    fun rechazarOrden(orden: OrdenTrabajo, motivo: String, onSuccess: () -> Unit = {}) {
        if (orden.estado != EstadoOrden.PENDIENTE && orden.estado != EstadoOrden.ACEPTADA) return
        if (motivo.isBlank()) {
            _error.value = "Indique el motivo para rechazar la orden"
            return
        }
        viewModelScope.launch {
            ordenDao.update(orden.copy(estado = EstadoOrden.RECHAZADA_TECNICO, motivoRechazo = motivo.trim()))
            anunciar("Orden rechazada")
            onSuccess()
        }
    }

    // La ubicación es opcional: sin señal GPS igual queda registrada la hora (HU-10)
    fun registrarCheckIn(orden: OrdenTrabajo, ubicacion: Pair<Double, Double>?) {
        if (orden.estado != EstadoOrden.ACEPTADA) return
        if (!avisoAceptado()) return
        viewModelScope.launch {
            ordenDao.update(
                orden.copy(
                    estado = EstadoOrden.EN_CURSO,
                    checkIn = System.currentTimeMillis(),
                    checkInLat = ubicacion?.first,
                    checkInLon = ubicacion?.second
                )
            )
            anunciar(if (ubicacion == null) "Check-in registrado (sin ubicación)" else "Check-in registrado")
        }
    }

    fun guardarInforme(orden: OrdenTrabajo) {
        if (orden.estado != EstadoOrden.EN_CURSO) return
        viewModelScope.launch {
            ordenDao.update(orden.conInforme())
            anunciar("Informe guardado")
        }
    }

    fun registrarCheckOut(orden: OrdenTrabajo, ubicacion: Pair<Double, Double>?) {
        if (orden.estado != EstadoOrden.EN_CURSO || orden.checkIn == null || orden.checkOut != null) return
        if (!avisoAceptado()) return
        viewModelScope.launch {
            ordenDao.update(
                orden.conInforme().copy(
                    checkOut = System.currentTimeMillis(),
                    checkOutLat = ubicacion?.first,
                    checkOutLon = ubicacion?.second
                )
            )
            anunciar(if (ubicacion == null) "Check-out registrado (sin ubicación)" else "Check-out registrado")
        }
    }

    // No se puede cerrar sin check-out y diagnóstico (HU-13)
    fun cerrarOrden(orden: OrdenTrabajo, onSuccess: () -> Unit = {}) {
        if (orden.estado != EstadoOrden.EN_CURSO) return
        when {
            orden.checkOut == null -> {
                _error.value = "Para cerrar la OT primero registre el check-out"
                return
            }
            _diagnostico.value.isBlank() -> {
                _error.value = "Para cerrar la OT debe registrar el diagnóstico"
                return
            }
        }
        viewModelScope.launch {
            ordenDao.update(orden.conInforme().copy(estado = EstadoOrden.FINALIZADA))
            // La última mantención del equipo se actualiza al cerrar la OT (HU-03)
            equipoDao.getById(orden.equipoId)?.let { equipo ->
                equipoDao.update(equipo.copy(ultimaMantencion = hoyUtc()))
            }
            anunciar("Orden finalizada")
            onSuccess()
        }
    }

    // ---------- Fotos (HU-12) ----------

    fun registrarFoto(orden: OrdenTrabajo, ruta: String, tipo: String) {
        if (orden.estado != EstadoOrden.EN_CURSO || !avisoAceptado()) {
            File(ruta).delete()
            return
        }
        viewModelScope.launch {
            fotoDao.insert(FotoOrden(ordenId = orden.id, ruta = ruta, tipo = tipo, tomadaPor = orden.tecnicoRut))
            anunciar("Foto guardada")
        }
    }

    fun eliminarFoto(foto: FotoOrden) {
        viewModelScope.launch {
            fotoDao.delete(foto)
            File(foto.ruta).delete()
        }
    }

    // ---------- Apoyo ----------

    private fun OrdenTrabajo.conInforme() = copy(
        diagnostico = _diagnostico.value.trim().ifBlank { null },
        actividades = _actividades.value.trim().ifBlank { null },
        materiales = _materiales.value.trim().ifBlank { null }
    )

    // Sin aceptar el aviso no se recopila ubicación ni fotos
    private fun avisoAceptado(): Boolean {
        if (_avisoAceptado.value == true) return true
        _error.value = "Debe aceptar el aviso de datos personales para registrar ubicación y fotos"
        return false
    }

    private fun hoyUtc(): Long =
        LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private suspend fun anunciar(texto: String) {
        _mensajeExito.value = texto
        delay(2000)
        _mensajeExito.value = null
    }

    companion object {
        val ESTADOS_ABIERTOS = listOf(EstadoOrden.PENDIENTE, EstadoOrden.ACEPTADA, EstadoOrden.EN_CURSO)

        fun rangoPrioridad(prioridad: String): Int = when (prioridad) {
            PrioridadOrden.ALTA -> 0
            PrioridadOrden.MEDIA -> 1
            else -> 2
        }
    }
}
