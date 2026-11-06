package com.atzael.proyectosemestralg11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atzael.proyectosemestralg11.data.ClienteDao
import com.atzael.proyectosemestralg11.data.UsuarioDao
import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.Usuario
import com.atzael.proyectosemestralg11.util.normalizarRut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val usuarioDao: UsuarioDao,
    clienteDao: ClienteDao
) : ViewModel() {

    // Empresas disponibles para registrar un usuario con rol Cliente
    val clientes: StateFlow<List<Cliente>> = clienteDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _rutActual = MutableStateFlow("")
    val rutActual: StateFlow<String> = _rutActual.asStateFlow()

    private val _nombreActual = MutableStateFlow("")
    val nombreActual: StateFlow<String> = _nombreActual.asStateFlow()

    fun iniciarSesion(
        rut: String,
        password: String,
        rol: String,
        onExito: () -> Unit,
        onError: (String) -> Unit
    ) {
        val rut = rut.normalizarRut()
        viewModelScope.launch {
            val usuario = usuarioDao.login(rut, password, rol)
            if (usuario != null) {
                _rutActual.value = usuario.rut
                _nombreActual.value = "${usuario.nombre} ${usuario.apellido}"
                onExito()
            } else {
                val existente = usuarioDao.getUsuarioByRut(rut)
                onError(
                    when {
                        existente == null -> "RUT no registrado."
                        existente.rol != rol -> "Este RUT está registrado como ${existente.rol}, no como $rol."
                        else -> "Contraseña incorrecta."
                    }
                )
            }
        }
    }

    fun registrarUsuario(
        rut: String,
        password: String,
        nombre: String,
        apellido: String,
        rol: String,
        clienteId: Int? = null,
        onExito: () -> Unit,
        onError: (String) -> Unit
    ) {
        val rut = rut.normalizarRut()
        viewModelScope.launch {
            if (usuarioDao.getUsuarioByRut(rut) != null) {
                onError("El RUT ya está registrado.")
            } else {
                usuarioDao.insertUsuario(Usuario(rut, nombre, apellido, password, rol, clienteId))
                onExito()
            }
        }
    }

    fun cerrarSesion() {
        _rutActual.value = ""
        _nombreActual.value = ""
    }
}
