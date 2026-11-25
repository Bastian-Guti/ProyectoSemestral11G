package com.atzael.proyectosemestralg11.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

// HU-10 y HU-22: se pide la ubicación UNA vez en cada check-in y check-out; nunca hay seguimiento continuo
object Ubicacion {
    private const val ESPERA_MS = 10_000L

    fun tienePermiso(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    // Devuelve (latitud, longitud), o null si no hay permiso o no se pudo obtener a tiempo.
    // Al cancelarse la corrutina también se cancela la consulta al sistema.
    @SuppressLint("MissingPermission")
    suspend fun obtenerUnaVez(context: Context): Pair<Double, Double>? {
        val gestor = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (gestor == null || !tienePermiso(context)) return null

        val proveedor = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .firstOrNull { runCatching { gestor.isProviderEnabled(it) }.getOrDefault(false) }

        val actual: Location? =
            if (proveedor == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) null
            else withTimeoutOrNull(ESPERA_MS) {
                suspendCancellableCoroutine { continuacion ->
                    val cancelacion = CancellationSignal()
                    continuacion.invokeOnCancellation { cancelacion.cancel() }
                    gestor.getCurrentLocation(
                        proveedor,
                        cancelacion,
                        ContextCompat.getMainExecutor(context)
                    ) { continuacion.resume(it) }
                }
            }

        return (actual ?: ultimaConocida(gestor))?.let { it.latitude to it.longitude }
    }

    @SuppressLint("MissingPermission")
    private fun ultimaConocida(gestor: LocationManager): Location? =
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .mapNotNull { runCatching { gestor.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
}
