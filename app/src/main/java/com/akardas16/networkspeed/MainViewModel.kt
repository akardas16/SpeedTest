package com.akardas16.networkspeed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import pk.farimarwat.speedtest.PingCalculator
import pk.farimarwat.speedtest.TestDownloader
import pk.farimarwat.speedtest.TestUploader
import pk.farimarwat.speedtest.models.STProvider
import pk.farimarwat.speedtest.models.STServer
import pk.farimarwat.speedtest.models.ServersResponse

class MainViewModel:ViewModel() {
    private val _state = MutableStateFlow("")
    val state = _state.asStateFlow()

    private val _servers = MutableStateFlow<List<STServer>>(listOf())
    val servers = _servers.asStateFlow()

    private val _progressUI = MutableStateFlow(0f)
    val progressUI = _progressUI.asStateFlow()

    private val _download = MutableStateFlow(0f)
    val download = _download.asStateFlow()

    private val _upload = MutableStateFlow(0f)
    val upload = _upload.asStateFlow()

    private val _ping = MutableStateFlow(0.0)
    val ping = _ping.asStateFlow()

    private val _jitter = MutableStateFlow(0.0)
    val jitter = _jitter.asStateFlow()

    private val _selectedServer = MutableStateFlow<STServer?>(null)
    val selectedServer = _selectedServer.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private var mBuilderDownload: TestDownloader? = null
    private var mBuilderUpload: TestUploader? = null

    init {
        viewModelScope.launch {

        }

    }

    fun changeSelectedServer(server:STServer){
        _selectedServer.update { server }
    }

    suspend fun getServerList(result:(onSuccess:(ServersResponse?), onError:(String?)) -> Unit){
        var provider:STProvider? = null

        val client = HttpClient(Android)
        //Get provider
        val response: HttpResponse = client.get("https://www.speedtest.net/speedtest-config.php")
        if (response.status.value == 200) {
            val body = response.body<String>()
            val doc = Jsoup.parse(body, Parser.xmlParser())
            val clients = doc.select("client")
            provider = STProvider(
                clients.attr("isp"),
                clients.attr("isp"),
                clients.attr("lat"),
                clients.attr("lon")
            )
        }else{
            Log.i("Error", "An error occurred while getting provider")
            result(null, "An error occurred while getting provider")
        }
        provider?.let {
            //Get Servers
            val responseServers: HttpResponse = client.get("https://www.speedtest.net/speedtest-servers-static.php")
            if (responseServers.status.value == 200) {
                val body = responseServers.body<String>()
                val doc = Jsoup.parse(body, Parser.xmlParser())
                val servers = doc.getElementsByTag("server")
                if (servers.isNotEmpty()) {
                    val list = getServers(servers, provider)
                    val resp = ServersResponse(
                        provider,
                        list
                    )
                    if (resp.servers?.isEmpty()?.not() == true){
                        val sortedList = resp.servers!!.sortedBy { it.distance }
                        _servers.update { sortedList }
                    }
                    _selectedServer.update { resp.servers?.first() }
                    result(resp, null)
                    resp.servers?.forEach {
                        Log.i("sfgsgsfsdfsf", ": ----> $it")
                    }
                } else {
                    Log.i("Error", "No server found")
                    result(null, "No server found")
                }
            }else{
                Log.i("Error", "An error occurred while getting server list")
                result(null, "An error occurred while getting server list")
            }

        }


    }

    private fun getServers(servers: Elements, stProvider: STProvider?): List<STServer> {
        val list = mutableListOf<STServer>()
        for (item in servers) {
            val server = item.select("server")
            var url = server.attr("url")
            if(!url.contains("8080")){
                url = url.replace(":80", ":8080")
            }
            val stserver = STServer(
                url,
                server.attr("lat"),
                server.attr("lon"),
                server.attr("name"),
                server.attr("sponsor")
            )
            stProvider?.let {
                val from = LatLng(
                    it.lat?.toDouble() ?: 0.0,
                    it.lon?.toDouble() ?: 0.0
                )
                val to = LatLng(
                    stserver.lat?.toDouble() ?: 0.0,
                    stserver.lon?.toDouble() ?: 0.0
                )
                val distance = SphericalUtil.computeDistanceBetween(from, to) / 1000
                stserver.distance = distance.toInt()
            }
            list.add(stserver)
        }
        return list
    }

