package com.atzael.proyectosemestralg11.model

import androidx.room.Embedded

// Resultado de unir la OT con los nombres de cliente, local, equipo y técnico
data class OrdenConDetalle(
    @Embedded val orden: OrdenTrabajo,
    val clienteNombre: String,
    val localNombre: String,
    val equipoNombre: String,
    val equipoUbicacion: String,
    val tecnicoNombre: String,
    val slaHoras: Int,
    val localCiudad: String = "",
    val cantidadFotos: Int = 0
)
