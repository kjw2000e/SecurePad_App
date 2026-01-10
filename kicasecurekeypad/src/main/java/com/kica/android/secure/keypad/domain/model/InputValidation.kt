package com.kica.android.secure.keypad.domain.model

/**
 * 입력값 검증 결과
 */
data class ValidationResult(
    /** 유효성 여부 */
    val isValid: Boolean,
    
    /** 에러 메시지 (유효하지 않을 경우) */
    val errorMessage: String? = null
) {
    companion object {
        fun valid() = ValidationResult(true)
        fun invalid(message: String) = ValidationResult(false, message)
    }
}

/**
 * 입력 검증 설정
 */
data class InputValidation(
    /** 최소 입력 길이 (null이면 제한 없음) */
    val minLength: Int? = null,
    
    /** 
     * 최대 입력 길이 (KeypadConfig.maxLength보다 우선 적용됨)
     * null이면 KeypadConfig.maxLength를 따름
     */
    val maxLength: Int? = null,
    
    /** 
     * 정규식 패턴 (예: 숫자만, 영문만 등)
     * 키패드 타입에 의해 이미 제한되지만, 추가적인 포맷 검증이 필요할 때 사용
     */
    val regex: Regex? = null,
    
    /** 정규식 검증 실패 시 에러 메시지 */
    val regexErrorMessage: String? = null,
    
    /** 커스텀 검증 함수 */
    val customValidator: ((String) -> ValidationResult)? = null
)
