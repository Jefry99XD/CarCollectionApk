package com.example.carcollection.featureuser.data

import android.net.Uri
import androidx.core.net.toUri
import com.example.carcollection.featureAchievements.domain.AchievementGlobal
import com.example.carcollection.featureAchievements.domain.AchievementRarity
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featureuser.domain.User
import com.example.carcollection.featureuser.domain.XPActivity
import com.example.carcollection.featureuser.domain.XPSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserMethods {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun registerUser(
        username: String,
        email: String,
        photoUrl: String,
        password: String // Password is essential for Auth registration!
    ): Result<User> { // Assuming `User` is the data class defined above
        return try {
            // 1. Create the user account with Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: throw Exception("Firebase Auth user was null after creation.")

            val uid = firebaseUser.uid

            // 2. Update the user's profile in Firebase Authentication (optional but good practice)
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .apply {
                    if (photoUrl.isNotEmpty()) { // Only set photoUri if photoUrl is provided
                        setPhotoUri(photoUrl.toUri())
                    }
                }
                .build()

            firebaseUser.updateProfile(profileUpdates).await()
            SecureLogger.success("Firebase Auth profile updated for UID")


            // 3. Create the user document in Cloud Firestore
            // Use the User data class to ensure consistent structure
            val userFirestoreData = User(
                uid = uid,
                username = username,
                email = email,
                photoUrl = photoUrl,
                createdAt = System.currentTimeMillis(), // Set the timestamp
                adminRights = false // Explicitly set to false, can only be changed in Firebase Console
            )

            db.collection("users")
                .document(uid)
                .set(userFirestoreData)
                .await()

            SecureLogger.success("User document saved to Firestore")

            Result.success(userFirestoreData) // Return the created User object

        } catch (e: Exception) {
            // Provide more specific error handling for Authentication exceptions
            when (e) {
                is FirebaseAuthWeakPasswordException ->
                    Result.failure(Exception("Password is not strong enough. Please choose a stronger password."))
                is FirebaseAuthInvalidCredentialsException ->
                    Result.failure(Exception("The email address is malformed or invalid."))
                is FirebaseAuthUserCollisionException ->
                    Result.failure(Exception("An account with this email address already exists."))
                else ->
                    Result.failure(Exception("Registration failed: ${e.message}"))
            }
        }
    }

    suspend fun editUserProfile(
        username: String,
        photoUrl: String,
        password: String,
        email: String,
        bio: String
    )
    : Result<User> {
        val firebaseUser = auth.currentUser
            ?: return Result.failure(Exception("No user is currently logged in."))

        return try {
            // Update Auth profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .apply {
                    if (photoUrl.isNotEmpty()) {
                        setPhotoUri(Uri.parse(photoUrl))
                    }
                }
                .build()

            firebaseUser.updateProfile(profileUpdates).await()

            // Update email if changed
            if (firebaseUser.email != email) {
                firebaseUser.updateEmail(email).await()
            }

            // Update password if provided
            if (password.isNotEmpty()) {
                firebaseUser.updatePassword(password).await()
            }

            // Update Firestore document
            val uid = firebaseUser.uid
            val userDocRef = db.collection("users").document(uid)

            val updates = mutableMapOf<String, Any>(
                "username" to username,
                "photoUrl" to photoUrl,
                "email" to email,
                "bio" to bio
            )

            userDocRef.update(updates).await()

            // Fetch the updated user document
            val updatedUserDoc = userDocRef.get().await()
            val updatedUser = updatedUserDoc.toObject(User::class.java)
                ?: throw Exception("Failed to parse updated user data from Firestore.")

            Result.success(updatedUser)

        } catch (e: Exception) {
            Result.failure(Exception("Failed to update profile: ${e.message}"))
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("No se encontró el usuario en Auth.")
            val uid = firebaseUser.uid
            val userDoc = db.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val user = userDoc.toObject(User::class.java)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("No se pudo obtener el usuario de Firestore."))
                }
            } else {
                Result.failure(Exception("No existe el usuario en Firestore."))
            }
        } catch (e: Exception) {
            when (e) {
                is FirebaseAuthInvalidCredentialsException ->
                    Result.failure(Exception("Email o contraseña incorrectos."))
                else ->
                    Result.failure(Exception("Error al iniciar sesión: ${e.message}"))
            }
        }
    }

    fun logoutUser(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al cerrar sesión: ${e.message}"))
        }
    }


    suspend fun getUserProfile(): Result<User> {
        val firebaseUser = auth.currentUser // Get the currently authenticated user

        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val userDocRef = db.collection("users").document(userId)
                val documentSnapshot = userDocRef.get().await() // Fetch the document from Firestore

                if (documentSnapshot.exists()) {
                    // Convert the document snapshot to your User data class
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null) {
                        // For consistency, ensure the uid is set if @Exclude is used
                        // (though it's implicitly part of the document ID)
                        Result.success(user.copy(uid = userId))
                    } else {
                        Result.failure(Exception("Failed to parse user data from Firestore."))
                    }
                } else {
                    Result.failure(Exception("User profile document not found in Firestore."))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Error fetching user profile: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user currently logged in."))
        }
    }

    suspend fun getUserStats(): Result<Map<String, Int>> {
        val firebaseUser = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
        val userId = firebaseUser.uid

        return try {
            val carsCount = db.collection("users").document(userId)
                .collection("carsCollection").get().await().size()
            val tagsCount = db.collection("users").document(userId)
                .collection("tags").get().await().size()

            // Para series diferentes (asumiendo que los autos tienen un campo "serie")
            val cars = db.collection("users").document(userId)
                .collection("carsCollection").get().await()
            val seriesSet = cars.documents
                .mapNotNull { it.getString("serie") }
                .filter { it.isNotBlank() }
                .toSet()

            Result.success(
                mapOf(
                    "cars" to carsCount,
                    "tags" to tagsCount,
                    "series" to seriesSet.size
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch user stats: ${e.message}"))
        }
    }

    suspend fun deleteUserAccount(): Result<Unit> {
        val firebaseUser = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
        val userId = firebaseUser.uid

        return try {
            // 1. Eliminar datos en Firestore
            db.collection("users").document(userId).delete().await()
            // 2. Eliminar usuario de Auth
            firebaseUser.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete account: ${e.message}"))
        }
    }

    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            val user = snapshot.toObject(User::class.java)
            if (user != null) Result.success(user.copy(uid = userId))
            else Result.failure(Exception("User not found"))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch user by ID: ${e.message}"))
        }
    }

    suspend fun fetchUserComments(userId: String): Result<List<String>> {
        return try {
            val commentsSnapshot = db.collection("users")
                .document(userId)
                .collection("comments")
                .get()
                .await()

            val comments = commentsSnapshot.documents.mapNotNull { it.getString("text") }
            Result.success(comments)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch user comments: ${e.message}"))
        }
    }

    // ────────────────────────────────────────────────
