package com.atzael.proyectosemestralg11.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ordenes_trabajo",
    foreignKeys = [
        ForeignKey(
            entity = Cliente::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Local::class,
            parentColumns = ["id"],
            childColumns = ["localId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Equipo::class,
            parentColumns = ["id"],
            childColumns = ["equipoId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("clienteId"), Index("localId"), Index("equipoId"), Index("tecnicoRut")]
)
data class OrdenTrabajo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val clienteId: Int,
    val localId: Int,
    val equipoId: Int,
    val tipo: String, // TipoOrden
    val prioridad: String, // PrioridadOrden
    val tecnicoRut: String, // RUT del técnico asignado
    val fechaVisita: Long, // Fecha (UTC) de la visita programada
    val estado: String = EstadoOrden.PENDIENTE,
    val creadaPor: String, // RUT de quien creó la OT
    val fechaCreacion: Long = System.currentTimeMillis(),

    val checkIn: Long? = null,
    val checkOut: Long? = null,
    val diagnostico: String? = null,
    val actividades: String? = null,
    val materiales: String? = null,
    // Ubicación solo en check-in y check-out, nunca de forma continua (HU-22)
    val checkInLat: Double? = null,
    val checkInLon: Double? = null,
    val checkOutLat: Double? = null,
    val checkOutLon: Double? = null,
    val motivoRechazo: String? = null, // Cuando el técnico rechaza la OT (HU-09)
    // Validación del cliente (HU-18)
    val conformidad: String? = null, // EstadoConformidad
    val observacionCliente: String? = null,
    val fechaConformidad: Long? = null,
    val conformidadPor: String? = null // RUT del usuario cliente que valido
)
