package com.atzael.proyectosemestralg11.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.atzael.proyectosemestralg11.model.Cliente
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes ORDER BY nombre")
    fun getAll(): Flow<List<Cliente>>

    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Cliente?

    @Insert
    suspend fun insert(cliente: Cliente)

    @Update
    suspend fun update(cliente: Cliente)

    @Delete
    suspend fun delete(cliente: Cliente)

    @Query("SELECT COUNT(*) FROM locales WHERE clienteId = :clienteId")
    suspend fun contarLocales(clienteId: Int): Int

    @Query("SELECT COUNT(*) FROM usuarios WHERE clienteId = :clienteId")
    suspend fun contarUsuarios(clienteId: Int): Int
}
