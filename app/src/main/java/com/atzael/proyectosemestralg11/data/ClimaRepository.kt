package com.atzael.proyectosemestralg11.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

data class PronosticoDia(
    val probabilidadLluvia: Int, // %
    val lluviaMm: Double,
    val vientoKmh: Double,
    val rachasKmh: Double
)

interface ClimaRepository {
    // Pronóstico día a día de la ciudad; null si no se pudo consultar (sin internet, ciudad desconocida)
    suspend fun pronostico(ciudad: String): Map<LocalDate, PronosticoDia>?

    companion object {
        const val DIAS_PRONOSTICO = 16
    }
}

// Fuente pública y gratuita, sin clave: Open-Meteo (https://open-meteo.com)
class OpenMeteoClimaRepository : ClimaRepository {

    private data class Entrada(val momento: Long, val datos: Map<LocalDate, PronosticoDia>)

    private val cache = ConcurrentHashMap<String, Entrada>()

    override suspend fun pronostico(ciudad: String): Map<LocalDate, PronosticoDia>? =
        withContext(Dispatchers.IO) {
            val clave = ciudad.trim().lowercase()
            cache[clave]?.let { if (System.currentTimeMillis() - it.momento < VIGENCIA_MS) return@withContext it.datos }
            runCatching {
                val (lat, lon) = buscarCoordenadas(ciudad.trim()) ?: return@runCatching null
                consultarPronostico(lat, lon)
            }.getOrNull()?.also { cache[clave] = Entrada(System.currentTimeMillis(), it) }
        }

    private fun buscarCoordenadas(ciudad: String): Pair<Double, Double>? {
        val nombre = URLEncoder.encode(ciudad, "UTF-8")
        val json = JSONObject(
            descargar("https://geocoding-api.open-meteo.com/v1/search?name=$nombre&count=1&language=es&format=json&countryCode=CL")
        )
        val resultado = json.optJSONArray("results")?.optJSONObject(0) ?: return null
        return resultado.getDouble("latitude") to resultado.getDouble("longitude")
    }

    private fun consultarPronostico(lat: Double, lon: Double): Map<LocalDate, PronosticoDia> {
        val json = JSONObject(
            descargar(
                "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                        "&daily=precipitation_probability_max,precipitation_sum,wind_speed_10m_max,wind_gusts_10m_max" +
                        "&timezone=auto&forecast_days=${ClimaRepository.DIAS_PRONOSTICO}"
            )
        ).getJSONObject("daily")
        val fechas = json.getJSONArray("time")
        val probabilidad = json.getJSONArray("precipitation_probability_max")
        val lluvia = json.getJSONArray("precipitation_sum")
        val viento = json.getJSONArray("wind_speed_10m_max")
        val rachas = json.getJSONArray("wind_gusts_10m_max")
        return (0 until fechas.length()).associate { i ->
            LocalDate.parse(fechas.getString(i)) to PronosticoDia(
                probabilidadLluvia = probabilidad.optInt(i, 0),
                lluviaMm = lluvia.optDouble(i, 0.0),
                vientoKmh = viento.optDouble(i, 0.0),
                rachasKmh = rachas.optDouble(i, 0.0)
            )
        }
    }

    private fun descargar(direccion: String): String {
        val conexion = URL(direccion).openConnection() as HttpURLConnection
        return try {
            conexion.connectTimeout = 8_000
            conexion.readTimeout = 8_000
            conexion.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conexion.disconnect()
        }
    }

    private companion object {
        const val VIGENCIA_MS = 30 * 60 * 1000L
    }
}
