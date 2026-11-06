package com.atzael.proyectosemestralg11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atzael.proyectosemestralg11.data.ClienteDao
import com.atzael.proyectosemestralg11.data.EquipoDao
import com.atzael.proyectosemestralg11.data.LocalDao
import com.atzael.proyectosemestralg11.data.OrdenTrabajoDao
import com.atzael.proyectosemestralg11.model.Equipo
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// HU-04: ficha del equipo y historial de intervenciones anteriores
@OptIn(ExperimentalCoroutinesApi::class)
class FichaEquipoViewModel(
    private val equipoDao: EquipoDao,
    private val localDao: LocalDao,
    private val clienteDao: ClienteDao,
    private val ordenDao: OrdenTrabajoDao
) : ViewModel() {

    private val _equipoId = MutableStateFlow<Int?>(null)

    private val _equipo = MutableStateFlow<Equipo?>(null)
    val equipo: StateFlow<Equipo?> = _equipo.asStateFlow()

    private val _ubicacionTexto = MutableStateFlow("")
    val ubicacionTexto: StateFlow<String> = _ubicacionTexto.asStateFlow()

    val historial: StateFlow<List<OrdenConDetalle>> = _equipoId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else ordenDao.getFinalizadasPorEquipo(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun cargarEquipo(id: Int) {
        _equipoId.value = id
        viewModelScope.launch {
            val equipo = equipoDao.getById(id)
            _equipo.value = equipo
            val local = equipo?.let { localDao.getById(it.localId) }
            val cliente = local?.let { clienteDao.getById(it.clienteId) }
            _ubicacionTexto.value = listOfNotNull(cliente?.nombre, local?.nombre, local?.direccion).joinToString(" - ")
        }
    }
}
