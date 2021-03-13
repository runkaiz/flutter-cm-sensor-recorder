
import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_cm_sensor_recorder/methods.dart';
import 'package:flutter_cm_sensor_recorder/types.dart';

class FlutterCmSensorRecorder {
  static const MethodChannel _channel =
      const MethodChannel('flutter_cm_sensor_recorder');

  static Future<bool> get isAccelerometerRecordingAvailable async {
    return await _channel.invokeMethod(Methods.isAccelerometerRecordingAvailable.toStringValue());
  }

  static Future<void> recordAccelerometer({
    @required Duration duration,
    String notificationText = 'In background',
    String notificationTitle = 'Recording accelerometer'
  }) async {
    return await _channel.invokeMethod(Methods.recordAccelerometer.toStringValue(), {
      'duration': duration.inSeconds,
      'notification_text': notificationText,
      'notification_title': notificationTitle
    });
  }

  static Future<void> stopRecording() async {
    return await _channel.invokeMethod(Methods.stopRecording.toStringValue());
  }

  static Future<Iterable<AccelerometerData>> accelerometerData({
    @required DateTime from,
    @required DateTime to
  }) async {
    final Map<String, String> params = {
      'from': from.toUtc().toIso8601String(),
      'to': to.toUtc().toIso8601String()
    };
    final response = await _channel.invokeMethod<List<dynamic>>(Methods.accelerometerData.toStringValue(), params);
    return response.length > 0 ? response.map((e) => AccelerometerData.fromMap(e)) : [];
  }
}
