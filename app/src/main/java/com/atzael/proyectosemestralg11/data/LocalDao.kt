package com.atzael.proyectosemestralg11.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.atzael.proyectosemestralg11.model.Local
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalDao {
    @Query("SELECT * FROM locales ORDER BY nombre")
    fun getAll(): Flow<List<Local>>

    @Query("SELECT * FROM locales WHERE clienteId = :clienteId")
    suspend fun getByCliente(clienteId: Int): List<Local>

    @Query("SELECT * FROM locales WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Local?

    // Crea un registro nuevo y devuelve el id que le asignó Room
    @Insert
    suspend fun insert(local: Local): Long

    // Sincronización: actualiza si existe o inserta si no, sin borrar la fila ni sus dependientes
    @Upsert
    suspend fun upsert(local: Local)

    @Update
    suspend fun update(local: Local)

    @Delete
    suspend fun delete(local: Local)

    @Query("SELECT COUNT(*) FROM equipos WHERE localId = :localId")
    suspend fun contarEquipos(localId: Int): Int
}
