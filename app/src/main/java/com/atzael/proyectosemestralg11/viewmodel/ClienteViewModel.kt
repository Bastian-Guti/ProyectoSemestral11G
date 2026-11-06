package com.atzael.proyectosemestralg11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atzael.proyectosemestralg11.data.ClienteDao
import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.util.esTelefonoValido
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClienteViewModel(private val clienteDao: ClienteDao) : ViewModel() {

    val clientes: StateFlow<List<Cliente>> = clienteDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var idEnEdicion = 0

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _contacto = MutableStateFlow("")
    val contacto: StateFlow<String> = _contacto.asStateFlow()

    private val _telefono = MutableStateFlow("")
    val telefono: StateFlow<String> = _telefono.asStateFlow()

    private val _slaHoras = MutableStateFlow(SLA_POR_DEFECTO)
    val slaHoras: StateFlow<String> = _slaHoras.asStateFlow()

    private val _nombreError = MutableStateFlow<String?>(null)
    val nombreError: StateFlow<String?> = _nombreError.asStateFlow()

    private val _contactoError = MutableStateFlow<String?>(null)
    val contactoError: StateFlow<String?> = _contactoError.asStateFlow()

    private val _telefonoError = MutableStateFlow<String?>(null)
    val telefonoError: StateFlow<String?> = _telefonoError.asStateFlow()

    private val _slaError = MutableStateFlow<String?>(null)
    val slaError: StateFlow<String?> = _slaError.asStateFlow()

    private val _isButtonEnabled = MutableStateFlow(false)
    val isButtonEnabled: StateFlow<Boolean> = _isButtonEnabled.asStateFlow()

    private val _showToast = MutableStateFlow(false)
    val showToast: StateFlow<Boolean> = _showToast.asStateFlow()

    private val _errorEliminar = MutableStateFlow<String?>(null)
    val errorEliminar: StateFlow<String?> = _errorEliminar.asStateFlow()

    fun onNombreChange(newValue: String) {
        _nombre.value = newValue
        _nombreError.value = if (newValue.isBlank()) "El nombre es obligatorio" else null
        validate()
    }

    fun onContactoChange(newValue: String) {
        _contacto.value = newValue
        _contactoError.value = if (newValue.isBlank()) "El contacto es obligatorio" else null
        validate()
    }

    fun onTelefonoChange(newValue: String) {
        _telefono.value = newValue
        _telefonoError.value = when {
            newValue.isBlank() -> "El teléfono es obligatorio"
            !newValue.esTelefonoValido() -> "El teléfono no es válido"
            else -> null
        }
        validate()
    }

    fun onSlaChange(newValue: String) {
        if (newValue.all { it.isDigit() }) {
            _slaHoras.value = newValue
            val horas = newValue.toIntOrNull() ?: 0
            _slaError.value = when {
                newValue.isBlank() -> "El SLA es obligatorio"
                horas <= 0 -> "El SLA debe ser mayor a 0"
                else -> null
            }
            validate()
        }
    }

    private fun validate() {
        val horas = _slaHoras.value.toIntOrNull() ?: 0
        _isButtonEnabled.value = _nombre.value.isNotBlank() &&
                _nombreError.value == null &&
                _contacto.value.isNotBlank() &&
                _contactoError.value == null &&
                _telefono.value.isNotBlank() &&
                _telefonoError.value == null &&
                _slaError.value == null &&
                horas > 0
    }

    private fun construirCliente() = Cliente(
        id = idEnEdicion,
        nombre = _nombre.value.trim(),
        contacto = _contacto.value.trim(),
        telefono = _telefono.value.trim(),
        slaHoras = _slaHoras.value.toInt()
    )

    fun registrarCliente(onSuccess: () -> Unit) {
        if (_isButtonEnabled.value) {
            viewModelScope.launch {
                clienteDao.insert(construirCliente().copy(id = 0))
                _showToast.value = true
                delay(2000)
                _showToast.value = false
                resetForm()
                onSuccess()
            }
        }
    }

    fun cargarCliente(id: Int) {
        viewModelScope.launch {
            clienteDao.getById(id)?.let { cliente ->
                idEnEdicion = cliente.id
                _nombre.value = cliente.nombre
                _contacto.value = cliente.contacto
                _telefono.value = cliente.telefono
                _slaHoras.value = cliente.slaHoras.toString()
                limpiarErrores()
                validate()
            }
        }
    }

    fun actualizarCliente(onSuccess: () -> Unit) {
        if (_isButtonEnabled.value) {
            viewModelScope.launch {
                clienteDao.update(construirCliente())
                _showToast.value = true
                delay(2000)
                _showToast.value = false
                resetForm()
                onSuccess()
            }
        }
    }

    fun eliminarCliente(cliente: Cliente) {
        viewModelScope.launch {
            if (clienteDao.contarLocales(cliente.id) > 0) {
                _errorEliminar.value =
                    "No se puede eliminar a '${cliente.nombre}' porque tiene locales asociados."
            } else if (clienteDao.contarUsuarios(cliente.id) > 0) {
                _errorEliminar.value =
                    "No se puede eliminar a '${cliente.nombre}' porque tiene usuarios registrados."
            } else {
                clienteDao.delete(cliente)
            }
        }
    }

    fun descartarErrorEliminar() {
        _errorEliminar.value = null
    }

    private fun limpiarErrores() {
        _nombreError.value = null
        _contactoError.value = null
        _telefonoError.value = null
        _slaError.value = null
    }

    fun resetForm() {
        idEnEdicion = 0
        _nombre.value = ""
        _contacto.value = ""
        _telefono.value = ""
        _slaHoras.value = SLA_POR_DEFECTO
        limpiarErrores()
        _isButtonEnabled.value = false
    }

    private companion object {
        const val SLA_POR_DEFECTO = "24"
    }
}
