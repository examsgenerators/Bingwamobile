package com.example.datamobile.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

class ConversationExpiryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ConversationExpiry"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<ConversationExpiryWorker>(
                24, TimeUnit.HOURS
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "conversation_expiry", ExistingPeriodicWorkPolicy.KEEP, workRequest
            )
            Log.d(TAG, "Conversation expiry worker scheduled")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Checking for expired conversations")
        return try {
            deleteExpiredConversations()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Conversation expiry failed: ${e.message}")
            Result.retry()
        }
    }

    private fun deleteExpiredConversations() {
        val expiryTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        Log.d(TAG, "Deleting conversations before timestamp: $expiryTime")
    }
}
