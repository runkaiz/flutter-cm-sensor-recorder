import 'package:flutter/foundation.dart';

class AccelerometerData {
  final DateTime timeStamp;
  final Acceleration acceleration;
  AccelerometerData({
    this.timeStamp,
    this.acceleration
});
  static AccelerometerData fromMap(map) => AccelerometerData(
    timeStamp: DateTime.parse(map['timestamp']),
    acceleration: Acceleration.fromMap(map['acceleration'])
  );

  @override
  String toString() {
    return 'AccelerometerData{timeStamp: $timeStamp, acceleration: $acceleration}';
  }
}

class Acceleration {
  final double x;
  final double y;
  final double z;
  Acceleration({
    @required this.x,
    @required this.y,
    @required this.z,
});
  static Acceleration fromMap(map) => Acceleration(
    x: map['x'],
    y: map['y'],
    z: map['z'],
  );

  @override
  String toString() {
    return 'Acceleration{x: $x, y: $y, z: $z}';
  }
}