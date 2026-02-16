package com.example.wifilogger

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var btnToggle: Button
    private lateinit var btnSaveNow: Button
    private lateinit var spinnerFloor: Spinner
    private lateinit var tvDataCount: TextView
    private lateinit var tvLastData: TextView
    private lateinit var tvNextSave: TextView
    private lateinit var tvServiceStatus: TextView

    private lateinit var dataCollector: WifiDataCollector

    private var uiUpdateJob: Job? = null

    private val floorOptions = arrayOf(
        "학술정보원_4층",
        "AI센터_지하1층",
        "학생회관_5층"
    )

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initCollector()
        setupSpinner()
        checkPermissions()

        btnToggle.setOnClickListener {
            if (WifiLoggerService.isRunning) {
                stopCollection()
            } else {
                startCollection()
            }
        }

        btnSaveNow.setOnClickListener {
            saveNow()
        }
    }

    override fun onResume() {
        super.onResume()
        startUIUpdate()
    }

    override fun onPause() {
        super.onPause()
        stopUIUpdate()
    }

    private fun initViews() {
        btnToggle = findViewById(R.id.btnToggle)
        btnSaveNow = findViewById(R.id.btnSaveNow)
        spinnerFloor = findViewById(R.id.spinnerFloor)
        tvDataCount = findViewById(R.id.tvDataCount)
        tvLastData = findViewById(R.id.tvLastData)
        tvNextSave = findViewById(R.id.tvNextSave)
        tvServiceStatus = findViewById(R.id.tvServiceStatus)
    }

    private fun initCollector() {
        dataCollector = WifiDataCollector(this)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, floorOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFloor.adapter = adapter
        spinnerFloor.setSelection(0)
    }

    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        for (permission in REQUIRED_PERMISSIONS) {
            if (permission == Manifest.permission.POST_NOTIFICATIONS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                continue
            }
            if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                continue
            }
            if (permission == Manifest.permission.FOREGROUND_SERVICE && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                continue
            }
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "일부 권한이 거부되었습니다. 기능이 제한될 수 있습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startCollection() {
        if (!dataCollector.isWifiConnected()) {
            Toast.makeText(this, "WiFi가 연결되어 있지 않습니다!", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedFloor = spinnerFloor.selectedItem.toString()
        val intent = Intent(this, WifiLoggerService::class.java).apply {
            action = WifiLoggerService.ACTION_START
            putExtra(WifiLoggerService.EXTRA_FLOOR, selectedFloor)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopCollection() {
        val intent = Intent(this, WifiLoggerService::class.java).apply {
            action = WifiLoggerService.ACTION_STOP
        }
        startService(intent)
    }

    private fun saveNow() {
        if (!WifiLoggerService.isRunning) {
            Toast.makeText(this, "측정이 실행 중이 아닙니다", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, WifiLoggerService::class.java).apply {
            action = WifiLoggerService.ACTION_SAVE_NOW
        }
        startService(intent)
        Toast.makeText(this, "CSV 파일을 저장하고 있습니다...", Toast.LENGTH_SHORT).show()
    }

    private fun startUIUpdate() {
        stopUIUpdate()
        uiUpdateJob = lifecycleScope.launch {
            while (isActive) {
                updateUI()
                delay(1000)
            }
        }
    }

    private fun stopUIUpdate() {
        uiUpdateJob?.cancel()
        uiUpdateJob = null
    }

    private suspend fun updateUI() {
        if (WifiLoggerService.isRunning) {
            withContext(Dispatchers.Main) {
                btnToggle.text = "측정 중지"
                btnToggle.setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark))
                btnSaveNow.isEnabled = true
                spinnerFloor.isEnabled = false
                tvServiceStatus.text = "백그라운드 실행 중 🔴"
                tvServiceStatus.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark))
                tvDataCount.text = "수집된 데이터: ${WifiLoggerService.currentDataCount}개"
                tvNextSave.text = "다음 저장: 자동 (30분 주기)"
            }

            val selectedFloor = withContext(Dispatchers.Main) { spinnerFloor.selectedItem.toString() }
            val data = dataCollector.collectData(selectedFloor)
            withContext(Dispatchers.Main) {
                if (data != null) {
                    tvLastData.text = "RSSI: ${data.rssi} dBm, Ping: ${data.pingMs} ms\n" +
                            "SSID: ${data.ssid}, Speed: ${data.linkSpeed} Mbps\n" +
                            "위치: ${data.floor}"
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                btnToggle.text = "측정 시작"
                btnToggle.setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_green_dark))
                btnSaveNow.isEnabled = false
                spinnerFloor.isEnabled = true
                tvServiceStatus.text = "대기 중 ⚪"
                tvServiceStatus.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.darker_gray))
                tvDataCount.text = "수집된 데이터: 0개"
                tvLastData.text = "측정을 시작하세요"
                tvNextSave.text = "다음 저장: -"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopUIUpdate()
    }
}