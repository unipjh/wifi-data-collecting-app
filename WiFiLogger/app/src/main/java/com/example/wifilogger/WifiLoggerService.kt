package com.example.wifilogger

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class WifiLoggerService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var dataCollector: WifiDataCollector
    private lateinit var csvExporter: CsvExporter
    
    private val dataList = mutableListOf<WifiData>()
    private var collectionJob: Job? = null
    private var saveJob: Job? = null
    private var nextSaveTimeMillis = 0L
    private var selectedFloor = "학술정보원_4층"
    
    companion object {
        const val CHANNEL_ID = "wifi_logger_service_channel"
        const val NOTIFICATION_ID = 1000
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_SAVE_NOW = "ACTION_SAVE_NOW"
        const val EXTRA_FLOOR = "EXTRA_FLOOR"
        
        private const val SAVE_INTERVAL_MS = 1800000L // 30분
        private const val COLLECTION_INTERVAL_MS = 3000L // 3초
        
        var isRunning = false
            private set
        
        var currentDataCount = 0
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        dataCollector = WifiDataCollector(this)
        csvExporter = CsvExporter(this)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                selectedFloor = intent.getStringExtra(EXTRA_FLOOR) ?: "학술정보원_4층"
                startForegroundService()
            }
            ACTION_STOP -> {
                stopForegroundService()
            }
            ACTION_SAVE_NOW -> {
                saveDataNow()
            }
        }
        return START_STICKY
    }
    
    private fun startForegroundService() {
        isRunning = true
        currentDataCount = 0
        
        val notification = createNotification(0)
        startForeground(NOTIFICATION_ID, notification)
        
        dataCollector.startLocationUpdates()
        nextSaveTimeMillis = System.currentTimeMillis() + SAVE_INTERVAL_MS
        
        // 데이터 수집 Job
        collectionJob = serviceScope.launch {
            while (isActive) {
                val data = dataCollector.collectData(selectedFloor)
                if (data != null) {
                    dataList.add(data)
                    currentDataCount = dataList.size
                    updateNotification()
                }
                delay(COLLECTION_INTERVAL_MS)
            }
        }
        
        // 자동 저장 Job
        saveJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                
                // 30분마다 저장
                if (System.currentTimeMillis() >= nextSaveTimeMillis) {
                    saveDataToCsv()
                    nextSaveTimeMillis = System.currentTimeMillis() + SAVE_INTERVAL_MS
                }
            }
        }
    }
    
    private fun stopForegroundService() {
        isRunning = false
        
        dataCollector.stopLocationUpdates()
        collectionJob?.cancel()
        saveJob?.cancel()
        
        // 남은 데이터 저장
        if (dataList.isNotEmpty()) {
            serviceScope.launch {
                saveDataToCsv()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    stopForeground(true)
                }
                stopSelf()
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                stopForeground(true)
            }
            stopSelf()
        }
    }
    
    private fun saveDataNow() {
        serviceScope.launch {
            saveDataToCsv()
            nextSaveTimeMillis = System.currentTimeMillis() + SAVE_INTERVAL_MS
        }
    }
    
    private suspend fun saveDataToCsv() {
        if (dataList.isEmpty()) return
        
        val success = csvExporter.exportToCsv(dataList.toList())
        if (success) {
            dataList.clear()
            currentDataCount = 0
            withContext(Dispatchers.Main) {
                updateNotification()
            }
        }
    }
    
    private fun updateNotification() {
        val notification = createNotification(dataList.size)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun createNotification(dataCount: Int): Notification {
        val stopIntent = Intent(this, WifiLoggerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val saveIntent = Intent(this, WifiLoggerService::class.java).apply {
            action = ACTION_SAVE_NOW
        }
        val savePendingIntent = PendingIntent.getService(
            this,
            1,
            saveIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val contentIntent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            2,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WiFi Logger 측정 중...")
            .setContentText("수집된 데이터: $dataCount 개")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_menu_save, "지금 저장", savePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "중지", stopPendingIntent)
            .build()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WiFi Logger Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "백그라운드 WiFi 데이터 수집"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        collectionJob?.cancel()
        saveJob?.cancel()
        serviceScope.cancel()
    }
}
