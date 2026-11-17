package com.pfayoux.edwige.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.pfayoux.edwige.MainActivity
import com.pfayoux.edwige.R
import com.pfayoux.edwige.data.ProgressStore
import com.pfayoux.edwige.domain.AdventCalendar
import com.pfayoux.edwige.domain.Messages
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/** Programmation des rappels, vue depuis le jeu. */
interface ReminderScheduler {
    fun reschedule()
    fun showNow()
}

class AndroidReminderScheduler(private val context: Context) : ReminderScheduler {
    override fun reschedule() = Reminders.reschedule(context)
    override fun showNow() = Reminders.show(context)
}

/**
 * Rappel du matin, entre 9 h 00 et 9 h 10, du 1er au 24 décembre (vraie date du téléphone).
 * Une alarme « à fenêtre » ne demande pas la permission des alarmes exactes.
 */
object Reminders {
    private const val CHANNEL_ID = "carte_du_jour"
    private const val NOTIFICATION_ID = 1
    private val REMINDER_TIME: LocalTime = LocalTime.of(9, 0)
    private const val WINDOW_MILLIS = 10 * 60 * 1000L

    fun nextReminder(now: LocalDateTime): LocalDateTime? {
        var candidate = LocalDateTime.of(now.toLocalDate(), REMINDER_TIME)
        if (!now.isBefore(candidate)) candidate = candidate.plusDays(1)
        val first = LocalDateTime.of(AdventCalendar.unlockDate(1), REMINDER_TIME)
        if (candidate.isBefore(first)) candidate = first
        return if (candidate.toLocalDate().isAfter(AdventCalendar.unlockDate(AdventCalendar.DAYS))) null else candidate
    }

    fun reschedule(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pending = pendingIntent(context)
        alarmManager.cancel(pending)
        if (!ProgressStore(context).load().notificationsEnabled) return
        val next = nextReminder(LocalDateTime.now()) ?: return
        val millis = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, millis, WINDOW_MILLIS, pending)
    }

    /** Envoie le rappel sauf si la carte du jour a déjà été jouée. */
    fun onReminderTime(context: Context) {
        val save = ProgressStore(context).load()
        val day = AdventCalendar.dayOf(LocalDate.now())
        if (save.notificationsEnabled && day != null && !save.progress(day).isStarted) {
            show(context)
        }
        reschedule(context)
    }

    fun show(context: Context) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        ensureChannel(context)
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFFB3342E.toInt())
            .setContentTitle("Calendrier de l'Avent")
            .setContentText(Messages.pick(Messages.notifications))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Carte du jour", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Le petit rappel du matin quand une nouvelle carte t'attend"
            },
        )
    }

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context, 0, Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Reminders.onReminderTime(context)
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Reminders.reschedule(context)
}
