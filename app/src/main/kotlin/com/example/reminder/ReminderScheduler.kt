package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ReminderScheduler {
    const val ACTION_REMINDER_ALARM = "com.example.reminder.ACTION_REMINDER_ALARM"
    const val EXTRA_REMINDER_ID = "reminder_id"
    const val EXTRA_FIRST_NAME = "first_name"
    const val EXTRA_LAST_NAME = "last_name"
    const val EXTRA_MESSAGE = "message"
    const val EXTRA_MELODY = "melody"
    const val EXTRA_SOUND_URI = "sound_uri"
    const val EXTRA_DAY = "day"
    const val EXTRA_TIME = "time"
    const val EXTRA_IS_SNOOZE = "is_snooze"
    const val DEFAULT_SNOOZE_MINUTES = 10

    fun scheduleAll(context: Context, reminders: List<Reminder>) {
        cancelAll(context, reminders)
        reminders.filter { it.enabled }.forEach { scheduleReminder(context, it) }
    }

    fun cancelAll(context: Context, reminders: List<Reminder>) {
        reminders.forEach { cancelReminder(context, it) }
    }

    fun scheduleReminder(context: Context, reminder: Reminder) {
        reminder.days.sorted().forEach { day ->
            reminder.times.forEach { time ->
                scheduleAlarm(context, reminder, day, time)
            }
        }
    }

    fun cancelReminder(context: Context, reminder: Reminder) {
        reminder.days.sorted().forEach { day ->
            reminder.times.forEach { time ->
                cancelAlarm(context, reminder, day, time)
            }
        }
    }

    private fun scheduleAlarm(context: Context, reminder: Reminder, day: Int, time: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = calculateNextTrigger(day, time)
        val pendingIntent = getAlarmPendingIntent(context, reminder, day, time)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    private fun cancelAlarm(context: Context, reminder: Reminder, day: Int, time: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = getAlarmPendingIntent(context, reminder, day, time)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun getAlarmPendingIntent(context: Context, reminder: Reminder, day: Int, time: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER_ALARM
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_FIRST_NAME, reminder.firstName)
            putExtra(EXTRA_LAST_NAME, reminder.lastName)
            putExtra(EXTRA_MESSAGE, reminder.message)
            putExtra(EXTRA_MELODY, reminder.melody)
            putExtra(EXTRA_SOUND_URI, reminder.soundUri)
            putExtra(EXTRA_DAY, day)
            putExtra(EXTRA_TIME, time)
        }
        val requestCode = ("${reminder.id}:$day:$time").hashCode()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Schedules a one-off re-ring [minutes] from now, independent of the weekly recurring slots. */
    fun scheduleSnooze(context: Context, reminder: Reminder, minutes: Int = DEFAULT_SNOOZE_MINUTES) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + minutes * 60_000L
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER_ALARM
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_FIRST_NAME, reminder.firstName)
            putExtra(EXTRA_LAST_NAME, reminder.lastName)
            putExtra(EXTRA_MESSAGE, reminder.message)
            putExtra(EXTRA_MELODY, reminder.melody)
            putExtra(EXTRA_SOUND_URI, reminder.soundUri)
            putExtra(EXTRA_IS_SNOOZE, true)
        }
        val requestCode = ("${reminder.id}:snooze").hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    private fun calculateNextTrigger(day: Int, time: String): Long {
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance()
        val (hour, minute) = time.split(":").map { it.toIntOrNull() ?: 0 }
        trigger.set(Calendar.HOUR_OF_DAY, hour)
        trigger.set(Calendar.MINUTE, minute)
        trigger.set(Calendar.SECOND, 0)
        trigger.set(Calendar.MILLISECOND, 0)

        val targetDay = adaptDayToCalendar(day)
        val currentDay = trigger.get(Calendar.DAY_OF_WEEK)
        val daysUntil = ((targetDay - currentDay) + 7) % 7
        if (daysUntil == 0 && trigger.timeInMillis <= now.timeInMillis) {
            trigger.add(Calendar.DAY_OF_YEAR, 7)
        } else {
            trigger.add(Calendar.DAY_OF_YEAR, daysUntil)
        }
        return trigger.timeInMillis
    }

    private fun adaptDayToCalendar(day: Int): Int {
        return when (day) {
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            7 -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }
    }
}
