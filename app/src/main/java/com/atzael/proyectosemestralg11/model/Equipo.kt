package com.atzael.proyectosemestralg11.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "equipos",
    foreignKeys = [
        ForeignKey(
            entity = Local::class,
            parentColumns = ["id"],
            childColumns = ["localId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("localId")]
)
data class Equipo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val localId: Int,
    val nombre: String,
    val marca: String = "",
    val ubicacion: String = UbicacionEquipo.INTERIOR, // Interior o Exterior (azotea/patio)
    val ultimaMantencion: Long? = null // Fecha (UTC) de la última mantención
)
