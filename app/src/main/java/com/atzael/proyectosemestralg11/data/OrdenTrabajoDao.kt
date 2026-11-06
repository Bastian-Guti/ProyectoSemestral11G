package com.atzael.proyectosemestralg11.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import com.atzael.proyectosemestralg11.model.OrdenTrabajo
import com.atzael.proyectosemestralg11.model.Reprogramacion
import kotlinx.coroutines.flow.Flow

private const val SELECT_DETALLE = """
    SELECT o.*,
           c.nombre AS clienteNombre,
           l.nombre AS localNombre,
           e.nombre AS equipoNombre,
           e.ubicacion AS equipoUbicacion,
           COALESCE(u.nombre || ' ' || u.apellido, o.tecnicoRut) AS tecnicoNombre,
           c.slaHoras AS slaHoras,
           (SELECT COUNT(*) FROM fotos_orden f WHERE f.ordenId = o.id) AS cantidadFotos
    FROM ordenes_trabajo o
    INNER JOIN clientes c ON c.id = o.clienteId
    INNER JOIN locales l ON l.id = o.localId
    INNER JOIN equipos e ON e.id = o.equipoId
    LEFT JOIN usuarios u ON u.rut = o.tecnicoRut
"""

@Dao
abstract class OrdenTrabajoDao {
    @Insert
    abstract suspend fun insert(orden: OrdenTrabajo)

    @Update
    abstract suspend fun update(orden: OrdenTrabajo)

    @Query("SELECT * FROM ordenes_trabajo WHERE id = :id LIMIT 1")
    abstract suspend fun getById(id: Int): OrdenTrabajo?

    @Query("$SELECT_DETALLE ORDER BY o.id DESC")
    abstract fun getAllConDetalle(): Flow<List<OrdenConDetalle>>

    // Solo las OT de la empresa del usuario cliente
    @Query("$SELECT_DETALLE WHERE o.clienteId = :clienteId ORDER BY o.id DESC")
    abstract fun getByCliente(clienteId: Int): Flow<List<OrdenConDetalle>>

    // Solo las OT asignadas al técnico
    @Query("$SELECT_DETALLE WHERE o.tecnicoRut = :tecnicoRut ORDER BY o.id DESC")
    abstract fun getByTecnico(tecnicoRut: String): Flow<List<OrdenConDetalle>>

    // Historial de intervenciones de un equipo (HU-04)
    @Query("$SELECT_DETALLE WHERE o.equipoId = :equipoId AND o.estado = 'Finalizada' ORDER BY o.id DESC")
    abstract fun getFinalizadasPorEquipo(equipoId: Int): Flow<List<OrdenConDetalle>>

    @Query("$SELECT_DETALLE WHERE o.id = :id")
    abstract fun getConDetalle(id: Int): Flow<OrdenConDetalle?>

    @Insert
    abstract suspend fun insertReprogramacion(reprogramacion: Reprogramacion)

    @Query("SELECT * FROM reprogramaciones WHERE ordenId = :ordenId ORDER BY id DESC")
    abstract fun getReprogramaciones(ordenId: Int): Flow<List<Reprogramacion>>

    // Actualiza la OT y deja el registro del cambio en una sola transacción
    @Transaction
    open suspend fun reprogramar(orden: OrdenTrabajo, reprogramacion: Reprogramacion) {
        update(orden)
        insertReprogramacion(reprogramacion)
    }
}
