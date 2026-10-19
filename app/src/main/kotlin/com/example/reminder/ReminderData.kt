package com.example.reminder

import androidx.compose.ui.graphics.Color
import com.example.reminder.ui.theme.CategoryHealth
import com.example.reminder.ui.theme.CategoryOther
import com.example.reminder.ui.theme.CategoryPersonal
import com.example.reminder.ui.theme.CategoryWork
import org.json.JSONArray
import org.json.JSONObject

enum class ReminderCategory(val label: String, val color: Color) {
    WORK("İş", CategoryWork),
    HEALTH("Sağlık", CategoryHealth),
    PERSONAL("Kişisel", CategoryPersonal),
    OTHER("Diğer", CategoryOther);

    companion object {
        fun fromLabel(label: String): ReminderCategory =
            entries.firstOrNull { it.label.normalizeForSearch() == label.normalizeForSearch() } ?: OTHER
    }
}

/** Marker stored in [Reminder.melody] when a custom sound file is used instead of a preset. */
const val CUSTOM_MELODY_LABEL = "Özel"

data class Reminder(
    val id: Long = System.currentTimeMillis(),
    val firstName: String,
    val lastName: String,
    val message: String,
    val melody: String = "Klasik",
    val soundUri: String? = null,
    val category: ReminderCategory = ReminderCategory.OTHER,
    val days: Set<Int> = emptySet(),
    val times: List<String> = emptyList(),
    val enabled: Boolean = true
)

internal fun Reminder.toJson(): JSONObject {
    val json = JSONObject()
    json.put("id", id)
    json.put("firstName", firstName)
    json.put("lastName", lastName)
    json.put("message", message)
    json.put("melody", melody)
    json.put("soundUri", soundUri)
    json.put("category", category.name)
    json.put("enabled", enabled)
    val daysArray = JSONArray()
    days.sorted().forEach { daysArray.put(it) }
    json.put("days", daysArray)
    val timeArray = JSONArray()
    times.forEach { timeArray.put(it) }
    json.put("times", timeArray)
    return json
}

internal fun JSONObject.toReminder(): Reminder {
    val id = optLong("id", System.currentTimeMillis())
    val firstName = optString("firstName", "")
    val lastName = optString("lastName", "")
    val message = optString("message", "")
    val melody = optString("melody", "Klasik")
    val soundUri = if (has("soundUri") && !isNull("soundUri")) optString("soundUri") else null
    val category = ReminderCategory.entries.firstOrNull { it.name == optString("category", "") }
        ?: ReminderCategory.OTHER
    val enabled = optBoolean("enabled", true)
    val days = mutableSetOf<Int>()
    optJSONArray("days")?.let { array ->
        for (i in 0 until array.length()) {
            days.add(array.optInt(i, 0))
        }
    }
    val times = mutableListOf<String>()
    optJSONArray("times")?.let { array ->
        for (i in 0 until array.length()) {
            array.optString(i, "").takeIf { it.isNotBlank() }?.let { times.add(it) }
        }
    }
    return Reminder(
        id = id,
        firstName = firstName,
        lastName = lastName,
        message = message,
        melody = melody,
        soundUri = soundUri,
        category = category,
        days = days,
        times = times,
        enabled = enabled
    )
}
