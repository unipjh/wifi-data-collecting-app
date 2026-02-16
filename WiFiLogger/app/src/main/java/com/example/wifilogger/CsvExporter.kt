package com.example.wifilogger

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class CsvExporter(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID = "wifi_logger_channel"
        private const val NOTIFICATION_ID = 1001
    }
    
    init {
        createNotificationChannel()
    }
    
    suspend fun exportToCsv(dataList: List<WifiData>): Boolean = withContext(Dispatchers.IO) {
        try {
            if (dataList.isEmpty()) {
                return@withContext false
            }
            
            // 파일명 생성
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "wifi_${dateFormat.format(Date())}.csv"
            
            // Downloads 폴더 경로
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            
            // CSV 파일 작성
            FileWriter(file).use { writer ->
                // 헤더 작성
                writer.append(WifiData.getCsvHeader())
                writer.append("\n")
                
                // 데이터 작성
                dataList.forEach { data ->
                    writer.append(data.toCsvRow())
                    writer.append("\n")
                }
            }
            
            // 알림 표시
            showNotification(fileName, dataList.size)
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "WiFi Logger"
            val descriptionText = "WiFi 데이터 로깅 알림"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showNotification(fileName: String, dataCount: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("CSV 파일 저장 완료")
            .setContentText("$fileName (데이터 $dataCount 개)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
