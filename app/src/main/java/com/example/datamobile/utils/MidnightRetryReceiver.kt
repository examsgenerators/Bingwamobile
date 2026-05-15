package com.example.datamobile.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log

class MidnightRetryReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "MidnightRetryReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Midnight retry triggered!")
        try {
            retryFailedTransactions(context)
            cleanupOldRecords(context)
        } catch (e: Exception) {
            Log.e(TAG, "Midnight retry failed: ${e.message}")
        }
    }

    private fun retryFailedTransactions(context: Context) {
        val prefs = context.getSharedPreferences("failed_transactions", Context.MODE_PRIVATE)
        val failedTransactions = prefs.getStringSet("failed_list", emptySet()) ?: emptySet()
        if (failedTransactions.isNotEmpty()) {
            Log.d(TAG, "Retrying ${failedTransactions.size} failed transactions")
            prefs.edit().remove("failed_list").apply()
            for (transactionId in failedTransactions) {
                processFailedTransaction(context, transactionId)
            }
        }
    }

    private fun processFailedTransaction(context: Context, transactionId: String) {
        val prefs = context.getSharedPreferences("pending_ussd", Context.MODE_PRIVATE)
        val ussdCode = prefs.getString("ussd_$transactionId", null)
        if (ussdCode != null) {
            USSDAccessibilityService.setCallback { result ->
                if (result == "SUCCESS") {
                    prefs.edit().remove("ussd_$transactionId").apply()
                    val amount = prefs.getInt("amount_$transactionId", 0)
                    if (amount > 0) {
                        val tokenManager = TokenManager(context)
                        val tokens = when {
                            amount >= 500 -> 500
                            amount >= 250 -> 250
                            amount >= 100 -> 100
                            amount >= 50 -> 50
                            else -> 0
                        }
                        tokenManager.addTokens(amount, tokens, amount >= 500, if (amount >= 500) 30 else 0)
                    }
                }
            }
            Handler(Looper.getMainLooper()).postDelayed({
                USSDAccessibilityService.dialUSSD(ussdCode)
            }, 1000)
        }
    }

    private fun cleanupOldRecords(context: Context) {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val db = TransactionDatabase.getInstance(context)
        val oldTransactions = db.getAllTransactions().filter {
            try {
                val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).parse(it.date)
                (date?.time ?: 0) < thirtyDaysAgo
            } catch (e: Exception) {
                false
            }
        }
        for (transaction in oldTransactions) {
            db.deleteTransaction(transaction.id)
        }
        Log.d(TAG, "Cleaned up ${oldTransactions.size} old transactions")
    }
}
