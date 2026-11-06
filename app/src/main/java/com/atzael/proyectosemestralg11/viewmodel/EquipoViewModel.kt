package com.atzael.proyectosemestralg11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atzael.proyectosemestralg11.data.ClienteDao
import com.atzael.proyectosemestralg11.data.EquipoDao
import com.atzael.proyectosemestralg11.data.LocalDao
import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.Equipo
import com.atzael.proyectosemestralg11.model.Local
import com.atzael.proyectosemestralg11.model.UbicacionEquipo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EquipoViewModel(
    private val equipoDao: EquipoDao,
    localDao: LocalDao,
    clienteDao: ClienteDao
) : ViewModel() {

    val equipos: StateFlow<List<Equipo>> = equipoDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val locales: StateFlow<List<Local>> = localDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val clientes: StateFlow<List<Cliente>> = clienteDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var idEnEdicion = 0

    private val _localId = MutableStateFlow<Int?>(null)
    val localId: StateFlow<Int?> = _localId.asStateFlow()

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _marca = MutableStateFlow("")
    val marca: StateFlow<String> = _marca.asStateFlow()

    private val _ubicacion = MutableStateFlow(UbicacionEquipo.INTERIOR)
    val ubicacion: StateFlow<String> = _ubicacion.asStateFlow()

    private val _ultimaMantencion = MutableStateFlow<Long?>(null)
    val ultimaMantencion: StateFlow<Long?> = _ultimaMantencion.asStateFlow()

    private val _localError = MutableStateFlow<String?>(null)
    val localError: StateFlow<String?> = _localError.asStateFlow()

    private val _nombreError = MutableStateFlow<String?>(null)
    val nombreError: StateFlow<String?> = _nombreError.asStateFlow()

    private val _isButtonEnabled = MutableStateFlow(false)
    val isButtonEnabled: StateFlow<Boolean> = _isButtonEnabled.asStateFlow()

    private val _showToast = MutableStateFlow(false)
    val showToast: StateFlow<Boolean> = _showToast.asStateFlow()

    private val _errorEliminar = MutableStateFlow<String?>(null)
    val errorEliminar: StateFlow<String?> = _errorEliminar.asStateFlow()

    fun onLocalChange(id: Int) {
        _localId.value = id
        _localError.value = null
        validate()
    }

    fun onNombreChange(newValue: String) {
        _nombre.value = newValue
        _nombreError.value = if (newValue.isBlank()) "El nombre del equipo es obligatorio" else null
        validate()
    }

    fun onMarcaChange(newValue: String) {
        _marca.value = newValue
    }

    fun onUbicacionChange(newValue: String) {
        _ubicacion.value = newValue
    }

    fun onUltimaMantencionChange(fecha: Long?) {
        _ultimaMantencion.value = fecha
    }

    private fun validate() {
        _isButtonEnabled.value = _localId.value != null &&
                _nombre.value.isNotBlank() &&
                _nombreError.value == null
    }

    private fun construirEquipo() = Equipo(
        id = idEnEdicion,
        localId = _localId.value!!,
        nombre = _nombre.value.trim(),
        marca = _marca.value.trim(),
        ubicacion = _ubicacion.value,
        ultimaMantencion = _ultimaMantencion.value
    )

    fun registrarEquipo(onSuccess: () -> Unit) {
        if (_isButtonEnabled.value) {
            viewModelScope.launch {
                equipoDao.insert(construirEquipo().copy(id = 0))
                _showToast.value = true
                delay(2000)
                _showToast.value = false
                resetForm()
                onSuccess()
            }
        }
    }

    fun cargarEquipo(id: Int) {
        viewModelScope.launch {
            equipoDao.getById(id)?.let { equipo ->
                idEnEdicion = equipo.id
                _localId.value = equipo.localId
                _nombre.value = equipo.nombre
                _marca.value = equipo.marca
                _ubicacion.value = equipo.ubicacion
                _ultimaMantencion.value = equipo.ultimaMantencion
                _localError.value = null
                _nombreError.value = null
                validate()
            }
        }
    }

    fun actualizarEquipo(onSuccess: () -> Unit) {
        if (_isButtonEnabled.value) {
            viewModelScope.launch {
                equipoDao.update(construirEquipo())
                _showToast.value = true
                delay(2000)
                _showToast.value = false
                resetForm()
                onSuccess()
            }
        }
    }

    fun eliminarEquipo(equipo: Equipo) {
        viewModelScope.launch {
            if (equipoDao.contarOrdenes(equipo.id) > 0) {
                _errorEliminar.value =
                    "No se puede eliminar el equipo '${equipo.nombre}' porque tiene órdenes de trabajo asociadas."
            } else {
                equipoDao.delete(equipo)
            }
        }
    }

    fun descartarErrorEliminar() {
        _errorEliminar.value = null
    }

    fun resetForm() {
        idEnEdicion = 0
        _localId.value = null
        _nombre.value = ""
        _marca.value = ""
        _ubicacion.value = UbicacionEquipo.INTERIOR
        _ultimaMantencion.value = null
        _localError.value = null
        _nombreError.value = null
        _isButtonEnabled.value = false
    }
}
