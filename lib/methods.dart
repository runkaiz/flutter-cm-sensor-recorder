import 'package:flutter/foundation.dart';

enum Methods {
  isAccelerometerRecordingAvailable,
  recordAccelerometer,
  accelerometerData,
  stopRecording
}

extension Tostring on Methods {
  toStringValue() => describeEnum(this);
}