import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:graphhooper_route/graphhooper_route.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _latLngDataPoints = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String latLngDataPoints;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      latLngDataPoints = await GraphhooperRoute.getRouteAsLatLng(
          [27.7297, 85.3290, 27.7006, 85.3120], "");
    } on PlatformException {
      latLngDataPoints = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _latLngDataPoints = latLngDataPoints;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Response: $_latLngDataPoints\n'),
        ),
      ),
    );
  }
}
