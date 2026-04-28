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
            val assetLocation = assetPackManager.getAssetLocation(
                ASSET_PACK_NAME,
                LocalAiRepository.MODEL_FILENAME
            )
            if (assetLocation != null) {
                return assetLocation.path()
            }
        } catch (e: Exception) {
            // AssetPackManager not available (debug APK, RuStore, etc.)
        }

        // 2. Fallback: filesDir (downloaded model)
        val downloadedFile = File(context.filesDir, LocalAiRepository.MODEL_FILENAME)
        if (downloadedFile.exists()) {
            return downloadedFile.absolutePath
        }

        // 3. Model not found
        return null
    }

    companion object {
        private const val ASSET_PACK_NAME = "gemma_model"
    }
}