    suspend fun downloadTest(onFinished:() -> Unit){
        withContext(Dispatchers.IO){
            _isRunning.update { true }
            val mUrl = _selectedServer.value?.url ?: ""
            val url = mUrl.replace(
                mUrl.split("/").toTypedArray()[mUrl.split("/")
                    .toTypedArray().size - 1],
                ""
            )
             mBuilderDownload = TestDownloader.Builder(url)
                .addListener(object : TestDownloader.TestDownloadListener {
                    override fun onStart() {
                        Log.i("NetworkSpeedTheme", "onStart: ")
                        _progressUI.update { 0f }
                        _download.update { 0f }
                    }

                    override fun onProgress(progress: Double, elapsedTimeMillis: Double) {
                        _progressUI.update { progress.toFloat() }

                        Log.i("NetworkSpeedTheme", "progress: $progress elapsedTimeMillis: ${(elapsedTimeMillis*1000).toFloat()}")
                    }

                    override fun onFinished(finalprogress: Double, datausedinkb: Int, elapsedTimeMillis: Double) {
                        _progressUI.update { finalprogress.toFloat() }
                        _download.update { finalprogress.toFloat() }
                        _progressUI.update { 0f }
                        onFinished()
                        Log.i("NetworkSpeedTheme", "onFinished: $finalprogress $elapsedTimeMillis")
                    }

                    override fun onError(msg: String) {
                        _isRunning.update { false }
                        Log.i("NetworkSpeedTheme", "onError: $msg")
                    }

                })
                .setTimeOUt(12)
                .setThreadsCount(2)
                .build()
            mBuilderDownload?.start()
        }

    }

    suspend fun uploadTest(onFinished:() -> Unit){
        _isRunning.update { true }
        val mUrl = _selectedServer.value?.url ?: ""
        withContext(Dispatchers.IO){
            mBuilderUpload = TestUploader.Builder(mUrl)
                .addListener(object : TestUploader.TestUploadListener {
                    override fun onStart() {
                        Log.i("UPLOAD", "onStart: ")
                        _progressUI.update { 0f }
                        _upload.update { 0f }
                    }

                    override fun onProgress(progress: Double, elapsedTimeMillis: Double) {
                        Log.i("UPLOAD", "onProgress: $progress ${(elapsedTimeMillis*1000).toFloat()}")
                        _progressUI.update { progress.toFloat() }
                    }


                    override fun onFinished(finalprogress: Double, datausedinkb: Int, elapsedTimeMillis: Double) {
                        Log.i("UPLOAD", "onFinished: $finalprogress $elapsedTimeMillis")
                        _progressUI.update { finalprogress.toFloat() }
                        _upload.update { finalprogress.toFloat() }
                        onFinished()
                        _progressUI.update { 0f }
                    }

                    override fun onError(msg: String) {
                        _isRunning.update { false }
                        Log.i("UPLOAD", "onError: $msg")
                    }

                })
                .setTimeOUt(12)
                .setThreadsCount(2)
                .build()
            mBuilderUpload?.start()
        }
    }

    suspend fun calculatePingAndJitter(){
        withContext(Dispatchers.IO){
            val pingCalculator = PingCalculator()
            val metrics = pingCalculator.calculateNetworkMetrics("google.com")
            if (metrics.avgPing >= 0) {
                _ping.update { metrics.avgPing }
                _jitter.update { metrics.jitter }
                Log.i("fgdfgdfgdfgdf", "Average ping: ${String.format("%.2f", metrics.avgPing)} ms")
                Log.i("fgdfgdfgdfgdf", "Jitter: ${String.format("%.2f", metrics.jitter)} ms")
                _isRunning.update { false }
            } else {
                _isRunning.update { false }
                println("Failed to calculate network metrics. Please check your internet connection.")
            }
        }
    }

    fun updateProgressUI(value:Float){
        _progressUI.update {value}
    }

    fun releaseResources(){
        mBuilderDownload?.apply {
            removeListener()
            stop()
        }
        mBuilderDownload = null
        mBuilderUpload?.apply {
            removeListener()
            stop()
        }
        mBuilderUpload = null

    }


}