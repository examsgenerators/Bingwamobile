package com.example.datamobile.utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*

class SmsProcessingService : Service() {

    companion object {
        private const val TAG = "SmsProcessingService"
        private var isRunning = false
        private val smsQueue = java.util.concurrent.LinkedBlockingQueue<SmsData>()

        fun queueSms(smsBody: String, sender: String) {
            smsQueue.offer(SmsData(smsBody, sender))
        }

        fun startService(context: android.content.Context) {
            val intent = Intent(context, SmsProcessingService::class.java)
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startProcessing()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    private fun startProcessing() {
        serviceScope.launch {
            while (isRunning) {
                try {
                    val smsData = smsQueue.poll(5, java.util.concurrent.TimeUnit.SECONDS)
                    if (smsData != null) {
                        processSms(smsData)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error: ${e.message}")
                }
            }
        }
    }

    private suspend fun processSms(smsData: SmsData) = withContext(Dispatchers.IO) {
        if (smsData.smsBody.contains("CORMAKS TECH", ignoreCase = true)) {
            val amount = extractAmount(smsData.smsBody)
            val tokens = calculateTokens(amount)
            if (tokens > 0) {
                val tokenManager = TokenManager(this@SmsProcessingService)
                tokenManager.addTokens(amount, tokens, amount >= 500, if (amount >= 500) 30 else 0)
                NotificationHelper.showPaymentSuccess(this@SmsProcessingService, amount, tokens)
            }
        }
    }

    private fun extractAmount(messageBody: String): Int {
        val pattern = Regex("KSh\\s*(\\d+(?:,\\d+)*(?:\\.\\d+)?)", RegexOption.IGNORE_CASE)
        val match = pattern.find(messageBody)
        val amountStr = match?.groupValues?.get(1)?.replace(",", "") ?: return 0
        return amountStr.toDoubleOrNull()?.toInt() ?: 0
    }

    private fun calculateTokens(amount: Int): Int {
        return when {
            amount >= 500 -> 500
            amount >= 250 -> 250
            amount >= 100 -> 100
            amount >= 50 -> 50
            else -> 0
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
    }
}

data class SmsData(val smsBody: String, val senderAddress: String)
