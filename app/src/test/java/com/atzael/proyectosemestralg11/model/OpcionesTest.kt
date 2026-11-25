package com.atzael.proyectosemestralg11.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OpcionesTest {

    @Test
    fun estadoOrden_todosContieneLosSeisEstadosSinRepetir() {
        assertEquals(6, EstadoOrden.todos.size)
        assertEquals(EstadoOrden.todos.size, EstadoOrden.todos.toSet().size)
    }

    @Test
    fun estadoOrden_gestionablesSonSubconjuntoDeTodos() {
        assertTrue(EstadoOrden.todos.containsAll(EstadoOrden.gestionablesPorJefatura))
    }

    @Test
    fun estadoOrden_jefaturaNoGestionaOrdenesEnCursoNiCerradas() {
        val gestionables = EstadoOrden.gestionablesPorJefatura
        assertFalse(gestionables.contains(EstadoOrden.EN_CURSO))
        assertFalse(gestionables.contains(EstadoOrden.FINALIZADA))
        assertFalse(gestionables.contains(EstadoOrden.CANCELADA))
    }

    @Test
    fun listasDeOpciones_tienenElTamanoEsperado() {
        assertEquals(2, TipoFoto.todos.size)
        assertEquals(2, TipoOrden.todos.size)
        assertEquals(3, PrioridadOrden.todos.size)
        assertEquals(2, UbicacionEquipo.todos.size)
        assertEquals(4, MotivoReagendar.todos.size)
    }
}
