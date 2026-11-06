package com.atzael.proyectosemestralg11.viewmodel

import com.atzael.proyectosemestralg11.data.ClienteDao
import com.atzael.proyectosemestralg11.data.EquipoDao
import com.atzael.proyectosemestralg11.data.FotoDao
import com.atzael.proyectosemestralg11.data.LocalDao
import com.atzael.proyectosemestralg11.data.OrdenTrabajoDao
import com.atzael.proyectosemestralg11.data.UsuarioDao
import com.atzael.proyectosemestralg11.model.EstadoOrden
import com.atzael.proyectosemestralg11.model.MotivoReagendar
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import com.atzael.proyectosemestralg11.model.OrdenTrabajo
import com.atzael.proyectosemestralg11.model.PrioridadOrden
import com.atzael.proyectosemestralg11.model.Reprogramacion
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

    private fun orden(id: Int, estado: String, prioridad: String, tecnico: String = "11111111-1") =
        OrdenConDetalle(
            orden = OrdenTrabajo(
                id = id, clienteId = 1, localId = 1, equipoId = 1,
                tipo = "Mantención preventiva", prioridad = prioridad,
                tecnicoRut = tecnico, fechaVisita = 1_000L, estado = estado, creadaPor = "99999999-9"
            ),
            clienteNombre = "Cliente", localNombre = "Local", equipoNombre = "Split",
            equipoUbicacion = "Interior", tecnicoNombre = "Tec", slaHoras = 24
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { ordenDao.getAllConDetalle() } returns flowOf(emptyList())
        every { clienteDao.getAll() } returns flowOf(emptyList())
        every { localDao.getAll() } returns flowOf(emptyList())
        every { equipoDao.getAll() } returns flowOf(emptyList())
        every { usuarioDao.getTecnicos() } returns flowOf(emptyList())
        viewModel = crearViewModel()
    }

    private fun crearViewModel() = OrdenViewModel(ordenDao, clienteDao, localDao, equipoDao, usuarioDao, fotoDao)

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun completarFormulario() {
        viewModel.onClienteChange(1)
        viewModel.onLocalChange(2)
        viewModel.onEquipoChange(3)
        viewModel.onTecnicoChange("11111111-1")
        viewModel.onFechaVisitaChange(86_400_000L)
    }

    @Test
    fun `boton deshabilitado hasta completar cliente local equipo tecnico y fecha`() {
        assertFalse(viewModel.isButtonEnabled.value)
        viewModel.onClienteChange(1)
        viewModel.onLocalChange(2)
        assertFalse(viewModel.isButtonEnabled.value)
        viewModel.onEquipoChange(3)
        viewModel.onTecnicoChange("11111111-1")
        assertFalse(viewModel.isButtonEnabled.value)
        viewModel.onFechaVisitaChange(86_400_000L)
        assertTrue(viewModel.isButtonEnabled.value)
    }

    @Test
    fun `cambiar de cliente limpia local y equipo`() {
        completarFormulario()

        viewModel.onClienteChange(9)

        assertEquals(null, viewModel.localId.value)
        assertEquals(null, viewModel.equipoId.value)
        assertFalse(viewModel.isButtonEnabled.value)
    }

    @Test
    fun `registrar orden guarda estado pendiente y quien la creo`() {
        completarFormulario()
        viewModel.onPrioridadChange(PrioridadOrden.ALTA)
        val guardada = slot<OrdenTrabajo>()
        coEvery { ordenDao.insert(capture(guardada)) } returns Unit

        viewModel.registrarOrden("88888888-8") { }

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
        // Mantiene suscrito el StateFlow (WhileSubscribed) mientras se consulta
        backgroundScope.launch(testDispatcher) { viewModel.ordenesFiltradas.collect { } }

        viewModel.onFiltroEstadoChange(EstadoOrden.PENDIENTE)
        viewModel.onFiltroPrioridadChange(PrioridadOrden.ALTA)

        assertEquals(listOf(1), viewModel.ordenesFiltradas.value.map { it.orden.id })
    }

    @Test
    fun `cancelar una orden cambia su estado y no la borra`() {
        val original = orden(7, EstadoOrden.PENDIENTE, PrioridadOrden.MEDIA).orden

        viewModel.cancelarOrden(original)

        coVerify { ordenDao.update(original.copy(estado = EstadoOrden.CANCELADA)) }
    }

    @Test
    fun `reagendar deja el historial con fecha tecnico y motivo`() {
        val original = orden(8, EstadoOrden.PENDIENTE, PrioridadOrden.MEDIA).orden
        val actualizada = slot<OrdenTrabajo>()
        val historial = slot<Reprogramacion>()
        coEvery { ordenDao.reprogramar(capture(actualizada), capture(historial)) } returns Unit

        viewModel.reagendarOrden(original, 5_000L, "22222222-2", MotivoReagendar.CLIMA)

        assertEquals(5_000L, actualizada.captured.fechaVisita)
        assertEquals("22222222-2", actualizada.captured.tecnicoRut)
        assertEquals(1_000L, historial.captured.fechaAnterior)
        assertEquals("11111111-1", historial.captured.tecnicoAnterior)
        assertEquals(MotivoReagendar.CLIMA, historial.captured.motivo)
        assertEquals(8, historial.captured.ordenId)
    }
}
