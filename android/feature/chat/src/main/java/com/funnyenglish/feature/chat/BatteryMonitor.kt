package com.funnyenglish.feature.chat

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build

object BatteryMonitor {
    fun getBatteryPercentage(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            safeRead(context, 100) { intent ->
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level >= 0 && scale > 0) level * 100 / scale else 100
            }
        }
    }

    fun isBatteryLow(context: Context, threshold: Int = 30): Boolean =
        getBatteryPercentage(context) < threshold

    fun isCharging(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        } else {
            safeRead(context, false) { intent ->
                val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
            }
        }
    }

    private fun <T> safeRead(context: Context, default: T, block: (Intent?) -> T): T =
        try { block(context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))) }
        catch (_: Exception) { default }
}
