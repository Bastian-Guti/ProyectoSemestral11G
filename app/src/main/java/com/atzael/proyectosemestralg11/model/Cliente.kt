package com.atzael.proyectosemestralg11.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val contacto: String,
    val telefono: String,

    val slaHoras: Int = 24
)
