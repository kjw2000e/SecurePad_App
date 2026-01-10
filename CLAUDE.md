# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

KICASecurePad is an Android secure keypad library with a sample application. The project consists of two modules:
- `kicasecurekeypad`: The library module providing secure keypad functionality
- `app`: Sample application demonstrating keypad usage

## Build Commands

```bash
# Build the project
./gradlew build

# Build specific modules
./gradlew :kicasecurekeypad:build
./gradlew :app:build

# Install app on device
./gradlew :app:installDebug

# Run tests
./gradlew test
./gradlew :kicasecurekeypad:test

# Clean build
./gradlew clean
```

## Project Configuration

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Min SDK**: 29 (Android 10)
- **Compile SDK**: 36
- **Java Version**: 11

## Architecture

### Module Structure

**kicasecurekeypad** (Library):
- `domain/model/`: Data models (Key, KeyType, KeypadConfig, KeypadColors, KeypadType)
- `data/layout/`: Keyboard layout definitions (NumericLayout, EnglishLayout, KoreanLayout)
- `viewmodel/`: State management (KeypadViewModel)
- `ui/`: Composable components (KeypadButton, InputDisplay)
- `security/`: Encryption infrastructure (KeyDataManager, AESHelper, RSAHelper, MACHelper, KeyManager, KeyDataException)
- `utils/`: Utilities (StringUtil, HangulAssembler, HangulComposer)

**app** (Sample):
- Demonstrates keypad integration in MainActivity

### Key Architectural Concepts

#### 1. Keypad Types (KeypadType.kt)
- `NUMERIC`: Numbers only (0-9)
- `ENGLISH`: Alphabetic input
- `KOREAN`: Korean characters
- `ALPHANUMERIC`: Switchable between English/Korean

#### 2. State Management Pattern
Uses Kotlin StateFlow with ViewModel:
- `KeypadViewModel` manages input buffer, masking, language state, shift state
- UI components observe StateFlow and react to changes
- Separation of concerns: ViewModel handles logic, Composables handle rendering

#### 3. Korean Language Support (HangulAssembler & HangulComposer)
The library provides real-time Korean character composition:

**HangulComposer** (utils/HangulComposer.kt):
- Static utility for Korean Jamo (초성, 중성, 종성) composition
- Functions: `compose()`, `decompose()`, `combineJungsung()`, `combineJongsung()`
- Handles complex vowels (ㅘ, ㅝ, etc.) and double consonants (ㄳ, ㄺ, etc.)
- Character validation: `isChosung()`, `isJungsung()`, `isJongsung()`, `isHangul()`

**HangulAssembler** (utils/HangulAssembler.kt):
- Stateful class for real-time Korean character assembly
- Tracks current composition state (chosung, jungsung, jongsung)
- `append(char)`: Processes each Jamo input and returns assembled character
- `backspace()`: Smart backspace handling for Korean composition
- `commit()`: Finalizes current character and clears state
- `isComposing()`: Checks if currently assembling a character

**Korean Input Flow**:
1. User presses Korean Jamo key (ㄱ, ㅏ, etc.)
2. HangulAssembler.append() processes the input
3. Returns assembled syllable (가, 각, etc.) or partial composition
4. ViewModel updates inputBuffer with assembled result
5. Backspace intelligently decomposes characters (각 → 가 → ㄱ → deleted)

**Key Features**:
- Real-time character composition without commit button
- Automatic syllable boundary detection
- Complex Jamo combination support
- Smart backspace (decomposes syllables step by step)

#### 4. Security Layer Architecture
The library implements a multi-layer encryption strategy:

**Current Status**:
- Security infrastructure fully implemented in `security/` package
- KeyDataManager provides AES/RSA encryption for keystroke data
- **Note**: Currently, KeypadViewModel uses plain-text inputBuffer for simplicity
- Security layer can be integrated by replacing inputBuffer operations with KeyDataManager calls

