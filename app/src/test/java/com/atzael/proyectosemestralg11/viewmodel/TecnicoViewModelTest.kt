package com.atzael.proyectosemestralg11.viewmodel

import com.atzael.proyectosemestralg11.data.ClimaRepository
import com.atzael.proyectosemestralg11.data.EquipoDao
import com.atzael.proyectosemestralg11.data.FotoDao
import com.atzael.proyectosemestralg11.data.OrdenTrabajoDao
import com.atzael.proyectosemestralg11.data.UsuarioDao
import com.atzael.proyectosemestralg11.model.Equipo
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.model.FotoOrden
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import com.atzael.proyectosemestralg11.model.OrdenTrabajo
import com.atzael.proyectosemestralg11.model.PrioridadOrden
import com.atzael.proyectosemestralg11.model.RolUsuario
import com.atzael.proyectosemestralg11.model.TipoFoto
import com.atzael.proyectosemestralg11.model.Usuario
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
    private val clima = mockk<ClimaRepository>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun orden(
        id: Int = 1,
        estado: String,
        prioridad: String = PrioridadOrden.MEDIA,
        fechaVisita: Long = 1_000L,
        checkIn: Long? = null,
        checkOut: Long? = null
    ) = OrdenTrabajo(
        id = id, clienteId = 1, localId = 1, equipoId = 9,
        tipo = "Mantención preventiva", prioridad = prioridad, tecnicoRut = "444444444",
        fechaVisita = fechaVisita, estado = estado, creadaPor = "111111111",
        checkIn = checkIn, checkOut = checkOut
    )

    private fun detalle(o: OrdenTrabajo) = OrdenConDetalle(
        orden = o, clienteNombre = "Cliente", localNombre = "Local", equipoNombre = "Split",
        equipoUbicacion = "Interior", tecnicoNombre = "Juan", slaHoras = 24
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { ordenDao.getByTecnico(any()) } returns flowOf(emptyList())
        every { fotoDao.getByOrden(any()) } returns flowOf(emptyList())
        coEvery { usuarioDao.getUsuarioByRut("444444444") } returns
                Usuario("444444444", "Juan", "Pérez", "juan", RolUsuario.TECNICO, avisoDatosAceptado = true)
        viewModel = TecnicoViewModel(ordenDao, fotoDao, equipoDao, usuarioDao, clima)
        viewModel.cargarUsuario("444444444")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `la jornada se ordena por prioridad y luego por fecha`() = runTest {
        every { ordenDao.getByTecnico("444444444") } returns flowOf(
            listOf(
                detalle(orden(1, EstadoOrden.PENDIENTE, PrioridadOrden.BAJA, 100)),
                detalle(orden(2, EstadoOrden.PENDIENTE, PrioridadOrden.ALTA, 300)),
                detalle(orden(3, EstadoOrden.PENDIENTE, PrioridadOrden.ALTA, 200)),
                detalle(orden(4, EstadoOrden.FINALIZADA, PrioridadOrden.ALTA, 50))
            )
        )
        viewModel = TecnicoViewModel(ordenDao, fotoDao, equipoDao, usuarioDao, clima)
        backgroundScope.launch(testDispatcher) { viewModel.ordenes.collect { } }

        viewModel.cargarUsuario("444444444")

        assertEquals(listOf(3, 2, 1), viewModel.ordenes.value.map { it.orden.id })
        viewModel.onVerCerradasChange(true)
        assertEquals(listOf(4, 3, 2, 1), viewModel.ordenes.value.map { it.orden.id })
    }

    @Test
    fun `aceptar una orden pendiente la pasa a aceptada`() {
        val guardada = slot<OrdenTrabajo>()
        coEvery { ordenDao.update(capture(guardada)) } returns Unit

        viewModel.aceptarOrden(orden(estado = EstadoOrden.PENDIENTE))

        assertEquals(EstadoOrden.ACEPTADA, guardada.captured.estado)
    }

    @Test
    fun `rechazar sin motivo no guarda y rechazar con motivo si`() {
        val guardada = slot<OrdenTrabajo>()
        coEvery { ordenDao.update(capture(guardada)) } returns Unit

        viewModel.rechazarOrden(orden(estado = EstadoOrden.PENDIENTE), "  ")
        coVerify(exactly = 0) { ordenDao.update(any()) }
        assertNotNull(viewModel.error.value)

        viewModel.rechazarOrden(orden(estado = EstadoOrden.PENDIENTE), "Estoy de vacaciones")
        assertEquals(EstadoOrden.RECHAZADA_TECNICO, guardada.captured.estado)
        assertEquals("Estoy de vacaciones", guardada.captured.motivoRechazo)
    }

    @Test
    fun `check-in guarda hora y ubicacion y deja la orden en curso`() {
        val guardada = slot<OrdenTrabajo>()
        coEvery { ordenDao.update(capture(guardada)) } returns Unit

        viewModel.registrarCheckIn(orden(estado = EstadoOrden.ACEPTADA), -33.45 to -70.66)

        assertEquals(EstadoOrden.EN_CURSO, guardada.captured.estado)
        assertNotNull(guardada.captured.checkIn)
        assertEquals(-33.45, guardada.captured.checkInLat!!, 0.0)
    }

    @Test
    fun `check-in sin ubicacion igual registra la hora`() {
        val guardada = slot<OrdenTrabajo>()
        coEvery { ordenDao.update(capture(guardada)) } returns Unit

        viewModel.registrarCheckIn(orden(estado = EstadoOrden.ACEPTADA), null)

        assertNotNull(guardada.captured.checkIn)
        assertNull(guardada.captured.checkInLat)
    }

    @Test
    fun `sin aceptar el aviso de datos no se registra check-in`() {
        coEvery { usuarioDao.getUsuarioByRut("444444444") } returns
                Usuario("444444444", "Juan", "Pérez", "juan", RolUsuario.TECNICO, avisoDatosAceptado = false)
        viewModel.cargarUsuario("444444444")

        viewModel.registrarCheckIn(orden(estado = EstadoOrden.ACEPTADA), 1.0 to 2.0)

        coVerify(exactly = 0) { ordenDao.update(any()) }
        assertNotNull(viewModel.error.value)
    }

    @Test
    fun `no se puede cerrar sin check-out ni sin diagnostico`() {
        viewModel.onDiagnosticoChange("Falla en el compresor")
        viewModel.cerrarOrden(orden(estado = EstadoOrden.EN_CURSO, checkIn = 10))
        coVerify(exactly = 0) { ordenDao.update(any()) }
        assertNotNull(viewModel.error.value)

        viewModel.descartarError()
        viewModel.onDiagnosticoChange("")
        viewModel.cerrarOrden(orden(estado = EstadoOrden.EN_CURSO, checkIn = 10, checkOut = 20))
        coVerify(exactly = 0) { ordenDao.update(any()) }
        assertNotNull(viewModel.error.value)
    }

    @Test
    fun `cerrar finaliza la orden y actualiza la ultima mantencion del equipo`() {
        val guardada = slot<OrdenTrabajo>()
        val equipoGuardado = slot<Equipo>()
        coEvery { ordenDao.update(capture(guardada)) } returns Unit
        coEvery { equipoDao.getById(9) } returns Equipo(id = 9, localId = 1, nombre = "Split")
        coEvery { equipoDao.update(capture(equipoGuardado)) } returns Unit

        viewModel.onDiagnosticoChange("Cambio de filtro")
        viewModel.cerrarOrden(orden(estado = EstadoOrden.EN_CURSO, checkIn = 10, checkOut = 20))

        assertEquals(EstadoOrden.FINALIZADA, guardada.captured.estado)
        assertEquals("Cambio de filtro", guardada.captured.diagnostico)
        assertNotNull(equipoGuardado.captured.ultimaMantencion)
    }

    @Test
    fun `las fotos solo se registran con la orden en curso`() {
        viewModel.registrarFoto(orden(estado = EstadoOrden.ACEPTADA), "/tmp/no-existe.jpg", TipoFoto.ANTES)
        coVerify(exactly = 0) { fotoDao.insert(any()) }

        val foto = slot<FotoOrden>()
        coEvery { fotoDao.insert(capture(foto)) } returns Unit
        viewModel.registrarFoto(orden(estado = EstadoOrden.EN_CURSO), "/tmp/foto.jpg", TipoFoto.DESPUES)

        assertEquals(TipoFoto.DESPUES, foto.captured.tipo)
        assertEquals("444444444", foto.captured.tomadaPor)
        assertTrue(foto.captured.ordenId == 1)
    }
}
