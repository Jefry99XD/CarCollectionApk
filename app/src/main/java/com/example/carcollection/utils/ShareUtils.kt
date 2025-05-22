package com.example.carcollection.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap

object ShareUtils {

    /**
     * Guarda un [Bitmap] como archivo JPG en cache y devuelve su URI para compartir.
     */
    fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String = "shared_image.jpg"): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()

            val file = File(cachePath, fileName)
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            stream.flush()
            stream.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Crea un [Bitmap] a partir de una [android.view.View].
     */
    fun createBitmapFromView(view: android.view.View): Bitmap {
        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}