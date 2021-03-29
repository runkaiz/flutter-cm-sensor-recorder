

# :arrow_up: Flutter CM Sensor Recorder
 This plugin allows to use the CoreMotion Sensor Recorder features of iOS in your Flutter mobile app on both iOS and Android.

 Because of the lack of OS-specific support for recording raw accelerometer data in background on Android, the Android implementation uses a Foreground service.


## Requesting permissions
Additionally, you will have to manage the permission request. If you choose to go with permission [permission_handler](https://pub.dev/packages/permission_handler) make sure to follow their installation steps as well.

## Units
The only difference compared to the iOS native implementation lays in the fact that this plugin will give you accelerations in m/s² instead of Apple's G.

## Usage  
You can check the example for a rather complete implementation and you can always refer the official iOS documentation, this plugin behaves in the same way.
