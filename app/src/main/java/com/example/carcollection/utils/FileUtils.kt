package com.example.carcollection.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.example.carcollection.data.local.Car
import com.example.carcollection.data.repository.CarRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

private fun detectDelimiter(headerLine: String): String {
    return if (headerLine.contains(";")) ";" else ","
}

private fun parseCarFromTokens(tokens: List<String>, header: List<String>): Car? {
    try {
        fun getValue(column: String): String {
            val index = header.indexOfFirst { it.equals(column, ignoreCase = true) }
            return if (index >= 0 && index < tokens.size) tokens[index] else ""
        }

        return Car(
            brand = getValue("brand"),
            name = getValue("name"),
            serie = getValue("serie"),
            year = getValue("year"),
            color = getValue("color"),
            type = getValue("type"),
            photoUrl = getValue("photoUrl")


        )
    } catch (e: Exception) {
        return null
    }
}



fun exportCarsToCSV(context: Context, cars: List<Car>) {
    // Obtener la carpeta pública de Downloads
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    // Asegurarse de que exista
    if (!downloadsDir.exists()) {
        downloadsDir.mkdirs()
    }

    val fileName = "car_collection_export.csv"
    val file = File(downloadsDir, fileName)

    try {
        FileWriter(file).use { writer ->
            writer.write("id,brand,name,serie,year,color, type ,photoUrl\n")
            cars.forEach { car ->
                writer.write("${car.id},${car.brand},${car.name},${car.serie},${car.year},${car.color}, ${car.type},${car.photoUrl}\n")
            }
        }

        Toast.makeText(context, "Exportado a: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun importCarsFromCSV(context: Context, repository: CarRepository) {
    val fileName = "car_collection_export.csv"
    val file = File(context.filesDir, fileName)

    if (!file.exists()) {
        Toast.makeText(context, "Archivo no encontrado", Toast.LENGTH_SHORT).show()
        return
    }

    CoroutineScope(Dispatchers.IO).launch {
        val allLines = file.readLines()
        if (allLines.isEmpty()) return@launch

        val delimiter = detectDelimiter(allLines[0])
        val header = allLines[0].split(delimiter).map { it.trim() }
        val dataLines = allLines.drop(1)

        val cars = dataLines.mapNotNull { line ->
            val tokens = line.split(delimiter).map { it.trim() }
            parseCarFromTokens(tokens, header)
        }

        cars.forEach { repository.insertCar(it) }

        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "Importación completa", Toast.LENGTH_LONG).show()
        }
    }
}


fun importCarsFromUri(context: Context, repository: CarRepository, uri: Uri) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val allLines = reader.readLines()

            if (allLines.isEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "El archivo está vacío", Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            val delimiter = detectDelimiter(allLines[0])
            val header = allLines[0].split(delimiter).map { it.trim() }
            val dataLines = allLines.drop(1)

            val cars = dataLines.mapNotNull { line ->
                val tokens = line.split(delimiter).map { it.trim() }
                parseCarFromTokens(tokens, header)
            }

            cars.forEach { repository.insertCar(it) }

            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Importación completa", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Error al importar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}


fun exportCarsToUri(context: Context, cars: List<Car>, uri: Uri) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val outputStream = context.contentResolver.openOutputStream(uri)
            outputStream?.bufferedWriter()?.use { writer ->
                writer.write("id,brand,name,serie,year,color, type,photoUrl\n")
                cars.forEach { car ->
                    writer.write("${car.id},${car.brand},${car.name},${car.serie},${car.year},${car.color}, ${car.type},${car.photoUrl}\n")
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Archivo exportado correctamente", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}


