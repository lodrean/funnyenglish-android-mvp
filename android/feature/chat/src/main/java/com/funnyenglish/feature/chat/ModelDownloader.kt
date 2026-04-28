package com.funnyenglish.feature.chat

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ModelDownloader(private val context: Context) {

    suspend fun download(
        url: String = DEFAULT_DOWNLOAD_URL,
        onProgress: (Float) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(context.filesDir, LocalAiRepository.MODEL_FILENAME)
            val tempFile = File(context.filesDir, "${LocalAiRepository.MODEL_FILENAME}.tmp")

            // Resume partially downloaded file
            val startByte = if (tempFile.exists()) tempFile.length() else 0L

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            if (startByte > 0) {
                connection.setRequestProperty("Range", "bytes=$startByte-")
            }
            connection.connectTimeout = 30_000
            connection.readTimeout = 30_000
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                return@withContext Result.failure(
                    Exception("HTTP $responseCode")
                )
            }

            // If server doesn't support resume (HTTP_OK instead of HTTP_PARTIAL), restart from scratch
            val isResuming = responseCode == HttpURLConnection.HTTP_PARTIAL
            if (!isResuming && tempFile.exists()) {
                tempFile.delete()
            }

            val totalLength = connection.getHeaderFieldLong("Content-Length", -1L) +
                    if (isResuming) startByte else 0L

            connection.inputStream.use { input ->
                val outputMode = if (isResuming && startByte > 0) FileOutputStream(tempFile, true) else FileOutputStream(tempFile)
                outputMode.use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var downloaded = startByte
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloaded += bytesRead
                        if (totalLength > 0) {
                            onProgress(downloaded.toFloat() / totalLength.toFloat())
                        }
                    }
                }
            }

            if (!tempFile.renameTo(outputFile)) {
                // If rename fails, copy and delete temp
                tempFile.inputStream().use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile.delete()
            }

            Result.success(outputFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val BUFFER_SIZE = 8192

        // GitHub Releases fallback URL
        // Replace YOUR_USERNAME and YOUR_REPO with your actual GitHub credentials
        const val DEFAULT_DOWNLOAD_URL =
            "https://github.com/YOUR_USERNAME/YOUR_REPO/releases/download/model-v1.0/gemma-2b-it-gpu-int4.bin"
    }
}
