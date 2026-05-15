package com.example.datamobile.utils

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class USSDAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "USSDAccessibility"
        private var instance: USSDAccessibilityService? = null
        private var ussdCallback: ((String) -> Unit)? = null

        fun getInstance(): USSDAccessibilityService? = instance
        fun setCallback(callback: (String) -> Unit) { ussdCallback = callback }
        fun dialUSSD(ussdCode: String) { instance?.dialUSSDCode(ussdCode) }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isProcessing = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        setServiceInfo(info)
        Log.d(TAG, "USSD Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !isProcessing) return
        val className = event.className?.toString() ?: ""
        if (className.contains("AlertDialog") || className.contains("UssdAlertActivity")) {
            val root = rootInActiveWindow ?: return
            handleUSSDDialog(root)
        }
    }

    private fun handleUSSDDialog(root: AccessibilityNodeInfo) {
        val dialogText = extractDialogText(root)
        Log.d(TAG, "USSD Dialog: $dialogText")
        when {
            dialogText.contains("successful", true) || dialogText.contains("completed", true) -> {
                ussdCallback?.invoke("SUCCESS")
                isProcessing = false
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
            dialogText.contains("failed", true) || dialogText.contains("error", true) -> {
                ussdCallback?.invoke("FAILED")
                isProcessing = false
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
            else -> {
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        }
    }

    private fun extractDialogText(node: AccessibilityNodeInfo): String {
        val text = StringBuilder()
        if (node.text != null) text.append(node.text).append(" ")
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            text.append(extractDialogText(child))
            child.recycle()
        }
        return text.toString()
    }

    private fun dialUSSDCode(ussdCode: String) {
        isProcessing = true
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = android.net.Uri.parse("tel:$ussdCode")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        Log.d(TAG, "Dialing USSD: $ussdCode")
    }

    override fun onInterrupt() { isProcessing = false }
    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
