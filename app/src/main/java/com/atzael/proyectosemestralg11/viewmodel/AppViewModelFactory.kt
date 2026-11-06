package com.atzael.proyectosemestralg11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.atzael.proyectosemestralg11.data.AppDatabase

class AppViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(database.usuarioDao(), database.clienteDao())

            modelClass.isAssignableFrom(PortalClienteViewModel::class.java) ->
                PortalClienteViewModel(database.ordenTrabajoDao(), database.usuarioDao(), database.clienteDao())

            modelClass.isAssignableFrom(ClienteViewModel::class.java) ->
                ClienteViewModel(database.clienteDao())

            modelClass.isAssignableFrom(LocalViewModel::class.java) ->
                LocalViewModel(database.localDao(), database.clienteDao())

            modelClass.isAssignableFrom(EquipoViewModel::class.java) ->
                EquipoViewModel(database.equipoDao(), database.localDao(), database.clienteDao())

            modelClass.isAssignableFrom(OrdenViewModel::class.java) ->
                OrdenViewModel(
                    database.ordenTrabajoDao(),
                    database.clienteDao(),
                    database.localDao(),
                    database.equipoDao(),
                    database.usuarioDao(),
                    database.fotoDao()
                )

            modelClass.isAssignableFrom(ReporteViewModel::class.java) ->
                ReporteViewModel(
                    database.ordenTrabajoDao(),
                    database.clienteDao(),
                    database.localDao(),
                    database.fotoDao()
                )

            modelClass.isAssignableFrom(TecnicoViewModel::class.java) ->
                TecnicoViewModel(
                    database.ordenTrabajoDao(),
                    database.fotoDao(),
                    database.equipoDao(),
                    database.usuarioDao()
                )

            modelClass.isAssignableFrom(FichaEquipoViewModel::class.java) ->
                FichaEquipoViewModel(
                    database.equipoDao(),
                    database.localDao(),
                    database.clienteDao(),
                    database.ordenTrabajoDao()
                )

            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
        return viewModel as T
    }
}
