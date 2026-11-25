package com.atzael.proyectosemestralg11.util

fun String.esRutValido(): Boolean {
    val rutLimpio = this.replace(".", "").replace("-", "").uppercase()
    if (rutLimpio.length < 8) return false

    val cuerpo = rutLimpio.substring(0, rutLimpio.length - 1)
    val dv = rutLimpio.substring(rutLimpio.length - 1)

    if (!cuerpo.all { it.isDigit() }) return false

    return validarDV(cuerpo) == dv
}

fun validarDV(cuerpo: String): String {
    var suma = 0
    var multiplo = 2

    for (i in cuerpo.length - 1 downTo 0) {
        suma += multiplo * (cuerpo[i] - '0')
        if (multiplo < 7) multiplo++ else multiplo = 2
    }

    val dvEsperado = 11 - (suma % 11)
    return when (dvEsperado) {
        11 -> "0"
        10 -> "K"
        else -> dvEsperado.toString()
    }
}

fun String.formatearRut(): String {
    val limpio = this.replace(Regex("[^0-9kK]"), "").uppercase()
    if (limpio.length < 2) return limpio

    val cuerpo = limpio.substring(0, limpio.length - 1)
    val dv = limpio.substring(limpio.length - 1)
    return "$cuerpo-$dv"
}
