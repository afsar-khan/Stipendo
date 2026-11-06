package com.stipendo.app.data

import android.content.Context

/** The ids of the scholarships the student saved. Kept on the phone only. */
object SavedStore {
    private const val PREFS = "stipendo"
    private const val KEY = "saved_ids"

    fun load(context: Context): Set<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getStringSet(KEY, emptySet())?.toSet() ?: emptySet()

    fun save(context: Context, ids: Set<String>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putStringSet(KEY, ids.toSet()).apply()
    }
}
