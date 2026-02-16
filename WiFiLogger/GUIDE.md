# WiFi Logger 앱 설치 및 사용 가이드 (v2.0)

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
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml    # UI 레이아웃
│   │       │   ├── values/
│   │       │   │   ├── strings.xml
│   │       │   │   └── colors.xml
│   │       │   └── xml/
│   │       │       ├── data_extraction_rules.xml
│   │       │       └── backup_rules.xml
│   │       └── AndroidManifest.xml          # 앱 설정 및 권한
│   ├── build.gradle.kts                     # 앱 빌드 설정
│   └── proguard-rules.pro
├── build.gradle.kts                         # 프로젝트 빌드 설정
├── settings.gradle.kts                      # 프로젝트 설정
└── gradle.properties                        # Gradle 속성
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
- **수집 주기**: 3초마다 자동 수집
- **수집 항목**:
  - 현재 시각 (Unix timestamp, milliseconds)
  - WiFi RSSI (신호 세기, dBm)
  - WiFi Link Speed (Mbps)
  - WiFi BSSID (MAC 주소)
  - WiFi SSID (네트워크 이름)
  - Ping 측정 (sejong.ac.kr 또는 8.8.8.8, ms)
  - GPS 위치 (위도, 경도)
  - 사용자 선택 위치 (floor)

### 2. 백그라운드 실행 ✨ NEW
- **Foreground Service 사용**
- 화면이 꺼져도 측정 계속됨
- 알림바에 "측정 중... (327개)" 표시
- 알림에서 직접 "지금 저장" 및 "중지" 가능

### 3. 수동 위치 입력 ✨ NEW
- GPS 대신 또는 추가로 위치 정보 입력
- Spinner로 선택 가능:
  - 학술정보원_4층
  - AI센터_지하1층
  - 학생회관_5층
- 선택한 값이 CSV의 `floor` 컬럼에 저장

### 4. 즉시 저장 기능 ✨ NEW
- "지금 저장" 버튼으로 즉시 CSV 생성
- 30분을 기다리지 않고 언제든 저장 가능
- 알림바에서도 저장 가능

### 5. 자동 CSV 저장
- **저장 주기**: 30분마다 자동 저장
- **저장 위치**: Downloads 폴더
- **파일명 형식**: `wifi_YYYYMMDD_HHMMSS.csv`
- **CSV 형식**:
```csv
timestamp,rssi,link_speed,ssid,bssid,ping_ms,latitude,longitude,floor
1710489600000,-45,866,sejong,AA:BB:CC:DD:EE:FF,12,37.5505,127.0734,학술정보원_4층
```

### 6. 에러 처리 ✨ NEW
- **WiFi 미연결**: "WiFi가 연결되어 있지 않습니다!" Toast 표시
- **GPS 권한 없음**: 위도/경도를 0.0, 0.0으로 저장
- **Ping 실패**: ping_ms를 -1로 저장

### 7. 알림
- CSV 파일 저장 완료 시 자동 알림 표시
- 알림 내용: 파일명과 저장된 데이터 개수
- 백그라운드 실행 중 지속 알림 표시

## 🎯 사용 방법

### 1. 앱 실행
1. 앱 아이콘 터치하여 실행
2. 권한 요청 시 모두 허용 (필수)

### 2. 위치 선택
1. Spinner에서 측정 위치 선택
   - 학술정보원_4층
   - AI센터_지하1층
   - 학생회관_5층
2. 측정 시작 후에는 위치 변경 불가 (측정 중지 후 변경)

### 3. 측정 시작
1. WiFi 연결 확인 (필수!)
2. "측정 시작" 버튼 터치
3. 백그라운드 서비스 시작
4. 알림바에 "측정 중..." 표시
5. 화면에 실시간 정보 표시:
   - 수집된 데이터 개수
   - 마지막 측정값 (RSSI, Ping, SSID, Speed, 위치)
   - 다음 CSV 저장 정보

