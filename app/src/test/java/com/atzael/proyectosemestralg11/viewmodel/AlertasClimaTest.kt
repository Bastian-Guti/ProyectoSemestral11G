package com.atzael.proyectosemestralg11.viewmodel

import com.atzael.proyectosemestralg11.data.ClimaRepository
import com.atzael.proyectosemestralg11.data.PronosticoDia
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import com.atzael.proyectosemestralg11.model.OrdenTrabajo
import com.atzael.proyectosemestralg11.model.UbicacionEquipo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class AlertasClimaTest {

    private val hoy = LocalDate.of(2026, 11, 6)

    private fun millis(fecha: LocalDate) = fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun item(
        id: Int,
        fecha: LocalDate,
        ubicacion: String = UbicacionEquipo.EXTERIOR,
        estado: String = EstadoOrden.PENDIENTE,
        ciudad: String = "Santiago"
    ) = OrdenConDetalle(
        orden = OrdenTrabajo(
            id = id, clienteId = 1, localId = 1, equipoId = 1,
            tipo = "Mantención preventiva", prioridad = "Media", tecnicoRut = "444444444",
            fechaVisita = millis(fecha), estado = estado, creadaPor = "111111111"
        ),
        clienteNombre = "Cliente", localNombre = "Local", equipoNombre = "Condensador",
        equipoUbicacion = ubicacion, tecnicoNombre = "Juan", slaHoras = 24, localCiudad = ciudad
    )

    private fun clima(vararg dias: Pair<LocalDate, PronosticoDia>?) = object : ClimaRepository {
        override suspend fun pronostico(ciudad: String) = if (dias.any { it == null }) null else dias.filterNotNull().toMap()
    }

    private val seco = PronosticoDia(10, 0.0, 12.0, 20.0)
    private val lluvia = PronosticoDia(80, 5.2, 12.0, 20.0)
    private val ventoso = PronosticoDia(5, 0.0, 45.0, 70.0)

    @Test
    fun `un dia seco y tranquilo no genera alerta`() {
        assertNull(AlertasClima.evaluar(seco))
    }

    @Test
    fun `detecta lluvia, viento o ambos`() {
        assertNotNull(AlertasClima.evaluar(lluvia))
        assertNotNull(AlertasClima.evaluar(ventoso))
        val ambos = AlertasClima.evaluar(PronosticoDia(90, 6.0, 50.0, 80.0))!!
        assertEquals(true, ambos.contains("lluvia") && ambos.contains("viento"))
    }

    @Test
    fun `solo aplica a equipos exteriores abiertos y dentro del alcance del pronostico`() {
        assertEquals(true, AlertasClima.aplica(item(1, hoy.plusDays(2)), hoy))
        assertEquals(false, AlertasClima.aplica(item(2, hoy.plusDays(2), ubicacion = UbicacionEquipo.INTERIOR), hoy))
        assertEquals(false, AlertasClima.aplica(item(3, hoy.plusDays(2), estado = EstadoOrden.FINALIZADA), hoy))
        assertEquals(false, AlertasClima.aplica(item(4, hoy.minusDays(1)), hoy))
        assertEquals(false, AlertasClima.aplica(item(5, hoy.plusDays(30)), hoy))
    }

    @Test
    fun `genera alertas solo para los dias con mal clima`() = runTest {
        val manana = hoy.plusDays(1)
        val pasado = hoy.plusDays(2)
        val resultado = AlertasClima.evaluarOrdenes(
            ordenes = listOf(item(1, manana), item(2, pasado)),
            clima = clima(manana to lluvia, pasado to seco),
            hoy = hoy
        )

        assertEquals(listOf(1), resultado.alertas.map { it.orden.orden.id })
        assertEquals(0, resultado.sinPronostico)
    }

    @Test
    fun `informa cuando no hay ciudad o no se pudo consultar`() = runTest {
        val manana = hoy.plusDays(1)

        val sinCiudad = AlertasClima.evaluarOrdenes(listOf(item(1, manana, ciudad = "")), clima(manana to lluvia), hoy)
        assertEquals(1, sinCiudad.sinCiudad)
        assertEquals(emptyList<AlertaClima>(), sinCiudad.alertas)

        val sinInternet = AlertasClima.evaluarOrdenes(listOf(item(2, manana)), clima(null), hoy)
        assertEquals(1, sinInternet.sinPronostico)
        assertEquals(emptyList<AlertaClima>(), sinInternet.alertas)
    }
}
