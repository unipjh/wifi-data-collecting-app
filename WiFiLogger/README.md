# WiFi Logger Android App

## 프로젝트 구조
```
WiFiLogger/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/wifilogger/
│   │       │   ├── MainActivity.kt
│   │       │   ├── WifiDataCollector.kt
│   │       │   ├── CsvExporter.kt
│   │       │   └── WifiData.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml
│   │       │   └── values/
│   │       │       ├── strings.xml
│   │       │       └── colors.xml
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── build.gradle.kts (Project level)
```

## 주요 기능
- 3초마다 WiFi 정보, Ping, GPS 수집
- 30분마다 자동 CSV 저장
- Downloads 폴더에 파일 저장
- 알림 표시

## 권한
- 위치 권한 (WiFi 정보 접근)
- 인터넷 권한 (Ping)
- 저장소 권한
- 알림 권한
