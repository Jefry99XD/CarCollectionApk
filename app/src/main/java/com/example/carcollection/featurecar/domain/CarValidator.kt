package com.example.carcollection.featurecar.domain

/**
 * Errores específicos de operaciones con carros
 */
sealed class CarError(open val message: String) {
    object MissingBrand : CarError("Marca es requerida")
    object MissingName : CarError("Nombre del carro es requerido")
    object MissingYear : CarError("Año es requerido")
    object InvalidYear : CarError("Año debe ser número entre 1900-2050")
    data class DuplicateCar(val existingCarId: String = "") : CarError("Este carro ya existe en tu colección")
    data class StringTooLong(val field: String, val maxLength: Int) : CarError("$field no puede exceder $maxLength caracteres")
    object QuotaExceeded : CarError("Has alcanzado el límite de carros")
    data class NetworkError(val code: Int = 0) : CarError("Error de red: código $code")
    data class UnknownError(override val message: String) : CarError(message)

    fun toUserMessage(): String = when (this) {
        is MissingBrand -> "Por favor, ingresa la marca del carro"
        is MissingName -> "Por favor, ingresa el nombre del carro"
        is MissingYear -> "Por favor, ingresa el año del carro"
        is InvalidYear -> message
        is DuplicateCar -> "Este carro ya está en tu colección"
        is StringTooLong -> "$field es demasiado largo (máx $maxLength)"
        is QuotaExceeded -> "Has agregado demasiados carros"
        is NetworkError -> "Revisa tu conexión a internet"
        is UnknownError -> "Algo salió mal. Intenta de nuevo."
    }
}

/**
 * Validador de carros
 */
object CarValidator {
    private const val MAX_BRAND_LENGTH = 50
    private const val MAX_NAME_LENGTH = 100
    private const val MAX_YEAR_LENGTH = 4
    private const val MAX_CARS_PER_USER = 5000

    fun validateBrand(brand: String?): CarError? {
        return when {
            brand.isNullOrBlank() -> CarError.MissingBrand
            brand.length > MAX_BRAND_LENGTH -> CarError.StringTooLong("Marca", MAX_BRAND_LENGTH)
            else -> null
        }
    }

    fun validateName(name: String?): CarError? {
        return when {
            name.isNullOrBlank() -> CarError.MissingName
            name.length > MAX_NAME_LENGTH -> CarError.StringTooLong("Nombre", MAX_NAME_LENGTH)
            else -> null
        }
    }

    fun validateYear(year: String?): CarError? {
        return when {
            year.isNullOrBlank() -> CarError.MissingYear
            !year.matches(Regex("^\\d{4}$")) -> CarError.InvalidYear
            year.toIntOrNull()?.let { it < 1900 || it > 2050 } == true -> CarError.InvalidYear
            else -> null
        }
    }

    fun validateCar(car: Car): List<CarError> {
        val errors = mutableListOf<CarError>()

        validateBrand(car.brand)?.let { errors.add(it) }
        validateName(car.name)?.let { errors.add(it) }
        validateYear(car.year)?.let { errors.add(it) }

        if (car.tags.any { it.isBlank() }) {
            errors.add(CarError.UnknownError("Tags no pueden estar vacíos"))
        }

        return errors
    }
}

/**
 * Resultado de operación batch
 */
data class BatchAddResult(
    val successCount: Int,
    val failureCount: Int,
    val skippedDuplicates: Int,
    val errors: List<String> = emptyList()
)

