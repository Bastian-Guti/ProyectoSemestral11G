package com.atzael.proyectosemestralg11.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicBoolean

// HU-10 y HU-22: se pide la ubicación UNA vez en cada check-in y check-out; nunca hay seguimiento continuo
object Ubicacion {
    private const val ESPERA_MS = 10_000L

    fun tienePermiso(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    // Entrega (latitud, longitud), o null si no hay permiso o no se pudo obtener a tiempo
    @SuppressLint("MissingPermission")
    fun obtenerUnaVez(context: Context, onResultado: (Pair<Double, Double>?) -> Unit) {
        val gestor = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (gestor == null || !tienePermiso(context)) {
            onResultado(null)
            return
        }

        val entregado = AtomicBoolean(false)
        val manejador = Handler(Looper.getMainLooper())
        fun entregar(ubicacion: Location?) {
            if (entregado.compareAndSet(false, true)) {
                manejador.removeCallbacksAndMessages(null)
                onResultado((ubicacion ?: ultimaConocida(gestor))?.let { it.latitude to it.longitude })
            }
        }

        val proveedor = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .firstOrNull { runCatching { gestor.isProviderEnabled(it) }.getOrDefault(false) }
        if (proveedor == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            entregar(null)
            return
        }

        val cancelacion = CancellationSignal()
        manejador.postDelayed({
            cancelacion.cancel()
            entregar(null)
        }, ESPERA_MS)
        gestor.getCurrentLocation(proveedor, cancelacion, ContextCompat.getMainExecutor(context)) { entregar(it) }
    }

    @SuppressLint("MissingPermission")
    private fun ultimaConocida(gestor: LocationManager): Location? =
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .mapNotNull { runCatching { gestor.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
}
