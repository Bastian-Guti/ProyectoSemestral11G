package com.atzael.proyectosemestralg11.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.atzael.proyectosemestralg11.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object SyncManager {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var isSyncing = false

    fun iniciarSincronizacionAuto(context: Context) {
        val database = AppDatabase.getDatabase(context)

        // 1. Sincronizar inmediatamente al iniciar la app si hay conexión a internet
        if (hayConexionInternet(context)) {
            sincronizarTodo(database)
        }

        // 2. Registrar oyente de red para sincronizar de forma transparente cuando el dispositivo reconecte a internet
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        runCatching {
            connectivityManager?.registerNetworkCallback(
                networkRequest,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        sincronizarTodo(database)
                    }
                }
            )
        }
    }

    fun sincronizarTodo(database: AppDatabase) {
        if (isSyncing) return
        isSyncing = true
        scope.launch {
            try {
                // 1. PULL: Descargar datos de Supabase y guardarlos en Room (para que la app muestre datos remotos)
                runCatching {
                    val clientesRemotos = SupabaseClient.api.getClientes()
                    clientesRemotos.forEach { c -> database.clienteDao().insert(c) }
                }
                runCatching {
                    val usuariosRemotos = SupabaseClient.api.getUsuarios()
                    usuariosRemotos.forEach { u -> database.usuarioDao().insertUsuario(u) }
                }
                runCatching {
                    val localesRemotos = SupabaseClient.api.getLocales()
                    localesRemotos.forEach { l -> database.localDao().insert(l) }
                }
                runCatching {
                    val equiposRemotos = SupabaseClient.api.getEquipos()
                    equiposRemotos.forEach { e -> database.equipoDao().insert(e) }
                }
                runCatching {
                    val ordenesRemotas = SupabaseClient.api.getOrdenesTrabajo()
                    ordenesRemotas.forEach { o -> database.ordenTrabajoDao().insert(o) }
                }

                // 2. PUSH: Subir datos locales pendientes a Supabase
                val clientes = database.clienteDao().getAll().first()
                clientes.forEach { c -> runCatching { SupabaseClient.api.crearCliente(c) } }

                val usuarios = database.usuarioDao().getAllUsuarios()
                usuarios.forEach { u -> runCatching { SupabaseClient.api.crearUsuario(u) } }

                val locales = database.localDao().getAll().first()
                locales.forEach { l -> runCatching { SupabaseClient.api.crearLocal(l) } }

                val equipos = database.equipoDao().getAll().first()
                equipos.forEach { e -> runCatching { SupabaseClient.api.crearEquipo(e) } }

                val ordenes = database.ordenTrabajoDao().getAllConDetalle().first()
                ordenes.forEach { o -> runCatching { SupabaseClient.api.crearOrdenTrabajo(o.orden) } }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSyncing = false
            }
        }
    }

    private fun hayConexionInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
