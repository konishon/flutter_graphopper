package np.com.naxa.graphhooper_route

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.GsonBuilder
import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import com.graphhopper.util.Parameters
import com.graphhopper.util.StopWatch
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.io.BufferedInputStream
import java.io.File
import java.lang.Exception
import java.util.*
import java.util.zip.ZipFile

class GraphhooperRoutePlugin : MethodCallHandler, PluginRegistry.ActivityResultListener {

    override fun onActivityResult(p0: Int, p1: Int, p2: Intent?): Boolean {
        return true
    }

    companion object {
        private lateinit var locationManager: LocationManager
        private lateinit var ctx: Context
        private lateinit var mpoints: List<Double>
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "graphhooper_route")
            channel.setMethodCallHandler(GraphhooperRoutePlugin())
            ctx = registrar.activeContext()
            LocalBroadcastManager.getInstance(ctx).registerReceiver(CalcPath().onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        } else if (call.method.equals("getRoutes")) {
            val mapPath = call.argument<String>("path_to_map")
            val points = call.argument<List<Double>>("points")
            if (points != null) {
                mpoints = points.toMutableList()
            }
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
        var downloadId: Long? = null

        constructor(result: Result, mapPath: String?) : this() {
            this.result = result
            this.mapPath = mapPath
        }

        private fun ifMapFileExists(): Boolean {
            Log.d("IFMAPFILEEXISTS: ", File(Environment.getExternalStorageDirectory(), "/Download/nepal-gh/").exists().toString())
            return File(Environment.getExternalStorageDirectory(), "/Download/nepal-gh").exists()
        }

        private fun downloadMapFromRemoteSource() {
//            val baseUrl = ""
//            val fileName = "nepal-gh.zip"
//            val url = baseUrl
//
//            val request = DownloadManager.Request(Uri.parse(url))
//            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
//            request.setTitle("Downloading $fileName")
//            request.setDescription("Please wait")
//            request.allowScanningByMediaScanner()
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"nepal-gh")
//
//            val manager = ctx.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//            downloadId = manager.enqueue(request)
            unzip(Environment.getExternalStorageDirectory().absolutePath + "/Download/nepal-gh.zip", Environment.getExternalStorageDirectory().absolutePath + "/Download/")
        }

        val onDownloadComplete = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                Log.d("download: ", "DOWNLOAD COMPLETED")
                unzip(Environment.getExternalStorageDirectory().absolutePath + "/Download/nepal-gh.zip", Environment.getExternalStorageDirectory().absolutePath + "/Download/")
            }
        }

        private fun unzip(zipFile: String, targetPath: String) {
            Log.d("UNZIP", targetPath)
            Log.d("UNZIP", zipFile)

            val zip = ZipFile(zipFile)
            val enumeration = zip.entries()
            while (enumeration.hasMoreElements()) {
                val entry = enumeration.nextElement()
                val destFilePath = File(targetPath, entry.name)
                destFilePath.parentFile.mkdirs()
                if (entry.isDirectory)
                    continue
                val bufferedIs = BufferedInputStream(zip.getInputStream(entry))
                bufferedIs.use {
                    destFilePath.outputStream().buffered(1024).use { bos ->
                        bufferedIs.copyTo(bos)
                    }
                }
            }
            Log.d("MPOINTS: ", mpoints.toString())
            CalcPath().doInBackground(mpoints.get(0), mpoints.get(1), mpoints.get(2), mpoints.get(3))
        }

        override fun doInBackground(vararg params: Double?): String? {

            try {
                if (ifMapFileExists()) {
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
                } else {
                    Log.d("Download: ", "calling downloadFromMapSource")
                    downloadMapFromRemoteSource()
                    return "Map Downloaded"
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            return "Cannot Load Map"
        }

        override fun onPreExecute() {
            super.onPreExecute()
            // ...
        }

        override fun onPostExecute(path: String?) {
            super.onPostExecute(path)
            result?.success(path)
        }

        @SuppressLint("ObsoleteSdkInt")
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
