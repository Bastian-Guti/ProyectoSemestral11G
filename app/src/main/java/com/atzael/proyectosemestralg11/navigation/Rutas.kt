package com.atzael.proyectosemestralg11.navigation

sealed class Rutas(val route: String) {
    data object Login : Rutas("login")
    data object Registro : Rutas("registro")
    data object Tecnico : Rutas("tecnico")
    data object TecnicoOrden : Rutas("tecnico_orden/{id}") {
        fun createRoute(id: Int) = "tecnico_orden/$id"
    }
    data object FichaEquipo : Rutas("ficha_equipo/{id}") {
        fun createRoute(id: Int) = "ficha_equipo/$id"
    }
    data object AlertasClima : Rutas("alertas_clima")
    data object Jefatura : Rutas("jefatura")

    data object PortalCliente : Rutas("portal_cliente")
    data object PortalClienteOrden : Rutas("portal_cliente_orden/{id}") {
        fun createRoute(id: Int) = "portal_cliente_orden/$id"
    }

    data object Ordenes : Rutas("ordenes")
    data object OrdenNueva : Rutas("orden_nueva")
    data object OrdenEdicion : Rutas("orden_edicion/{id}") {
        fun createRoute(id: Int) = "orden_edicion/$id"
    }
    data object OrdenDetalle : Rutas("orden_detalle/{id}") {
        fun createRoute(id: Int) = "orden_detalle/$id"
    }

    data object Clientes : Rutas("clientes")
    data object ClienteNuevo : Rutas("cliente_nuevo")
    data object ClienteEdicion : Rutas("cliente_edicion/{id}") {
        fun createRoute(id: Int) = "cliente_edicion/$id"
    }

    data object Locales : Rutas("locales")
    data object LocalNuevo : Rutas("local_nuevo")
    data object LocalEdicion : Rutas("local_edicion/{id}") {
        fun createRoute(id: Int) = "local_edicion/$id"
    }

    data object Equipos : Rutas("equipos")
    data object EquipoNuevo : Rutas("equipo_nuevo")
    data object EquipoEdicion : Rutas("equipo_edicion/{id}") {
        fun createRoute(id: Int) = "equipo_edicion/$id"
    }

    data object Reportes : Rutas("reportes")
}
