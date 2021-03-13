package com.delay76.flutter_cm_sensor_recorder

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SensorDataDao {
    @Insert
    fun insert(data: SensorData)
    @Query("SELECT * from sensor_data_table WHERE timestamp >= :since AND timestamp <= :to")
    fun getBetween(since: Long, to: Long): List<SensorData>

    @Query("SELECT * from sensor_data_table")
    fun getAll(): List<SensorData>

    @Query("DELETE from sensor_data_table WHERE timestamp <= (strftime('%s','now') - 12*60*60) * 1000")
    fun deleteOlderThan12h()

    @Query("DELETE FROM sensor_data_table")
    fun clear()
}
