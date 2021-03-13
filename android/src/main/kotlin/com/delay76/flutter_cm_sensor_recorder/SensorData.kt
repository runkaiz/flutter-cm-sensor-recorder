package com.delay76.flutter_cm_sensor_recorder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_data_table")
data class SensorData(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,
//        timestamp is in milliseconds
        @ColumnInfo(name = "timestamp")
        var timestamp: Long,
        @ColumnInfo(name = "acceleration_x")
        var accelerationX: Float,
        @ColumnInfo(name = "acceleration_y")
        var accelerationY: Float,
        @ColumnInfo(name = "acceleration_Z")
        var accelerationZ: Float
)