### 4. 백그라운드 실행
1. 홈 버튼 또는 전원 버튼으로 화면 끄기
2. 백그라운드에서 계속 측정됨
3. 알림바에서 상태 확인 가능
4. 알림 터치하여 앱으로 돌아가기

### 5. 즉시 저장
1. "지금 저장" 버튼 터치 또는
2. 알림바의 "지금 저장" 액션 터치
3. 현재까지 수집된 데이터 즉시 CSV 저장
4. 데이터 카운터 초기화

### 6. 측정 중지
1. 앱에서 "측정 중지" 버튼 터치 또는
2. 알림바의 "중지" 액션 터치
3. 수집 중단 및 남은 데이터 자동 저장

### 7. CSV 파일 확인
1. 파일 관리자 앱 실행
2. Downloads 폴더 이동
3. `wifi_*.csv` 파일 확인

## 📱 UI 구성

### 메인 화면
```
┌─────────────────────────┐
│     WiFi Logger         │  ← 앱 제목
│   백그라운드 실행 중 🔴  │  ← 서비스 상태
├─────────────────────────┤
│ 측정 위치 선택          │
│ [학술정보원_4층 ▼]      │  ← Spinner
├─────────────────────────┤
│   [ 측정 중지 ]         │  ← 토글 버튼
│   [ 지금 저장 ]         │  ← 즉시 저장 버튼
├─────────────────────────┤
│ 수집된 데이터: 327개    │  ← 데이터 카운터
│ RSSI: -45 dBm           │  ← 마지막 측정값
│ Ping: 12 ms             │
│ SSID: sejong            │
│ Speed: 866 Mbps         │
│ 위치: 학술정보원_4층    │
│ 다음 저장: 자동 (30분)  │  ← 저장 정보
├─────────────────────────┤
│ ✓ 백그라운드 실행 지원  │
│ ✓ 화면 꺼져도 측정 계속 │
│ ✓ 알림바에서 제어 가능  │
└─────────────────────────┘
```

### 알림바
```
┌─────────────────────────┐
│ WiFi Logger 측정 중...  │
│ 수집된 데이터: 327 개   │
│ [지금 저장] [중지]      │  ← 액션 버튼
└─────────────────────────┘
```

## 🔑 필요한 권한

### 앱이 요청하는 권한
1. **ACCESS_FINE_LOCATION**: WiFi 정보 및 GPS 위치 접근
2. **ACCESS_COARSE_LOCATION**: 대략적인 위치 정보
3. **ACCESS_WIFI_STATE**: WiFi 상태 정보 읽기
4. **INTERNET**: Ping 테스트용 인터넷 접근
5. **WRITE_EXTERNAL_STORAGE**: CSV 파일 저장 (Android 9 이하)
6. **POST_NOTIFICATIONS**: 저장 완료 알림 (Android 13 이상)
7. **FOREGROUND_SERVICE**: 백그라운드 실행 (Android 9 이상)
8. **FOREGROUND_SERVICE_LOCATION**: 위치 기반 포그라운드 서비스

## ⚙️ 기술 상세

### 아키텍처
- **MainActivity**: UI 제어 및 서비스 연동
- **WifiLoggerService**: 백그라운드 Foreground Service
- **WifiDataCollector**: WiFi/GPS 데이터 수집
- **CsvExporter**: CSV 파일 생성 및 저장
- **WifiData**: 데이터 모델 (Data Class)

### 주요 라이브러리
- **Kotlin Coroutines**: 비동기 작업 처리
- **Google Play Services Location**: GPS 위치 수집
- **AndroidX Lifecycle**: 생명주기 관리

### 데이터 수집 방식
1. **WiFi 정보**: WifiManager를 통해 연결된 네트워크 정보 수집
2. **Ping 측정**: InetAddress.isReachable()로 도달 시간 측정
3. **GPS 위치**: FusedLocationProviderClient로 실시간 위치 업데이트
4. **타이머**: Kotlin Coroutines의 delay()로 3초 주기 구현
5. **백그라운드**: Foreground Service로 화면 꺼져도 실행

