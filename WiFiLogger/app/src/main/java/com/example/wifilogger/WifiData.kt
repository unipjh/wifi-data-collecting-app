package com.example.wifilogger

data class WifiData(
    val timestamp: Long,
    val rssi: Int,
    val linkSpeed: Int,
    val ssid: String,
    val bssid: String,
    val pingMs: Long,
    val latitude: Double,
    val longitude: Double,
    val floor: String
) {
    fun toCsvRow(): String {
        return "$timestamp,$rssi,$linkSpeed,$ssid,$bssid,$pingMs,$latitude,$longitude,$floor"
    }
    
    companion object {
        fun getCsvHeader(): String {
            return "timestamp,rssi,link_speed,ssid,bssid,ping_ms,latitude,longitude,floor"
        }
    }
}
