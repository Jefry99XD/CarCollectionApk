package com.example.carcollection.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCar(car: Car)

    @Update
    suspend fun updateCar(car: Car)

    @Delete
    suspend fun deleteCar(car: Car)

    @Query("SELECT * FROM cars ORDER BY name ASC")
    fun getAllCars(): Flow<List<Car>>

    @Query("SELECT * FROM cars WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchCarsByName(query: String): Flow<List<Car>>

    @Query("SELECT * FROM cars WHERE id = :id LIMIT 1")
    suspend fun getCarById(id: Int): Car?

    @Query("SELECT * FROM cars ORDER BY name ASC")
    suspend fun getAllCarsList(): List<Car> // nombre diferente al del Flow

    @Query("DELETE FROM cars")
    suspend fun deleteAll()

    @Query("SELECT DISTINCT tags FROM cars")
    suspend fun getAllTags(): List<String>


}