//   GET PUBLIC USER PROFILE (solo lectura)
// ────────────────────────────────────────────────
    suspend fun getPublicUserProfile(userId: String): Result<User> {
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            val user = snapshot.toObject(User::class.java)

            if (user != null) {
                Result.success(user.copy(uid = userId))
            } else {
                Result.failure(Exception("Public user profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch public profile: ${e.message}"))
        }
    }

    // ────────────────────────────────────────────────
//   GET PUBLIC USER STATS (sin permisos especiales)
// ────────────────────────────────────────────────
    suspend fun getPublicUserStats(userId: String): Result<Map<String, Int>> {
        return try {
            val cars = db.collection("users").document(userId)
                .collection("carsCollection").get().await()

            val tags = db.collection("users").document(userId)
                .collection("tags").get().await()

            // Fetch only UNLOCKED achievements
            val achievements = db.collection("users").document(userId)
                .collection("achievements")
                .whereEqualTo("unlocked", true)
                .get().await()

            // Series únicas
            val seriesSet = cars.documents
                .mapNotNull { it.getString("serie") }
                .filter { it.isNotBlank() }
                .toSet()

            Result.success(
                mapOf(
                    "cars" to cars.size(),
                    "tags" to tags.size(),
                    "achievements" to achievements.size(),
                    "series" to seriesSet.size
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch public stats: ${e.message}"))
        }
    }

    // ────────────────────────────────────────────────
//   GET PUBLIC RECENT CARS (MAX 10)
// ────────────────────────────────────────────────
    suspend fun getPublicRecentCars(userId: String): Result<List<Car>> {
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("carsCollection")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()

            val cars = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Car::class.java)?.copy(id = doc.id)
            }

            Result.success(cars)

        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch public recent cars: ${e.message}"))
        }
    }


    // ────────────────────────────────────────────────
