package com.atzael.proyectosemestralg11.viewmodel

import com.atzael.proyectosemestralg11.data.EquipoDao
import com.atzael.proyectosemestralg11.data.FotoDao
import com.atzael.proyectosemestralg11.data.OrdenTrabajoDao
import com.atzael.proyectosemestralg11.data.UsuarioDao
import com.atzael.proyectosemestralg11.model.Equipo
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TecnicoViewModelTest {

    private lateinit var viewModel: TecnicoViewModel
    private val ordenDao = mockk<OrdenTrabajoDao>(relaxed = true)
    private val fotoDao = mockk<FotoDao>(relaxed = true)
    private val equipoDao = mockk<EquipoDao>(relaxed = true)
    private val usuarioDao = mockk<UsuarioDao>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun orden(id: Int = 1, estado: String = EstadoOrden.PENDIENTE, checkIn: Long? = null, checkOut: Long? = null) = OrdenConDetalle(
        orden = OrdenTrabajo(
            id = id, clienteId = 1, localId = 1, equipoId = 9,
            tipo = "Mantención preventiva", prioridad = "Alta", tecnicoRut = "444444444",
            fechaVisita = 1_000L, estado = estado, creadaPor = "999",
            checkIn = checkIn, checkOut = checkOut
        ),
        clienteNombre = "Cliente", localNombre = "Local", equipoNombre = "Equipo",
        equipoUbicacion = "Interior", tecnicoNombre = "Tecnico", slaHoras = 24
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { ordenDao.getByTecnico(any()) } returns flowOf(emptyList())
        coEvery { usuarioDao.getUsuarioByRut("444444444") } returns
                Usuario("444444444", "Juan", "Pérez", "juan", RolUsuario.TECNICO, avisoDatosAceptado = true)
        viewModel = TecnicoViewModel(ordenDao, fotoDao, equipoDao, usuarioDao)
        viewModel.cargarUsuario("444444444")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `solo consulta las OT del tecnico autenticado`() = runTest {
        backgroundScope.launch(testDispatcher) { viewModel.todasLasOrdenes.collect { } }

        verify { ordenDao.getByTecnico("444444444") }
    }

    @Test
    fun `las ordenes abiertas van primero ordenadas por prioridad`() = runTest {
        every { ordenDao.getByTecnico("444444444") } returns flowOf(
            listOf(
                orden(id = 10, estado = EstadoOrden.FINALIZADA),
                orden(id = 11, estado = EstadoOrden.EN_CURSO),
                orden(id = 12, estado = EstadoOrden.ACEPTADA)
            )
        )
        viewModel = TecnicoViewModel(ordenDao, fotoDao, equipoDao, usuarioDao)
        backgroundScope.launch(testDispatcher) { viewModel.ordenes.collect { } }
        viewModel.cargarUsuario("444444444")

        val ids = viewModel.ordenes.value.map { it.orden.id }
        assertTrue(ids.containsAll(listOf(11, 12)))
        assertFalse(ids.contains(10))
    }

    @Test
    fun `al marcar ver cerradas aparecen las finalizadas`() = runTest {
        every { ordenDao.getByTecnico("444444444") } returns flowOf(
            listOf(orden(id = 10, estado = EstadoOrden.FINALIZADA))
        )
        viewModel = TecnicoViewModel(ordenDao, fotoDao, equipoDao, usuarioDao)
        backgroundScope.launch(testDispatcher) { viewModel.ordenes.collect { } }
        viewModel.cargarUsuario("444444444")

        viewModel.onVerCerradasChange(true)

        assertEquals(listOf(10), viewModel.ordenes.value.map { it.orden.id })
    }

    @Test
    fun `aceptar una orden pendiente la pasa a aceptada`() = runTest {
        val guardada = slot<OrdenTrabajo>()
        coEvery { ordenDao.update(capture(guardada)) } returns Unit

        viewModel.aceptarOrden(orden(estado = EstadoOrden.PENDIENTE).orden)

        assertEquals(EstadoOrden.ACEPTADA, guardada.captured.estado)
    }

    @Test
    fun `rechazar sin motivo no guarda y rechazar con motivo si`() = runTest {
        val guardada = slot<OrdenTrabajo>()
        coEvery { ordenDao.update(capture(guardada)) } returns Unit

        viewModel.rechazarOrden(orden(estado = EstadoOrden.PENDIENTE).orden, "  ")
        coVerify(exactly = 0) { ordenDao.update(any()) }
        assertNotNull(viewModel.error.value)

        viewModel.rechazarOrden(orden(estado = EstadoOrden.PENDIENTE).orden, "Estoy de vacaciones")
        assertEquals(EstadoOrden.RECHAZADA_TECNICO, guardada.captured.estado)
        assertEquals("Estoy de vacaciones", guardada.captured.motivoRechazo)
    }

    @Test
    fun `check-in guarda hora y ubicacion y deja la orden en curso`() = runTest {
        val guardada = slot<OrdenTrabajo>()
        coEvery { ordenDao.update(capture(guardada)) } returns Unit

        viewModel.registrarCheckIn(orden(estado = EstadoOrden.ACEPTADA).orden, -33.45 to -70.66)

        assertEquals(EstadoOrden.EN_CURSO, guardada.captured.estado)
        assertNotNull(guardada.captured.checkIn)
        assertEquals(-33.45, guardada.captured.checkInLat!!, 0.0)
    }

    @Test
    fun `check-in sin ubicacion igual registra la hora`() = runTest {
        val guardada = slot<OrdenTrabajo>()
        coEvery { ordenDao.update(capture(guardada)) } returns Unit

        viewModel.registrarCheckIn(orden(estado = EstadoOrden.ACEPTADA).orden, null)

        assertNotNull(guardada.captured.checkIn)
        assertNull(guardada.captured.checkInLat)
    }

    @Test
    fun `sin aceptar el aviso de datos no se registra check-in`() = runTest {
        coEvery { usuarioDao.getUsuarioByRut("444444444") } returns
                Usuario("444444444", "Juan", "Pérez", "juan", RolUsuario.TECNICO, avisoDatosAceptado = false)
        viewModel.cargarUsuario("444444444")

        viewModel.registrarCheckIn(orden(estado = EstadoOrden.ACEPTADA).orden, 1.0 to 2.0)

        coVerify(exactly = 0) { ordenDao.update(any()) }
        assertNotNull(viewModel.error.value)
    }

    @Test
    fun `no se puede cerrar sin check-out ni sin diagnostico`() = runTest {
        viewModel.onDiagnosticoChange("Falla en el compresor")
        viewModel.cerrarOrden(orden(estado = EstadoOrden.EN_CURSO, checkIn = 10).orden)
        coVerify(exactly = 0) { ordenDao.update(any()) }
        assertNotNull(viewModel.error.value)

        viewModel.descartarError()
        viewModel.onDiagnosticoChange("")
        viewModel.cerrarOrden(orden(estado = EstadoOrden.EN_CURSO, checkIn = 10, checkOut = 20).orden)
        coVerify(exactly = 0) { ordenDao.update(any()) }
        assertNotNull(viewModel.error.value)
    }

    @Test
    fun `cerrar finaliza la orden y actualiza la ultima mantencion del equipo`() = runTest {
        val guardada = slot<OrdenTrabajo>()
        val equipoGuardado = slot<Equipo>()
        coEvery { ordenDao.update(capture(guardada)) } returns Unit
        coEvery { equipoDao.getById(9) } returns Equipo(id = 9, localId = 1, nombre = "Split")
        coEvery { equipoDao.update(capture(equipoGuardado)) } returns Unit

        viewModel.onDiagnosticoChange("Cambio de filtro")
        viewModel.cerrarOrden(orden(estado = EstadoOrden.EN_CURSO, checkIn = 10, checkOut = 20).orden)

        assertEquals(EstadoOrden.FINALIZADA, guardada.captured.estado)
        assertEquals("Cambio de filtro", guardada.captured.diagnostico)
        assertNotNull(equipoGuardado.captured.ultimaMantencion)
    }
}
