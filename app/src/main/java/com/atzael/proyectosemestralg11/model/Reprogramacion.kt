package com.atzael.proyectosemestralg11.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reprogramaciones",
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
data class Reprogramacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ordenId: Int,
    val fechaAnterior: Long,
    val fechaNueva: Long,
    val tecnicoAnterior: String,
    val tecnicoNuevo: String,
    val motivo: String, // MotivoReagendar
    val fechaRegistro: Long = System.currentTimeMillis()
)
