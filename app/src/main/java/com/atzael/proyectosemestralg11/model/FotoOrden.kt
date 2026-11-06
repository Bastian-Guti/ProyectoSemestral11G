package com.atzael.proyectosemestralg11.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fotos_orden",
    foreignKeys = [
        ForeignKey(
            entity = OrdenTrabajo::class,
            parentColumns = ["id"],
            childColumns = ["ordenId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ordenId")]
)
data class FotoOrden(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ordenId: Int,
    val ruta: String, // Ruta absoluta del archivo dentro del almacenamiento privado de la app
    val tipo: String, // TipoFoto
    val fecha: Long = System.currentTimeMillis(),
    val tomadaPor: String // RUT del técnico
)
