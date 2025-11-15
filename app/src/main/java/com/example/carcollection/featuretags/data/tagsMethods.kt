package com.example.carcollection.featuretags.data

import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featuretags.domain.Tag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TagsMethods {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun addTag(name: String, color: String){
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val tag = Tag(name = name, color = color)
            db.collection("users").document(firebaseUser.uid).collection("tags")
                .add(tag)
        }
    }

     fun deleteTag(tagId: String){
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.uid).collection("tags")
                .document(tagId)
                .delete()
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
        }
    }

    suspend fun getAllTags(): List<Tag> {
        val firebaseUser = auth.currentUser
        val tags = mutableListOf<Tag>()
        if (firebaseUser != null) {
            val querySnapshot = db.collection("users").document(firebaseUser.uid).collection("tags")
                .get()
                .await()
            for (document in querySnapshot.documents) {
                val tag = document.toObject(Tag::class.java)
                tag?.id = document.id
                if (tag != null) {
                    tags.add(tag)
                }
            }
        }
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