package com.example.datamobile.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class TimeBackdateProtector private constructor(context: Context) {

    companion object {
        private const val TAG = "TimeBackdateProtector"
        private const val PREFS_NAME = "time_protector"
        private const val LAST_KNOWN_TIME = "last_known_time"
        private var instance: TimeBackdateProtector? = null

        fun getInstance(context: Context): TimeBackdateProtector {
            if (instance == null) {
                instance = TimeBackdateProtector(context.applicationContext)
            }
            return instance!!
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    var isAppBlocked = false

    fun checkAndUpdateTime(): Boolean {
        val currentTime = System.currentTimeMillis()
        val lastKnownTime = prefs.getLong(LAST_KNOWN_TIME, 0)
        if (lastKnownTime == 0L) {
            prefs.edit().putLong(LAST_KNOWN_TIME, currentTime).apply()
            return false
        }
        if (currentTime < lastKnownTime - 60000) {
            Log.e(TAG, "TIME BACKDATE DETECTED!")
            isAppBlocked = true
            return true
        }
        prefs.edit().putLong(LAST_KNOWN_TIME, currentTime).apply()
        isAppBlocked = false
        return false
    }
}
