package com.example.wifilogger

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import java.io.IOException
import java.net.InetAddress

class WifiDataCollector(private val context: Context) {
    
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    
    private var lastLocation: Location? = null
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
        .setMinUpdateIntervalMillis(1000)
        .build()
    
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            lastLocation = locationResult.lastLocation
        }
    }
    
    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }
    
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    
    fun isWifiConnected(): Boolean {
        return try {
            wifiManager.isWifiEnabled && wifiManager.connectionInfo.networkId != -1
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun collectData(floor: String): WifiData? = withContext(Dispatchers.IO) {
        try {
            val wifiInfo = wifiManager.connectionInfo
            
            // WiFi 연결 확인
            if (wifiInfo.networkId == -1) {
                return@withContext null
            }
            
            val rssi = wifiInfo.rssi
            val linkSpeed = wifiInfo.linkSpeed
            val ssid = wifiInfo.ssid?.replace("\"", "") ?: "unknown"
            val bssid = wifiInfo.bssid ?: "unknown"
            
            // Ping 측정 (실패 시 -1)
            val pingMs = measurePing()
            
            // GPS 위치 (권한 없으면 0.0, 0.0)
            val location = getLastKnownLocation()
            val latitude = location?.latitude ?: 0.0
            val longitude = location?.longitude ?: 0.0
            
            WifiData(
                timestamp = System.currentTimeMillis(),
                rssi = rssi,
                linkSpeed = linkSpeed,
                ssid = ssid,
                bssid = bssid,
                pingMs = pingMs,
                latitude = latitude,
                longitude = longitude,
                floor = floor
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private suspend fun measurePing(): Long = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()

            // 8.8.8.8 (구글 DNS) 사용 - 안정적이고 빠름
            val address = try {
                InetAddress.getByName("8.8.8.8")
            } catch (e: Exception) {
                // 대체 DNS (Cloudflare)
                InetAddress.getByName("1.1.1.1")
            }

            // 타임아웃을 1초로 단축 (3000ms → 1000ms)
            val reachable = address.isReachable(1000)
            val endTime = System.currentTimeMillis()

            if (reachable) {
                endTime - startTime
            } else {
                -1L
            }
        } catch (e: Exception) {
            -1L
        }
    }
    
    private suspend fun getLastKnownLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            continuation.resume(null) {}
            return@suspendCancellableCoroutine
        }
        
        // 캐시된 위치 반환
        if (lastLocation != null) {
            continuation.resume(lastLocation) {}
            return@suspendCancellableCoroutine
        }
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                continuation.resume(location) {}
            }
            .addOnFailureListener { 
                continuation.resume(null) {}
            }
    }
}
