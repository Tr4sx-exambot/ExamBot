package com.teahack.exambot

object ScreenReader {
    fun captureAndOcr(): String =
        ExamAccessibilityService.instance?.readScreen() ?: ""
}