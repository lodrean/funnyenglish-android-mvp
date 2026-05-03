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
        sizeLabel = "~1.3GB",
        displayName = "CPU"
    );

    companion object {
        /**
         * Picks the best variant for this device.
         *
         * NOTE: Defaults to CPU to avoid native crashes caused by broken vendor
         * OpenCL drivers (observed on OnePlus and some Samsung devices).
         * GPU can still be selected explicitly by the user in the future.
         */
        fun autoSelect(): ModelVariant {
            return CPU
        }
    }
}
