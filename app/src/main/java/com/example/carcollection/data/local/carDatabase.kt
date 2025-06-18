package com.example.carcollection.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Car::class, Tag::class], version = 1)
@TypeConverters(Converters::class)
abstract class CarDatabase : RoomDatabase() {

    abstract fun carDao(): CarDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: CarDatabase? = null

        fun getDatabase(context: Context): CarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                                context.applicationContext,
                                CarDatabase::class.java,
                                "car_database"
                            ).fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }
}
