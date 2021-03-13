package com.delay76.flutter_cm_sensor_recorder

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import androidx.core.app.NotificationCompat
import java.util.concurrent.TimeUnit


class ForegroundService : Service() {
    private var recording: Boolean = false
    private var sensorManager: SensorManager? = null
    private var listener: StreamHandlerImpl? = null
    private var db: SensorDataDatabase? = null
    private var handler: Handler = Handler()
    companion object {
        @JvmStatic
        val ACTION_SHUTDOWN = "SHUTDOWN"
        @JvmStatic
        val ACTION_START = "START"
        @JvmStatic
        val WAKELOCK_TAG = "FlutterCmSensorRecorderPlugin:Wakelock"
        @JvmStatic
        val CHANNEL_ID = "flutter_cm_sensor_recorder"
        @JvmStatic
        private val TAG = "ForegroundService"
    }

    override fun onBind(intent: Intent) : IBinder? {
        return null;
    }

    @SuppressLint("WakelockTimeout")
    override fun onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    CHANNEL_ID,
                    FlutterCmSensorRecorderPlugin.notificationTitle,
                    FlutterCmSensorRecorderPlugin.notificationImportance ?: NotificationCompat.PRIORITY_DEFAULT).apply {
                description = FlutterCmSensorRecorderPlugin.notificationText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val imageId = resources.getIdentifier("ic_launcher", "mipmap", packageName)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(FlutterCmSensorRecorderPlugin.notificationTitle)
                .setContentText(FlutterCmSensorRecorderPlugin.notificationText)
                .setSmallIcon(imageId)
                .setPriority(FlutterCmSensorRecorderPlugin.notificationImportance ?: NotificationCompat.PRIORITY_DEFAULT)
                .build()

        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG).apply {
                setReferenceCounted(false)
                acquire()
            }
        }
        startForeground(1, notification)

        super.onCreate()
    }

    fun cleanAndStop() {
        handler.removeCallbacksAndMessages(null)
        db?.SensorDataDatabaseDao?.deleteOlderThan12h()
        if (db?.isOpen == true) {
            db?.close()
        }
        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG).apply {
                if (isHeld) {
                    release()
                }
            }
        }
        listener?.stopListening()
        recording = false
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int) : Int {
        if (intent.action == ACTION_SHUTDOWN) {
            cleanAndStop()
        } else {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                                  cleanAndStop()
            },
                intent.getIntExtra("duration", 60).toLong() * 1000L)
            db = SensorDataDatabase.getInstance(this)
            db!!.SensorDataDatabaseDao.deleteOlderThan12h()
            if (!recording) {
                recording = true
                sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
                listener = StreamHandlerImpl(sensorManager!!, Sensor.TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_NORMAL, db!!)
                listener?.startListening()
            }
        }
        return START_STICKY;
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent);
//        stopSelf();
    }
}

class StreamHandlerImpl(private val sensorManager: SensorManager, sensorType: Int,
                        private var interval: Int = SensorManager.SENSOR_DELAY_NORMAL,
                        private var db: SensorDataDatabase
) :
        SensorEventListener {
    private val sensor = sensorManager.getDefaultSensor(sensorType)
    private var eventTimeOffsetComputed: Long? = null
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    /**
     * Calculates the static offset (ms) which needs to
     * be added to the `event.time` (ns) to get the Unix
     * timestamp of the event.
     *
     * @param eventTimeNanos the `SensorEvent.time` to be used to determine the time offset
     * @return the offset in ms
     */
    private fun eventTimeOffset(eventTimeNanos: Long): Long {
        // Capture timestamps of event reporting time
        val elapsedRealTimeMillis = SystemClock.elapsedRealtime()
        val upTimeMillis = SystemClock.uptimeMillis()
        val currentTimeMillis = System.currentTimeMillis()

        // Check which timestamp the event.time is closest to the event.time
        val eventTimeMillis = eventTimeNanos / 1000000L
        val elapsedTimeDiff = elapsedRealTimeMillis - eventTimeMillis
        val upTimeDiff = upTimeMillis - eventTimeMillis
        val currentTimeDiff = currentTimeMillis - eventTimeMillis

        // Default case (elapsedRealTime, following the documentation)
        if (Math.abs(elapsedTimeDiff) <= Math.min(
                Math.abs(upTimeDiff),
                Math.abs(currentTimeDiff)
            )
        ) {
            return (currentTimeMillis - elapsedRealTimeMillis)
        }

        // Other seen case (currentTime, e.g. Nexus 4)
        if (Math.abs(currentTimeDiff) <= Math.abs(upTimeDiff)) {
            return 0
        }
        throw IllegalStateException("The event.time seems to be upTime. In this case we cannot use a static offset to calculate the Unix timestamp of the event")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (eventTimeOffsetComputed == null) {
            eventTimeOffsetComputed = eventTimeOffset(event!!.timestamp)
        }
        val e = event!!
        val value = SensorData(
                timestamp = TimeUnit.MILLISECONDS.convert(e.timestamp, TimeUnit.NANOSECONDS) + eventTimeOffsetComputed!!,
                accelerationX = e.values[0],
                accelerationY = e.values[1],
                accelerationZ = e.values[2]
        )
        db.SensorDataDatabaseDao.insert(value)
    }

    fun setUpdateInterval(interval: Int) {
        this.interval = interval
        sensorManager.unregisterListener(this)
        sensorManager.registerListener(this, sensor, interval)
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }
    fun startListening() {
        sensorManager.registerListener(this, sensor, interval)
    }
}