package com.atzael.proyectosemestralg11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atzael.proyectosemestralg11.data.ClienteDao
import com.atzael.proyectosemestralg11.data.LocalDao
import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.Local
import com.atzael.proyectosemestralg11.util.esTelefonoValido
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocalViewModel(
    private val localDao: LocalDao,
    clienteDao: ClienteDao
) : ViewModel() {

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

    private val _clienteId = MutableStateFlow<Int?>(null)
    val clienteId: StateFlow<Int?> = _clienteId.asStateFlow()

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _direccion = MutableStateFlow("")
    val direccion: StateFlow<String> = _direccion.asStateFlow()

    private val _telefono = MutableStateFlow("")
    val telefono: StateFlow<String> = _telefono.asStateFlow()

    // Opcional: sin ciudad no se pueden consultar alertas de clima para el local
    private val _ciudad = MutableStateFlow("")
    val ciudad: StateFlow<String> = _ciudad.asStateFlow()

    private val _clienteError = MutableStateFlow<String?>(null)
    val clienteError: StateFlow<String?> = _clienteError.asStateFlow()

    private val _nombreError = MutableStateFlow<String?>(null)
    val nombreError: StateFlow<String?> = _nombreError.asStateFlow()

    private val _direccionError = MutableStateFlow<String?>(null)
    val direccionError: StateFlow<String?> = _direccionError.asStateFlow()

    private val _telefonoError = MutableStateFlow<String?>(null)
    val telefonoError: StateFlow<String?> = _telefonoError.asStateFlow()

    private val _isButtonEnabled = MutableStateFlow(false)
    val isButtonEnabled: StateFlow<Boolean> = _isButtonEnabled.asStateFlow()

    private val _showToast = MutableStateFlow(false)
    val showToast: StateFlow<Boolean> = _showToast.asStateFlow()

    private val _errorEliminar = MutableStateFlow<String?>(null)
    val errorEliminar: StateFlow<String?> = _errorEliminar.asStateFlow()

    fun onClienteChange(id: Int) {
        _clienteId.value = id
        _clienteError.value = null
        validate()
    }

    fun onNombreChange(newValue: String) {
        _nombre.value = newValue
        _nombreError.value = if (newValue.isBlank()) "El nombre es obligatorio" else null
        validate()
    }

    fun onDireccionChange(newValue: String) {
        _direccion.value = newValue
        _direccionError.value = if (newValue.isBlank()) "La dirección es obligatoria" else null
        validate()
    }

    fun onTelefonoChange(newValue: String) {
        _telefono.value = newValue
        _telefonoError.value = when {
            newValue.isBlank() -> "El teléfono de contacto es obligatorio"
            !newValue.esTelefonoValido() -> "El teléfono no es válido"
            else -> null
        }
        validate()
    }

    fun onCiudadChange(newValue: String) {
        _ciudad.value = newValue
    }

    private fun validate() {
        _isButtonEnabled.value = _clienteId.value != null &&
                _nombre.value.isNotBlank() &&
                _nombreError.value == null &&
                _direccion.value.isNotBlank() &&
                _direccionError.value == null &&
                _telefono.value.isNotBlank() &&
                _telefonoError.value == null
    }

    private fun construirLocal() = Local(
        id = idEnEdicion,
        clienteId = _clienteId.value!!,
        nombre = _nombre.value.trim(),
        direccion = _direccion.value.trim(),
        telefono = _telefono.value.trim(),
        ciudad = _ciudad.value.trim()
    )

    fun registrarLocal(onSuccess: () -> Unit) {
        if (_isButtonEnabled.value) {
            viewModelScope.launch {
                localDao.insert(construirLocal().copy(id = 0))
                _showToast.value = true
                delay(2000)
                _showToast.value = false
                resetForm()
                onSuccess()
            }
        }
    }

    fun cargarLocal(id: Int) {
        viewModelScope.launch {
            localDao.getById(id)?.let { local ->
                idEnEdicion = local.id
                _clienteId.value = local.clienteId
                _nombre.value = local.nombre
                _direccion.value = local.direccion
                _telefono.value = local.telefono
                _ciudad.value = local.ciudad
                limpiarErrores()
                validate()
            }
        }
    }

    fun actualizarLocal(onSuccess: () -> Unit) {
        if (_isButtonEnabled.value) {
            viewModelScope.launch {
                localDao.update(construirLocal())
                _showToast.value = true
                delay(2000)
                _showToast.value = false
                resetForm()
                onSuccess()
            }
        }
    }

    fun eliminarLocal(local: Local) {
        viewModelScope.launch {
            if (localDao.contarEquipos(local.id) > 0) {
                _errorEliminar.value =
                    "No se puede eliminar el local '${local.nombre}' porque tiene equipos asociados."
            } else {
                localDao.delete(local)
            }
        }
    }

    fun descartarErrorEliminar() {
        _errorEliminar.value = null
    }

    private fun limpiarErrores() {
        _clienteError.value = null
        _nombreError.value = null
        _direccionError.value = null
        _telefonoError.value = null
    }

    fun resetForm() {
        idEnEdicion = 0
        _clienteId.value = null
        _nombre.value = ""
        _direccion.value = ""
        _telefono.value = ""
        _ciudad.value = ""
        limpiarErrores()
        _isButtonEnabled.value = false
    }
}
