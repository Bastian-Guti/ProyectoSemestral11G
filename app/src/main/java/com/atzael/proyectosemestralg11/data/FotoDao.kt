package com.atzael.proyectosemestralg11.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.atzael.proyectosemestralg11.model.FotoOrden
import kotlinx.coroutines.flow.Flow

@Dao
interface FotoDao {
    // Crea una foto nueva y devuelve el id que le asignó Room
    @Insert
    suspend fun insert(foto: FotoOrden): Long

    // Sincronización: actualiza si existe o inserta si no
    @Upsert
    suspend fun upsert(foto: FotoOrden)

    @Delete
    suspend fun delete(foto: FotoOrden)

    @Query("SELECT * FROM fotos_orden WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): FotoOrden?

    @Query("SELECT * FROM fotos_orden WHERE ordenId = :ordenId ORDER BY id")
    fun getByOrden(ordenId: Int): Flow<List<FotoOrden>>

    @Query("SELECT * FROM fotos_orden WHERE ordenId IN (:ordenIds) ORDER BY ordenId, id")
    suspend fun getByOrdenes(ordenIds: List<Int>): List<FotoOrden>

    @Query("SELECT * FROM fotos_orden")
    suspend fun getAllFotos(): List<FotoOrden>
}
