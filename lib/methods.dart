import 'package:flutter/foundation.dart';

enum Methods {
  isAccelerometerRecordingAvailable,
  recordAccelerometer,
  accelerometerData,
  stopRecording
}

extension Tostring on Methods {
  String toStringValue() {
    print(describeEnum(this));
    return describeEnum(this);
  }
}

extension BoolParsing on String {
  bool parseBool() {
    return this.toLowerCase() == 'true';
  }
}
