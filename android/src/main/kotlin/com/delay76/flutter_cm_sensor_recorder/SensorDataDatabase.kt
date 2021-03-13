package com.delay76.flutter_cm_sensor_recorder

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SensorData::class], version = 1, exportSchema = false)
abstract class SensorDataDatabase : RoomDatabase(){
    abstract val SensorDataDatabaseDao: SensorDataDao
    companion object {
        @Volatile
        private var INSTANCE: SensorDataDatabase? = null
        fun getInstance(context: Context): SensorDataDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            SensorDataDatabase::class.java,
                            "sensor_data_database")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build()
                }
                return instance
            }
        }
    }
}