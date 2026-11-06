package com.atzael.proyectosemestralg11.model

object RolUsuario {
    const val TECNICO = "Tecnico"
    const val JEFATURA = "Jefatura"
    const val CLIENTE = "Cliente"
}

object EstadoConformidad {
    const val CONFORME = "Conforme"
    const val RECHAZADA = "Rechazada"
}

object EstadoOrden {
    const val PENDIENTE = "Pendiente"
    const val ACEPTADA = "Aceptada"
    const val EN_CURSO = "En curso"
    const val FINALIZADA = "Finalizada"
    const val RECHAZADA_TECNICO = "Rechazada por técnico"
    const val CANCELADA = "Cancelada"
    val todos = listOf(PENDIENTE, ACEPTADA, EN_CURSO, FINALIZADA, RECHAZADA_TECNICO, CANCELADA)

    // Estados en los que la jefatura todavía puede reagendar, reasignar o cancelar
    val gestionablesPorJefatura = listOf(PENDIENTE, ACEPTADA, RECHAZADA_TECNICO)
}

object TipoFoto {
    const val ANTES = "Antes"
    const val DESPUES = "Después"
    val todos = listOf(ANTES, DESPUES)
}

object TipoOrden {
    const val PREVENTIVO = "Mantención preventiva"
    const val CORRECTIVO = "Reparación correctiva"
    val todos = listOf(PREVENTIVO, CORRECTIVO)
}

object PrioridadOrden {
    const val ALTA = "Alta"
    const val MEDIA = "Media"
    const val BAJA = "Baja"
    val todos = listOf(ALTA, MEDIA, BAJA)
}

object UbicacionEquipo {
    const val INTERIOR = "Interior"
    const val EXTERIOR = "Exterior"
    val todos = listOf(INTERIOR, EXTERIOR)
}

object MotivoReagendar {
    const val CLIMA = "Clima"
    const val CLIENTE = "Solicitud del cliente"
    const val TECNICO = "Técnico no disponible"
    const val OTRO = "Otro"
    val todos = listOf(CLIMA, CLIENTE, TECNICO, OTRO)
}
