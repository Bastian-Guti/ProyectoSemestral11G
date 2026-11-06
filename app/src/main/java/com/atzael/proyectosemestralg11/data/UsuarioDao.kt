package com.atzael.proyectosemestralg11.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.atzael.proyectosemestralg11.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE rut = :rut AND password = :password AND rol = :rol")
    suspend fun login(rut: String, password: String, rol: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE rut = :rut")
    suspend fun getUsuarioByRut(rut: String): Usuario?

    @Query("UPDATE usuarios SET avisoDatosAceptado = 1 WHERE rut = :rut")
    suspend fun aceptarAvisoDatos(rut: String)

    @Query("SELECT * FROM usuarios WHERE rol = 'Tecnico' ORDER BY nombre")
    suspend fun getTecnicosList(): List<Usuario>

    @Query("SELECT * FROM usuarios WHERE rol = 'Tecnico' ORDER BY nombre")
    fun getTecnicos(): Flow<List<Usuario>>
}
