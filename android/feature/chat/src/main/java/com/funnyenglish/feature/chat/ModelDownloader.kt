package com.funnyenglish.feature.chat

import android.content.Context
import android.util.Log
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

            // If output file already exists, we're done
            if (outputFile.exists()) {
                Log.d(TAG, "Model already exists at ${outputFile.absolutePath}")
                return@withContext Result.success(outputFile.absolutePath)
            }

            val startByte = if (tempFile.exists()) tempFile.length() else 0L
            Log.d(TAG, "Resuming download from byte $startByte, temp file exists: ${tempFile.exists()}")

            val connection = openConnectionWithRange(url, startByte)

            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")

            // Handle 416 Range Not Satisfiable (file already fully downloaded)
            if (responseCode == HTTP_RANGE_NOT_SATISFIABLE) {
                Log.d(TAG, "File already fully downloaded, renaming temp file")
                if (tempFile.exists() && !tempFile.renameTo(outputFile)) {
                    copyAndDelete(tempFile, outputFile)
                }
                return@withContext Result.success(outputFile.absolutePath)
            }

            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                return@withContext Result.failure(Exception("HTTP $responseCode"))
            }

            val isResuming = responseCode == HttpURLConnection.HTTP_PARTIAL
            if (!isResuming && tempFile.exists()) {
                Log.d(TAG, "Server returned HTTP_OK without Range support — restarting download")
                tempFile.delete()
            } else if (isResuming) {
                Log.d(TAG, "Server supports resume, continuing from byte $startByte")
            }

            val contentLength = connection.getHeaderFieldLong("Content-Length", -1L)
            val totalLength = if (isResuming && startByte > 0 && contentLength > 0) {
                contentLength + startByte
            } else {
                contentLength
            }
            Log.d(TAG, "Content-Length: $contentLength, totalLength: $totalLength")

            connection.inputStream.use { input ->
                val outputMode = if (isResuming && startByte > 0) {
                    FileOutputStream(tempFile, true)
                } else {
                    FileOutputStream(tempFile)
                }
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

            Log.d(TAG, "Download complete, renaming temp file to output")
            if (!tempFile.renameTo(outputFile)) {
                copyAndDelete(tempFile, outputFile)
            }

            Result.success(outputFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            Result.failure(e)
        }
    }

    /**
     * Opens a connection with Range header and manually follows redirects,
     * preserving the Range header across redirects (GitHub → S3).
     */
    private fun openConnectionWithRange(
        url: String,
        startByte: Long,
        maxRedirects: Int = 5
    ): HttpURLConnection {
        var currentUrl = url
        var redirects = 0

        while (redirects < maxRedirects) {
            val connection = URL(currentUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 30_000
            connection.readTimeout = 30_000
            connection.instanceFollowRedirects = false

            if (startByte > 0) {
                connection.setRequestProperty("Range", "bytes=$startByte-")
            }

            connection.connect()
            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                responseCode == HTTP_TEMPORARY_REDIRECT ||
                responseCode == HTTP_PERMANENT_REDIRECT
            ) {
                val location = connection.getHeaderField("Location")
                connection.disconnect()
                currentUrl = location ?: throw Exception("Redirect without Location header")
                redirects++
                Log.d(TAG, "Following redirect (#$redirects) to: $currentUrl")
            } else {
                return connection
            }
        }

        throw Exception("Too many redirects")
    }

    private fun copyAndDelete(source: File, dest: File) {
        source.inputStream().use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        source.delete()
    }

    companion object {
        private const val TAG = "ModelDownloader"
        private const val BUFFER_SIZE = 8192
        private const val HTTP_RANGE_NOT_SATISFIABLE = 416
        private const val HTTP_TEMPORARY_REDIRECT = 307
        private const val HTTP_PERMANENT_REDIRECT = 308

        const val DEFAULT_DOWNLOAD_URL =
            "https://github.com/lodrean/funnyenglish-android-mvp/releases/download/model-v1.1/gemma-2b-it-cpu-int4.bin"
    }
}
