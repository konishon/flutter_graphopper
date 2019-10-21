import Flutter
import UIKit

public class SwiftGraphhooperRoutePlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "graphhooper_route", binaryMessenger: registrar.messenger())
    let instance = SwiftGraphhooperRoutePlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
