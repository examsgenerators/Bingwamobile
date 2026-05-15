package com.example.datamobile.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log

class SimChangeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SimChangeReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_SIM_CARD_STATE_CHANGED) {
            Log.d(TAG, "SIM card state changed")
            val simState = intent.getIntExtra(TelephonyManager.EXTRA_SIM_STATE, -1)
            when (simState) {
                TelephonyManager.SIM_STATE_LOADED -> {
                    Log.d(TAG, "New SIM loaded - updating SIM selection")
                    updateSimSelection(context)
                }
                TelephonyManager.SIM_STATE_ABSENT -> {
                    Log.d(TAG, "SIM card removed")
                }
            }
        }
    }

    private fun updateSimSelection(context: Context) {
        val prefs = context.getSharedPreferences("sim_prefs", Context.MODE_PRIVATE)
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val subscriptions = telephonyManager.activeSubscriptionInfoList
            if (!subscriptions.isNullOrEmpty()) {
                prefs.edit().putInt("available_sims", subscriptions.size).apply()
                Log.d(TAG, "Updated SIM info: ${subscriptions.size} SIM(s) available")
            }
        }
    }
}
