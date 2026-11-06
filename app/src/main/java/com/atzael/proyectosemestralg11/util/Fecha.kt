package com.atzael.proyectosemestralg11.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val formatoFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

// Las fechas elegidas en el DatePicker vienen a medianoche UTC
fun Long.formatearFecha(): String =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).format(formatoFecha)

// Marcas de tiempo reales (creación, check-in, check-out) en la zona del dispositivo
fun Long.formatearFechaHora(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(formatoFechaHora)

fun formatearDuracion(minutos: Long): String {
    val horas = minutos / 60
    val resto = minutos % 60
    return if (horas > 0) "$horas h $resto min" else "$resto min"
}
