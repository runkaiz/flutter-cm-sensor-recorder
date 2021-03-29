import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_cm_sensor_recorder/flutter_cm_sensor_recorder.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool _recordingAvailable = false;
  int _dataPoints = 0;

  @override
  void initState() {
    super.initState();
    checkRecordingAvailability();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> checkRecordingAvailability() async {
    bool recordingAvailable;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      recordingAvailable =
          await FlutterCmSensorRecorder.isAccelerometerRecordingAvailable;
    } on PlatformException {
      recordingAvailable = false;
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _recordingAvailable = recordingAvailable;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Flutter CM Sensor Recorder'),
        ),
        body: Column(
          children: [
            Center(
              child: Text('Recording available: $_recordingAvailable\n'),
            ),
            Center(
              child: Text('Data points: $_dataPoints\n'),
            ),
            MaterialButton(
              onPressed: () async {
                await FlutterCmSensorRecorder.recordAccelerometer(
                    duration: Duration(hours: 2));
              },
              child: Text('Start recording for 2 hours'),
            ),
            MaterialButton(
              onPressed: () async {
                await FlutterCmSensorRecorder.stopRecording();
              },
              child: Text('Stop recording'),
            ),
            MaterialButton(
              onPressed: () async {
                final points = await FlutterCmSensorRecorder.accelerometerData(
                    from: DateTime.now().subtract(Duration(hours: 2)),
                    to: DateTime.now());
                setState(() {
                  _dataPoints = points.length;
                });
              },
              child: Text('Read data from recorder'),
            )
          ],
        ),
      ),
    );
  }
}
