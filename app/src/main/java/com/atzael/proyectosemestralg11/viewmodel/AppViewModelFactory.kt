package com.atzael.proyectosemestralg11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.atzael.proyectosemestralg11.data.AppDatabase
import com.atzael.proyectosemestralg11.data.ClimaRepository
import com.atzael.proyectosemestralg11.data.OpenMeteoClimaRepository

class AppViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {

    // Una sola instancia: guarda en memoria los pronósticos ya consultados
    private val clima: ClimaRepository = OpenMeteoClimaRepository()

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
                    database.usuarioDao(),
                    clima
                )

            modelClass.isAssignableFrom(FichaEquipoViewModel::class.java) ->
                FichaEquipoViewModel(
                    database.equipoDao(),
                    database.localDao(),
                    database.clienteDao(),
                    database.ordenTrabajoDao()
                )

            modelClass.isAssignableFrom(ClimaViewModel::class.java) ->
                ClimaViewModel(database.ordenTrabajoDao(), clima)

            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
        return viewModel as T
    }
}
