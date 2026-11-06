package com.atzael.proyectosemestralg11.viewmodel

import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import com.atzael.proyectosemestralg11.model.OrdenTrabajo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReporteViewModelTest {

    private val minuto = 60_000L
    private val dia = 24L * 60 * minuto
    private val ahora = 100 * dia

    private fun orden(
        id: Int,
        clienteId: Int = 1,
        estado: String = EstadoOrden.FINALIZADA,
        fechaVisita: Long = ahora - dia,
        minutosAtencion: Long? = null,
        slaHoras: Int = 2
    ) = OrdenConDetalle(
        orden = OrdenTrabajo(
            id = id, clienteId = clienteId, localId = 1, equipoId = 1,
            tipo = "Mantención preventiva", prioridad = "Media", tecnicoRut = "11111111-1",
            fechaVisita = fechaVisita, estado = estado, creadaPor = "99999999-9",
            checkIn = minutosAtencion?.let { 1_000L },
            checkOut = minutosAtencion?.let { 1_000L + it * minuto }
        ),
        clienteNombre = "Cliente $clienteId", localNombre = "Local", equipoNombre = "Split",
        equipoUbicacion = "Interior", tecnicoNombre = "Tec", slaHoras = slaHoras
    )

    @Test
    fun `calcula tiempo promedio y porcentaje de SLA solo con OT atendidas`() {
        val ordenes = listOf(
            orden(1, minutosAtencion = 60),   // dentro (SLA 2 h)
            orden(2, minutosAtencion = 180),  // fuera
            orden(3, minutosAtencion = null)  // sin atender: no cuenta
        )

        val resumen = ReporteViewModel.calcularResumen(ordenes, null, null, ReporteViewModel.PERIODOS[0], ahora)

        assertEquals(3, resumen.filas.size)
        assertEquals(2, resumen.atendidas)
        assertEquals(120L, resumen.promedioMin)
        assertEquals(1, resumen.dentroSla)
        assertEquals(50, resumen.porcentajeSla)
    }

    @Test
    fun `sin OT atendidas no hay porcentaje ni promedio`() {
        val resumen = ReporteViewModel.calcularResumen(
            listOf(orden(1)), null, null, ReporteViewModel.PERIODOS[0], ahora
        )

        assertNull(resumen.porcentajeSla)
        assertNull(resumen.promedioMin)
    }

    @Test
    fun `filtra por cliente y excluye las canceladas`() {
        val ordenes = listOf(
            orden(1, clienteId = 1),
            orden(2, clienteId = 2),
            orden(3, clienteId = 1, estado = EstadoOrden.CANCELADA)
        )

        val resumen = ReporteViewModel.calcularResumen(ordenes, 1, null, ReporteViewModel.PERIODOS[0], ahora)

        assertEquals(listOf(1), resumen.filas.map { it.orden.orden.id })
    }

    @Test
    fun `filtra por periodo`() {
        val ordenes = listOf(
            orden(1, fechaVisita = ahora - 3 * dia),
            orden(2, fechaVisita = ahora - 20 * dia),
            orden(3, fechaVisita = ahora - 200 * dia)
        )

        val ultimos7 = ReporteViewModel.calcularResumen(ordenes, null, null, ReporteViewModel.PERIODOS[1], ahora)
        val ultimos30 = ReporteViewModel.calcularResumen(ordenes, null, null, ReporteViewModel.PERIODOS[2], ahora)
        val todo = ReporteViewModel.calcularResumen(ordenes, null, null, ReporteViewModel.PERIODOS[0], ahora)

        assertEquals(listOf(1), ultimos7.filas.map { it.orden.orden.id })
        assertEquals(listOf(1, 2), ultimos30.filas.map { it.orden.orden.id })
        assertEquals(3, todo.filas.size)
    }
}
