package com.example.carcollection.featurecar.data

import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featurecar.domain.CarValidator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Importador de datos en batch - soporta CSV y JSON
 */
object BatchImporter {

    /**
     * Parsea datos CSV en lista de carros
     * Formato esperado:
     * brand,name,year,type,serie,color,photoUrl,quality,backgroundName,tags
     */
    fun parseCSV(csvData: String): List<Car> {
        val lines = csvData.split("\n").drop(1) // Skip header

        return lines
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                try {
                    val fields = line.split(",").map { it.trim() }
                    if (fields.size < 4) return@mapNotNull null

                    Car(
                        brand = fields[0].ifBlank { null },
                        name = fields[1].ifBlank { null },
                        year = fields[2].ifBlank { null },
                        type = fields[3].ifBlank { null },
                        serie = fields.getOrNull(4)?.ifBlank { null },
                        color = fields.getOrNull(5)?.ifBlank { null },
                        photoUrl = fields.getOrNull(6)?.ifBlank { null },
                        quality = fields.getOrNull(7)?.ifBlank { null } ?: "Basico",
                        backgroundName = fields.getOrNull(8)?.ifBlank { null } ?: "fondo_1",
                        tags = fields.getOrNull(9)?.split(";")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
                        createdAt = null
                    )
                } catch (e: Exception) {
                    null
                }
            }
    }

    /**
     * Parsea datos JSON en lista de carros
     */
    fun parseJSON(jsonData: String): List<Car> {
        return try {
            val trimmed = jsonData.trim()
            if (trimmed.startsWith("[")) {
                // Es un array
                val type = object : TypeToken<List<Car>>() {}.type
                Gson().fromJson(trimmed, type) as? List<Car> ?: emptyList()
            } else if (trimmed.startsWith("{")) {
                // Es un objeto único
                val car = Gson().fromJson(trimmed, Car::class.java)
                listOf(car)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error parsing JSON: ${e.message}")
            emptyList()
        }
    }

    /**
     * Valida batch de carros antes de importar
     */
    suspend fun validateBatch(cars: List<Car>): BatchValidationResult {
        val validCars = mutableListOf<Car>()
        val invalidCars = mutableListOf<Pair<Car, String>>()
        val duplicates = mutableListOf<Car>()

        cars.forEach { car ->
            // Validar estructura
            val errors = CarValidator.validateCar(car)
            if (errors.isNotEmpty()) {
                invalidCars.add(car to errors[0].toUserMessage())
                return@forEach
            }

            // Detectar duplicados dentro del batch
            if (validCars.any {
                it.brand == car.brand &&
                it.name == car.name &&
                it.year == car.year
            }) {
                duplicates.add(car)
            } else {
                validCars.add(car)
            }
        }

        return BatchValidationResult(validCars, invalidCars, duplicates)
    }

    data class BatchValidationResult(
        val validCars: List<Car>,
        val invalidCars: List<Pair<Car, String>>,
        val duplicates: List<Car>
    )
}

/**
 * Creador CSV template para importación
 */
object CSVTemplateGenerator {
    fun generateTemplate(): String {
        return """brand,name,year,type,serie,color,photoUrl,quality,backgroundName,tags
Ferrari,F40,1987,Sport,Sport,Rojo,https://example.com/f40.jpg,STH,fondo_1,supercar;italiano
Lamborghini,Countach,1974,Sport,Sport,Amarillo,https://example.com/countach.jpg,TH,fondo_2,supercar;italiano;clasico
""".trimIndent()
    }
}

