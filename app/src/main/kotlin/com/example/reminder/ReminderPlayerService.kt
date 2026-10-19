package com.example.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import java.util.Locale

class ReminderPlayerService : Service() {
    private var tts: TextToSpeech? = null
    private var ringtone: Ringtone? = null
    private var currentReminderId: Long = -1L
    private val stopHandler = Handler(Looper.getMainLooper())
    private val stopRunnable = Runnable { stopSelf() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_NOT_STICKY
        }

        if (intent.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (intent.action == ACTION_SNOOZE) {
            val reminder = ReminderStorage.loadReminders(this).firstOrNull { it.id == currentReminderId }
            if (reminder != null) {
                ReminderScheduler.scheduleSnooze(this, reminder)
            }
            stopSelf()
            return START_NOT_STICKY
        }

        currentReminderId = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1L)
        startForeground(REMINDER_NOTIFICATION_ID, buildNotification())

        val firstName = intent.getStringExtra(ReminderScheduler.EXTRA_FIRST_NAME).orEmpty()
        val message = intent.getStringExtra(ReminderScheduler.EXTRA_MESSAGE).orEmpty()
        val melody = intent.getStringExtra(ReminderScheduler.EXTRA_MELODY).orEmpty().ifBlank { "Klasik" }
        val soundUri = intent.getStringExtra(ReminderScheduler.EXTRA_SOUND_URI)

        startRingtone(melody, soundUri)
        initTextToSpeech(firstName, message)

        stopHandler.removeCallbacks(stopRunnable)
        stopHandler.postDelayed(stopRunnable, REMINDER_DURATION_MS)
        return START_STICKY
    }

    override fun onDestroy() {
        stopHandler.removeCallbacks(stopRunnable)
        stopRingtone()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    private fun initTextToSpeech(firstName: String, message: String) {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = Locale("tr", "TR")
                tts?.language = locale
                selectFemaleVoice(locale)
                val spokenName = firstName.takeIf { it.isNotBlank() } ?: ""
                val text = if (spokenName.isNotBlank()) {
                    "$spokenName hanım, hatırlatma: $message"
                } else {
                    "Hatırlatma: $message"
                }
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "REMINDER_MESSAGE")
            }
        }
    }

    private fun selectFemaleVoice(locale: Locale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val available = tts?.voices.orEmpty()
            val candidate = available
                .firstOrNull { it.locale == locale && (it.name.contains("female", true) || it.name.contains("woman", true)) }
                ?: available.firstOrNull { it.locale == locale }
            candidate?.let { tts?.voice = it }
        }
    }

    private fun startRingtone(melody: String, soundUri: String?) {
        val customUri = soundUri?.let {
            runCatching { Uri.parse(it) }.getOrNull()
        }

        val playable = customUri?.let { uri ->
            runCatching { RingtoneManager.getRingtone(this, uri) }.getOrNull()
        }

        ringtone = playable ?: RingtoneManager.getRingtone(this, presetToneUri(melody))
        ringtone?.play()
    }

    private fun presetToneUri(melody: String): Uri = when (melody) {
        "Yumuşak" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        "Nazik" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    }

    private fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, ReminderPlayerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, ReminderPlayerService::class.java).apply {
            action = ACTION_SNOOZE
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, REMINDER_CHANNEL_ID)
            .setContentTitle("Hatırlatıcı aktif")
            .setContentText("Durdurmak veya ertelemek için bir seçenek seçin.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "Durdur", stopPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_recent_history,
                "Ertele (${ReminderScheduler.DEFAULT_SNOOZE_MINUTES} dk)",
                snoozePendingIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Hatırlatıcı Sesleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Hatırlatma çalarken kullanılacak bildirim kanalı"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_START = "com.example.reminder.ACTION_START"
        const val ACTION_STOP = "com.example.reminder.ACTION_STOP"
        const val ACTION_SNOOZE = "com.example.reminder.ACTION_SNOOZE"
        const val REMINDER_CHANNEL_ID = "reminder_channel"
        const val REMINDER_NOTIFICATION_ID = 1001
        const val REMINDER_DURATION_MS = 60_000L
    }
}
