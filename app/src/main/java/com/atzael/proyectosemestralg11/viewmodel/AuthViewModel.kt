package com.atzael.proyectosemestralg11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atzael.proyectosemestralg11.data.ClienteDao
import com.atzael.proyectosemestralg11.data.UsuarioDao
import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.Usuario
import com.atzael.proyectosemestralg11.util.enViewModelScope
import com.atzael.proyectosemestralg11.util.normalizarRut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

sealed interface ResultadoAuth {
    data class Exito(val rol: String) : ResultadoAuth
    data class Error(val mensaje: String) : ResultadoAuth
}

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

    // Éxito trae el rol del usuario; Error trae el mensaje para mostrar en el formulario
    suspend fun iniciarSesion(rut: String, password: String): ResultadoAuth {
        val rut = rut.normalizarRut()
        val usuario = usuarioDao.getUsuarioByRut(rut)
        return when {
            usuario == null -> ResultadoAuth.Error("RUT no registrado.")
            usuario.password != password -> ResultadoAuth.Error("Contraseña incorrecta.")
            else -> {
                _rutActual.value = usuario.rut
                _nombreActual.value = "${usuario.nombre} ${usuario.apellido}"
                ResultadoAuth.Exito(usuario.rol)
            }
        }
    }

    suspend fun registrarUsuario(
        rut: String,
        password: String,
        nombre: String,
        apellido: String,
        rol: String,
        clienteId: Int? = null
    ): ResultadoAuth {
        val rut = rut.normalizarRut()
        return enViewModelScope {
            if (usuarioDao.getUsuarioByRut(rut) != null) {
                ResultadoAuth.Error("El RUT ya está registrado.")
            } else {
                usuarioDao.insertUsuario(Usuario(rut, nombre, apellido, password, rol, clienteId))
                ResultadoAuth.Exito(rol)
            }
        }
    }

    fun cerrarSesion() {
        _rutActual.value = ""
        _nombreActual.value = ""
    }
}
