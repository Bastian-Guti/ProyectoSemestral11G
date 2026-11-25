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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.atzael.proyectosemestralg11.ui.theme.ProyectoSemestralG11Theme
import com.atzael.proyectosemestralg11.util.esRutValido
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    // Devuelve el mensaje de error, o null si el inicio de sesión fue exitoso
    onLoginClick: suspend (String, String) -> String? = { _, _ -> null },
    onRegisterClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var rut by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isRutInvalid = rut.isNotBlank() && !rut.esRutValido() && rut.length >= 8

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
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.White)) {
                        append("Bienvenido a la ")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFF2196F3))) {
                        append("ACIS")
                    }
                    withStyle(style = SpanStyle(color = Color.White)) {
                        append(" ")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFFFCC100))) {
                        append("OT")
                    }
                    withStyle(style = SpanStyle(color = Color.White)) {
                        append(" app")
                    }
                },
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
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
                    .padding(bottom = 24.dp)
                    .clickable(onClick = onRegisterClick)
            )

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
                        else -> {
                            errorMessage = null
                            scope.launch { errorMessage = onLoginClick(rut, password) }
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
