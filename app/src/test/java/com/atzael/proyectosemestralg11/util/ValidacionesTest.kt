package com.atzael.proyectosemestralg11.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesTest {

    // --- normalizarRut ---

    @Test
    fun normalizarRut_quitaPuntosGuionYEspacios() {
        assertEquals("123456785", "12.345.678-5".normalizarRut())
        assertEquals("123456785", " 12 345 678-5 ".normalizarRut())
    }

    @Test
    fun normalizarRut_pasaKAMayuscula() {
        assertEquals("1000005K", "1.000.005-k".normalizarRut())
    }

    @Test
    fun normalizarRut_formatosDistintosDanElMismoResultado() {
        assertEquals("12.345.678-5".normalizarRut(), "12345678-5".normalizarRut())
    }

    // --- esTelefonoValido ---

    @Test
    fun esTelefonoValido_aceptaFormatoInternacional() {
        assertTrue("+56912345678".esTelefonoValido())
    }

    @Test
    fun esTelefonoValido_aceptaNumeroConEspacios() {
        assertTrue("+56 9 1234 5678".esTelefonoValido())
    }

    @Test
    fun esTelefonoValido_ignoraEspaciosAlInicioYFinal() {
        assertTrue("  912345678  ".esTelefonoValido())
    }

    @Test
    fun esTelefonoValido_rechazaNumeroMuyCorto() {
        assertFalse("1234567".esTelefonoValido())
    }

    @Test
    fun esTelefonoValido_rechazaNumeroMuyLargo() {
        assertFalse("1234567890123456".esTelefonoValido())
    }

    @Test
    fun esTelefonoValido_rechazaLetrasYSimbolos() {
        assertFalse("9123abcd5".esTelefonoValido())
        assertFalse("9-1234-5678".esTelefonoValido())
        assertFalse("".esTelefonoValido())
    }
}
