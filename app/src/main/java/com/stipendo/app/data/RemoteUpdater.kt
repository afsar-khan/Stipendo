package com.stipendo.app.data

import android.content.Context
import android.util.Log
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads newer data from your website. It is careful, because it replaces what students see:
 *  - https only, short time limits, size limit
 *  - asks the server "has it changed?" first (ETag), so most checks cost almost nothing
 *  - the file must be valid, must not be older than what the phone already has,
 *    and must not suddenly be less than half the size (guards against a half-uploaded file)
 *  - anything wrong: ignored, and the phone keeps using what it has
 * Call from a background thread. Returns the new list, or null when nothing changed.
 */
object RemoteUpdater {
    private const val TAG = "Stipendo"
    private const val PREFS = "stipendo_remote"
    private const val MAX_CHARS = 2_000_000

    fun refresh(context: Context): List<Scholarship>? {
        val url = RemoteConfig.DATA_URL
        if (!url.startsWith("https://")) return null

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 15_000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                prefs.getString("etag", null)?.let { setRequestProperty("If-None-Match", it) }
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null   // 304 = unchanged

            val text = connection.inputStream.bufferedReader().use { readLimited(it) } ?: return null
            val downloaded = ScholarshipRepository.parseDataset(text) ?: return null
            if (downloaded.items.isEmpty()) return null

            val current = ScholarshipRepository.loadDataset(context)
            if (current != null) {
                if ((downloaded.updated ?: "") < (current.updated ?: "")) return null
                if (downloaded.items.size < current.items.size / 2) return null
            }

            ScholarshipRepository.saveCache(context, text)
            prefs.edit().putString("etag", connection.getHeaderField("ETag")).apply()
            return downloaded.items
        } catch (e: Exception) {
            Log.w(TAG, "Data update skipped: ${e.message}")
            return null
        } finally {
            connection?.disconnect()
        }
    }

    private fun readLimited(reader: Reader): String? {
        val out = StringBuilder()
        val buffer = CharArray(8192)
        while (true) {
            val n = reader.read(buffer)
            if (n < 0) break
            out.append(buffer, 0, n)
            if (out.length > MAX_CHARS) return null
        }
        return out.toString()
    }
}
