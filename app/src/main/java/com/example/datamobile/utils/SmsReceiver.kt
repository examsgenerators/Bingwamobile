package com.example.datamobile.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"
        private const val CORMAKS_TILL_NAME = "CORMAKS TECH"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in messages) {
                val messageBody = message.messageBody ?: continue
                val senderNumber = message.originatingAddress ?: ""
                Log.d(TAG, "SMS from: $senderNumber")
                if (senderNumber.contains("MPESA") && messageBody.contains(CORMAKS_TILL_NAME)) {
                    processPaymentSMS(context, messageBody)
                }
            }
        }
    }

    private fun processPaymentSMS(context: Context, messageBody: String) {
        val amount = extractAmount(messageBody)
        val tokens = calculateTokens(amount)
        if (tokens > 0) {
            val tokenManager = TokenManager(context)
            tokenManager.addTokens(amount, tokens, amount >= 500, if (amount >= 500) 30 else 0)
            Log.d(TAG, "Added $tokens tokens for KSh $amount")
            NotificationHelper.showPaymentSuccess(context, amount, tokens)
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
}
