package com.atzael.proyectosemestralg11.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
import com.atzael.proyectosemestralg11.viewmodel.ReporteViewModel

private val argumentoId = listOf(navArgument("id") { type = NavType.IntType })

private fun NavBackStackEntry.id(): Int = arguments?.getInt("id") ?: 0

// Cierra solo la pantalla indicada. Si ya no está en la pila (doble toque en "volver", o un aviso
// que llega tarde después de que el usuario ya retrocedió) no hace nada. Antes se hacía un
// popBackStack() a ciegas, que podía sacar de más y dejar la pila vacía: pantalla en blanco.
private fun NavHostController.cerrarPantalla(ruta: String) {
    popBackStack(ruta, inclusive = true)
}

private fun NavHostController.irALogin() {
    navigate(Rutas.Login.route) {
        popUpTo(graph.id) { inclusive = true }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Usamos remember para obtener la instancia solo una vez por ciclo de vida del Composable
    val database = remember { AppDatabase.getDatabase(context) }
    val factory = remember { AppViewModelFactory(database) }

    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val clienteViewModel: ClienteViewModel = viewModel(factory = factory)
    val localViewModel: LocalViewModel = viewModel(factory = factory)
    val equipoViewModel: EquipoViewModel = viewModel(factory = factory)
    val ordenViewModel: OrdenViewModel = viewModel(factory = factory)
    val reporteViewModel: ReporteViewModel = viewModel(factory = factory)
    val portalClienteViewModel: PortalClienteViewModel = viewModel(factory = factory)
    val tecnicoViewModel: TecnicoViewModel = viewModel(factory = factory)
    val fichaEquipoViewModel: FichaEquipoViewModel = viewModel(factory = factory)

    val rutActual by authViewModel.rutActual.collectAsState()
    val nombreActual by authViewModel.nombreActual.collectAsState()
    val clientesRegistrados by authViewModel.clientes.collectAsState()

    NavHost(navController = navController, startDestination = Rutas.Login.route) {

        // ---------- Acceso ----------
        composable(Rutas.Login.route) {
            LoginScreen(
                onLoginClick = { rut, password, rol, onError ->
                    authViewModel.iniciarSesion(
                        rut = rut,
                        password = password,
                        rol = rol,
                        onExito = {
                            val destino = when (rol) {
                                RolUsuario.JEFATURA -> Rutas.Jefatura.route
                                RolUsuario.CLIENTE -> Rutas.PortalCliente.route
                                else -> Rutas.Tecnico.route
                            }
                            navController.navigate(destino) {
                                popUpTo(Rutas.Login.route) { inclusive = true }
                            }
                        },
                        onError = onError
                    )
                },
                onRegisterClick = { navController.navigate(Rutas.Registro.route) }
            )
        }
        composable(Rutas.Registro.route) {
            RegisterScreen(
                onBackToLogin = { navController.cerrarPantalla(Rutas.Registro.route) },
                clientes = clientesRegistrados,
                onRegisterSuccess = { rut, password, nombre, apellido, rol, clienteId, onError ->
                    authViewModel.registrarUsuario(
                        rut = rut,
                        password = password,
                        nombre = nombre,
                        apellido = apellido,
                        rol = rol,
                        clienteId = clienteId,
                        onExito = { navController.cerrarPantalla(Rutas.Registro.route) },
                        onError = onError
                    )
                }
            )
        }
        composable(Rutas.Tecnico.route) {
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
            TecnicoOrdenScreen(
                viewModel = tecnicoViewModel,
                ordenId = entry.id(),
                onBack = { navController.cerrarPantalla(Rutas.TecnicoOrden.route) },
                onVerFicha = { equipoId -> navController.navigate(Rutas.FichaEquipo.createRoute(equipoId)) }
            )
        }
        composable(Rutas.FichaEquipo.route, arguments = argumentoId) { entry ->
            FichaEquipoScreen(
                viewModel = fichaEquipoViewModel,
                equipoId = entry.id(),
                onBack = { navController.cerrarPantalla(Rutas.FichaEquipo.route) }
            )
        }

        // ---------- Portal del cliente ----------
        composable(Rutas.PortalCliente.route) {
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
            PortalClienteOrdenScreen(
                viewModel = portalClienteViewModel,
                rutUsuario = rutActual,
                ordenId = entry.id(),
                onBack = { navController.cerrarPantalla(Rutas.PortalClienteOrden.route) }
            )
        }

        // ---------- Jefatura ----------
        composable(Rutas.Jefatura.route) {
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
            OrdenesScreen(
                viewModel = ordenViewModel,
                onBack = { navController.cerrarPantalla(Rutas.Ordenes.route) },
                onNuevaOrden = {
                    ordenViewModel.resetForm()
                    navController.navigate(Rutas.OrdenNueva.route)
                },
                onOrdenClick = { id -> navController.navigate(Rutas.OrdenDetalle.createRoute(id)) }
            )
        }
        composable(Rutas.OrdenNueva.route) {
            OrdenFormScreen(
                viewModel = ordenViewModel,
                rutUsuario = rutActual,
                ordenId = null,
                onBack = { navController.cerrarPantalla(Rutas.OrdenNueva.route) }
            )
        }
        composable(Rutas.OrdenEdicion.route, arguments = argumentoId) { entry ->
            OrdenFormScreen(
                viewModel = ordenViewModel,
                rutUsuario = rutActual,
                ordenId = entry.id(),
                onBack = {
                    ordenViewModel.resetForm()
                    navController.cerrarPantalla(Rutas.OrdenEdicion.route)
                }
            )
        }
        composable(Rutas.OrdenDetalle.route, arguments = argumentoId) { entry ->
            OrdenDetalleScreen(
                viewModel = ordenViewModel,
                ordenId = entry.id(),
                onBack = { navController.cerrarPantalla(Rutas.OrdenDetalle.route) },
                onEditar = { id ->
                    ordenViewModel.resetForm()
                    navController.navigate(Rutas.OrdenEdicion.createRoute(id))
                },
                onVerFicha = { equipoId -> navController.navigate(Rutas.FichaEquipo.createRoute(equipoId)) }
            )
        }

        // ---------- Clientes ----------
        composable(Rutas.Clientes.route) {
            ClientesScreen(
                viewModel = clienteViewModel,
                onBack = { navController.cerrarPantalla(Rutas.Clientes.route) },
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
            ClienteFormScreen(
                viewModel = clienteViewModel,
                clienteId = null,
                onBack = { navController.cerrarPantalla(Rutas.ClienteNuevo.route) }
            )
        }
        composable(Rutas.ClienteEdicion.route, arguments = argumentoId) { entry ->
            ClienteFormScreen(
                viewModel = clienteViewModel,
                clienteId = entry.id(),
                onBack = {
                    clienteViewModel.resetForm()
                    navController.cerrarPantalla(Rutas.ClienteEdicion.route)
                }
            )
        }

        // ---------- Locales ----------
        composable(Rutas.Locales.route) {
            LocalesScreen(
                viewModel = localViewModel,
                onBack = { navController.cerrarPantalla(Rutas.Locales.route) },
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
            LocalFormScreen(
                viewModel = localViewModel,
                localId = null,
                onBack = { navController.cerrarPantalla(Rutas.LocalNuevo.route) }
            )
        }
        composable(Rutas.LocalEdicion.route, arguments = argumentoId) { entry ->
            LocalFormScreen(
                viewModel = localViewModel,
                localId = entry.id(),
                onBack = {
                    localViewModel.resetForm()
                    navController.cerrarPantalla(Rutas.LocalEdicion.route)
                }
            )
        }

        // ---------- Equipos ----------
        composable(Rutas.Equipos.route) {
            EquiposScreen(
                viewModel = equipoViewModel,
                onBack = { navController.cerrarPantalla(Rutas.Equipos.route) },
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
            EquipoFormScreen(
                viewModel = equipoViewModel,
                equipoId = null,
                onBack = { navController.cerrarPantalla(Rutas.EquipoNuevo.route) }
            )
        }
        composable(Rutas.EquipoEdicion.route, arguments = argumentoId) { entry ->
            EquipoFormScreen(
                viewModel = equipoViewModel,
                equipoId = entry.id(),
                onBack = {
                    equipoViewModel.resetForm()
                    navController.cerrarPantalla(Rutas.EquipoEdicion.route)
                }
            )
        }

        // ---------- Alertas de clima (reservada, sin funcionalidad todavía) ----------
        composable(Rutas.AlertasClima.route) {
            AlertasClimaScreen(onBack = { navController.cerrarPantalla(Rutas.AlertasClima.route) })
        }

        // ---------- Reportes ----------
        composable(Rutas.Reportes.route) {
            ReportesScreen(
                viewModel = reporteViewModel,
                onBack = { navController.cerrarPantalla(Rutas.Reportes.route) }
            )
        }
    }
}
