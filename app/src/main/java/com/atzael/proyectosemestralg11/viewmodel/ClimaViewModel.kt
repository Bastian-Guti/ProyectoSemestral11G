package com.atzael.proyectosemestralg11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atzael.proyectosemestralg11.data.ClimaRepository
import com.atzael.proyectosemestralg11.data.OrdenTrabajoDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// HU-15: alertas de lluvia o viento fuerte para la jefatura
class ClimaViewModel(
    private val ordenDao: OrdenTrabajoDao,
    private val clima: ClimaRepository
) : ViewModel() {

    private val _resultado = MutableStateFlow(ResultadoAlertas())
    val resultado: StateFlow<ResultadoAlertas> = _resultado.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    init {
        // Se recalcula cada vez que cambian las OT (nueva OT, reagendada, etc.)
        viewModelScope.launch {
            ordenDao.getAllConDetalle().collectLatest { lista ->
                _cargando.value = true
                _resultado.value = AlertasClima.evaluarOrdenes(lista, clima)
                _cargando.value = false
            }
        }
    }
}
