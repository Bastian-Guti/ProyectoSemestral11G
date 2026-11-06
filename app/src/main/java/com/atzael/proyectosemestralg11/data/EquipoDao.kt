package com.atzael.proyectosemestralg11.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.atzael.proyectosemestralg11.model.Equipo
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipoDao {
    @Query("SELECT * FROM equipos ORDER BY nombre")
    fun getAll(): Flow<List<Equipo>>

    @Query("SELECT * FROM equipos WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Equipo?

    @Insert
    suspend fun insert(equipo: Equipo)

    @Update
    suspend fun update(equipo: Equipo)

    @Delete
    suspend fun delete(equipo: Equipo)

    @Query("SELECT COUNT(*) FROM ordenes_trabajo WHERE equipoId = :equipoId")
    suspend fun contarOrdenes(equipoId: Int): Int
}