**Encryption Flow** (when integrated):
1. User input → KeyDataManager.appendKeyData()
2. Each keystroke encrypted with AES using symmetric key
3. Symmetric key encrypted with RSA public key (from `vkeypad_public.pem` in assets)
4. For E2E transmission: `encryptedSymmetricKey + encryptedKeyData + HMAC-SHA1`

**Key Components**:
- `KeyDataManager`: Singleton managing encrypted input data
- `AESHelper`: AES/CBC/PKCS5Padding encryption (16-byte symmetric key)
- `RSAHelper`: RSA/ECB/PKCS1Padding encryption (256-byte asymmetric key)
- `MACHelper`: HMAC-SHA1 for data integrity (20-byte MAC)
- `KeyManager`: Secure random key generation
- `KeyDataException`: Custom exception for security layer errors

**Important Security Details**:
- Each keystroke stored as 16-byte block (1 byte data + 15 bytes random padding)
- Random padding prevents pattern analysis when same key pressed multiple times
- Symmetric key is randomly generated per session
- Memory should be cleared on ViewModel.onCleared()

#### 5. Key Types (KeyType.kt)
Different key types for various keypad functions:

- `NORMAL`: Regular input keys (numbers, letters, Korean Jamo)
- `BACKSPACE`: Delete last character/Jamo (⌫)
- `COMPLETE`: Submit input (✓)
- `SPACE`: Space character
- `SWITCH`: Language toggle (한/영) for ALPHANUMERIC mode
- `SHIFT`: Case/Jamo variant toggle (⇧)
- `SHUFFLE`: Randomize number layout (🔀) for NUMERIC mode

#### 6. Layout System
Each layout (Numeric/English/Korean) provides:

**NumericLayout** (data/layout/NumericLayout.kt):
- 3×4 grid layout (0-9 numbers + shuffle + backspace)
- `getKeys(shuffledNumbers?)`: Returns 12 keys in 3-column format
- Shuffle button randomizes number positions
- Default order: 1-9 in rows, then shuffle-0-backspace

**EnglishLayout** (data/layout/EnglishLayout.kt):
- QWERTY layout optimized for mobile
- Row structure: 10-9-9-3 keys (total 31 keys)
- Row 1: Q W E R T Y U I O P
- Row 2: A S D F G H J K L
- Row 3: Shift + Z X C V B N M + Backspace
- Row 4: 한/영 + Space + Complete
- `getKeys(uppercase, randomize)`: Returns keys with case toggling

**KoreanLayout** (data/layout/KoreanLayout.kt):
- Korean 2-set (두벌식) layout for mobile
- Row structure: 10-9-9-3 keys (total 31 keys)
- Row 1: ㅂ ㅈ ㄷ ㄱ ㅅ ㅛ ㅕ ㅑ ㅐ ㅔ
- Row 2: ㅁ ㄴ ㅇ ㄹ ㅎ ㅗ ㅓ ㅏ ㅣ
- Row 3: Shift + ㅋ ㅌ ㅊ ㅍ ㅠ ㅜ ㅡ + Backspace
- Row 4: 한/영 + Space + Complete
- `getKeys(shifted, randomize)`: Shift enables double consonants (ㄲ, ㄸ, etc.) and compound vowels (ㅒ, ㅖ)

**Common Features**:
- `rowSizes`: Defines keys per row for grid layout
- Optional randomization support (currently unused for alphabetic layouts)
- Special keys integrated into layout structure

#### 7. Compose UI Pattern
Main composable: `SecureKeypad` (SecureKeypad.kt:42)

**Structure**:
```
SecureKeypad
├── InputDisplay (shows masked/plain text)
└── Layout (varies by KeypadType)
    ├── NumericKeypadWithConfirm
    │   ├── NumericKeypadLayout (3×4 grid)
    │   └── Confirm Button
    └── AlphabeticKeypadLayout (4 rows with variable columns)
```

