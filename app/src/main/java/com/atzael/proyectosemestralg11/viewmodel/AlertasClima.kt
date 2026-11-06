package com.atzael.proyectosemestralg11.viewmodel

import com.atzael.proyectosemestralg11.data.ClimaRepository
import com.atzael.proyectosemestralg11.data.PronosticoDia
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import com.atzael.proyectosemestralg11.model.UbicacionEquipo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

data class AlertaClima(val orden: OrdenConDetalle, val mensaje: String)

data class ResultadoAlertas(
    val alertas: List<AlertaClima> = emptyList(),
    val sinCiudad: Int = 0, // OT exteriores cuyo local no tiene ciudad: no se pudo consultar
    val sinPronostico: Int = 0 // OT para las que falló la consulta (por ejemplo, sin internet)
)

// HU-15: alerta de lluvia o viento fuerte antes de una mantención de equipos exteriores
object AlertasClima {
    const val PROBABILIDAD_LLUVIA = 50 // %
    const val LLUVIA_MM = 1.0
    const val VIENTO_KMH = 40.0
    const val RACHAS_KMH = 60.0

    fun evaluar(p: PronosticoDia): String? {
        val motivos = buildList {
            if (p.probabilidadLluvia >= PROBABILIDAD_LLUVIA || p.lluviaMm >= LLUVIA_MM) {
                add("lluvia (${p.probabilidadLluvia}%, ${"%.1f".format(p.lluviaMm)} mm)")
            }
            if (p.vientoKmh >= VIENTO_KMH || p.rachasKmh >= RACHAS_KMH) {
                add("viento fuerte (${p.vientoKmh.toInt()} km/h, rachas ${p.rachasKmh.toInt()} km/h)")
            }
        }
        return if (motivos.isEmpty()) null else "Pronóstico de " + motivos.joinToString(" y ")
    }

    // Solo OT de equipos exteriores, aún no ejecutadas y dentro del alcance del pronóstico
    fun aplica(item: OrdenConDetalle, hoy: LocalDate): Boolean {
        val fecha = fechaVisita(item)
        return item.equipoUbicacion == UbicacionEquipo.EXTERIOR &&
                (item.orden.estado == EstadoOrden.PENDIENTE || item.orden.estado == EstadoOrden.ACEPTADA) &&
                !fecha.isBefore(hoy) &&
                fecha.isBefore(hoy.plusDays(ClimaRepository.DIAS_PRONOSTICO.toLong()))
    }

    private fun fechaVisita(item: OrdenConDetalle): LocalDate =
        Instant.ofEpochMilli(item.orden.fechaVisita).atZone(ZoneOffset.UTC).toLocalDate()

    suspend fun evaluarOrdenes(
        ordenes: List<OrdenConDetalle>,
        clima: ClimaRepository,
        hoy: LocalDate = LocalDate.now()
    ): ResultadoAlertas {
        val aplicables = ordenes.filter { aplica(it, hoy) }
        val (conCiudad, sinCiudad) = aplicables.partition { it.localCiudad.isNotBlank() }

        val pronosticos = conCiudad.map { it.localCiudad.trim() }.distinct().associateWith { clima.pronostico(it) }

        var sinPronostico = 0
        val alertas = conCiudad.mapNotNull { item ->
            val pronostico = pronosticos[item.localCiudad.trim()]
            if (pronostico == null) {
                sinPronostico++
                return@mapNotNull null
            }
            pronostico[fechaVisita(item)]?.let(::evaluar)?.let { AlertaClima(item, it) }
        }
        return ResultadoAlertas(alertas, sinCiudad.size, sinPronostico)
    }
}
