package com.teahack.exambot

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ExamAccessibilityService : AccessibilityService() {

    companion object { var instance: ExamAccessibilityService? = null }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    fun tapAt(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 80)
        dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    fun swipeUp() {
        val path = Path().apply {
            moveTo(540f, 1600f); lineTo(540f, 400f)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 300)
        dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    fun readScreen(): String {
        val root = rootInActiveWindow ?: return ""
        return collect(root)
    }

    private fun collect(node: AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        node.text?.let { sb.append(it).append(" ") }
        node.contentDescription?.let { sb.append(it).append(" ") }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { sb.append(collect(it)) }
        }
        return sb.toString()
    }

    fun clickByText(target: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val node = findNode(root, target) ?: return false
        val rect = Rect()
        node.getBoundsInScreen(rect)
        tapAt(rect.exactCenterX(), rect.exactCenterY())
        return true
    }

    private fun findNode(node: AccessibilityNodeInfo, txt: String): AccessibilityNodeInfo? {
        if (node.text?.contains(txt, true) == true) return node
        if (node.contentDescription?.contains(txt, true) == true) return node
        for (i in 0 until node.childCount) {
            findNode(node.getChild(i) ?: continue, txt)?.let { return it }
        }
        return null
    }
}