**Key Features**:
- Accepts KeypadConfig for customization
- Callbacks: onKeyPressed (masked value), onComplete (plaintext), onError
- Uses ViewModelFactory pattern for config injection
- Automatic haptic feedback on key press (configurable)
- Navigation bar padding for edge-to-edge display
- LaunchedEffect for vibration and error handling
- DisposableEffect cleanup on component disposal

**Layout Variations**:
- **NUMERIC**: 3-column fixed grid (NumericKeypadLayout) + separate confirm button
- **ENGLISH/KOREAN/ALPHANUMERIC**: Variable columns per row (AlphabeticKeypadLayout)
  - Row-based layout with different column counts (10, 9, 9, 3)
  - Automatic language switching for ALPHANUMERIC mode

## Usage Example

### Basic Integration
```kotlin
import com.kica.android.secure.keypad.SecureKeypad
import com.kica.android.secure.keypad.domain.model.KeypadConfig
import com.kica.android.secure.keypad.domain.model.KeypadColors
import com.kica.android.secure.keypad.domain.model.KeypadType

@Composable
fun MyScreen() {
    var inputValue by remember { mutableStateOf("") }

    SecureKeypad(
        config = KeypadConfig(
            type = KeypadType.NUMERIC,
            colors = KeypadColors.default(),
            maxLength = 6,
            showMasking = true
        ),
        onKeyPressed = { masked ->
            // Update UI with masked value
            println("Masked: $masked")
        },
        onComplete = { plaintext ->
            // Handle completed input
            inputValue = plaintext
            println("Input: $plaintext")
        },
        onError = { errorMsg ->
            // Handle errors (e.g., max length exceeded)
            println("Error: $errorMsg")
        }
    )
}
```

### ALPHANUMERIC Keypad with Custom Styling
```kotlin
SecureKeypad(
    modifier = Modifier.fillMaxWidth(),
    config = KeypadConfig(
        type = KeypadType.ALPHANUMERIC,
        colors = KeypadColors.kica(), // KICA-style theme
        maskingChar = '●',
        showMasking = false, // Show actual characters
        maxLength = 20,
        enableHapticFeedback = true,
        buttonHeight = 52.dp
    ),
    onComplete = { input ->
        // Process Korean/English mixed input
        submitForm(input)
    }
)
```

### Integration in Bottom Sheet
```kotlin
Scaffold(
    bottomBar = {
        SecureKeypad(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            config = KeypadConfig(
                type = KeypadType.NUMERIC,
                colors = KeypadColors.default()
            ),
            onComplete = { pin ->
                verifyPin(pin)
            }
        )
    }
) { padding ->
    // Main content
}
```

## Development Notes

### Adding New Keyboard Layout
1. Create layout file in `data/layout/` (extend pattern from NumericLayout/EnglishLayout/KoreanLayout)
2. Implement `getKeys(...)` returning List<Key>
3. Define `rowSizes` for grid structure
4. Update KeypadViewModel.loadKeys() to handle new type
5. Update SecureKeypad composable layout logic if needed

### Customizing Keypad Appearance and Behavior
Modify KeypadConfig parameters (domain/model/KeypadConfig.kt):

**Visual Customization**:
- `colors`: KeypadColors (background, text, button colors)
  - Predefined themes: `KeypadColors.default()`, `KeypadColors.kica()`
- `buttonHeight`: Dp value for button height (default: 48.dp)
- `maskingChar`: Character for masked display (default: '●')
- `showMasking`: Boolean to show masking (default: true)

**Input Control**:
- `maxLength`: Int? - Maximum input length (null = unlimited)
  - Triggers error callback when limit reached
- `type`: KeypadType - NUMERIC, ENGLISH, KOREAN, or ALPHANUMERIC
- `randomizeLayout`: Boolean - Randomize key positions (default: false)
  - Currently only affects NUMERIC mode via shuffle button

