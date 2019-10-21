import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:graphhooper_route/graphhooper_route.dart';

void main() {
  const MethodChannel channel = MethodChannel('graphhooper_route');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await GraphhooperRoute.platformVersion, '42');
  });
}
