# Changelog

All notable changes to KICASecureKeypad are documented here.

---

## [1.0.0] - 2026-01-11

### ✨ Features

#### Core
- 숫자/영문/한글/특수문자 키패드 지원
- 한글 자모 조합 (천지인/2벌식)
- 랜덤 키 배열 (어깨너머 공격 방지)
- 햅틱 피드백

#### Security
- AES-256/CBC 키 입력 암호화
- RSA-2048 세션 키 교환
- HMAC-SHA1 무결성 검증
- 화면 캡처 방지 (FLAG_SECURE)
- 메모리 보안 (`secureClear()`, `secureClearInput()`)

#### UI/UX
- 3가지 표시 모드 (FULL, HALF, COMPACT)
- 4가지 인디케이터 스타일 (DOT, BOX, UNDERLINE, TEXT)
- 고정/가변 입력 슬롯
- 다크 모드 자동 전환
- 제목/부제목/취소 버튼 지원

#### Themes
- `KeypadColors.kica()` - KICA 스타일
- `KeypadColors.dark()` - 다크 테마
- `KeypadColors.lavender()` - 연보라 테마
- 커스텀 색상 지원

#### Validation
- `InputValidation` 클래스 (minLength, maxLength, regex, customValidator)
- 검증 실패 시 에러 메시지 표시
- 검증 실패 시 완료 버튼 비활성화 + 햅틱 피드백

#### Callbacks
- `onKeyPressed` - 키 입력마다 호출 (마스킹값)
- `onComplete` - 완료 버튼 클릭
- `onCancel` - 취소 버튼 클릭
- `onError` - 에러 발생
- `onShow` - 키패드 표시
- `onHide` - 키패드 사라짐
- `onBackspace` - 삭제 키 클릭

#### Sample App
- 5가지 시나리오 프리셋 (PIN, 간편 비밀번호, 계정 비밀번호, OTP, 금액)
- 테마 선택기
- 옵션 토글 (랜덤화, 햅틱)

### 📝 Documentation
- `README.md` - 빠른 시작 가이드
- `개발자가이드.md` - API 사용법
- `라이브러리 분석.md` - 아키텍처 설명
- `암호화_분석.md` - 보안 상세
- `product-page.html` - 제품 소개 페이지

---

## [0.9.0] - 2025-12-XX (Initial Development)

- 기본 키패드 기능 구현
- 암호화 기능 초기 버전
