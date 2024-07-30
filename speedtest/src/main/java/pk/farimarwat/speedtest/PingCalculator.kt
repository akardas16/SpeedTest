package pk.farimarwat.speedtest

import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.*
import kotlin.math.abs

class PingCalculator {
    data class NetworkMetrics(val avgPing: Double, val jitter: Double)

    suspend fun calculateNetworkMetrics(hostName: String, port: Int = 80, numberOfPings: Int = 10): NetworkMetrics = withContext(Dispatchers.IO) {
        val pingTimes = mutableListOf<Long>()

        repeat(numberOfPings) {
            try {
                val startTime = System.nanoTime()
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(hostName, port), 5000) // 5 seconds timeout
                }
                val endTime = System.nanoTime()
                val pingTime = (endTime - startTime) / 1_000_000.0 // Convert to milliseconds
                pingTimes.add(pingTime.toLong())
            } catch (e: Exception) {
                println("Ping failed: ${e.message}")
            }
        }

        if (pingTimes.isEmpty()) {
            NetworkMetrics(-1.0, -1.0) // Return -1 for both metrics if no successful pings
        } else {
            val avgPing = pingTimes.average()
            val jitter = calculateJitter(pingTimes)
            NetworkMetrics(avgPing, jitter)
        }
    }

    private fun calculateJitter(pingTimes: List<Long>): Double {
        if (pingTimes.size < 2) return 0.0

        var sumDifferences = 0.0
        for (i in 1 until pingTimes.size) {
            sumDifferences += abs(pingTimes[i] - pingTimes[i-1]).toDouble()
        }
        return sumDifferences / (pingTimes.size - 1)
    }
}