### 에러 처리
1. **WiFi 미연결**: `isWifiConnected()` 체크, Toast 표시
2. **GPS 권한 없음**: `getLastKnownLocation()` null 반환 → 0.0, 0.0 저장
3. **Ping 실패**: `measurePing()` 예외 처리 → -1 저장
4. **일반 예외**: try-catch로 안전하게 처리

## 🐛 문제 해결

### WiFi 정보가 수집되지 않는 경우
- 위치 권한이 허용되었는지 확인
- WiFi가 연결되어 있는지 확인
- "WiFi가 연결되어 있지 않습니다!" 메시지 확인
- 앱을 재시작

### GPS 위치가 0.0으로 표시되는 경우
- 위치 서비스(GPS)가 켜져 있는지 확인
- 실외에서 테스트 (실내는 GPS 수신 약함)
- 위치 권한이 "항상 허용"으로 설정되었는지 확인
- GPS 권한이 없으면 자동으로 0.0, 0.0 저장됨

### CSV 파일이 저장되지 않는 경우
- 저장소 권한이 허용되었는지 확인
- Downloads 폴더 존재 여부 확인
- 디바이스 저장 공간 확인
- "지금 저장" 버튼으로 수동 저장 시도

### Ping이 -1로 표시되는 경우
- 인터넷 연결 상태 확인
- 방화벽/보안 앱이 ICMP를 차단하지 않는지 확인
- sejong.ac.kr 도메인 접근 가능 여부 확인
- Ping 실패는 정상이며, -1로 저장됨

### 백그라운드에서 측정이 중단되는 경우
- 배터리 최적화 설정 확인
- 앱 설정 → 배터리 → 배터리 최적화 제외
- 제조사별 백그라운드 제한 해제 (삼성, 샤오미 등)
- Foreground Service 알림이 표시되는지 확인

## 📊 CSV 데이터 분석 예시

### Python으로 데이터 읽기
```python
import pandas as pd

# CSV 파일 읽기
df = pd.read_csv('wifi_20240315_143022.csv')

# 평균 RSSI 계산
print(f"평균 RSSI: {df['rssi'].mean():.2f} dBm")

# 평균 Ping 계산 (실패한 -1 제외)
valid_pings = df[df['ping_ms'] >= 0]['ping_ms']
print(f"평균 Ping: {valid_pings.mean():.2f} ms")

# 위치별 통계
for floor in df['floor'].unique():
    floor_data = df[df['floor'] == floor]
    print(f"\n{floor}:")
    print(f"  평균 RSSI: {floor_data['rssi'].mean():.2f} dBm")
    print(f"  데이터 개수: {len(floor_data)}")

# 시간대별 분석
df['datetime'] = pd.to_datetime(df['timestamp'], unit='ms')
df.set_index('datetime', inplace=True)
```

## 📝 버전 정보

- **버전**: 2.0 (백그라운드 지원)
- **minSdk**: 26 (Android 8.0 Oreo)
- **targetSdk**: 34 (Android 14)
- **빌드 도구**: Gradle 8.2.0
- **Kotlin 버전**: 1.9.20

## ✨ 새로운 기능 (v2.0)

### 추가된 기능
1. ✅ **백그라운드 실행** - Foreground Service
2. ✅ **수동 위치 입력** - Spinner 선택
3. ✅ **즉시 CSV 저장** - "지금 저장" 버튼
4. ✅ **에러 처리** - WiFi/GPS/Ping 실패 처리
5. ✅ **알림바 제어** - 저장/중지 버튼

### 개선된 기능
- 화면 꺼져도 측정 계속
- 사용자가 위치 직접 선택 가능
- 실시간 서비스 상태 표시
- 더 나은 에러 메시지

## 💡 추가 기능 제안

향후 추가 가능한 기능:
- 실시간 그래프 표시
- 클라우드 자동 업로드
- 여러 WiFi 네트워크 동시 모니터링
- 커스텀 위치 추가 기능
- 저장 주기 커스터마이징
- 데이터 필터링 옵션
- 히트맵 시각화

## 📞 문의

문제가 발생하거나 기능 제안이 있으시면 이슈를 등록해주세요.
