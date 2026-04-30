package com.funnyenglish.feature.chat

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log

object BatteryMonitor {

    private const val TAG = "BatteryMonitor"

    fun getBatteryPercentage(context: Context): Int {
        return try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                (level * 100 / scale)
            } else {
                100
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get battery percentage", e)
            100 // assume full on error
        }
    }

    fun isBatteryLow(context: Context, threshold: Int = 30): Boolean {
        return try {
            getBatteryPercentage(context) < threshold
        } catch (e: Exception) {
            false // don't block on error
        }
    }

    fun isCharging(context: Context): Boolean {
        return try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check charging status", e)
            false
        }
    }
}
