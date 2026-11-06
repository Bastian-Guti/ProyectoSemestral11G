package com.atzael.proyectosemestralg11.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// Las fotos se guardan en el almacenamiento privado de la app (no en la galería del teléfono)
fun nuevoArchivoFoto(context: Context, ordenId: Int): File {
    val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }
    return File(carpeta, "ot${ordenId}_${System.currentTimeMillis()}.jpg")
}

fun uriDeArchivo(context: Context, archivo: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

// Copia una foto elegida de la galería al almacenamiento privado de la app
fun copiarFotoDesdeUri(context: Context, origen: Uri, destino: File): Boolean = runCatching {
    context.contentResolver.openInputStream(origen)?.use { entrada ->
        destino.outputStream().use { salida -> entrada.copyTo(salida) }
    } != null
}.getOrDefault(false)

// Carga reducida para mostrar miniaturas sin gastar memoria
fun cargarMiniatura(ruta: String, ladoMaximo: Int = 640): Bitmap? {
    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(ruta, medidas)
    if (medidas.outWidth <= 0) return null
    var muestreo = 1
    while (medidas.outWidth / muestreo > ladoMaximo * 2 || medidas.outHeight / muestreo > ladoMaximo * 2) {
        muestreo *= 2
    }
    return BitmapFactory.decodeFile(ruta, BitmapFactory.Options().apply { inSampleSize = muestreo })
}
