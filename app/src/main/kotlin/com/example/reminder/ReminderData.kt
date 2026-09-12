package com.example.reminder

import org.json.JSONArray
import org.json.JSONObject

data class Reminder(
    val id: Long = System.currentTimeMillis(),
    val firstName: String,
    val lastName: String,
    val message: String,
    val melody: String = "Klasik",
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
        days = days,
        times = times,
        enabled = enabled
    )
}
