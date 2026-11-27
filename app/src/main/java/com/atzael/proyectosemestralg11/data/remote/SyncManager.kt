package com.atzael.proyectosemestralg11.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.atzael.proyectosemestralg11.data.AppDatabase
import com.atzael.proyectosemestralg11.util.comprimirFotoABase64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

// Sincronización Room <-> Supabase. Supabase es la fuente principal:
//  1. PULL: lo que hay en Supabase se guarda en Room con upsert (actualiza la fila con ese id o la crea).
//  2. PUSH: solo se suben los registros locales cuyo id todavía NO existe en Supabase.
//     Nunca se sobrescribe Supabase desde aquí; los cambios a registros existentes van por PATCH en cada ViewModel.
// Si la descarga de una tabla falla (sin red, error del servidor), esa tabla no se sube:
// sin saber qué hay en Supabase no se puede decidir qué falta.
// Las filas remotas con id = 0 se ignoran: Room interpretaría el 0 como "sin id" y crearía un duplicado.
object SyncManager {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val isSyncing = AtomicBoolean(false)

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
        if (!isSyncing.compareAndSet(false, true)) return
        scope.launch {
            try {
                // El orden importa por las claves foráneas: primero los padres, luego los hijos.
                sincronizarClientes(database)
                sincronizarUsuarios(database)
                sincronizarLocales(database)
                sincronizarEquipos(database)
                sincronizarOrdenes(database)
                sincronizarReprogramaciones(database)
                sincronizarFotos(database)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSyncing.set(false)
            }
        }
    }

    private suspend fun sincronizarClientes(db: AppDatabase) {
        val remotos = runCatching { SupabaseClient.api.getClientes() }.getOrNull() ?: return
        remotos.filter { it.id != 0 }.forEach { runCatching { db.clienteDao().upsert(it) } }

        val idsRemotos = remotos.map { it.id }.toSet()
        db.clienteDao().getAll().first()
            .filter { it.id != 0 && it.id !in idsRemotos }
            .forEach { runCatching { SupabaseClient.api.crearCliente(it) } }
    }

    private suspend fun sincronizarUsuarios(db: AppDatabase) {
        val remotos = runCatching { SupabaseClient.api.getUsuarios() }.getOrNull() ?: return
        // Los usuarios se identifican por RUT: insertUsuario reemplaza el que tenga el mismo RUT
        remotos.filter { it.rut.isNotBlank() }.forEach { runCatching { db.usuarioDao().insertUsuario(it) } }

        val rutsRemotos = remotos.map { it.rut }.toSet()
        db.usuarioDao().getAllUsuarios()
            .filter { it.rut !in rutsRemotos }
            .forEach { runCatching { SupabaseClient.api.crearUsuario(it) } }
    }

    private suspend fun sincronizarLocales(db: AppDatabase) {
        val remotos = runCatching { SupabaseClient.api.getLocales() }.getOrNull() ?: return
        remotos.filter { it.id != 0 }.forEach { runCatching { db.localDao().upsert(it) } }

        val idsRemotos = remotos.map { it.id }.toSet()
        db.localDao().getAll().first()
            .filter { it.id != 0 && it.id !in idsRemotos }
            .forEach { runCatching { SupabaseClient.api.crearLocal(it) } }
    }

    private suspend fun sincronizarEquipos(db: AppDatabase) {
        val remotos = runCatching { SupabaseClient.api.getEquipos() }.getOrNull() ?: return
        remotos.filter { it.id != 0 }.forEach { runCatching { db.equipoDao().upsert(it) } }

        val idsRemotos = remotos.map { it.id }.toSet()
        db.equipoDao().getAll().first()
            .filter { it.id != 0 && it.id !in idsRemotos }
            .forEach { runCatching { SupabaseClient.api.crearEquipo(it) } }
    }

    private suspend fun sincronizarOrdenes(db: AppDatabase) {
        val remotos = runCatching { SupabaseClient.api.getOrdenesTrabajo() }.getOrNull() ?: return
        remotos.filter { it.id != 0 }.forEach { runCatching { db.ordenTrabajoDao().upsert(it) } }

        val idsRemotos = remotos.map { it.id }.toSet()
        db.ordenTrabajoDao().getAllConDetalle().first()
            .map { it.orden }
            .filter { it.id != 0 && it.id !in idsRemotos }
            .forEach { runCatching { SupabaseClient.api.crearOrdenTrabajo(it) } }
    }

    private suspend fun sincronizarReprogramaciones(db: AppDatabase) {
        val remotos = runCatching { SupabaseClient.api.getReprogramaciones() }.getOrNull() ?: return
        remotos.filter { it.id != 0 }.forEach { runCatching { db.ordenTrabajoDao().upsertReprogramacion(it) } }

        val idsRemotos = remotos.map { it.id }.toSet()
        db.ordenTrabajoDao().getAllReprogramaciones()
            .filter { it.id != 0 && it.id !in idsRemotos }
            .forEach { runCatching { SupabaseClient.api.crearReprogramacion(it) } }
    }

    private suspend fun sincronizarFotos(db: AppDatabase) {
        val remotos = runCatching { SupabaseClient.api.getFotosOrden() }.getOrNull() ?: return
        remotos.filter { it.id != 0 }.forEach { remota ->
            runCatching {
                val local = db.fotoDao().getById(remota.id)
                // Misma foto que ya está en el teléfono: se conserva la local (ruta a archivo, no Base64)
                val esLaMisma = local != null && local.ordenId == remota.ordenId && local.fecha == remota.fecha
                if (!esLaMisma) db.fotoDao().upsert(remota)
            }
        }

        val idsRemotos = remotos.map { it.id }.toSet()
        db.fotoDao().getAllFotos()
            .filter { it.id != 0 && it.id !in idsRemotos }
            .forEach { foto ->
                runCatching {
                    val base64 = comprimirFotoABase64(foto.ruta) ?: return@runCatching
                    SupabaseClient.api.crearFotoOrden(foto.copy(ruta = base64))
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
