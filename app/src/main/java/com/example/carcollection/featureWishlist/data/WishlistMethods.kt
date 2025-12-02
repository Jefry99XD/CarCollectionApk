package com.example.carcollection.featureWishlist.data

import com.example.carcollection.featurecar.domain.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WishlistMethods {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Adds a Car to the user's wishlist. Returns the Firestore document ID of the new wishlist item.
    suspend fun addToWishlist(car: Car): Result<String> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val wishlistRef = db.collection("users")
                    .document(userId)
                    .collection("wishlist")

                // Check for duplicates: retrieve existing items and compare all relevant fields (except id)
                val snapshot = wishlistRef.get().await()
                val existing = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Car::class.java)?.copy(id = doc.id)
                }

                val duplicate = existing.firstOrNull { existingCar -> areSameCar(existingCar, car) }
                if (duplicate != null) {
                    return Result.failure(Exception("Item already in wishlist"))
                }

                val docRef = wishlistRef.add(car).await()
                Result.success(docRef.id)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to add to wishlist: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to add to wishlist."))
        }
    }

    // Helper to compare two Car objects ignoring the id field.
    private fun areSameCar(a: Car, b: Car): Boolean {
        fun eq(s1: String?, s2: String?): Boolean = (s1 ?: "").trim().equals((s2 ?: "").trim(), ignoreCase = true)

        if (!eq(a.name, b.name)) return false
        if (!eq(a.brand, b.brand)) return false
        if (!eq(a.year, b.year)) return false
        if (!eq(a.serie, b.serie)) return false
        if (!eq(a.color, b.color)) return false
        if (!eq(a.photoUrl, b.photoUrl)) return false
        if (!eq(a.type, b.type)) return false

        val tagsA = a.tags ?: emptyList()
        val tagsB = b.tags ?: emptyList()
        val normA = tagsA.map { it.trim().lowercase() }.toSet()
        val normB = tagsB.map { it.trim().lowercase() }.toSet()
        if (normA != normB) return false

        return true
    }

    // Removes a wishlist item by its wishlist document ID.
    suspend fun removeFromWishlist(wishlistItemId: String): Result<Unit> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val docRef = db.collection("users")
                    .document(userId)
                    .collection("wishlist")
                    .document(wishlistItemId)
                docRef.delete().await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to remove from wishlist: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to remove from wishlist."))
        }
    }

    // Retrieves the user's wishlist as a list of Car objects (each Car's id is set to the wishlist doc id).
    suspend fun retrieveWishlist(): Result<List<Car>> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val snapshot = db.collection("users")
                    .document(userId)
                    .collection("wishlist")
                    .get()
                    .await()

                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Car::class.java)?.copy(id = doc.id)
                }

                Result.success(list)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to retrieve wishlist: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to retrieve wishlist."))
        }
    }
}