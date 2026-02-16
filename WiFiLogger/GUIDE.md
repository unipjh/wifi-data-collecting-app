# WiFi Logger 앱 설치 및 사용 가이드 (v2.1)

## 📱 프로젝트 구조

```
WiFiLogger/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/wifilogger/
│   │       │   ├── MainActivity.kt           # 메인 액티비티
│   │       │   ├── WifiLoggerService.kt     # 백그라운드 서비스
│   │       │   ├── WifiDataCollector.kt     # WiFi 데이터 수집
│   │       │   ├── CsvExporter.kt           # CSV 파일 저장
│   │       │   └── WifiData.kt              # 데이터 모델
│   │       ├── res/                       # 리소스 폴더
│   │       └── AndroidManifest.xml          # 앱 설정 및 권한
│   └── build.gradle.kts                     # 앱 빌드 설정
├── GUIDE.md                               # 사용자 가이드
├── TROUBLESHOOTING.md                     # 문제 해결 가이드
├── build.gradle.kts                         # 프로젝트 빌드 설정
└── settings.gradle.kts                      # 프로젝트 설정
```

## 🚀 설치 방법

### 1. Android Studio에서 프로젝트 열기
1. Android Studio 실행
2. File → Open 선택
3. WiFiLogger 폴더 선택
4. Gradle 동기화 대기

### 2. 실제 디바이스에서 실행
1. Android 디바이스의 개발자 옵션 활성화
2. USB 디버깅 활성화
3. 디바이스를 컴퓨터에 연결
4. Android Studio에서 Run 버튼 클릭

## 📋 주요 기능

### 1. 데이터 수집
- **수집 주기**: 약 3초마다 자동 수집
- **수집 항목**:
  - 현재 시각 (Unix timestamp, milliseconds)
  - WiFi RSSI (신호 세기, dBm)
  - WiFi Link Speed (Mbps)
  - WiFi BSSID (MAC 주소)
  - WiFi SSID (네트워크 이름)
  - **Ping 측정 (8.8.8.8 또는 1.1.1.1, ms) ✨ 최적화**
  - GPS 위치 (위도, 경도)
  - 사용자 선택 위치 (floor)

### 2. 백그라운드 실행
- **Foreground Service 사용**
- 화면이 꺼져도 측정 계속됨
- 알림바에 "측정 중..." 표시
- 알림에서 직접 "지금 저장" 및 "중지" 가능

### 3. 수동 위치 입력
- GPS 대신 또는 추가로 위치 정보 입력
- Spinner로 선택 가능:
  - 학술정보원_4층
  - AI센터_지하1층
  - 학생회관_5층
- 선택한 값이 CSV의 `floor` 컬럼에 저장

### 4. 즉시 저장 기능
- "지금 저장" 버튼으로 즉시 CSV 생성
- 30분을 기다리지 않고 언제든 저장 가능
- 알림바에서도 저장 가능

### 5. 자동 CSV 저장
- **저장 주기**: 30분마다 자동 저장
- **저장 위치**: Downloads 폴더
- **파일명 형식**: `wifi_YYYYMMDD_HHMMSS.csv`

## 🎯 사용 방법

1.  **앱 실행 및 권한 허용**: 앱을 처음 실행할 때 요청하는 모든 권한을 허용해야 정상 작동합니다.
2.  **위치 선택**: 측정할 위치를 Spinner에서 선택합니다. 측정 시작 후에는 변경할 수 없습니다.
3.  **측정 시작/중지**: "측정 시작" 버튼을 누르면 데이터 수집이 시작되며, 버튼은 "측정 중지"로 바뀝니다. 다시 누르면 수집이 중지되고 남은 데이터가 저장됩니다.
4.  **백그라운드 실행**: 앱이 백그라운드로 전환되거나 화면이 꺼져도 알림바를 통해 계속 상태를 확인할 수 있으며, "지금 저장" 또는 "중지"가 가능합니다.
5.  **CSV 파일 확인**: 수집된 데이터는 `Downloads` 폴더에 `wifi_*.csv` 파일로 저장됩니다.

## 🐛 문제 해결

자세한 내용은 [TROUBLESHOOTING.md](TROUBLESHOOTING.md) 파일을 참고하세요.

## ⚙️ 기술 상세

### Ping 측정 방식 최적화
- **안정적인 서버 사용**: `sejong.ac.kr` 대신 응답이 빠르고 안정적인 `8.8.8.8`(Google DNS)과 `1.1.1.1`(Cloudflare DNS)을 사용합니다.
- **타임아웃 단축**: Ping 타임아웃을 1초로 줄여 불필요한 대기 시간을 제거하고 데이터 수집 주기를 3초로 안정화했습니다.

## 📝 버전 정보

- **버전**: 2.1
- **minSdk**: 26 (Android 8.0 Oreo)
- **targetSdk**: 34 (Android 14)
- **Kotlin 버전**: 1.9.20