**Feedback & Accessibility**:
- `enableHapticFeedback`: Boolean - Vibration on key press (default: true)
- `enableSoundFeedback`: Boolean - Sound on key press (default: false) *[not yet implemented]*
- `enableAccessibility`: Boolean - Screen reader support (default: true)

**Advanced Features**:
- `autoResetTimeout`: Long? - Auto-clear input after ms (null = disabled) *[not yet implemented]*

**Example Configuration**:
```kotlin
KeypadConfig(
    type = KeypadType.ALPHANUMERIC,
    colors = KeypadColors.kica(),
    maskingChar = '●',
    showMasking = false, // Show actual input
    maxLength = 20,
    randomizeLayout = false,
    enableHapticFeedback = true,
    buttonHeight = 48.dp
)
```

### Working with Encrypted Data

**Local Usage** (within app):
```kotlin
val encryptedData = keyDataManager.encryptedData // ByteArray
val plainText = keyDataManager.plainText // ByteArray (decrypted)
```

**Server Transmission**:
```kotlin
val e2eData = keyDataManager.encryptedE2eData // Includes RSA key + HMAC
```

**Important**: KeyDataManager requires Context initialization:
```kotlin
KeyDataManager.getInstance(context)
```

### Testing Strategy
- ViewModel logic testable in isolation (StateFlow patterns)
- UI components use Compose preview for visual testing
- Security layer requires test RSA keys in test assets

## Common Issues

### Korean Input Issues
**Problem**: Korean characters not assembling correctly
- Ensure HangulAssembler instance is maintained per ViewModel (not recreated on each input)
- Check that `commit()` is called when switching from Korean to English input
- Verify backspace calls `HangulAssembler.backspace()` first before deleting from buffer

**Problem**: Korean composition state lost after backspace
- HangulAssembler maintains composition state across backspace operations
- Only clears state when composition is complete or `clear()` is called explicitly
- Check that inputBuffer synchronization matches HangulAssembler state

### Encryption Failures (when integrating security layer)
If `KeyDataException` occurs:
1. Verify `vkeypad_public.pem` exists in `kicasecurekeypad/src/main/assets/`
2. Check RSA key format (PEM format required)
3. Ensure Context passed to KeyDataManager.getInstance()
4. Verify KeyDataManager.initialize() is called before first use

### Layout Not Updating
KeypadViewModel.loadKeys() is automatically called when:
- Language switches (ALPHANUMERIC mode) via SWITCH key
- Shift toggles via SHIFT key
- Shuffle triggered (NUMERIC mode) via SHUFFLE key
- Config type changes via updateConfig()

If keys don't update:
- Check that StateFlow is being observed in UI (collectAsState())
- Verify ViewModel is not recreated unexpectedly
- Ensure ViewModelFactory passes config correctly

### Max Length Errors
When `maxLength` is set in KeypadConfig:
- Error callback fires when attempting to exceed limit
- Korean composition counts assembled syllables, not individual Jamo
- Error message: "최대 N자리까지 입력 가능합니다"
- Check error handling in onError callback

### Memory Management
- KeyDataManager is singleton - uses applicationContext (prevents Activity leaks)
- ViewModel clears inputBuffer and HangulAssembler in onCleared()
- SecureKeypad has DisposableEffect cleanup that calls clearInput()
- HangulAssembler state is lightweight (3 Char? references)

### UI Layout Issues
**Problem**: Buttons too small or overlapping
- Adjust `buttonHeight` in KeypadConfig (default: 48.dp)
- For numeric keypad, buttons use 1:1 aspect ratio
- For alphabetic layouts, height is fixed but width fills available space

**Problem**: Keypad hidden by system keyboard
- Ensure system keyboard is disabled for input field
- Use `imeOptions = ImeOptions.NoExtractUi` if needed
- SecureKeypad includes navigationBarsPadding() for edge-to-edge display