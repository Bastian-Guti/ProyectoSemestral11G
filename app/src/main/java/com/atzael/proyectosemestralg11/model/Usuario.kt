package com.atzael.proyectosemestralg11.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey
    val rut: String,
    val nombre: String,
    val apellido: String,
    val password: String,
    val rol: String, // RolUsuario
    val clienteId: Int? = null, // Solo para rol Cliente: empresa a la que pertenece
    // HU-22: el técnico acepta el aviso de qué datos se recopilan (ubicación y fotos)
    @ColumnInfo(defaultValue = "0")
    val avisoDatosAceptado: Boolean = false
)
