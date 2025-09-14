package com.example.carcollection.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Query("SELECT * FROM cars WHERE tags LIKE '%' || :tagName || '%'")
    suspend fun getCarsByTag(tagName: String): List<Car>


    @Query("SELECT DISTINCT year FROM cars ORDER BY year ASC")
    fun getDistinctYears(): Flow<List<String>>

    @Query("SELECT DISTINCT brand FROM cars ORDER BY brand ASC")
    fun getDistinctBrands(): Flow<List<String>>

    @Query("SELECT DISTINCT serie FROM cars ORDER BY serie ASC")
    fun getDistinctSeries(): Flow<List<String>>

    @Query("SELECT DISTINCT type FROM cars ORDER BY type ASC")
    fun getDistinctTypes(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM cars")
    fun getCarCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM cars")
    suspend fun getCarCountOnce(): Int

}
