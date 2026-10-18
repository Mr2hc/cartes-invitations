package com.invitecard.util

import android.content.Context
import com.invitecard.viewmodel.VisualElement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object DraftManager {
    private const val PREFS_NAME = "invitecard_drafts"
    private const val KEY_ELEMENTS = "elements_draft"

    fun saveDraft(context: Context, elements: List<VisualElement>) {
        val json = Json.encodeToString(elements)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ELEMENTS, json)
            .apply()
    }

    fun loadDraft(context: Context): List<VisualElement>? {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ELEMENTS, null) ?: return null
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            null
        }
    }

    fun clearDraft(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_ELEMENTS)
            .apply()
    }
}
