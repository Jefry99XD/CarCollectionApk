package com.example.carcollection.featureuser.data

import android.net.Uri
import androidx.core.net.toUri
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featureuser.domain.User
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
            println("Firebase Auth profile updated for UID: $uid")


            // 3. Create the user document in Cloud Firestore
            // Use the User data class to ensure consistent structure
            val userFirestoreData = User(
                uid = uid,
                username = username,
                email = email,
                photoUrl = photoUrl,
                createdAt = System.currentTimeMillis() // Set the timestamp
            )

            db.collection("users")
                .document(uid) // Use the Auth UID as the document ID
                .set(userFirestoreData) // Store the User data class directly
                .await()

            println("User document for UID: $uid saved to Firestore.")

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
            val friendsCount = db.collection("users").document(userId)
                .collection("friends").get().await().size()

            // Para series diferentes (asumiendo que los autos tienen un campo "series")
            val cars = db.collection("users").document(userId)
                .collection("carsCollection").get().await()
            val seriesSet = cars.documents.mapNotNull { it.getString("series") }.toSet()

            Result.success(
                mapOf(
                    "cars" to carsCount,
                    "tags" to tagsCount,
                    "friends" to friendsCount,
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

            val friends = db.collection("users").document(userId)
                .collection("friends").get().await()

            // Series únicas
            val seriesSet = cars.documents.mapNotNull { it.getString("serie") }.toSet()

            Result.success(
                mapOf(
                    "cars" to cars.size(),
                    "tags" to tags.size(),
                    "achievements" to achievements.size(),
                    "friends" to friends.size(),
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






}