import 'dart:async';

import 'package:flutter/services.dart';
import 'package:permission/permission.dart';
import 'package:permission/permission.dart' as prefix0;

class GraphhooperRoute {
  static const MethodChannel _channel =
      const MethodChannel('graphhooper_route');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String> getRouteAsLatLng(points, pathToMap) async {
    String route;
    await Permission.getPermissionsStatus([PermissionName.Storage])
        .then((onValue) async {
      if (!(onValue[0].permissionStatus.toString() ==
          "PermissionStatus.allow")) {
        await Permission.requestPermissions([PermissionName.Storage]);
      }
    });
    route = await _channel.invokeMethod('getRoutes',
        <String, dynamic>{'points': points, 'path_to_map': pathToMap});
    return route;
  }
}
