package com.example.carcollection.featureuser.data

import android.net.Uri
import androidx.core.net.toUri
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
        email: String
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
                "photoUrl" to photoUrl
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



}