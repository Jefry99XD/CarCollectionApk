/*
package com.example.carcollection.utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

private fun detectDelimiter(headerLine: String): String {
    return if (headerLine.contains(";")) ";" else ","
}

private fun parseCarFromTokens(tokens: List<String>, header: List<String>): Car? {

    try {
        fun getValue(column: String): String {
            val index = header.indexOfFirst { it.equals(column, ignoreCase = true) }
            return if (index >= 0 && index < tokens.size) unescapeCsvField(tokens[index]) else ""
        }


        val tagsRaw = getValue("tags")
        if (tagsRaw.isNotBlank()) tagsRaw.split("|") else emptyList()

        val backgroundName = if (header.any { it.equals("backgroundName", ignoreCase = true) }) {
            getValue("backgroundName").ifBlank { "fondo" }
        } else {
            "fondo"
        }


        return Car(
            brand = getValue("brand"),
            name = getValue("name"),
            serie = getValue("serie"),
            year = getValue("year"),
            color = getValue("color"),
            type = getValue("type"),
            photoUrl = getValue("photoUrl"),
            tags = getValue("tags").split("|").map { it.trim() }.filter { it.isNotEmpty() },
            backgroundName = backgroundName
        )


    } catch (_: Exception) {
        return null
    }
}


private fun unescapeCsvField(field: String): String {
    return field.trim().removeSurrounding("\"").replace("\"\"", "\"")
}

private fun escapeCsvField(field: String): String {
    val cleanedField = field.replace("\n", " ").replace("\r", " ") // quita saltos de línea
    val escapedField = cleanedField.replace("\"", "\"\"") // escapa comillas dobles
    return "\"$escapedField\"" // encierra en comillas
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
                writer.write("id,brand,name,serie,year,color,type,photoUrl,tags, backgroundName\n")
                cars.forEach { car ->
                    val tagsJoined = car.tags.joinToString("|")
                    writer.write(listOf(
                        car.id.toString(),
                        car.brand,
                        car.name,
                        car.serie,
                        car.year,
                        car.color,
                        car.type,
                        car.photoUrl,
                        tagsJoined,
                        car.backgroundName
                    ).joinToString(",") { escapeCsvField(it) } + "\n")


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


*/
