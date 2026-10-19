package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null || intent.action != ReminderScheduler.ACTION_REMINDER_ALARM) {
            return
        }

        val reminderId = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1)
        val reminders = ReminderStorage.loadReminders(context)
        val reminder = reminders.firstOrNull { it.id == reminderId } ?: return
        val isSnooze = intent.getBooleanExtra(ReminderScheduler.EXTRA_IS_SNOOZE, false)

        if (!isSnooze) {
            // A recurring weekly slot fired; roll every reminder's next occurrence forward.
            ReminderScheduler.scheduleAll(context, reminders)
        }

        val serviceIntent = Intent(context, ReminderPlayerService::class.java).apply {
            action = ReminderPlayerService.ACTION_START
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderScheduler.EXTRA_FIRST_NAME, reminder.firstName)
            putExtra(ReminderScheduler.EXTRA_LAST_NAME, reminder.lastName)
            putExtra(ReminderScheduler.EXTRA_MESSAGE, reminder.message)
            putExtra(ReminderScheduler.EXTRA_MELODY, reminder.melody)
            putExtra(ReminderScheduler.EXTRA_SOUND_URI, reminder.soundUri)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
