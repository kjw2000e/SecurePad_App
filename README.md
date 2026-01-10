# KICASecureKeypad

**한국정보인증(KICA)** 보안 키패드 SDK for Android

금융 앱, 핀테크, 인증 서비스를 위한 상용급 보안 키패드 라이브러리

---

## ✨ 주요 기능

- 🔐 **AES-256 암호화** - 키 입력마다 개별 암호화
- 🔑 **RSA-2048 키 교환** - 안전한 세션 키 전송
- 🛡️ **HMAC-SHA1 무결성 검증**
- 🌏 **한글/영문/숫자/특수문자** 지원
- 🎨 **다양한 테마** - KICA, Dark, Lavender, Custom
- 📱 **화면 캡처 방지** (FLAG_SECURE)
- ✅ **입력 검증** - minLength, regex, customValidator
- 🔄 **랜덤 키 배열** - 어깨너머 공격 방지

---

## 🚀 빠른 시작

### 1. 의존성 추가

#### JitPack 사용 (권장)

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // 추가
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.kjw2000e:SecurePad_App:1.0.0")
}
```

[![](https://jitpack.io/v/kjw2000e/SecurePad_App.svg)](https://jitpack.io/#kjw2000e/SecurePad_App)

#### 로컬 모듈 사용 (개발용)

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":kicasecurekeypad"))
}
```

### 2. 기본 사용

```kotlin
import com.kica.android.secure.keypad.SecureKeypad
import com.kica.android.secure.keypad.domain.model.*

@Composable
fun PinScreen() {
    SecureKeypad(
        config = KeypadConfig(
            type = KeypadType.NUMERIC,
            title = "PIN 입력",
            subtitle = "6자리 숫자를 입력해주세요",
            maxLength = 6,
            enableEncryption = true
        ),
        onComplete = { encrypted -> sendToServer(encrypted) },
        onCancel = { navigateBack() }
    )
}
```

---

## 📦 키패드 타입

| 타입 | 설명 |
|------|------|
| `NUMERIC` | 숫자 전용 (PIN) |
| `ALPHANUMERIC` | 한글/영문/숫자/특수문자 |
| `ENGLISH` | 영문 전용 |
| `KOREAN` | 한글 전용 |

---

## 🎨 테마

```kotlin
// 기본 제공 테마
KeypadColors.kica()     // KICA 스타일 (라이트)
KeypadColors.dark()     // 다크 테마
KeypadColors.lavender() // 연보라 테마
KeypadColors.default()  // 시스템 테마 자동 적용
```

---

## 📐 표시 모드

| 모드 | 설명 |
|------|------|
| `FULL` | 전체화면 (상단 헤더 + 하단 키패드) |
| `HALF` | 하단 시트 형태 |
| `COMPACT` | 최소 높이 (헤더 숨김) |

---

## 📖 문서

| 문서 | 설명 |
|------|------|
| [개발자 가이드](docs/개발자가이드.md) | API 사용법, 설정 옵션 |
| [라이브러리 분석](docs/라이브러리%20분석.md) | 아키텍처, 파일 구조 |
| [암호화 분석](docs/암호화_분석.md) | 보안 알고리즘 상세 |

---

## 🔧 요구사항

- Android API 29+ (Android 10)
- Jetpack Compose
- Kotlin 1.9+

---

## 📄 라이센스

© 2025 한국정보인증 (Korea Information Certificate Authority)
