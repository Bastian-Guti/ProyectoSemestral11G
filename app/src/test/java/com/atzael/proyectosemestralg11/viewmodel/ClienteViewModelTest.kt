package com.atzael.proyectosemestralg11.viewmodel

import com.atzael.proyectosemestralg11.data.ClienteDao
import com.atzael.proyectosemestralg11.model.Cliente
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClienteViewModelTest {

    private lateinit var viewModel: ClienteViewModel
    private val clienteDao = mockk<ClienteDao>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { clienteDao.getAll() } returns flowOf(emptyList())
        viewModel = ClienteViewModel(clienteDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun completarFormularioValido() {
        viewModel.onNombreChange("Supermercado Sur")
        viewModel.onContactoChange("Ana Rojas")
        viewModel.onTelefonoChange("+56912345678")
        viewModel.onSlaChange("8")
    }

    @Test
    fun `boton deshabilitado con formulario vacio`() {
        assertFalse(viewModel.isButtonEnabled.value)
    }

    @Test
    fun `telefono invalido muestra error y deshabilita el boton`() {
        completarFormularioValido()
        viewModel.onTelefonoChange("123")

        assertNotNull(viewModel.telefonoError.value)
        assertFalse(viewModel.isButtonEnabled.value)
    }

    @Test
    fun `registrar cliente con datos validos`() {
        completarFormularioValido()
        assertTrue(viewModel.isButtonEnabled.value)

        viewModel.registrarCliente { }

        coVerify {
            clienteDao.insert(
                Cliente(
                    id = 0,
                    nombre = "Supermercado Sur",
                    contacto = "Ana Rojas",
                    telefono = "+56912345678",
                    slaHoras = 8
                )
            )
        }
    }

    @Test
    fun `actualizar cliente conserva su id`() {
        coEvery { clienteDao.getById(5) } returns Cliente(5, "Antiguo", "Pedro", "+56911111111", 12)

        viewModel.cargarCliente(5)
        viewModel.onNombreChange("Nuevo nombre")
        viewModel.actualizarCliente { }

        coVerify {
            clienteDao.update(Cliente(5, "Nuevo nombre", "Pedro", "+56911111111", 12))
        }
    }

    @Test
    fun `no elimina un cliente que tiene locales`() {
        val cliente = Cliente(3, "Con locales", "Luis", "+56922222222")
        coEvery { clienteDao.contarLocales(3) } returns 2

        viewModel.eliminarCliente(cliente)

        coVerify(exactly = 0) { clienteDao.delete(any()) }
        assertNotNull(viewModel.errorEliminar.value)
    }

    @Test
    fun `elimina un cliente sin locales`() {
        val cliente = Cliente(4, "Sin locales", "Eva", "+56933333333")
        coEvery { clienteDao.contarLocales(4) } returns 0

        viewModel.eliminarCliente(cliente)

        coVerify { clienteDao.delete(cliente) }
        assertEquals(null, viewModel.errorEliminar.value)
    }
}
