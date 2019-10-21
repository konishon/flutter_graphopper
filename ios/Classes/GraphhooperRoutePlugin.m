#import "GraphhooperRoutePlugin.h"
#import <graphhooper_route/graphhooper_route-Swift.h>

@implementation GraphhooperRoutePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftGraphhooperRoutePlugin registerWithRegistrar:registrar];
}
@end
