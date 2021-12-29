import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
// import 'package:flutter_cm_sensor_recorder/flutter_cm_sensor_recorder.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_cm_sensor_recorder');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  /*
  test('getPlatformVersion', () async {
    expect(
        await FlutterCmSensorRecorder.isAccelerometerRecordingAvailable, true);
  });
  */
}
