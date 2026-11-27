package com.atzael.proyectosemestralg11.util

import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.Local
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DuplicadosTest {

    private fun cliente(id: Int, nombre: String) = Cliente(id = id, nombre = nombre, contacto = "C", telefono = "+56900000000")
    private fun local(id: Int, clienteId: Int, nombre: String, direccion: String) =
        Local(id = id, clienteId = clienteId, nombre = nombre, direccion = direccion, telefono = "+56900000000")

    @Test
    fun `compara sin mayusculas tildes ni espacios extra`() {
        assertEquals("cafe central", "  Café   CENTRAL ".claveComparacion())
    }

    @Test
    fun `detecta cliente con el mismo nombre`() {
        assertTrue(listOf(cliente(1, "Empresa Demo")).hayOtroClienteConNombre("empresa  demo"))
    }

    @Test
    fun `al editar un cliente no choca consigo mismo`() {
        assertFalse(listOf(cliente(1, "Empresa Demo")).hayOtroClienteConNombre("Empresa Demo", exceptoId = 1))
    }

    @Test
    fun `local con el mismo nombre en el mismo cliente es duplicado`() {
        val existentes = listOf(local(1, 1, "Sucursal Centro", "Av. Uno 100"))
        assertEquals(CampoRepetido.NOMBRE, existentes.campoRepetidoDeLocal(local(0, 1, "sucursal centro", "Otra 200")))
    }

    @Test
    fun `local con la misma direccion en el mismo cliente es duplicado`() {
        val existentes = listOf(local(1, 1, "Sucursal Centro", "Av. Uno 100"))
        assertEquals(CampoRepetido.DIRECCION, existentes.campoRepetidoDeLocal(local(0, 1, "Sucursal Norte", "av. uno 100")))
    }

    @Test
    fun `otro cliente puede tener un local con el mismo nombre`() {
        val existentes = listOf(local(1, 1, "Sucursal Centro", "Av. Uno 100"))
        assertNull(existentes.campoRepetidoDeLocal(local(0, 2, "Sucursal Centro", "Av. Uno 100")))
    }
}
