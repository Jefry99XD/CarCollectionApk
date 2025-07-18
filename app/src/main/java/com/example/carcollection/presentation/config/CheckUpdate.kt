package com.example.carcollection.presentation.config
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import androidx.core.net.toUri


fun checkForUpdateAndDownload(context: Context, currentVersion: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://raw.githubusercontent.com/Jefry99XD/CarCollectionApk/main/version.json")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (body != null) {
                val json = JSONObject(body)
                val latestVersion = json.getString("version")
                val apkUrl = json.getString("apk_url")

                if (latestVersion != currentVersion) {
                    val requestDownload = DownloadManager.Request(apkUrl.toUri())
                        .setTitle("Descargando actualización")
                        .setDescription("APK de versión $latestVersion")
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "carcollection_update.apk")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

                    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    manager.enqueue(requestDownload)

                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Descargando actualización...", Toast.LENGTH_LONG).show()
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Ya tienes la última versión", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Error al buscar actualización", Toast.LENGTH_SHORT).show()
            }
        }
    }
}




