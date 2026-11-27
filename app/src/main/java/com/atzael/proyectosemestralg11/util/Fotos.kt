package com.atzael.proyectosemestralg11.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.collection.LruCache
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

// Caché en memoria para miniaturas de fotos (utiliza hasta el 12.5% de la memoria asignada a la app)
private val maxMemoriaKB = (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt().coerceAtLeast(1024 * 5)
private val fotoCache = object : LruCache<String, Bitmap>(maxMemoriaKB) {
    override fun sizeOf(key: String, value: Bitmap): Int {
        return value.byteCount / 1024
    }
}

// Las fotos se guardan en el almacenamiento privado de la app (no en la galería del teléfono)
fun nuevoArchivoFoto(context: Context, ordenId: Int): File {
    val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }
    return File(carpeta, "ot${ordenId}_${System.currentTimeMillis()}.jpg")
}

fun uriDeArchivo(context: Context, archivo: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

// Copia una foto elegida de la galería al almacenamiento privado de la app
// Corre en Dispatchers.IO: es seguro llamarla desde el hilo principal
suspend fun copiarFotoDesdeUri(context: Context, origen: Uri, destino: File): Boolean =
    withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(origen)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            } != null
        }.getOrDefault(false)
    }

// Carga reducida para mostrar miniaturas sin gastar memoria.
// Soporta archivos locales y cadenas Base64 provenientes de la API.
suspend fun cargarMiniatura(ruta: String, ladoMaximo: Int = 640): Bitmap? = withContext(Dispatchers.IO) {
    if (ruta.isBlank()) return@withContext null
    val claveCache = "${ruta.hashCode()}_$ladoMaximo"
    fotoCache.get(claveCache)?.let { return@withContext it }

    // Si la ruta es una cadena codificada en Base64 cargada desde la API
    if (ruta.startsWith("data:image/") || (ruta.length > 200 && !ruta.startsWith("/"))) {
        runCatching {
            val base64Limpio = if (ruta.contains(",")) ruta.substringAfter(",") else ruta
            val bytes = Base64.decode(base64Limpio, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                fotoCache.put(claveCache, bitmap)
            }
            return@withContext bitmap
        }
    }

    // De lo contrario es una ruta de archivo local
    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(ruta, medidas)
    if (medidas.outWidth <= 0) return@withContext null
    var muestreo = 1
    while (medidas.outWidth / muestreo > ladoMaximo * 2 || medidas.outHeight / muestreo > ladoMaximo * 2) {
        muestreo *= 2
    }
    val bitmap = BitmapFactory.decodeFile(ruta, BitmapFactory.Options().apply { inSampleSize = muestreo })
    if (bitmap != null) {
        fotoCache.put(claveCache, bitmap)
    }
    bitmap
}

// Convierte un archivo local de imagen a una cadena Base64 optimizada/comprimida en JPEG
fun comprimirFotoABase64(rutaArchivo: String, ladoMaximo: Int = 800): String? {
    if (rutaArchivo.startsWith("data:image/")) return rutaArchivo
    val file = File(rutaArchivo)
    if (!file.exists()) return null
    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(rutaArchivo, medidas)
    if (medidas.outWidth <= 0) return null
    var muestreo = 1
    while (medidas.outWidth / muestreo > ladoMaximo || medidas.outHeight / muestreo > ladoMaximo) {
        muestreo *= 2
    }
    val bitmap = BitmapFactory.decodeFile(rutaArchivo, BitmapFactory.Options().apply { inSampleSize = muestreo }) ?: return null
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
    val bytes = outputStream.toByteArray()
    return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
}
