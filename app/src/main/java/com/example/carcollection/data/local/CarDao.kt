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
}
