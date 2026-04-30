package com.funnyenglish.feature.chat

import android.content.Context
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import java.io.File

class ModelPathResolver(private val context: Context) {

    /**
     * Resolves the absolute path to the Gemma model.
     *
     * Priority:
     * 1. Play Asset Delivery (install-time asset pack) — for Google Play installs
     * 2. App files directory — for RuStore / manual download / debug
     * 3. null — model not available, user needs to download
     */
    fun resolveModelPath(): String? {
        // 1. Try Play Asset Delivery first
        try {
            val assetPackManager = AssetPackManagerFactory.getInstance(context)
            // Try GPU first, then CPU in asset pack
            val gpuAsset = assetPackManager.getAssetLocation(
                ASSET_PACK_NAME,
                ModelVariant.GPU.fileName
            )
            if (gpuAsset != null) return gpuAsset.path()

            val cpuAsset = assetPackManager.getAssetLocation(
                ASSET_PACK_NAME,
                ModelVariant.CPU.fileName
            )
            if (cpuAsset != null) return cpuAsset.path()
        } catch (e: Exception) {
            // AssetPackManager not available (debug APK, RuStore, etc.)
        }

        // 2. Fallback: filesDir (downloaded model) — prefer GPU, fallback to CPU
        val gpuFile = File(context.filesDir, ModelVariant.GPU.fileName)
        if (gpuFile.exists()) return gpuFile.absolutePath

        val cpuFile = File(context.filesDir, ModelVariant.CPU.fileName)
        if (cpuFile.exists()) return cpuFile.absolutePath

        // 3. Model not found
        return null
    }

    fun resolveModelPath(variant: ModelVariant): String? {
        // Try Asset Pack first for specific variant
        try {
            val assetPackManager = AssetPackManagerFactory.getInstance(context)
            val assetLocation = assetPackManager.getAssetLocation(ASSET_PACK_NAME, variant.fileName)
            if (assetLocation != null) return assetLocation.path()
        } catch (e: Exception) { /* AssetPackManager not available */ }

        // Fallback to filesDir
        val file = File(context.filesDir, variant.fileName)
        return if (file.exists()) file.absolutePath else null
    }

    companion object {
        private const val ASSET_PACK_NAME = "gemma_model"
    }
}