//   GET PUBLIC COMMENTS
// ────────────────────────────────────────────────
    suspend fun getPublicComments(userId: String): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("comments")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val comments = snapshot.documents.mapNotNull { it.data }

            Result.success(comments)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch comments: ${e.message}"))
        }
    }

    // ────────────────────────────────────────────────
//   ADD COMMENT TO PUBLIC PROFILE
// ────────────────────────────────────────────────
    suspend fun addCommentToUser(
        targetUserId: String,
        authorId: String,
        authorName: String,
        text: String
    ): Result<Unit> {
        return try {
            val commentData = mapOf(
                "authorId" to authorId,
                "authorName" to authorName,
                "text" to text,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("users")
                .document(targetUserId)
                .collection("comments")
                .add(commentData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add comment: ${e.message}"))
        }
    }

    suspend fun getAllPublicUsers(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = db.collection("users")
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null

                // Fetch actual counts from subcollections
                val carsCount = try {
                    db.collection("users").document(doc.id)
                        .collection("carsCollection")
                        .get()
                        .await()
                        .size()
                } catch (e: Exception) {
                    0
                }

                val achievementsCount = try {
                    db.collection("users").document(doc.id)
                        .collection("achievements")
                        .whereEqualTo("unlocked", true)
                        .get()
                        .await()
                        .size()
                } catch (e: Exception) {
                    0
                }

                mapOf(
                    "id" to doc.id,
                    "username" to (data["username"] ?: ""),
                    "photoUrl" to (data["photoUrl"] ?: ""),
                    "level" to (data["level"] ?: 1),
                    "totalXP" to (data["totalXP"] ?: 0L),
                    "carsCount" to carsCount,
                    "achievementsCount" to achievementsCount
                )
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchPublicUserCars(userId: String): Result<List<Car>> {
        return try {
            // First try with orderBy, if it fails OR returns 0, fetch without ordering
            var snapshot = try {
                db.collection("users")
                    .document(userId)
                    .collection("carsCollection")
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
            } catch (orderError: Exception) {
                // If orderBy fails (missing index or field), fetch all without ordering
                db.collection("users")
                    .document(userId)
                    .collection("carsCollection")
                    .get()
                    .await()
            }

            // If orderBy returned 0 but collection might have data, try without orderBy
            if (snapshot.isEmpty) {
                snapshot = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")
                    .get()
                    .await()
            }

            val cars = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Car::class.java)?.copy(id = doc.id)
            }

            // Sort in code if we have createdAt values
            val sortedCars = cars.sortedByDescending { it.createdAt ?: 0L }

            Result.success(sortedCars)

        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch public user cars: ${e.message}"))
        }
    }

    // ════════════════════════════════════════════════════════════════
    // SISTEMA DE NIVELES Y XP
    // ════════════════════════════════════════════════════════════════

    /**
     * Calcular XP basado en la calidad del carro
     * Escala desde 100 (Basico) hasta 2000 (máxima rareza)
     */
    fun calculateXPByCarQuality(quality: String?): Int {
        return when (quality?.uppercase()) {
            "BASICO" -> 100
            "STANDARD" -> 150
            "TH" -> 250        // Treasure Hunt
            "STH" -> 500       // Super Treasure Hunt
            "RLC" -> 1500      // Real Riders/Collector Edition
            "SPECIAL" -> 2000  // Máxima rareza
            else -> 100        // Default
        }
    }

    /**
     * Agregar XP al usuario actual
     * @param amount Cantidad de XP a agregar
     * @param source Fuente de la XP (CAR_ADDED, ACHIEVEMENT_UNLOCKED)
     * @param sourceId ID del objeto relacionado (carro, logro, etc.)
     */
    suspend fun addXP(amount: Int, source: XPSource, sourceId: String? = null): Result<User> {
        val firebaseUser = auth.currentUser
            ?: return Result.failure(Exception("No user logged in"))

        val userId = firebaseUser.uid

        return try {
            val userDocRef = db.collection("users").document(userId)

            // Usar transacción para garantizar consistencia
            val updatedUser = db.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)
                val currentUser = snapshot.toObject(User::class.java)
                    ?: throw Exception("User not found")

                // Calcular nueva XP
                val newTotalXP = currentUser.totalXP + amount
                val newXPFromCars = if (source == XPSource.CAR_ADDED) {
                    currentUser.xpFromCars + amount
                } else {
                    currentUser.xpFromCars
                }
                val newXPFromAchievements = if (source == XPSource.ACHIEVEMENT_UNLOCKED) {
                    currentUser.xpFromAchievements + amount
                } else {
                    currentUser.xpFromAchievements
                }

                // Calcular nuevo nivel
                val newLevel = User.calculateLevelFromXP(newTotalXP)
                val leveledUp = newLevel > currentUser.level

                // Actualizar campos
                val updates = mapOf(
                    "totalXP" to newTotalXP,
                    "level" to newLevel,
                    "xpFromCars" to newXPFromCars,
                    "xpFromAchievements" to newXPFromAchievements
                )

                transaction.update(userDocRef, updates)

                // Registrar actividad de XP
                val xpActivity = XPActivity(
                    userId = userId,
                    amount = amount,
                    source = source.name,
                    sourceId = sourceId,
                    timestamp = System.currentTimeMillis(),
                    levelBefore = currentUser.level,
                    levelAfter = newLevel
                )

                // Guardar en subcollection (sin bloquear la transacción)
                val activityRef = userDocRef.collection("xpHistory").document()
                transaction.set(activityRef, xpActivity)

                // Retornar usuario actualizado
                currentUser.copy(
                    uid = userId,
                    totalXP = newTotalXP,
                    level = newLevel,
                    xpFromCars = newXPFromCars,
                    xpFromAchievements = newXPFromAchievements
                )
            }.await()

            Result.success(updatedUser)

        } catch (e: Exception) {
            Result.failure(Exception("Failed to add XP: ${e.message}"))
        }
    }

    /**
     * Obtener historial de XP del usuario actual
     */
    suspend fun getXPHistory(limit: Int = 50): Result<List<XPActivity>> {
        val firebaseUser = auth.currentUser
            ?: return Result.failure(Exception("No user logged in"))

        val userId = firebaseUser.uid

        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("xpHistory")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val history = snapshot.documents.mapNotNull {
                it.toObject(XPActivity::class.java)?.copy(id = it.id)
            }

            Result.success(history)

        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch XP history: ${e.message}"))
        }
    }

    /**
     * Migración retroactiva: Calcular y asignar XP a usuarios existentes
     * basado en sus carros y logros actuales
     *
     * Calcula la diferencia y AGREGA solo lo faltante, no sobrescribe
     */
    suspend fun migrateUserXP(): Result<User> {
        val firebaseUser = auth.currentUser
            ?: return Result.failure(Exception("No user logged in"))

        val userId = firebaseUser.uid

        return try {
            val userDocRef = db.collection("users").document(userId)
            val userSnapshot = userDocRef.get().await()
            val currentUser = userSnapshot.toObject(User::class.java)
                ?: throw Exception("User not found")

            // Obtener todos los carros con sus calidades
            val carsSnapshot = db.collection("users")
                .document(userId)
                .collection("carsCollection")
                .get()
                .await()

            // Calcular XP total de carros basado en calidad
            var xpFromCars = 0
            for (carDoc in carsSnapshot.documents) {
                val quality = carDoc.getString("quality")
                val carXP = calculateXPByCarQuality(quality)
                xpFromCars += carXP
            }

            // Obtener todos los logros desbloqueados con sus rarezas
            val achievementsSnapshot = db.collection("users")
                .document(userId)
                .collection("achievements")
                .whereEqualTo("unlocked", true)
                .get()
                .await()

            // Calcular XP total de logros basado en rareza
            var xpFromAchievements = 0
            for (achievementDoc in achievementsSnapshot.documents) {
                val achievementId = achievementDoc.id
                // Obtener el logro global para conocer su rareza
                try {
                    val globalAchievement = db.collection("achievements")
                        .document(achievementId)
                        .get()
                        .await()
                        .toObject(AchievementGlobal::class.java)

                    if (globalAchievement != null) {
                        val xpAmount = when (globalAchievement.rarity) {
                            AchievementRarity.COMUN -> 200
                            AchievementRarity.RARO -> 400
                            AchievementRarity.LEGENDARIO -> 800
                            AchievementRarity.SPECIAL -> 1200
                        }
                        xpFromAchievements += xpAmount
                    }
                } catch (e: Exception) {
                    // Si no encontramos el logro global, usar default
                    xpFromAchievements += 200
                }
            }

            // Calcular XP total que DEBERÍA tener
            val calculatedTotalXP = xpFromCars + xpFromAchievements

            // 🔹 FIX: Usar maxOf para nunca perder XP
            // Si XP calculada es mayor que la actual, usar calculada
            // Si XP actual es mayor, mantener la actual (nunca bajar XP)
            val newTotalXP = maxOf(currentUser.totalXP, calculatedTotalXP.toLong())
            val newLevel = User.calculateLevelFromXP(newTotalXP)

            // Actualizar usuario
            val updates = mapOf(
                "totalXP" to newTotalXP,
                "level" to newLevel,
                "xpFromCars" to xpFromCars.toLong(),
                "xpFromAchievements" to xpFromAchievements.toLong()
            )

            userDocRef.update(updates).await()

            // Obtener usuario actualizado
            val updatedSnapshot = userDocRef.get().await()
            val updatedUser = updatedSnapshot.toObject(User::class.java)
                ?: throw Exception("Failed to fetch updated user")

            Result.success(updatedUser.copy(uid = userId))

        } catch (e: Exception) {
            Result.failure(Exception("Failed to migrate XP: ${e.message}"))
        }
    }

    /**
     * Verificar si el usuario necesita migración/recalculación de XP
     * Ahora SIEMPRE recalcula la XP si tiene carros o logros, independiente de si ya tiene XP
     * (para corregir cualquier discrepancia en el cálculo)
     */
    suspend fun needsXPMigration(): Result<Boolean> {
        val firebaseUser = auth.currentUser
            ?: return Result.failure(Exception("No user logged in"))

        val userId = firebaseUser.uid

        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java)

            if (user == null) {
                return Result.success(false)
            }


            // Verificar si tiene carros o logros
            val carsCount = db.collection("users")
                .document(userId)
                .collection("carsCollection")
                .limit(1)
                .get()
                .await()
                .size()

            val achievementsCount = db.collection("users")
                .document(userId)
                .collection("achievements")
                .whereEqualTo("unlocked", true)
                .limit(1)
                .get()
                .await()
                .size()

            // Necesita migración si tiene carros o logros pero XP = 0
            Result.success(carsCount > 0 || achievementsCount > 0)

        } catch (e: Exception) {
            Result.failure(Exception("Failed to check migration status: ${e.message}"))
        }
    }

    /**
     * Obtener lista de todos los usuarios para seleccionar en dropdowns
     * Retorna pares de (uid, username/email)
     */
    suspend fun getAllUsers(): Result<List<Pair<String, String>>> {
        return try {
            val usersSnapshot = db.collection("users").get().await()
            val users = usersSnapshot.documents.mapNotNull { doc ->
                val user = doc.toObject(User::class.java)
                val uid = doc.id
                val displayName = user?.username ?: user?.email ?: uid
                uid to displayName
            }
            Result.success(users.sortedBy { it.second })
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch users: ${e.message}"))
        }
    }

}