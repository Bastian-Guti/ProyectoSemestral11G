package com.atzael.proyectosemestralg11.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.atzael.proyectosemestralg11.model.FotoOrden
import kotlinx.coroutines.flow.Flow

@Dao
interface FotoDao {
    @Insert
    suspend fun insert(foto: FotoOrden)

    @Delete
    suspend fun delete(foto: FotoOrden)

    @Query("SELECT * FROM fotos_orden WHERE ordenId = :ordenId ORDER BY id")
    fun getByOrden(ordenId: Int): Flow<List<FotoOrden>>

    @Query("SELECT * FROM fotos_orden WHERE ordenId IN (:ordenIds) ORDER BY ordenId, id")
    suspend fun getByOrdenes(ordenIds: List<Int>): List<FotoOrden>
}
