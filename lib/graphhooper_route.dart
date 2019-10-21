import 'dart:async';

import 'package:flutter/services.dart';

class GraphhooperRoute {
  static const MethodChannel _channel =
      const MethodChannel('graphhooper_route');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String> getRouteAsLatLng(points, pathToMap) async {
    final String route =
    await _channel.invokeMethod('getRoutes', <String, dynamic>{
      'points': points,
      'path_to_map': pathToMap
    });

    return route;
  }
}
