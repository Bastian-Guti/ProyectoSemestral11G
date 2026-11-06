package com.atzael.proyectosemestralg11.view.components

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.atzael.proyectosemestralg11.model.FotoOrden
import com.atzael.proyectosemestralg11.util.cargarMiniatura
import com.atzael.proyectosemestralg11.util.copiarFotoDesdeUri
import com.atzael.proyectosemestralg11.util.nuevoArchivoFoto
import com.atzael.proyectosemestralg11.util.uriDeArchivo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ImagenLocal(ruta: String, modifier: Modifier = Modifier, ladoMaximo: Int = 640) {
    val bitmap by produceState<Bitmap?>(initialValue = null, ruta) {
        value = withContext(Dispatchers.IO) { cargarMiniatura(ruta, ladoMaximo) }
    }
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Foto de la orden de trabajo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } ?: Text("...", style = MaterialTheme.typography.bodySmall)
    }
}

// Miniaturas de las fotos; al tocar una se abre ampliada. Con onEliminar se puede borrar.
@Composable
fun GaleriaFotos(
    fotos: List<FotoOrden>,
    modifier: Modifier = Modifier,
    onEliminar: ((FotoOrden) -> Unit)? = null
) {
    var ampliada by remember { mutableStateOf<FotoOrden?>(null) }

    if (fotos.isEmpty()) {
        Text("Sin fotos.", style = MaterialTheme.typography.bodyMedium, modifier = modifier)
    } else {
        Row(
            modifier = modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            fotos.forEach { foto ->
                ImagenLocal(
                    ruta = foto.ruta,
                    ladoMaximo = 240,
                    modifier = Modifier
                        .size(96.dp)
                        .clickable { ampliada = foto }
                )
            }
        }
    }

    ampliada?.let { foto ->
        AlertDialog(
            onDismissRequest = { ampliada = null },
            title = { Text(foto.tipo) },
            text = { ImagenLocal(ruta = foto.ruta, modifier = Modifier.fillMaxWidth().height(360.dp)) },
            confirmButton = { TextButton(onClick = { ampliada = null }) { Text("Cerrar") } },
            dismissButton = onEliminar?.let { eliminar ->
                {
                    TextButton(onClick = {
                        eliminar(foto)
                        ampliada = null
                    }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                }
            }
        )
    }
}

// HU-12: tomar la foto con la cámara o elegirla de la galería del teléfono.
// No necesita permiso de cámara ni de almacenamiento: usa la app de cámara y el selector del sistema.
@Composable
fun SeccionFotos(
    titulo: String,
    ordenId: Int,
    fotos: List<FotoOrden>,
    habilitado: Boolean,
    onFotoNueva: (File) -> Unit,
    onEliminar: (FotoOrden) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var archivoPendiente by remember { mutableStateOf<File?>(null) }
    var errorCopia by remember { mutableStateOf(false) }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        val archivo = archivoPendiente
        archivoPendiente = null
        if (archivo != null) {
            if (tomada && archivo.length() > 0) onFotoNueva(archivo) else archivo.delete()
        }
    }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val destino = nuevoArchivoFoto(context, ordenId)
                val copiada = withContext(Dispatchers.IO) { copiarFotoDesdeUri(context, uri, destino) }
                if (copiada) onFotoNueva(destino) else {
                    destino.delete()
                    errorCopia = true
                }
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleSmall)
        GaleriaFotos(fotos = fotos, onEliminar = if (habilitado) onEliminar else null)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val archivo = nuevoArchivoFoto(context, ordenId)
                    archivoPendiente = archivo
                    camara.launch(uriDeArchivo(context, archivo))
                },
                enabled = habilitado,
                modifier = Modifier.weight(1f)
            ) { Text("Tomar foto") }
            OutlinedButton(
                onClick = { galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                enabled = habilitado,
                modifier = Modifier.weight(1f)
            ) { Text("Desde galería") }
        }
        if (errorCopia) {
            Text(
                text = "No se pudo cargar la foto elegida.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
