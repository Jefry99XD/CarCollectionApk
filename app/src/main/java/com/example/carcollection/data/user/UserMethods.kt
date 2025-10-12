package com.example.carcollection.data.user

import android.net.Uri
import com.example.carcollection.data.car.Car
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
                        setPhotoUri(Uri.parse(photoUrl))
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
    suspend fun addCarToCollection(car: Car): Result<String> { // Returns the Firestore document ID of the new car
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                // Get a reference to the specific user's carsCollection subcollection
                val carsCollectionRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection") // Make sure this matches your subcollection name!

                // Add the Car object directly. Firestore will auto-generate a document ID.
                val documentReference = carsCollectionRef.add(car).await()

                println("Car '${car.name}' added to user $userId with ID: ${documentReference.id}")
                Result.success(documentReference.id) // Return the ID of the newly added car

            } catch (e: Exception) {
                Result.failure(Exception("Failed to add car: ${e.message}"))
            }
        } else {
            // No user is currently logged in
            Result.failure(Exception("No user logged in to add car to collection."))
        }
    }
}