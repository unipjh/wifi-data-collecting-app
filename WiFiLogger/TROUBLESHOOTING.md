# Gradle 오류 해결 가이드

## 오류 1: Unable to load class 'org.gradle.api.internal.HasConvention'

이 오류는 Gradle 버전 호환성 문제로 발생합니다.

## 오류 2: Incompatible Java 21 and Gradle 8.0 ⚠️ 최신 오류

**증상:**
```
Your build is currently configured to use incompatible Java 21.0.9 and Gradle 8.0
Cannot sync the project
```

**원인:** Java 21은 Gradle 8.5 이상 필요

**해결:** 이미 Gradle 8.5로 설정되어 있으므로 캐시 삭제 후 재동기화만 하면 됩니다!

---

## 🚀 빠른 해결 방법 (Java 21 사용 시)

### ✅ 가장 쉬운 방법: Gradle 캐시 삭제

1. **Android Studio에서:**
   ```
   File → Invalidate Caches / Restart...
   ↓
   Invalidate and Restart 클릭
   ```

2. **재시작 후:**
   ```
   Sync Project with Gradle Files 클릭
   ```

3. **완료!** Gradle 8.5가 자동으로 다운로드되고 동기화됩니다.

---

## 📋 상세 해결 방법 (순서대로 시도)

### 방법 1: Gradle 캐시 삭제 및 재동기화 ⭐ 추천

1. Android Studio에서:
   - `File` → `Invalidate Caches / Restart...` 클릭
   - `Invalidate and Restart` 선택
   
2. 또는 터미널에서:
   ```bash
   cd 프로젝트폴더
   ./gradlew clean
   ./gradlew --stop
   ```

3. Android Studio 재시작 후 `Sync Project with Gradle Files` 클릭

### 방법 2: Gradle Wrapper 강제 다운로드

터미널에서:

```bash
# 프로젝트 폴더로 이동
cd WiFiLogger

# Gradle Daemon 중지
./gradlew --stop

# .gradle 폴더 삭제
rm -rf .gradle

# Gradle wrapper 다운로드 (8.5 자동 다운로드)
./gradlew wrapper

# 클린 빌드
./gradlew clean
```

### 방법 3: JDK 버전 변경 (Java 21이 문제라면)

**옵션 A: Gradle JDK를 JDK 17로 변경**

1. `File` → `Settings` (Windows/Linux) 또는 `Preferences` (Mac)
2. `Build, Execution, Deployment` → `Build Tools` → `Gradle`
3. `Gradle JDK` 를 `JDK 17` 로 변경
4. `Apply` → `OK`
5. `Sync Project with Gradle Files`

**옵션 B: Java 21 계속 사용 (권장)**

프로젝트는 이미 Gradle 8.5로 설정되어 있어서 Java 21과 호환됩니다.
캐시만 삭제하면 됩니다!

---

## ⚙️ 현재 프로젝트 설정

이 프로젝트는 이미 다음과 같이 설정되어 있습니다:

- **Gradle**: 8.5 (Java 21 호환)
- **Android Gradle Plugin**: 8.3.0
- **Kotlin**: 1.9.22
- **지원 JDK**: 17, 21

## 🎯 호환성 표

| Java 버전 | 최소 Gradle 버전 | 프로젝트 설정 | 상태 |
|-----------|-----------------|--------------|------|
| Java 17   | Gradle 7.3+     | Gradle 8.5   | ✅ OK |
| Java 21   | Gradle 8.5+     | Gradle 8.5   | ✅ OK |

---

## 권장 환경

- **Gradle**: 8.5 (자동 설정됨)
- **Android Gradle Plugin**: 8.3.0 (자동 설정됨)
- **Kotlin**: 1.9.22 (자동 설정됨)
- **JDK**: 17 또는 21 (둘 다 OK)
- **Android Studio**: Hedgehog (2023.1.1) 이상

---

## 여전히 문제가 있다면

### 1단계: 완전 초기화

```bash
# 프로젝트 폴더에서
rm -rf .gradle
rm -rf .idea
rm -rf app/build
rm -rf build
```

### 2단계: Android Studio 재시작

```
File → Invalidate Caches / Restart
```

### 3단계: 재동기화

```
Sync Project with Gradle Files
```

---

## 빠른 해결 체크리스트

- [ ] `File` → `Invalidate Caches / Restart`
- [ ] `./gradlew --stop` 실행
- [ ] `.gradle` 폴더 삭제
- [ ] Gradle JDK 확인 (JDK 17 또는 21)
- [ ] gradle-wrapper.properties에서 Gradle 8.5 확인
- [ ] Android Studio 재시작
- [ ] Sync Project with Gradle Files

---

## 🎉 대부분의 경우

**캐시 삭제 (방법 1)** 만으로 해결됩니다!

```
File → Invalidate Caches / Restart
↓
Invalidate and Restart
↓
재시작 후 자동 동기화
↓
완료!
```
