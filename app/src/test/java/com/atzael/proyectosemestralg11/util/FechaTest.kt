package com.atzael.proyectosemestralg11.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

class FechaTest {

    private lateinit var zonaOriginal: TimeZone

    @Before
    fun setup() {
        // Fijamos la zona horaria para que formatearFechaHora sea determinista
        zonaOriginal = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(zonaOriginal)
    }

    // --- formatearFecha ---

    @Test
    fun formatearFecha_epochCero() {
        assertEquals("01/01/1970", 0L.formatearFecha())
    }

    @Test
    fun formatearFecha_fechaConocida() {
        // 2026-11-24T00:00:00Z
        assertEquals("24/11/2026", 1795478400000L.formatearFecha())
    }

    // --- formatearFechaHora ---

    @Test
    fun formatearFechaHora_incluyeHoraYMinutos() {
        // 2026-11-24T14:30:00Z
        assertEquals("24/11/2026 14:30", 1795530600000L.formatearFechaHora())
    }

    @Test
    fun formatearFechaHora_usaZonaDelDispositivo() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Santiago"))
        // 2026-11-24T14:30:00Z -> 11:30 en Chile (UTC-3 en horario de verano)
        assertEquals("24/11/2026 11:30", 1795530600000L.formatearFechaHora())
    }

    // --- formatearDuracion ---

    @Test
    fun formatearDuracion_menosDeUnaHora() {
        assertEquals("45 min", formatearDuracion(45))
        assertEquals("0 min", formatearDuracion(0))
    }

    @Test
    fun formatearDuracion_horaExacta() {
        assertEquals("1 h 0 min", formatearDuracion(60))
    }

    @Test
    fun formatearDuracion_horasYMinutos() {
        assertEquals("2 h 5 min", formatearDuracion(125))
    }
}
