package com.example.reminder

import android.content.Context
import org.json.JSONArray

object ReminderStorage {
    private const val PREFS_NAME = "voice_reminder_prefs"
    private const val KEY_REMINDERS = "reminders"

    fun loadReminders(context: Context): List<Reminder> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_REMINDERS, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<Reminder>()
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i)
                item?.let { list.add(it.toReminder()) }
            }
            list
        } catch (ignored: Exception) {
            emptyList()
        }
    }

    fun saveReminders(context: Context, reminders: List<Reminder>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        reminders.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_REMINDERS, array.toString()).apply()
    }
}
