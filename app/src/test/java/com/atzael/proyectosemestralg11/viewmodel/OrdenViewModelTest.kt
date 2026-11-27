package com.atzael.proyectosemestralg11.viewmodel

import com.atzael.proyectosemestralg11.data.ClienteDao
import com.atzael.proyectosemestralg11.data.EquipoDao
import com.atzael.proyectosemestralg11.data.FotoDao
import com.atzael.proyectosemestralg11.data.LocalDao
import com.atzael.proyectosemestralg11.data.OrdenTrabajoDao
import com.atzael.proyectosemestralg11.data.UsuarioDao
import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.Equipo
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.model.Local
import com.atzael.proyectosemestralg11.model.MotivoReagendar
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import com.atzael.proyectosemestralg11.model.OrdenTrabajo
import com.atzael.proyectosemestralg11.model.PrioridadOrden
import com.atzael.proyectosemestralg11.model.Reprogramacion
import com.atzael.proyectosemestralg11.model.TipoOrden
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrdenViewModelTest {

    private lateinit var viewModel: OrdenViewModel
    private val ordenDao = mockk<OrdenTrabajoDao>(relaxed = true)
    private val clienteDao = mockk<ClienteDao>(relaxed = true)
    private val localDao = mockk<LocalDao>(relaxed = true)
    private val equipoDao = mockk<EquipoDao>(relaxed = true)
    private val usuarioDao = mockk<UsuarioDao>(relaxed = true)
    private val fotoDao = mockk<FotoDao>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun orden(id: Int, estado: String, prioridad: String) = OrdenConDetalle(
        orden = OrdenTrabajo(
            id = id, clienteId = 1, localId = 2, equipoId = 3,
            tipo = TipoOrden.PREVENTIVO, prioridad = prioridad, tecnicoRut = "11111111-1",
            fechaVisita = 1_000L, estado = estado, creadaPor = "99999999-9"
        ),
        clienteNombre = "Cliente", localNombre = "Local", equipoNombre = "Equipo",
        equipoUbicacion = "Interior", tecnicoNombre = "Tecnico", slaHoras = 24
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { ordenDao.getAllConDetalle() } returns flowOf(emptyList())
        every { clienteDao.getAll() } returns flowOf(listOf(Cliente(1, "Cliente Demo", "Contacto", "+5691111")))
        every { localDao.getAll() } returns flowOf(listOf(Local(2, 1, "Local 1", "Direccion", "+5692222")))
        every { equipoDao.getAll() } returns flowOf(listOf(Equipo(3, 2, "Equipo 1")))
        every { usuarioDao.getTecnicos() } returns flowOf(listOf(Usuario("11111111-1", "Tec", "Demo", "p", "Tecnico")))
        viewModel = crearViewModel()
    }

    private fun crearViewModel() = OrdenViewModel(
        ordenDao, clienteDao, localDao, equipoDao, usuarioDao, fotoDao
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun completarFormulario() {
        viewModel.onClienteChange(1)
        viewModel.onLocalChange(2)
        viewModel.onEquipoChange(3)
        viewModel.onTecnicoChange("11111111-1")
        viewModel.onFechaVisitaChange(2_000L)
    }

    @Test
    fun `boton deshabilitado sin campos obligatorios`() {
        assertFalse(viewModel.isButtonEnabled.value)
    }

    @Test
    fun `limpia local y equipo al cambiar de cliente`() {
        completarFormulario()
        assertTrue(viewModel.isButtonEnabled.value)

        viewModel.onClienteChange(9)

        assertEquals(null, viewModel.localId.value)
        assertEquals(null, viewModel.equipoId.value)
        assertFalse(viewModel.isButtonEnabled.value)
    }

    @Test
    fun `registrar orden guarda estado pendiente y quien la creo`() = runTest {
        completarFormulario()
        viewModel.onPrioridadChange(PrioridadOrden.ALTA)
        val guardada = slot<OrdenTrabajo>()
        coEvery { ordenDao.insert(capture(guardada)) } returns 1L

        viewModel.registrarOrden("88888888-8")

        coVerify { ordenDao.insert(any()) }
        assertEquals(EstadoOrden.PENDIENTE, guardada.captured.estado)
        assertEquals("88888888-8", guardada.captured.creadaPor)
        assertEquals(PrioridadOrden.ALTA, guardada.captured.prioridad)
        assertEquals(2, guardada.captured.localId)
    }

    @Test
    fun `filtra las ordenes por estado y prioridad`() = runTest {
        every { ordenDao.getAllConDetalle() } returns flowOf(
            listOf(
                orden(1, EstadoOrden.PENDIENTE, PrioridadOrden.ALTA),
                orden(2, EstadoOrden.PENDIENTE, PrioridadOrden.BAJA),
                orden(3, EstadoOrden.FINALIZADA, PrioridadOrden.ALTA)
            )
        )
        viewModel = crearViewModel()
        backgroundScope.launch(testDispatcher) { viewModel.ordenesFiltradas.collect { } }

        viewModel.onFiltroEstadoChange(EstadoOrden.PENDIENTE)
        viewModel.onFiltroPrioridadChange(PrioridadOrden.ALTA)

        assertEquals(listOf(1), viewModel.ordenesFiltradas.value.map { it.orden.id })
    }

    @Test
    fun `cancelar una orden cambia su estado y no la borra`() = runTest {
        val original = orden(7, EstadoOrden.PENDIENTE, PrioridadOrden.MEDIA).orden

        viewModel.cancelarOrden(original)

        coVerify { ordenDao.update(original.copy(estado = EstadoOrden.CANCELADA)) }
    }

    @Test
    fun `reagendar deja el historial con fecha tecnico y motivo`() = runTest {
        val original = orden(8, EstadoOrden.PENDIENTE, PrioridadOrden.MEDIA).orden
        val actualizada = slot<OrdenTrabajo>()
        val historial = slot<Reprogramacion>()
        coEvery { ordenDao.reprogramar(capture(actualizada), capture(historial)) } returns 1L

        viewModel.reagendarOrden(original, 5_000L, "22222222-2", MotivoReagendar.CLIMA)

        assertEquals(5_000L, actualizada.captured.fechaVisita)
        assertEquals("22222222-2", actualizada.captured.tecnicoRut)
        assertEquals(1_000L, historial.captured.fechaAnterior)
        assertEquals("11111111-1", historial.captured.tecnicoAnterior)
        assertEquals(MotivoReagendar.CLIMA, historial.captured.motivo)
        assertEquals(8, historial.captured.ordenId)
    }
}
