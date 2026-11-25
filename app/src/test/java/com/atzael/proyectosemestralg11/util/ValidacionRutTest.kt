package com.atzael.proyectosemestralg11.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionRutTest {

    // --- validarDV ---

    @Test
    fun validarDV_calculaDigitoNumerico() {
        assertEquals("5", validarDV("12345678"))
        assertEquals("1", validarDV("11111111"))
    }

    @Test
    fun validarDV_devuelveK_cuandoCorresponde() {
        assertEquals("K", validarDV("1000005"))
    }

    @Test
    fun validarDV_devuelveCero_cuandoCorresponde() {
        assertEquals("0", validarDV("10000004"))
    }

    // --- esRutValido ---

    @Test
    fun esRutValido_aceptaRutConPuntosYGuion() {
        assertTrue("12.345.678-5".esRutValido())
    }

    @Test
    fun esRutValido_aceptaRutSinFormato() {
        assertTrue("123456785".esRutValido())
        assertTrue("12345678-5".esRutValido())
    }

    @Test
    fun esRutValido_aceptaDigitoK_enMayusculaYMinuscula() {
        assertTrue("1000005-K".esRutValido())
        assertTrue("1000005-k".esRutValido())
    }

    @Test
    fun esRutValido_rechazaDigitoVerificadorIncorrecto() {
        assertFalse("12.345.678-9".esRutValido())
    }

    @Test
    fun esRutValido_rechazaRutDemasiadoCorto() {
        assertFalse("1234-5".esRutValido())
        assertFalse("".esRutValido())
    }

    @Test
    fun esRutValido_rechazaCuerpoConLetras() {
        assertFalse("12A45678-5".esRutValido())
    }

    // --- formatearRut ---

    @Test
    fun formatearRut_agregaGuionAntesDelDV() {
        assertEquals("12345678-5", "123456785".formatearRut())
    }

    @Test
    fun formatearRut_eliminaPuntosYCaracteresInvalidos() {
        assertEquals("12345678-5", "12.345.678-5".formatearRut())
    }

    @Test
    fun formatearRut_pasaKAMayuscula() {
        assertEquals("1000005-K", "1000005k".formatearRut())
    }

    @Test
    fun formatearRut_textoMuyCorto_seDevuelveSinGuion() {
        assertEquals("1", "1".formatearRut())
        assertEquals("", "".formatearRut())
    }
}
