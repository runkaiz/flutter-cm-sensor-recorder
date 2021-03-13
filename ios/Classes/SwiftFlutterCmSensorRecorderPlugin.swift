import Flutter
import UIKit
import CoreMotion

extension CMSensorDataList: Sequence {
    public typealias Iterator = NSFastEnumerationIterator
    public func makeIterator() -> NSFastEnumerationIterator {
        return NSFastEnumerationIterator(self)
    }
}

public class SwiftFlutterCmSensorRecorderPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_cm_sensor_recorder", binaryMessenger: registrar.messenger())
    let instance = SwiftFlutterCmSensorRecorderPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }
    
    enum Methods: String {
        case isAccelerometerRecordingAvailable,
             recordAccelerometer,
             accelerometerData,
             stopRecording
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch(call.method) {
        case Methods.isAccelerometerRecordingAvailable.rawValue:
            result(CMSensorRecorder.isAccelerometerRecordingAvailable())
    case Methods.recordAccelerometer.rawValue:
        let params = call.arguments as! [String: Any]
        let duration = params["duration"] as! Double
        if(CMSensorRecorder.isAccelerometerRecordingAvailable()){
            let recorder = CMSensorRecorder()
            recorder.recordAccelerometer(forDuration: duration)
            result(nil)
        } else {
            result(FlutterError(code: "recording_unavailable", message: nil, details: nil))
        }
    case Methods.accelerometerData.rawValue:
        let params = call.arguments as! [String: String]
        if(CMSensorRecorder.isAccelerometerRecordingAvailable()){
            let recorder = CMSensorRecorder()
            let isoFormatter = ISO8601DateFormatter()
            isoFormatter.formatOptions = [
                .withFractionalSeconds,
                .withInternetDateTime
            ]
            let from = isoFormatter.date(from: params["from"]!)
            let to = isoFormatter.date(from: params["to"]!)
            let data = recorder.accelerometerData(from: from!, to: to!)
            if (data != nil) {
                let processed = data!.map { (e) -> [String: Any] in
                    if let el = e as? CMRecordedAccelerometerData {
                        var res = [String:Any]()
                        res["timestamp"] = isoFormatter.string(from: el.startDate)
                        var acceleration = [String:Double]()
                        acceleration["x"] = el.acceleration.x * 9.80665
                        acceleration["y"] = el.acceleration.y * 9.80665
                        acceleration["z"] = el.acceleration.z * 9.80665
                        res["acceleration"] = acceleration
                        return res
                    } else {
                        return [String: Any]()
                    }
                }
                result(processed)
            }
            else {
                result([])
            }
        } else {
            result(FlutterError(code: "recording_unavailable", message: nil, details: nil))
        }
    case Methods.stopRecording.rawValue:
        result(nil)
    default:
        result(FlutterMethodNotImplemented)
    }
  }
}
