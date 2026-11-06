package com.stipendo.app.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import java.io.File
import org.json.JSONObject

/**
 * Loads the scholarships: the file bundled in the app (app/src/main/assets/scholarships.json, made by
 * tools/csv_to_json.py) and, if newer, the copy the app downloaded from your website.
 * One bad entry is skipped and logged (tag "Stipendo"); it never crashes the app.
 */
/** A data file: the date the converter wrote it, and its entries. */
data class Dataset(val updated: String?, val items: List<Scholarship>)

object ScholarshipRepository {
    private const val FILE = "scholarships.json"
    private const val CACHE_FILE = "scholarships_cache.json"
    private const val TAG = "Stipendo"

    /** The best data on the phone: the downloaded copy if it is at least as new as the bundled one. */
    fun load(context: Context): List<Scholarship> = loadDataset(context)?.items ?: emptyList()

    fun loadDataset(context: Context): Dataset? {
        val bundled = readBundled(context)
        val cached = readCache(context)
        return when {
            cached == null -> bundled
            bundled == null -> cached
            (cached.updated ?: "") >= (bundled.updated ?: "") -> cached
            else -> bundled
        }
    }

    /** Saves a downloaded file. Written to a temporary file first so a crash cannot leave half a file. */
    fun saveCache(context: Context, text: String) {
        val temp = File(context.filesDir, "$CACHE_FILE.tmp")
        temp.writeText(text, Charsets.UTF_8)
        temp.renameTo(File(context.filesDir, CACHE_FILE))
    }

    /** Reads a data file. Null if it is not a valid file at all; a bad single entry is skipped. */
    fun parseDataset(text: String): Dataset? {
        val root = try { JSONObject(text) } catch (e: Exception) { return null }
        val array: JSONArray = root.optJSONArray("scholarships") ?: return null
        val items = mutableListOf<Scholarship>()
        for (i in 0 until array.length()) {
            try {
                items += parse(array.getJSONObject(i))
            } catch (e: Exception) {
                Log.w(TAG, "Skipping entry #$i: ${e.message}")
            }
        }
        return Dataset(root.optString("updated").takeIf { it.isNotBlank() }, items)
    }

    private fun readBundled(context: Context): Dataset? = try {
        parseDataset(context.assets.open(FILE).bufferedReader().use { it.readText() })
    } catch (e: Exception) {
        Log.e(TAG, "Could not read bundled $FILE: ${e.message}")
        null
    }

    private fun readCache(context: Context): Dataset? {
        val file = File(context.filesDir, CACHE_FILE)
        if (!file.exists()) return null
        return try { parseDataset(file.readText(Charsets.UTF_8)) } catch (e: Exception) { null }
    }

    private fun parse(o: JSONObject): Scholarship {
        val officialUrl = o.str("officialUrl")
        val lastChecked = o.str("lastChecked")

        var verification = enumValueOf<Verification>(o.getString("verification").trim().uppercase())
        // Trust rule, enforced here too: "checked" needs an official page and a check date.
        if (verification == Verification.OFFICIAL_CHECKED && (officialUrl == null || lastChecked == null)) {
            verification = Verification.REPORTED
        }

        val stipendObject = o.optJSONObject("stipendRmbByDegree")
        val stipends = buildMap<DegreeLevel, Int> {
            stipendObject?.keys()?.forEach { key ->
                put(enumValueOf<DegreeLevel>(key.trim().uppercase()), stipendObject.getInt(key))
            }
        }

        return Scholarship(
            id = o.getString("id"),
            university = o.getString("university"),
            universityZh = o.str("universityZh"),
            city = o.getString("city"),
            name = o.getString("name"),
            provider = o.getString("provider"),
            degrees = o.enumList<DegreeLevel>("degrees"),
            languages = o.enumList<TeachingLanguage>("languages"),
            lengths = o.enumList<StudyLength>("lengths"),
            funding = enumValueOf<Funding>(o.getString("funding").trim().uppercase()),
            coversTuition = o.bool("coversTuition"),
            coversHousing = o.bool("coversHousing"),
            tuitionRmbPerYear = o.int("tuitionRmbPerYear"),
            stipendRmbByDegree = stipends,
            languageRequirement = o.str("languageRequirement"),
            deadline = o.str("deadline"),
            cscDeadline = o.str("cscDeadline"),
            cscAgencyNumber = o.str("cscAgencyNumber"),
            officialUrl = officialUrl,
            sourceUrl = o.getString("sourceUrl"),
            verification = verification,
            lastChecked = lastChecked,
            addedOn = o.str("addedOn"),
            updatedOn = o.str("updatedOn"),
        )
    }

    private fun JSONObject.str(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key).takeIf { it.isNotBlank() }

    private fun JSONObject.int(key: String): Int? =
        if (!has(key) || isNull(key)) null else getInt(key)

    private fun JSONObject.bool(key: String): Boolean? =
        if (!has(key) || isNull(key)) null else getBoolean(key)

    private inline fun <reified T : Enum<T>> JSONObject.enumList(key: String): List<T> {
        val array = optJSONArray(key) ?: return emptyList()
        return List(array.length()) { enumValueOf<T>(array.getString(it).trim().uppercase()) }
    }
}
