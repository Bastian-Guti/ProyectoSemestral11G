package com.atzael.proyectosemestralg11.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.atzael.proyectosemestralg11.data.AppDatabase
import com.atzael.proyectosemestralg11.model.RolUsuario
import com.atzael.proyectosemestralg11.view.PortalClienteOrdenScreen
import com.atzael.proyectosemestralg11.view.PortalClienteScreen
import com.atzael.proyectosemestralg11.view.AlertasClimaScreen
import com.atzael.proyectosemestralg11.view.ClienteFormScreen
import com.atzael.proyectosemestralg11.view.ClientesScreen
import com.atzael.proyectosemestralg11.view.EquipoFormScreen
import com.atzael.proyectosemestralg11.view.EquiposScreen
import com.atzael.proyectosemestralg11.view.JefaturaScreen
import com.atzael.proyectosemestralg11.view.LocalFormScreen
import com.atzael.proyectosemestralg11.view.LocalesScreen
import com.atzael.proyectosemestralg11.view.LoginScreen
import com.atzael.proyectosemestralg11.view.OrdenDetalleScreen
import com.atzael.proyectosemestralg11.view.OrdenFormScreen
import com.atzael.proyectosemestralg11.view.OrdenesScreen
import com.atzael.proyectosemestralg11.view.RegisterScreen
import com.atzael.proyectosemestralg11.view.ReportesScreen
import com.atzael.proyectosemestralg11.view.TecnicoScreen
import com.atzael.proyectosemestralg11.view.FichaEquipoScreen
import com.atzael.proyectosemestralg11.view.TecnicoOrdenScreen
import com.atzael.proyectosemestralg11.viewmodel.FichaEquipoViewModel
import com.atzael.proyectosemestralg11.viewmodel.TecnicoViewModel
import com.atzael.proyectosemestralg11.viewmodel.AppViewModelFactory
import com.atzael.proyectosemestralg11.viewmodel.AuthViewModel
import com.atzael.proyectosemestralg11.viewmodel.ClienteViewModel
import com.atzael.proyectosemestralg11.viewmodel.EquipoViewModel
import com.atzael.proyectosemestralg11.viewmodel.LocalViewModel
import com.atzael.proyectosemestralg11.viewmodel.OrdenViewModel
import com.atzael.proyectosemestralg11.viewmodel.PortalClienteViewModel
import com.atzael.proyectosemestralg11.viewmodel.ResultadoAuth
import com.atzael.proyectosemestralg11.viewmodel.ReporteViewModel

private val argumentoId = listOf(navArgument("id") { type = NavType.IntType })

private fun NavBackStackEntry.id(): Int = arguments?.getInt("id") ?: 0

private fun NavHostController.cerrarPantalla(ruta: String) {
    popBackStack(ruta, inclusive = true)
}

private fun NavHostController.irALogin() {
    navigate(Rutas.Login.route) {
        popUpTo(graph.id) { inclusive = true }
    }
}

