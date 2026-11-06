package com.atzael.proyectosemestralg11.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import com.atzael.proyectosemestralg11.model.RolUsuario
import com.atzael.proyectosemestralg11.ui.theme.ProyectoSemestralG11Theme
import com.atzael.proyectosemestralg11.util.esRutValido

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginClick: (String, String, String, (String?) -> Unit) -> Unit = { _, _, _, _ -> },
    onRegisterClick: () -> Unit = {}
) {
    var rut by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var roleError by remember { mutableStateOf<String?>(null) }
    
    var isTecnicoSelected by remember { mutableStateOf(false) }
    var isJefaturaSelected by remember { mutableStateOf(false) }
    var isClienteSelected by remember { mutableStateOf(false) }

    val isRutInvalid = rut.isNotBlank() && !rut.esRutValido() && rut.length >= 8
    val hasRoleError = roleError != null
    val boxColor = if (hasRoleError) MaterialTheme.colorScheme.error else Color.White
    val textColor = if (hasRoleError) MaterialTheme.colorScheme.error else Color.White

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Iniciar Sesión") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
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
                    .padding(bottom = 16.dp)
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
                    .padding(bottom = 8.dp)
            )

            Text(
                text = "Registrarse",
                color = Color.Blue,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable(onClick = onRegisterClick)
            )

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Entrar como...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
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

                val currentRoleError = roleError
                if (currentRoleError != null) {
                    Text(
                        text = currentRoleError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
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
                        .padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    when {
                        rut.isBlank() || password.isBlank() -> {
                            errorMessage = "Por favor ingrese RUT y contraseña"
                        }
                        !rut.esRutValido() -> {
                            errorMessage = "RUT inválido"
                        }
                        !isTecnicoSelected && !isJefaturaSelected && !isClienteSelected -> {
                            roleError = "debes seleccionar como entrar."
                        }
                        else -> {
                            errorMessage = null
                            roleError = null
                            val role = when {
                                isTecnicoSelected -> RolUsuario.TECNICO
                                isJefaturaSelected -> RolUsuario.JEFATURA
                                else -> RolUsuario.CLIENTE
                            }
                            onLoginClick(rut, password, role) { error ->
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
                Text(text = "Iniciar Sesión", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ProyectoSemestralG11Theme {
        LoginScreen()
    }
}
