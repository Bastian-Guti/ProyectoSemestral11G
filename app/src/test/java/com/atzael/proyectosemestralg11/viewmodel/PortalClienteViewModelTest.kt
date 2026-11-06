package com.atzael.proyectosemestralg11.viewmodel

import com.atzael.proyectosemestralg11.data.ClienteDao
import com.atzael.proyectosemestralg11.data.OrdenTrabajoDao
import com.atzael.proyectosemestralg11.data.UsuarioDao
import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.EstadoConformidad
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import com.atzael.proyectosemestralg11.model.OrdenTrabajo
import com.atzael.proyectosemestralg11.model.RolUsuario
import com.atzael.proyectosemestralg11.model.Usuario
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PortalClienteViewModelTest {

    private lateinit var viewModel: PortalClienteViewModel
    private val ordenDao = mockk<OrdenTrabajoDao>(relaxed = true)
    private val usuarioDao = mockk<UsuarioDao>(relaxed = true)
    private val clienteDao = mockk<ClienteDao>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun item(id: Int, estado: String, conformidad: String? = null) = OrdenConDetalle(
        orden = OrdenTrabajo(
            id = id, clienteId = 7, localId = 1, equipoId = 1,
            tipo = "Mantención preventiva", prioridad = "Media", tecnicoRut = "11111111-1",
            fechaVisita = 1_000L, estado = estado, creadaPor = "99999999-9",
            conformidad = conformidad
        ),
        clienteNombre = "Cliente", localNombre = "Local", equipoNombre = "Split",
        equipoUbicacion = "Interior", tecnicoNombre = "Tec", slaHoras = 24
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { ordenDao.getByCliente(any()) } returns flowOf(emptyList())
        viewModel = PortalClienteViewModel(ordenDao, usuarioDao, clienteDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `solo consulta las OT de la empresa del usuario`() = runTest {
        coEvery { usuarioDao.getUsuarioByRut("55555555-5") } returns
                Usuario("55555555-5", "Ana", "Rojas", "clave", RolUsuario.CLIENTE, clienteId = 7)
        coEvery { clienteDao.getById(7) } returns Cliente(7, "Supermercado Sur", "Ana", "+56912345678")
        backgroundScope.launch(testDispatcher) { viewModel.todasLasOrdenes.collect { } }

        viewModel.cargarUsuario("55555555-5")

        verify { ordenDao.getByCliente(7) }
        verify(exactly = 0) { ordenDao.getByCliente(neq(7)) }
        assertEquals("Supermercado Sur", viewModel.nombreEmpresa.value)
    }

    @Test
    fun `un usuario sin empresa no ve ordenes`() = runTest {
        coEvery { usuarioDao.getUsuarioByRut("66666666-6") } returns
                Usuario("66666666-6", "Luis", "Soto", "clave", RolUsuario.CLIENTE, clienteId = null)
        backgroundScope.launch(testDispatcher) { viewModel.todasLasOrdenes.collect { } }

        viewModel.cargarUsuario("66666666-6")

        verify(exactly = 0) { ordenDao.getByCliente(any()) }
        assertEquals(emptyList<OrdenConDetalle>(), viewModel.todasLasOrdenes.value)
    }

    @Test
    fun `confirmar conformidad registra fecha y usuario`() {
        val guardada = slot<OrdenTrabajo>()
        coEvery { ordenDao.update(capture(guardada)) } returns Unit

        viewModel.validarConformidad(item(1, EstadoOrden.FINALIZADA), true, "55555555-5")

        assertEquals(EstadoConformidad.CONFORME, guardada.captured.conformidad)
        assertEquals("55555555-5", guardada.captured.conformidadPor)
        assertNotNull(guardada.captured.fechaConformidad)
    }

    @Test
    fun `rechazar sin observaciones no guarda y muestra error`() {
        viewModel.validarConformidad(item(1, EstadoOrden.FINALIZADA), false, "55555555-5")

        coVerify(exactly = 0) { ordenDao.update(any()) }
        assertNotNull(viewModel.observacionesError.value)
    }

    @Test
    fun `rechazar con observaciones guarda el motivo`() {
        val guardada = slot<OrdenTrabajo>()
        coEvery { ordenDao.update(capture(guardada)) } returns Unit

        viewModel.onObservacionesChange("El equipo sigue goteando")
        viewModel.validarConformidad(item(1, EstadoOrden.FINALIZADA), false, "55555555-5")

        assertEquals(EstadoConformidad.RECHAZADA, guardada.captured.conformidad)
        assertEquals("El equipo sigue goteando", guardada.captured.observacionCliente)
        assertNull(viewModel.observacionesError.value)
    }

    @Test
    fun `no permite validar una OT sin finalizar ni una ya validada`() {
        viewModel.validarConformidad(item(1, EstadoOrden.PENDIENTE), true, "55555555-5")
        viewModel.validarConformidad(
            item(2, EstadoOrden.FINALIZADA, EstadoConformidad.CONFORME), true, "55555555-5"
        )

        coVerify(exactly = 0) { ordenDao.update(any()) }
    }
}
