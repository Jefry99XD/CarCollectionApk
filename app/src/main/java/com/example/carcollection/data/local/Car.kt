package com.example.carcollection.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class Car(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val brand: String,
    val name: String,
    val serie: String,
    val year: String,
    val photoUrl: String,
    val color: String,
    val type: String,
    val tags: List<String> = emptyList()
)
