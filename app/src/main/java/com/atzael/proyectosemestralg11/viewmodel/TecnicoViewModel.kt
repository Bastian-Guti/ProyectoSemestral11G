package com.atzael.proyectosemestralg11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atzael.proyectosemestralg11.data.EquipoDao
import com.atzael.proyectosemestralg11.data.FotoDao
import com.atzael.proyectosemestralg11.data.OrdenTrabajoDao
import com.atzael.proyectosemestralg11.data.UsuarioDao
import com.atzael.proyectosemestralg11.data.remote.SupabaseClient
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.model.FotoOrden
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import com.atzael.proyectosemestralg11.model.OrdenTrabajo
import com.atzael.proyectosemestralg11.model.PrioridadOrden
import com.atzael.proyectosemestralg11.util.comprimirFotoABase64
import com.atzael.proyectosemestralg11.util.enViewModelScope
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
    private val usuarioDao: UsuarioDao
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

    private fun rangoPrioridad(prioridad: String): Int = when (prioridad) {
        PrioridadOrden.ALTA -> 1
        PrioridadOrden.MEDIA -> 2
        else -> 3
    }

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _avisoAceptado = MutableStateFlow(false)
    val avisoAceptado: StateFlow<Boolean> = _avisoAceptado.asStateFlow()

    // ---------- Detalle de la OT seleccionada ----------

    private val _ordenSeleccionadaId = MutableStateFlow<Int?>(null)

    val ordenSeleccionada: StateFlow<OrdenConDetalle?> = _ordenSeleccionadaId
        .flatMapLatest { id -> if (id == null) flowOf(null) else ordenDao.getConDetalle(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

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
            // Supabase manda al sincronizar: si no se guarda allá, la descarga lo volvería a "no aceptado"
            runCatching { SupabaseClient.api.actualizarUsuario("eq.$rut", mapOf("aviso_datos_aceptado" to true)) }
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
            val actualizada = orden.copy(estado = EstadoOrden.ACEPTADA, motivoRechazo = null)
            ordenDao.update(actualizada)
            runCatching { SupabaseClient.api.actualizarOrdenTrabajo("eq.${actualizada.id}", actualizada) }
            anunciar("Orden aceptada")
        }
    }

    // El motivo es obligatorio (HU-09)
    // Devuelve true si se rechazó (la pantalla puede entonces volver atrás)
    suspend fun rechazarOrden(orden: OrdenTrabajo, motivo: String): Boolean {
        if (orden.estado != EstadoOrden.PENDIENTE && orden.estado != EstadoOrden.ACEPTADA) return false
        if (motivo.isBlank()) {
            _error.value = "Indique el motivo para rechazar la orden"
            return false
        }
        return enViewModelScope {
            val rechazada = orden.copy(estado = EstadoOrden.RECHAZADA_TECNICO, motivoRechazo = motivo.trim())
            ordenDao.update(rechazada)
            runCatching { SupabaseClient.api.actualizarOrdenTrabajo("eq.${rechazada.id}", rechazada) }
            anunciar("Orden rechazada")
            true
        }
    }

    // La ubicación es opcional: sin señal GPS igual queda registrada la hora (HU-10)
    fun registrarCheckIn(orden: OrdenTrabajo, ubicacion: Pair<Double, Double>?) {
        if (orden.estado != EstadoOrden.ACEPTADA) return
        if (!avisoAceptado()) return
        viewModelScope.launch {
            val enCurso = orden.copy(
                estado = EstadoOrden.EN_CURSO,
                checkIn = System.currentTimeMillis(),
                checkInLat = ubicacion?.first,
                checkInLon = ubicacion?.second
            )
            ordenDao.update(enCurso)
            runCatching { SupabaseClient.api.actualizarOrdenTrabajo("eq.${enCurso.id}", enCurso) }
            anunciar(if (ubicacion == null) "Check-in registrado (sin ubicación)" else "Check-in registrado")
        }
    }

    fun guardarInforme(orden: OrdenTrabajo) {
        if (orden.estado != EstadoOrden.EN_CURSO) return
        viewModelScope.launch {
            val conInforme = orden.conInforme()
            ordenDao.update(conInforme)
            runCatching { SupabaseClient.api.actualizarOrdenTrabajo("eq.${conInforme.id}", conInforme) }
            anunciar("Informe guardado")
        }
    }

    fun registrarCheckOut(orden: OrdenTrabajo, ubicacion: Pair<Double, Double>?) {
        if (orden.estado != EstadoOrden.EN_CURSO || orden.checkIn == null || orden.checkOut != null) return
        if (!avisoAceptado()) return
        viewModelScope.launch {
            val conCheckOut = orden.conInforme().copy(
                checkOut = System.currentTimeMillis(),
                checkOutLat = ubicacion?.first,
                checkOutLon = ubicacion?.second
            )
            ordenDao.update(conCheckOut)
            runCatching { SupabaseClient.api.actualizarOrdenTrabajo("eq.${conCheckOut.id}", conCheckOut) }
            anunciar(if (ubicacion == null) "Check-out registrado (sin ubicación)" else "Check-out registrado")
        }
    }

    // No se puede cerrar sin check-out y diagnóstico (HU-13)
    suspend fun cerrarOrden(orden: OrdenTrabajo): Boolean {
        if (orden.estado != EstadoOrden.EN_CURSO) return false
        when {
            orden.checkOut == null -> {
                _error.value = "Para cerrar la OT primero registre el check-out"
                return false
            }
            _diagnostico.value.isBlank() -> {
                _error.value = "Para cerrar la OT debe registrar el diagnóstico"
                return false
            }
        }
        return enViewModelScope {
            val finalizada = orden.conInforme().copy(estado = EstadoOrden.FINALIZADA)
            ordenDao.update(finalizada)
            runCatching { SupabaseClient.api.actualizarOrdenTrabajo("eq.${finalizada.id}", finalizada) }
            // La última mantención del equipo se actualiza al cerrar la OT (HU-03)
            equipoDao.getById(orden.equipoId)?.let { equipo ->
                val eqAct = equipo.copy(ultimaMantencion = hoyUtc())
                equipoDao.update(eqAct)
                runCatching { SupabaseClient.api.actualizarEquipo("eq.${eqAct.id}", eqAct) }
            }
            anunciar("Orden finalizada")
            true
        }
    }

    // ---------- Fotos (HU-12) ----------

    fun registrarFoto(orden: OrdenTrabajo, ruta: String, tipo: String) {
        if (orden.estado != EstadoOrden.EN_CURSO || !avisoAceptado()) {
            File(ruta).delete()
            return
        }
        viewModelScope.launch {
            val nuevaFoto = FotoOrden(ordenId = orden.id, ruta = ruta, tipo = tipo, tomadaPor = orden.tecnicoRut)
            val id = fotoDao.insert(nuevaFoto).toInt()
            anunciar("Foto guardada")

            // Se sube comprimida con el mismo id local: si se vuelve a enviar, Supabase la ignora
            val base64 = comprimirFotoABase64(ruta)
            if (base64 != null) {
                runCatching { SupabaseClient.api.crearFotoOrden(nuevaFoto.copy(id = id, ruta = base64)) }
            }
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

    private fun hoyUtc(): Long = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun anunciar(mensaje: String) {
        viewModelScope.launch {
            _mensajeExito.value = mensaje
            delay(2000)
            _mensajeExito.value = null
        }
    }

    private fun avisoAceptado(): Boolean {
        if (!_avisoAceptado.value) {
            _error.value = "Para continuar debe aceptar el aviso de datos que se recopilan."
        }
        return _avisoAceptado.value
    }

    companion object {
        private val ESTADOS_ABIERTOS = listOf(EstadoOrden.ACEPTADA, EstadoOrden.EN_CURSO)
    }
}
