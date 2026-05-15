package com.example.datamobile.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.Date
import java.util.concurrent.TimeUnit

class TokenManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("token_manager", Context.MODE_PRIVATE)

    fun getTokenBalance(): Int = prefs.getInt("token_balance", 0)

    fun addTokens(amount: Int, tokens: Int, isUnlimited: Boolean = false, days: Int = 0) {
        if (isUnlimited) {
            prefs.edit().putBoolean("is_unlimited", true).apply()
            val expiry = Date().time + TimeUnit.DAYS.toMillis(days.toLong())
            prefs.edit().putLong("unlimited_expiry", expiry).apply()
        } else {
            val currentBalance = getTokenBalance()
            prefs.edit().putInt("token_balance", currentBalance + tokens).apply()
        }
        val transaction = Transaction(
            id = System.currentTimeMillis().toInt(),
            description = "Token Purchase - $tokens tokens for KSh $amount",
            tokens = tokens,
            type = "PURCHASE",
            status = "COMPLETED",
            date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(Date())
        )
        saveTransaction(transaction)
    }

    fun useToken(): Boolean {
        if (isUnlimited()) return true
        val currentBalance = getTokenBalance()
        if (currentBalance > 0) {
            prefs.edit().putInt("token_balance", currentBalance - 1).apply()
            val transaction = Transaction(
                id = System.currentTimeMillis().toInt(),
                description = "USSD Transaction",
                tokens = 1,
                type = "USAGE",
                status = "COMPLETED",
                date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(Date())
            )
            saveTransaction(transaction)
            return true
        }
        return false
    }

    fun isUnlimited(): Boolean {
        val isUnlimited = prefs.getBoolean("is_unlimited", false)
        if (isUnlimited) {
            val expiry = prefs.getLong("unlimited_expiry", 0)
            if (expiry > 0 && expiry < Date().time) {
                prefs.edit().putBoolean("is_unlimited", false).apply()
                return false
            }
        }
        return isUnlimited
    }

    fun getAutoRenew(): Boolean = prefs.getBoolean("auto_renew", false)

    fun setAutoRenew(enabled: Boolean) {
        prefs.edit().putBoolean("auto_renew", enabled).apply()
    }

    private fun saveTransaction(transaction: Transaction) {
        val db = TransactionDatabase.getInstance(context)
        db.insertTransaction(transaction)
    }
}

data class Transaction(
    val id: Int,
    val description: String,
    val tokens: Int,
    val type: String,
    val status: String,
    val date: String
)
