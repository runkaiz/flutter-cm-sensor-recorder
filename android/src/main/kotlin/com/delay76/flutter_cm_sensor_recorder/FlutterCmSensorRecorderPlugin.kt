package com.delay76.flutter_cm_sensor_recorder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import org.joda.time.Instant
import org.joda.time.format.ISODateTimeFormat

enum class METHODS{
  isAccelerometerRecordingAvailable,
  recordAccelerometer,
  accelerometerData,
  stopRecording
}

/** FlutterCmSensorRecorderPlugin */
class FlutterCmSensorRecorderPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private var methodChannel : MethodChannel? = null
  private var activity: Activity? = null
  private var context: Context? = null


  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "flutter_cm_sensor_recorder")
      channel.setMethodCallHandler(FlutterCmSensorRecorderPlugin())
    }

    @JvmStatic
    var notificationTitle: String? = "Flutter CM Sensor Recorder Plugin"
    @JvmStatic
    var notificationText: String? = "Collecting accelerometer data"
    @JvmStatic
    var notificationImportance: Int? = NotificationCompat.PRIORITY_DEFAULT
  }
  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when(call.method) {
      METHODS.isAccelerometerRecordingAvailable.name -> result.success(true)
      METHODS.recordAccelerometer.name -> {
        FlutterCmSensorRecorderPlugin.notificationTitle = call.argument<String>("notification_title")
        FlutterCmSensorRecorderPlugin.notificationText = call.argument<String>("notification_text")
        val duration = call.argument<Int>("duration")
        val intent = Intent(context, ForegroundService::class.java)
        intent.putExtra("duration", duration)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
          context!!.startForegroundService(intent)
        } else {
          context!!.startService(intent)
        }
        result.success(null)
      }
      METHODS.stopRecording.name -> {
        val intent = Intent(context!!, ForegroundService::class.java)
        intent.action = ForegroundService.ACTION_SHUTDOWN
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
          context!!.startForegroundService(intent)
        } else {
          context!!.startService(intent)
        }
        result.success(null)
      }
      METHODS.accelerometerData.name -> {
        val parser = ISODateTimeFormat.dateTimeParser()
        val formatter = ISODateTimeFormat.dateHourMinuteSecondFraction()
        val from = parser.parseDateTime(call.argument<String>("from"))
        val to = parser.parseDateTime(call.argument<String>("to"))
        val db = SensorDataDatabase.getInstance(context!!)
        val data:List<SensorData> = db.SensorDataDatabaseDao.getBetween( from.millis, to.millis)
//        val data:List<SensorData> = db.SensorDataDatabaseDao.getAll()
        val res:List<Map<String, Any>> = data.map { e -> mapOf(
          "timestamp" to Instant(e.timestamp).toString(),
          "acceleration" to mapOf<String, Float>(
            "x" to e.accelerationX,
            "y" to e.accelerationY,
            "z" to e.accelerationZ
          )
        ) }
        db.close()
        result.success(res)
      }
      else -> result.notImplemented()
    }
  }
  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    startListening(binding.applicationContext, binding.binaryMessenger)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    stopListening()
  }

  override fun onDetachedFromActivity() {
    stopListeningToActivity()
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(binding)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    startListeningToActivity(
            binding.activity,
            binding::addActivityResultListener,
            binding::addRequestPermissionsResultListener)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity()
  }

  private fun startListening(applicationContext: Context, messenger: BinaryMessenger) {
    methodChannel = MethodChannel(
            messenger,
            "flutter_cm_sensor_recorder"
    )
    methodChannel!!.setMethodCallHandler(this)
    context = applicationContext
  }

  private fun stopListening() {
    methodChannel!!.setMethodCallHandler(null)
    methodChannel = null
    context = null
  }

  private fun startListeningToActivity(
          activity: Activity,
          addActivityResultListener: ((PluginRegistry.ActivityResultListener) -> Unit),
          addRequestPermissionResultListener: ((PluginRegistry.RequestPermissionsResultListener) -> Unit)
  ) {
    this.activity = activity
  }

  private fun stopListeningToActivity() {
    this.activity = null
  }
}
