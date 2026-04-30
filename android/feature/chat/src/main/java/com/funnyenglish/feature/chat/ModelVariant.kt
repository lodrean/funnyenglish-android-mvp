package com.funnyenglish.feature.chat

enum class ModelVariant(
    val fileName: String,
    val downloadUrl: String,
    val sizeLabel: String,
    val displayName: String
) {
    GPU(
        fileName = "gemma-2b-it-gpu-int4.bin",
        downloadUrl = "https://github.com/lodrean/funnyenglish-android-mvp/releases/download/model-v1.0/gemma-2b-it-gpu-int4.bin",
        sizeLabel = "~1.3GB",
        displayName = "GPU"
    ),
    CPU(
        fileName = "gemma-2b-it-cpu-int4.bin",
        downloadUrl = "https://github.com/lodrean/funnyenglish-android-mvp/releases/download/model-v1.1/gemma-2b-it-cpu-int4.bin",
        sizeLabel = "~2.5GB",
        displayName = "CPU"
    );

    companion object {
        /**
         * Picks the best variant for this device:
         * - GPU if OpenCL is available (faster, smaller)
         * - CPU as fallback (works everywhere)
         */
        fun autoSelect(): ModelVariant {
            return if (GpuCapabilityChecker.hasGpuSupport()) GPU else CPU
        }
    }
}
