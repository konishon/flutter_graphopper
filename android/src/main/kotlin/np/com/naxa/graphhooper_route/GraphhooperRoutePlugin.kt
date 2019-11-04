package np.com.naxa.graphhooper_route

import android.annotation.SuppressLint
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import com.google.gson.GsonBuilder
import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import com.graphhopper.util.Parameters
import com.graphhopper.util.StopWatch
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.io.File
import java.lang.Exception

class GraphhooperRoutePlugin : MethodCallHandler {

    companion object {
        private lateinit var locationManager: LocationManager

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "graphhooper_route")
            channel.setMethodCallHandler(GraphhooperRoutePlugin())
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        } else if (call.method.equals("getRoutes")) {
            val mapPath = call.argument<String>("path_to_map")
            val points = call.argument<List<Double>>("points")
            CalcPath(result, mapPath).execute(points?.get(0), points?.get(1), points?.get(2), points?.get(3))
        } else {
            result.notImplemented()
        }
    }

    @SuppressLint("NewApi")
    class CalcPath() : AsyncTask<Double, Void, String>() {

        internal var time: Float = 0.toFloat()

        var result: Result? = null
        var mapPath: String? = null

        constructor(result: Result, mapPath: String?) : this() {
            this.result = result
            this.mapPath = mapPath
        }

        private fun ifMapFileExists(): Boolean {
            return getMapFolder().exists()
        }

        private fun downloadMapFromRemoteSource(): String {
            return "Map file is not found"
        }

        override fun doInBackground(vararg params: Double?): String? {

            try {
                if(ifMapFileExists()) {
                    val sw = StopWatch().start()
                    val hopper = GraphHopper().forMobile()
                    hopper.load(File(getMapFolder(), "nepal").getAbsolutePath() + "-gh")
                    val req = GHRequest(params[0]!!, params[1]!!, params[2]!!, params[3]!!)
                            .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)

                    val hints = req.getHints().put(Parameters.Routing.INSTRUCTIONS, "true")
                    val resp = hopper.route(req)

                    time = sw.stop().seconds
                    val gson = GsonBuilder().serializeSpecialFloatingPointValues().create()
                    return resp.all.toString()
                }else{
                    return  downloadMapFromRemoteSource()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            return "Can't Load Map"
        }

        override fun onPreExecute() {
            super.onPreExecute()
            // ...
        }

        override fun onPostExecute(path: String?) {
            super.onPostExecute(path)
            result?.success(path)
        }

        fun getMapFolder(): File {
            result
            val mapsFolder: File
            val greaterOrEqKitkat = Build.VERSION.SDK_INT >= 19
            if (greaterOrEqKitkat) {
                if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
                    throw RuntimeException("GraphHopper is not usable without an external storage!")
                }
                mapsFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        mapPath)
            } else
                mapsFolder = File(Environment.getExternalStorageDirectory(), mapPath)

            return mapsFolder
        }
    }
}
