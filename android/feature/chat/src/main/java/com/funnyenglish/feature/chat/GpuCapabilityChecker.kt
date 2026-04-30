package com.funnyenglish.feature.chat

import android.util.Log

/**
 * Checks whether the device supports GPU-accelerated inference via OpenCL.
 *
 * Note: this is a best-effort check. Some devices have libOpenCL.so but
 * lack required symbols (e.g. clSetPerfHintQCOM). In that case the actual
 * [LocalAiRepository.initModel] will fail and we fall back to the CPU model.
 */
object GpuCapabilityChecker {

    private const val TAG = "GpuCapabilityChecker"

    fun hasOpenCL(): Boolean {
        return try {
            System.loadLibrary("OpenCL")
            Log.d(TAG, "libOpenCL.so found — GPU likely supported")
            true
        } catch (e: UnsatisfiedLinkError) {
            Log.d(TAG, "libOpenCL.so not found — GPU not supported")
            false
        } catch (e: SecurityException) {
            Log.d(TAG, "SecurityException loading libOpenCL.so")
            false
        } catch (e: Exception) {
            Log.d(TAG, "Exception loading libOpenCL.so: ${e.message}")
            false
        }
    }

    fun hasGpuSupport(): Boolean = hasOpenCL()
}
