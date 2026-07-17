<!-- problem-first-summary:start -->
**Huge Problem(Pain Point):** 캠퍼스 WiFi 문제는 위치·시간·단말 조건이 포함된 반복 측정 데이터가 없어 재현하기 어렵다.

**솔루션 한 줄 정의:** Android 포그라운드 수집기로 WiFi·Ping·GPS 지표를 주기적으로 기록하고 CSV로 내보낸다.

**현재 상태:** WiFi 시스템 Android 수집기

**문제 해결 중심의 사고 흐름**

1. **관찰** — 사용자 체감만으로는 신호·지연·손실과 실제 위치를 같은 시점에 비교할 수 없었다.
2. **선택** — 단발 측정보다 백그라운드 주기 수집과 표준 CSV 내보내기를 선택했다.
3. **구현** — Kotlin 서비스가 3초 간격으로 WiFi·Ping·GPS를 수집하고 30분 단위 또는 수동으로 저장한다.
4. **검증과 한계** — Android 권한과 수집 흐름, APK가 포함되어 있다. 다수 빌드 산출물은 저장소 위생과 용량 측면에서 후속 정리가 필요하다.
<!-- problem-first-summary:end -->

---
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
## 인터페이스 
<img width="648" height="1404" alt="image" src="https://github.com/user-attachments/assets/2b0775e6-66b3-4b72-b257-745bb6ad2cd8" />


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

## 다운로드 앱
`app-debug.apk` 다운로드 (모바일) > 이용
