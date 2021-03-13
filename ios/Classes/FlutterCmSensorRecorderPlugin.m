#import "FlutterCmSensorRecorderPlugin.h"
#if __has_include(<flutter_cm_sensor_recorder/flutter_cm_sensor_recorder-Swift.h>)
#import <flutter_cm_sensor_recorder/flutter_cm_sensor_recorder-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_cm_sensor_recorder-Swift.h"
#endif

@implementation FlutterCmSensorRecorderPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterCmSensorRecorderPlugin registerWithRegistrar:registrar];
}
@end
