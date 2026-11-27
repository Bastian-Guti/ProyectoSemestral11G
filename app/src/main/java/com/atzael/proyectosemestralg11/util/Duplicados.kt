package com.atzael.proyectosemestralg11.util

import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.Local
import java.text.Normalizer

// Reglas de duplicados: solo se bloquea lo que en la vida real no puede existir dos veces.
//  - Usuario: mismo RUT (es la clave primaria).
//  - Cliente: misma empresa = mismo nombre.
//  - Local: dentro del mismo cliente, mismo nombre o misma dirección.
//  - Equipos, órdenes, fotos y reprogramaciones SÍ pueden repetirse (dos equipos iguales en un local,
//    varias visitas al mismo equipo, etc.), por eso no se validan.

private val tildes = Regex("\\p{Mn}+")
private val espacios = Regex("\\s+")

// "  Café   Central " y "cafe central" se consideran el mismo texto
fun String.claveComparacion(): String =
    Normalizer.normalize(trim(), Normalizer.Form.NFD)
        .replace(tildes, "")
        .replace(espacios, " ")
        .lowercase()

// true si otro cliente (distinto id) ya usa ese nombre
fun List<Cliente>.hayOtroClienteConNombre(nombre: String, exceptoId: Int = 0): Boolean {
    val clave = nombre.claveComparacion()
    return any { it.id != exceptoId && it.nombre.claveComparacion() == clave }
}

enum class CampoRepetido { NOMBRE, DIRECCION }

// Devuelve el campo repetido si otro local del mismo cliente ya tiene ese nombre o esa dirección
fun List<Local>.campoRepetidoDeLocal(local: Local): CampoRepetido? {
    val mismos = filter { it.id != local.id && it.clienteId == local.clienteId }
    val nombre = local.nombre.claveComparacion()
    val direccion = local.direccion.claveComparacion()
    return when {
        mismos.any { it.nombre.claveComparacion() == nombre } -> CampoRepetido.NOMBRE
        mismos.any { it.direccion.claveComparacion() == direccion } -> CampoRepetido.DIRECCION
        else -> null
    }
}
