package com.example.datamobile.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AlarmBootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmBootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device boot completed - rescheduling services")
            EnhancedMidnightRetryWorker.scheduleMidnightRetryWorker(context)
            ConversationExpiryWorker.schedule(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, SmsProcessingService::class.java))
            } else {
                context.startService(Intent(context, SmsProcessingService::class.java))
            }
            Log.d(TAG, "All services rescheduled after boot")
        }
    }
}
