package com.atzael.proyectosemestralg11.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.atzael.proyectosemestralg11.model.Cliente
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes ORDER BY nombre")
    fun getAll(): Flow<List<Cliente>>

    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Cliente?

    // Crea un registro nuevo y devuelve el id que le asignó Room
    @Insert
    suspend fun insert(cliente: Cliente): Long

    // Sincronización: actualiza si existe o inserta si no, sin borrar la fila ni sus dependientes
    @Upsert
    suspend fun upsert(cliente: Cliente)

    @Update
    suspend fun update(cliente: Cliente)

    @Delete
    suspend fun delete(cliente: Cliente)

    @Query("SELECT COUNT(*) FROM locales WHERE clienteId = :clienteId")
    suspend fun contarLocales(clienteId: Int): Int

    @Query("SELECT COUNT(*) FROM usuarios WHERE clienteId = :clienteId")
    suspend fun contarUsuarios(clienteId: Int): Int
}
