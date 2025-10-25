package com.example.carcollection.featurecar.data;


import com.example.carcollection.featurecar.domain.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CarMethods {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun addCarToCollection(car:Car): Result<String> { // Returns the Firestore document ID of the new car
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

    suspend fun syncLocalCarsToFirebase(localCars: List<Car>): Result<Pair<Int, Int>> {
        var successCount = 0
        var errorCount = 0
        for (car in localCars) {
            val result = addCarToCollection(car)
            if (result.isSuccess) {
                successCount++
            } else {
                errorCount++
            }
        }
        return Result.success(Pair(successCount, errorCount))
    }

    suspend fun deleteCarFromCollection(carId: String): Result<Unit> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val carDocRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")
                    .document(carId)
                carDocRef.delete().await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to delete car: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to delete car from collection."))
        }
    }

    suspend fun updateCarInCollection(carId: String, updatedCar: Car): Result<Unit> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val carDocRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")
                    .document(carId)
                carDocRef.set(updatedCar).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to update car: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to update car in collection."))
        }
    }


    suspend fun getUserCars(): Result<List<Car>> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val carsCollectionRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")
                val querySnapshot = carsCollectionRef.get().await()
                val cars = querySnapshot.documents.mapNotNull { it.toObject(Car::class.java) }
                Result.success(cars)
            } catch (e: Exception) {
                Result.failure(Exception("Error fetching cars: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user currently logged in."))
        }
    }

    suspend fun getCarById(carId: String): Result<Car> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val carDocRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")
                    .document(carId)
                val documentSnapshot = carDocRef.get().await()
                if (documentSnapshot.exists()) {
                    val car = documentSnapshot.toObject(Car::class.java)
                    if (car != null) {
                        Result.success(car)
                    } else {
                        Result.failure(Exception("Failed to parse car data from Firestore."))
                    }
                } else {
                    Result.failure(Exception("Car with ID $carId not found in Firestore."))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Error fetching car by ID: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user currently logged in."))
        }
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
