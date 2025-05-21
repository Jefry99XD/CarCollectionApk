package com.example.carcollection.data.repository

import com.example.carcollection.data.local.Car
import com.example.carcollection.data.local.CarDao
import kotlinx.coroutines.flow.Flow

class CarRepository(private val dao: CarDao) {

    fun getAllCars(): Flow<List<Car>> = dao.getAllCars()

    fun searchCars(query: String): Flow<List<Car>> = dao.searchCarsByName(query)

    suspend fun insertCar(car: Car) = dao.insertCar(car)

    suspend fun updateCar(car: Car) = dao.updateCar(car)

    suspend fun deleteCar(car: Car) = dao.deleteCar(car)
    suspend fun getCarById(id: Int): Car? {
        return dao.getCarById(id)
    }

}
