package com.atzael.proyectosemestralg11.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.RolUsuario
import com.atzael.proyectosemestralg11.util.esRutValido
import com.atzael.proyectosemestralg11.view.components.CampoDesplegable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onBackToLogin: () -> Unit,
    clientes: List<Cliente> = emptyList(),
    onRegisterSuccess: (String, String, String, String, String, Int?, (String?) -> Unit) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var roleError by remember { mutableStateOf<String?>(null) }

    var isTecnicoSelected by remember { mutableStateOf(false) }
    var isJefaturaSelected by remember { mutableStateOf(false) }
    var isClienteSelected by remember { mutableStateOf(false) }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }

    val isRutInvalid = rut.isNotBlank() && !rut.esRutValido() && rut.length >= 8
    val hasRoleError = roleError != null
    val boxColor = if (hasRoleError) MaterialTheme.colorScheme.error else Color.White

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Registrar Usuario") },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear Cuenta",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { 
                    nombre = it
                    errorMessage = null
                },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = apellido,
                onValueChange = { 
                    apellido = it
                    errorMessage = null
                },
                label = { Text("Apellido") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = rut,
                onValueChange = { 
                    rut = it
                    errorMessage = null
                },
                label = { Text("RUT") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "RUT") },
                singleLine = true,
                isError = isRutInvalid,
                supportingText = {
                    if (isRutInvalid) {
                        Text(text = "Formato de RUT inválido")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    errorMessage = null
                },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Contraseña") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text("Confirmar Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirmar Contraseña") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Registrarse como...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isTecnicoSelected,
                            onCheckedChange = { 
                                isTecnicoSelected = it
                                if (it) {
                                    isJefaturaSelected = false
                                    isClienteSelected = false
                                }
                                roleError = null
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = boxColor,
                                uncheckedColor = boxColor,
                                checkmarkColor = Color.Black
                            )
                        )
                        Text(text = "Tecnico", color = Color.White)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isJefaturaSelected,
                            onCheckedChange = { 
                                isJefaturaSelected = it
                                if (it) {
                                    isTecnicoSelected = false
                                    isClienteSelected = false
                                }
                                roleError = null
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = boxColor,
                                uncheckedColor = boxColor,
                                checkmarkColor = Color.Black
                            )
                        )
                        Text(text = "Jefatura", color = Color.White)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isClienteSelected,
                            onCheckedChange = {
                                isClienteSelected = it
                                if (it) {
                                    isTecnicoSelected = false
                                    isJefaturaSelected = false
                                }
                                roleError = null
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = boxColor,
                                uncheckedColor = boxColor,
                                checkmarkColor = Color.Black
                            )
                        )
                        Text(text = "Cliente", color = Color.White)
                    }
                }

                if (isClienteSelected) {
                    CampoDesplegable(
                        label = "Empresa a la que pertenece",
                        valorSeleccionado = clienteSeleccionado?.nombre ?: "",
                        opciones = clientes,
                        textoOpcion = { it.nombre },
                        onSeleccion = {
                            clienteSeleccionado = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        error = if (clientes.isEmpty()) "Jefatura debe registrar primero a la empresa" else null
                    )
                }

                val currentRoleError = roleError
                if (currentRoleError != null) {
                    Text(
                        text = currentRoleError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            val currentError = errorMessage
            if (currentError != null) {
                Text(
                    text = currentError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    when {
                        nombre.isBlank() || apellido.isBlank() || rut.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                            errorMessage = "Por favor complete todos los campos"
                        }
                        !rut.esRutValido() -> {
                            errorMessage = "RUT inválido"
                        }
                        password != confirmPassword -> {
                            errorMessage = "Las contraseñas no coinciden"
                        }
                        !isTecnicoSelected && !isJefaturaSelected && !isClienteSelected -> {
                            roleError = "debes seleccionar como registrarse."
                        }
                        isClienteSelected && clienteSeleccionado == null -> {
                            errorMessage = "Seleccione la empresa a la que pertenece"
                        }
                        else -> {
                            errorMessage = null
                            roleError = null
                            val role = when {
                                isTecnicoSelected -> RolUsuario.TECNICO
                                isJefaturaSelected -> RolUsuario.JEFATURA
                                else -> RolUsuario.CLIENTE
                            }
                            val clienteId = if (isClienteSelected) clienteSeleccionado?.id else null
                            onRegisterSuccess(rut, password, nombre, apellido, role, clienteId) { error ->
                                errorMessage = error
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text(text = "Registrar", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
