package com.funnyenglish.feature.chat

import android.util.Log
import java.io.File

/**
 * Checks whether the device supports GPU-accelerated inference via OpenCL.
 *
 * SAFETY: we only check file existence instead of calling System.loadLibrary("OpenCL")
 * because dlopen on some vendor OpenCL drivers can segfault (native crash) and
 * the signal is not catchable from Java/Kotlin.
 */
object GpuCapabilityChecker {

    private const val TAG = "GpuCapabilityChecker"

    private val OPENCL_PATHS = listOf(
        "/vendor/lib64/libOpenCL.so",
        "/vendor/lib/libOpenCL.so",
        "/system/vendor/lib64/libOpenCL.so",
        "/system/vendor/lib/libOpenCL.so",
        "/system/lib64/libOpenCL.so",
        "/system/lib/libOpenCL.so"
    )

    fun hasOpenCL(): Boolean {
        val found = OPENCL_PATHS.any { File(it).exists() }
        Log.d(TAG, if (found) "libOpenCL.so found — GPU likely supported" else "libOpenCL.so not found — GPU not supported")
        return found
    }

    fun hasGpuSupport(): Boolean = hasOpenCL()
}
