package com.example.carcollection.featuretags.data

import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featuretags.domain.Tag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TagsMethods {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ─────────────────────────────────────────────────────────────────────────
    // In-memory cache — shared via companion object across all TagsMethods instances
    // ─────────────────────────────────────────────────────────────────────────
    companion object {
        /** Cached tags per userId  */
        @Volatile private var cachedTags: List<Tag> = emptyList()
        @Volatile private var cachedUserId: String? = null
        @Volatile private var cacheTimestamp: Long = 0L
        private const val CACHE_TTL_MS = 5 * 60 * 1000L // 5 minutes

        fun invalidateCache() {
            cachedTags = emptyList()
            cachedUserId = null
            cacheTimestamp = 0L
        }

        fun isCacheValid(userId: String): Boolean {
            val age = System.currentTimeMillis() - cacheTimestamp
            return cachedUserId == userId && cachedTags.isNotEmpty() && age < CACHE_TTL_MS
        }
    }

    fun addTag(name: String, color: String){
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val tag = Tag(name = name, color = color)
            db.collection("users").document(firebaseUser.uid).collection("tags")
                .add(tag)
            invalidateCache()
        }
    }

     fun deleteTag(tagId: String){
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.uid).collection("tags")
                .document(tagId)
                .delete()
            invalidateCache()
        }
    }

     fun editTag(tagId: String, name: String, color: String){
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val tagUpdates = mapOf(
                "name" to name,
                "color" to color
            )
            db.collection("users").document(firebaseUser.uid).collection("tags")
                .document(tagId)
                .update(tagUpdates)
            invalidateCache()
        }
    }

    suspend fun getAllTags(): List<Tag> {
        val firebaseUser = auth.currentUser ?: return emptyList()
        val userId = firebaseUser.uid

        // Return cached result if valid
        if (isCacheValid(userId)) return cachedTags

        val tags = mutableListOf<Tag>()
        val querySnapshot = db.collection("users").document(userId).collection("tags")
            .get()
            .await()
        for (document in querySnapshot.documents) {
            val tag = document.toObject(Tag::class.java)
            tag?.id = document.id
            if (tag != null) tags.add(tag)
        }

        // Update cache
        cachedTags = tags
        cachedUserId = userId
        cacheTimestamp = System.currentTimeMillis()

        return tags
    }

    suspend fun getTagById(tagId: String): Tag? {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val documentSnapshot = db.collection("users").document(firebaseUser.uid).collection("tags")
                .document(tagId)
                .get()
                .await()
            val tag = documentSnapshot.toObject(Tag::class.java)
            tag?.id = documentSnapshot.id
            return tag
        }
        return null
    }
    suspend fun updateTagNameInAllCars(oldTagName: String, newTagName: String): Result<Unit> {
        invalidateCache() // tag name changed → invalidate
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val carsCollectionRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")
                val querySnapshot = carsCollectionRef.get().await()
                val batch = db.batch()

                for (document in querySnapshot.documents) {
                    val car = document.toObject(Car::class.java)
                    if (car != null && oldTagName in car.tags) {
                        val updatedTags = car.tags.map { if (it == oldTagName) newTagName else it }
                        val updatedCar = car.copy(tags = updatedTags)
                        val carDocRef = carsCollectionRef.document(document.id)
                        batch.set(carDocRef, updatedCar)
                    }
                }

                batch.commit().await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to update tag names in cars: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to update tags in cars."))
        }
    }


}