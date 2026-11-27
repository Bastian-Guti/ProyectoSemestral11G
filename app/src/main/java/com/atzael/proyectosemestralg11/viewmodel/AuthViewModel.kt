package com.atzael.proyectosemestralg11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atzael.proyectosemestralg11.data.ClienteDao
import com.atzael.proyectosemestralg11.data.UsuarioDao
import com.atzael.proyectosemestralg11.data.remote.SupabaseClient
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
        val rutNorm = rut.normalizarRut()
        var usuario = usuarioDao.getUsuarioByRut(rutNorm)

        // Si el usuario no está en el almacenamiento local del teléfono, busca en Supabase
        if (usuario == null) {
            runCatching {
                val remotos = SupabaseClient.api.getUsuarioByRut("eq.$rutNorm")
                remotos.firstOrNull()?.also { u ->
                    usuarioDao.insertUsuario(u) // Guarda copia local en Room
                    usuario = u
                }
            }
        }

        val u = usuario ?: return ResultadoAuth.Error("RUT no registrado.")
        return if (u.password != password) {
            ResultadoAuth.Error("Contraseña incorrecta.")
        } else {
            _rutActual.value = u.rut
            _nombreActual.value = "${u.nombre} ${u.apellido}"
            ResultadoAuth.Exito(u.rol)
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
        val rutNorm = rut.normalizarRut()
        return enViewModelScope {
            // Una persona = un RUT. Se revisa el teléfono y también Supabase, porque otro
            // dispositivo pudo registrarlo (Supabase ignoraría el nuevo y luego lo pisaría al sincronizar).
            val enSupabase = runCatching {
                SupabaseClient.api.getUsuarioByRut("eq.$rutNorm").isNotEmpty()
            }.getOrDefault(false)
            if (enSupabase || usuarioDao.getUsuarioByRut(rutNorm) != null) {
                ResultadoAuth.Error("El RUT ya está registrado.")
            } else {
                val nuevoUsuario = Usuario(
                    rut = rutNorm,
                    nombre = nombre,
                    apellido = apellido,
                    password = password,
                    rol = rol,
                    clienteId = clienteId
                )

                // 1. Guarda en la base de datos local del teléfono (Room)
                usuarioDao.insertUsuario(nuevoUsuario)

                // 2. Envía y guarda el nuevo usuario a Supabase en la nube
                runCatching {
                    SupabaseClient.api.crearUsuario(nuevoUsuario)
                }

                ResultadoAuth.Exito(rol)
            }
        }
    }

    fun cerrarSesion() {
        _rutActual.value = ""
        _nombreActual.value = ""
    }
}
