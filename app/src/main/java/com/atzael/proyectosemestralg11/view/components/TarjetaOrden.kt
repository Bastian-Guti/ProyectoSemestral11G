package com.atzael.proyectosemestralg11.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.model.OrdenConDetalle
import com.atzael.proyectosemestralg11.util.formatearFecha

@Composable
fun TarjetaOrden(
    item: OrdenConDetalle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orden = item.orden
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "OT #${orden.id}", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = orden.estado,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${item.clienteNombre} - ${item.localNombre}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Equipo: ${item.equipoNombre} (${item.equipoUbicacion})", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Técnico: ${item.tecnicoNombre}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Visita: ${orden.fechaVisita.formatearFecha()}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${orden.tipo} - Prioridad ${orden.prioridad}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
