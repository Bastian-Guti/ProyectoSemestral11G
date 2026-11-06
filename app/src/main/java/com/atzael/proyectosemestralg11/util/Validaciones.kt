package com.atzael.proyectosemestralg11.util

private val regexTelefono = Regex("^\\+?[0-9 ]{8,15}$")

// "12.345.678-5" y "12345678-5" son el mismo RUT: se guarda y se busca como "123456785"
fun String.normalizarRut(): String = trim().replace(".", "").replace("-", "").replace(" ", "").uppercase()

fun String.esTelefonoValido(): Boolean = regexTelefono.matches(this.trim())
