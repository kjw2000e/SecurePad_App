package com.kica.android.secure.keypad.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Context에서 Activity를 찾는 확장 함수
 *
 * Compose에서 Window 접근 시 사용
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}
