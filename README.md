<div align="center">

# 🔐 KICASecureKeypad

**Enterprise-grade Secure Keypad SDK for Android**

한국정보인증(KICA) 보안 키패드 라이브러리

[![JitPack](https://jitpack.io/v/kjw2000e/SecurePad_App.svg)](https://jitpack.io/#kjw2000e/SecurePad_App)
[![API](https://img.shields.io/badge/API-29%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=29)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blueviolet.svg?style=flat)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5-4285F4.svg?style=flat)](https://developer.android.com/jetpack/compose)

<br/>

<img src="docs/demo.gif" alt="Demo" width="300"/>

</div>

---

## 🛡️ Security Stack

<table>
<tr>
<td align="center"><b>256-bit</b><br/>AES Encryption</td>
<td align="center"><b>2048-bit</b><br/>RSA Key Exchange</td>
<td align="center"><b>SHA-1</b><br/>HMAC Integrity</td>
<td align="center"><b>FLAG_SECURE</b><br/>Screen Protection</td>
</tr>
</table>

---

## ✨ Features

| Category | Feature |
|----------|---------|
| 🔐 **Security** | Per-keystroke AES-256 encryption, RSA-2048 key exchange, HMAC-SHA1 integrity |
| 🌏 **i18n** | Korean (한글) with Jamo composition, English, Numbers, Special chars |
| 🎨 **Themes** | KICA, Dark, Lavender, Custom color support |
| 📱 **UX** | Haptic feedback, Random key layout, Screen capture prevention |
| ✅ **Validation** | minLength, maxLength, regex, customValidator |
| 📐 **Display** | FULL, HALF, COMPACT modes + 4 indicator styles |

---

## 📦 Installation

### JitPack (Recommended)

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.github.kjw2000e:SecurePad_App:1.0.0")
}
```

---

## 🚀 Quick Start

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

## 🎨 Themes

<table>
<tr>
<td align="center"><b>KICA</b><br/><code>KeypadColors.kica()</code></td>
<td align="center"><b>Dark</b><br/><code>KeypadColors.dark()</code></td>
<td align="center"><b>Lavender</b><br/><code>KeypadColors.lavender()</code></td>
</tr>
</table>

---

## 📐 Display Modes

| Mode | Description |
|------|-------------|
| `FULL` | Full screen (Header at top, Keypad at bottom) |
| `HALF` | Bottom sheet style |
| `COMPACT` | Minimal height (no header) |

---

## 📊 Indicator Styles

| Style | Preview | Use Case |
|-------|---------|----------|
| `DOT` | ●●●○○○ | PIN, Password |
| `UNDERLINE` | _ _ _ | OTP |
| `BOX` | [●][●][○] | Verification codes |
| `TEXT` | abc... | Amount input |

---

## 📖 Documentation

| Document | Description |
|----------|-------------|
| [개발자 가이드](docs/개발자가이드.md) | API usage, configuration |
| [라이브러리 분석](docs/라이브러리%20분석.md) | Architecture, file structure |
| [암호화 분석](docs/암호화_분석.md) | Security algorithm details |
| [CHANGELOG](CHANGELOG.md) | Version history |

---

## 🔧 Requirements

- **Min SDK**: 29 (Android 10)
- **Target SDK**: 36
- **Language**: Kotlin 1.9+
- **UI**: Jetpack Compose 1.5+

---

<div align="center">

**Made with ❤️ by Jiwon**

</div>
