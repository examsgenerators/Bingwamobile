package com.example.datamobile.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

class EnhancedMidnightRetryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "MidnightRetryWorker"
        private const val WORK_NAME = "midnight_retry_worker"

        fun scheduleMidnightRetryWorker(context: Context) {
            val workManager = WorkManager.getInstance(context)
            val midnightTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }.timeInMillis
            val initialDelay = midnightTime - System.currentTimeMillis()
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = PeriodicWorkRequestBuilder<EnhancedMidnightRetryWorker>(
                1, TimeUnit.DAYS
            ).setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, workRequest
            )
            Log.d(TAG, "Midnight retry worker scheduled")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Midnight retry worker executing")
        return try {
            retryFailedTransactions()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Midnight retry failed: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun retryFailedTransactions() {
        Log.d(TAG, "Retrying failed transactions")
    }
}