// Obtiene un ViewModel de forma Lazy, vinculado al flujo o al destino en la pila de navegación
@Composable
private inline fun <reified T : ViewModel> NavHostController.obtenerViewModel(
    factory: ViewModelProvider.Factory,
    rutaPadre: String? = null
): T {
    val parentEntry = if (rutaPadre != null) {
        val entry = currentBackStackEntry
        remember(entry, rutaPadre) {
            runCatching { getBackStackEntry(rutaPadre) }.getOrNull()
        }
    } else null
    return if (parentEntry != null) viewModel(parentEntry, factory = factory) else viewModel(factory = factory)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val database = remember { AppDatabase.getDatabase(context) }
    val factory = remember { AppViewModelFactory(database) }

    // Únicamente AuthViewModel se inicializa en la raíz para mantener la sesión global del usuario
    val authViewModel: AuthViewModel = viewModel(factory = factory)

    val rutActual by authViewModel.rutActual.collectAsState()
    val nombreActual by authViewModel.nombreActual.collectAsState()
    val clientesRegistrados by authViewModel.clientes.collectAsState()

    val onLogoutGlobal: () -> Unit = {
        authViewModel.cerrarSesion()
        navController.irALogin()
    }

    NavHost(navController = navController, startDestination = Rutas.Login.route) {

        // ---------- Acceso ----------
        composable(Rutas.Login.route) {
            LoginScreen(
                onLoginClick = { rut, password ->
                    when (val resultado = authViewModel.iniciarSesion(rut, password)) {
                        is ResultadoAuth.Error -> resultado.mensaje
                        is ResultadoAuth.Exito -> {
                            val destino = when (resultado.rol) {
                                RolUsuario.JEFATURA -> Rutas.Jefatura.route
                                RolUsuario.CLIENTE -> Rutas.PortalCliente.route
                                else -> Rutas.Tecnico.route
                            }
                            navController.navigate(destino) {
                                popUpTo(Rutas.Login.route) { inclusive = true }
                            }
                            null
                        }
                    }
                },
                onRegisterClick = { navController.navigate(Rutas.Registro.route) }
            )
        }
        composable(Rutas.Registro.route) {
            RegisterScreen(
                onBackToLogin = { navController.cerrarPantalla(Rutas.Registro.route) },
                clientes = clientesRegistrados,
                onRegisterSuccess = { rut, password, nombre, apellido, rol, clienteId ->
                    when (val resultado = authViewModel.registrarUsuario(rut, password, nombre, apellido, rol, clienteId)) {
                        is ResultadoAuth.Error -> resultado.mensaje
                        is ResultadoAuth.Exito -> {
                            navController.cerrarPantalla(Rutas.Registro.route)
                            null
                        }
                    }
                }
            )
        }
        composable(Rutas.Tecnico.route) {
            val tecnicoViewModel: TecnicoViewModel = navController.obtenerViewModel(factory)
            TecnicoScreen(
                rut = rutActual,
                viewModel = tecnicoViewModel,
                onOrdenClick = { id -> navController.navigate(Rutas.TecnicoOrden.createRoute(id)) },
                onLogout = {
                    authViewModel.cerrarSesion()
                    navController.irALogin()
                }
            )
        }
        composable(Rutas.TecnicoOrden.route, arguments = argumentoId) { entry ->
            val tecnicoViewModel: TecnicoViewModel = navController.obtenerViewModel(factory, Rutas.Tecnico.route)
            TecnicoOrdenScreen(
                viewModel = tecnicoViewModel,
                ordenId = entry.id(),
                onBack = { navController.cerrarPantalla(Rutas.TecnicoOrden.route) },
                onLogout = onLogoutGlobal,
                onVerFicha = { equipoId -> navController.navigate(Rutas.FichaEquipo.createRoute(equipoId)) }
            )
        }
        composable(Rutas.FichaEquipo.route, arguments = argumentoId) { entry ->
            val fichaEquipoViewModel: FichaEquipoViewModel = navController.obtenerViewModel(factory)
            FichaEquipoScreen(
                viewModel = fichaEquipoViewModel,
                equipoId = entry.id(),
                onBack = { navController.cerrarPantalla(Rutas.FichaEquipo.route) },
                onLogout = onLogoutGlobal
            )
        }

        // ---------- Portal del cliente ----------
        composable(Rutas.PortalCliente.route) {
            val portalClienteViewModel: PortalClienteViewModel = navController.obtenerViewModel(factory)
            PortalClienteScreen(
                rut = rutActual,
                nombre = nombreActual,
                viewModel = portalClienteViewModel,
                onOrdenClick = { id -> navController.navigate(Rutas.PortalClienteOrden.createRoute(id)) },
                onLogout = {
                    authViewModel.cerrarSesion()
                    portalClienteViewModel.onFiltroEstadoChange(null)
                    navController.irALogin()
                }
            )
        }
        composable(Rutas.PortalClienteOrden.route, arguments = argumentoId) { entry ->
            val portalClienteViewModel: PortalClienteViewModel = navController.obtenerViewModel(factory, Rutas.PortalCliente.route)
            PortalClienteOrdenScreen(
                viewModel = portalClienteViewModel,
                rutUsuario = rutActual,
                ordenId = entry.id(),
                onBack = { navController.cerrarPantalla(Rutas.PortalClienteOrden.route) },
                onLogout = onLogoutGlobal
            )
        }

        // ---------- Jefatura ----------
        composable(Rutas.Jefatura.route) {
            val ordenViewModel: OrdenViewModel = navController.obtenerViewModel(factory)
            JefaturaScreen(
                nombre = nombreActual,
                viewModel = ordenViewModel,
                onOrdenes = { navController.navigate(Rutas.Ordenes.route) },
                onNuevaOrden = {
                    ordenViewModel.resetForm()
                    navController.navigate(Rutas.OrdenNueva.route)
                },
                onClientes = { navController.navigate(Rutas.Clientes.route) },
                onLocales = { navController.navigate(Rutas.Locales.route) },
                onEquipos = { navController.navigate(Rutas.Equipos.route) },
                onReportes = { navController.navigate(Rutas.Reportes.route) },
                onAlertasClima = { navController.navigate(Rutas.AlertasClima.route) },
                onLogout = {
                    authViewModel.cerrarSesion()
                    ordenViewModel.limpiarFiltros()
                    navController.irALogin()
                }
            )
        }

        // ---------- Órdenes de trabajo ----------
        composable(Rutas.Ordenes.route) {
            val ordenViewModel: OrdenViewModel = navController.obtenerViewModel(factory, Rutas.Jefatura.route)
            OrdenesScreen(
                viewModel = ordenViewModel,
                onBack = { navController.cerrarPantalla(Rutas.Ordenes.route) },
                onLogout = onLogoutGlobal,
                onNuevaOrden = {
                    ordenViewModel.resetForm()
                    navController.navigate(Rutas.OrdenNueva.route)
                },
                onOrdenClick = { id -> navController.navigate(Rutas.OrdenDetalle.createRoute(id)) }
            )
        }
        composable(Rutas.OrdenNueva.route) {
            val ordenViewModel: OrdenViewModel = navController.obtenerViewModel(factory, Rutas.Jefatura.route)
            OrdenFormScreen(
                viewModel = ordenViewModel,
                rutUsuario = rutActual,
                ordenId = null,
                onBack = { navController.cerrarPantalla(Rutas.OrdenNueva.route) },
                onLogout = onLogoutGlobal
            )
        }
        composable(Rutas.OrdenEdicion.route, arguments = argumentoId) { entry ->
            val ordenViewModel: OrdenViewModel = navController.obtenerViewModel(factory, Rutas.Jefatura.route)
            OrdenFormScreen(
                viewModel = ordenViewModel,
                rutUsuario = rutActual,
                ordenId = entry.id(),
                onBack = {
                    ordenViewModel.resetForm()
                    navController.cerrarPantalla(Rutas.OrdenEdicion.route)
                },
                onLogout = onLogoutGlobal
            )
        }
        composable(Rutas.OrdenDetalle.route, arguments = argumentoId) { entry ->
            val ordenViewModel: OrdenViewModel = navController.obtenerViewModel(factory, Rutas.Jefatura.route)
            OrdenDetalleScreen(
                viewModel = ordenViewModel,
                ordenId = entry.id(),
                onBack = { navController.cerrarPantalla(Rutas.OrdenDetalle.route) },
                onLogout = onLogoutGlobal,
                onEditar = { id ->
                    ordenViewModel.resetForm()
                    navController.navigate(Rutas.OrdenEdicion.createRoute(id))
                },
                onVerFicha = { equipoId -> navController.navigate(Rutas.FichaEquipo.createRoute(equipoId)) }
            )
        }

        // ---------- Clientes ----------
        composable(Rutas.Clientes.route) {
            val clienteViewModel: ClienteViewModel = navController.obtenerViewModel(factory)
            ClientesScreen(
                viewModel = clienteViewModel,
                onBack = { navController.cerrarPantalla(Rutas.Clientes.route) },
                onLogout = onLogoutGlobal,
                onNuevo = {
                    clienteViewModel.resetForm()
                    navController.navigate(Rutas.ClienteNuevo.route)
                },
                onEditar = { id ->
                    clienteViewModel.resetForm()
                    navController.navigate(Rutas.ClienteEdicion.createRoute(id))
                }
            )
        }
        composable(Rutas.ClienteNuevo.route) {
            val clienteViewModel: ClienteViewModel = navController.obtenerViewModel(factory, Rutas.Clientes.route)
            ClienteFormScreen(
                viewModel = clienteViewModel,
                clienteId = null,
                onBack = { navController.cerrarPantalla(Rutas.ClienteNuevo.route) },
                onLogout = onLogoutGlobal
            )
        }
        composable(Rutas.ClienteEdicion.route, arguments = argumentoId) { entry ->
            val clienteViewModel: ClienteViewModel = navController.obtenerViewModel(factory, Rutas.Clientes.route)
            ClienteFormScreen(
                viewModel = clienteViewModel,
                clienteId = entry.id(),
                onBack = {
                    clienteViewModel.resetForm()
                    navController.cerrarPantalla(Rutas.ClienteEdicion.route)
                },
                onLogout = onLogoutGlobal
            )
        }

        // ---------- Locales ----------
        composable(Rutas.Locales.route) {
            val localViewModel: LocalViewModel = navController.obtenerViewModel(factory)
            LocalesScreen(
                viewModel = localViewModel,
                onBack = { navController.cerrarPantalla(Rutas.Locales.route) },
                onLogout = onLogoutGlobal,
                onNuevo = {
                    localViewModel.resetForm()
                    navController.navigate(Rutas.LocalNuevo.route)
                },
                onEditar = { id ->
                    localViewModel.resetForm()
                    navController.navigate(Rutas.LocalEdicion.createRoute(id))
                }
            )
        }
        composable(Rutas.LocalNuevo.route) {
            val localViewModel: LocalViewModel = navController.obtenerViewModel(factory, Rutas.Locales.route)
            LocalFormScreen(
                viewModel = localViewModel,
                localId = null,
                onBack = { navController.cerrarPantalla(Rutas.LocalNuevo.route) },
                onLogout = onLogoutGlobal
            )
        }
        composable(Rutas.LocalEdicion.route, arguments = argumentoId) { entry ->
            val localViewModel: LocalViewModel = navController.obtenerViewModel(factory, Rutas.Locales.route)
            LocalFormScreen(
                viewModel = localViewModel,
                localId = entry.id(),
                onBack = {
                    localViewModel.resetForm()
                    navController.cerrarPantalla(Rutas.LocalEdicion.route)
                },
                onLogout = onLogoutGlobal
            )
        }

        // ---------- Equipos ----------
        composable(Rutas.Equipos.route) {
            val equipoViewModel: EquipoViewModel = navController.obtenerViewModel(factory)
            EquiposScreen(
                viewModel = equipoViewModel,
                onBack = { navController.cerrarPantalla(Rutas.Equipos.route) },
                onLogout = onLogoutGlobal,
                onNuevo = {
                    equipoViewModel.resetForm()
                    navController.navigate(Rutas.EquipoNuevo.route)
                },
                onEditar = { id ->
                    equipoViewModel.resetForm()
                    navController.navigate(Rutas.EquipoEdicion.createRoute(id))
                }
            )
        }
        composable(Rutas.EquipoNuevo.route) {
            val equipoViewModel: EquipoViewModel = navController.obtenerViewModel(factory, Rutas.Equipos.route)
            EquipoFormScreen(
                viewModel = equipoViewModel,
                equipoId = null,
                onBack = { navController.cerrarPantalla(Rutas.EquipoNuevo.route) },
                onLogout = onLogoutGlobal
            )
        }
        composable(Rutas.EquipoEdicion.route, arguments = argumentoId) { entry ->
            val equipoViewModel: EquipoViewModel = navController.obtenerViewModel(factory, Rutas.Equipos.route)
            EquipoFormScreen(
                viewModel = equipoViewModel,
                equipoId = entry.id(),
                onBack = {
                    equipoViewModel.resetForm()
                    navController.cerrarPantalla(Rutas.EquipoEdicion.route)
                },
                onLogout = onLogoutGlobal
            )
        }

        // ---------- Alertas de clima ----------
        composable(Rutas.AlertasClima.route) {
            AlertasClimaScreen(
                onBack = { navController.cerrarPantalla(Rutas.AlertasClima.route) },
                onLogout = onLogoutGlobal
            )
        }

        // ---------- Reportes ----------
        composable(Rutas.Reportes.route) {
            val reporteViewModel: ReporteViewModel = navController.obtenerViewModel(factory)
            ReportesScreen(
                viewModel = reporteViewModel,
                onBack = { navController.cerrarPantalla(Rutas.Reportes.route) },
                onLogout = onLogoutGlobal
            )
        }
    }
}
