package com.atzael.proyectosemestralg11.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

// Ejecuta el bloque en el viewModelScope y espera su resultado.
// Sirve para exponer funciones suspend que la pantalla puede esperar (en vez de pasar callbacks):
// si la pantalla se destruye mientras espera, el guardado igual termina porque corre en el ViewModel.
suspend fun <T> ViewModel.enViewModelScope(bloque: suspend CoroutineScope.() -> T): T =
    viewModelScope.async(block = bloque).await()
