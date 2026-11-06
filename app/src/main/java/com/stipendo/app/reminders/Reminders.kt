package com.stipendo.app.reminders

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.stipendo.app.MainActivity
import com.stipendo.app.R
import com.stipendo.app.data.SavedStore
import com.stipendo.app.data.Scholarship
import com.stipendo.app.ui.AppLanguage
import com.stipendo.app.ui.stringsFor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Deadline reminders for saved scholarships: 7 days and 1 day before the deadline, at 09:00.
 * Uses the phone's alarm service, so no extra libraries are needed. Alarms do not survive a
 * restart by themselves, so BootReceiver and the app start both call rescheduleAll().
 */
object Reminders {
    const val EXTRA_ID = "id"
    const val EXTRA_NAME = "name"
    const val EXTRA_DAYS = "days"
    private const val CHANNEL = "deadlines"
    private const val HOUR = 9
    private val OFFSETS = listOf(7, 1)

    fun schedule(context: Context, s: Scholarship) {
        val deadline = deadlineDay(s.deadline) ?: return
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (offset in OFFSETS) {
            val at = (deadline.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -offset) }.timeInMillis
            if (at <= System.currentTimeMillis()) {
                alarms.cancel(pending(context, s, offset))   // too late for this one: drop any old alarm
                continue
            }
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending(context, s, offset))
        }
    }

    fun cancel(context: Context, s: Scholarship) {
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (offset in OFFSETS) alarms.cancel(pending(context, s, offset))
    }

    fun rescheduleAll(context: Context, all: List<Scholarship>) {
        val saved = SavedStore.load(context)
        all.filter { it.id in saved }.forEach { schedule(context, it) }
    }

    @SuppressLint("MissingPermission")
    fun show(context: Context, id: String, name: String, days: Int) {
        val prefs = context.getSharedPreferences("stipendo", Context.MODE_PRIVATE)
        val t = stringsFor(AppLanguage.fromCode(prefs.getString("lang", null)))
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL, t.channelName, NotificationManager.IMPORTANCE_DEFAULT)
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
        val open = PendingIntent.getActivity(
            context, id.hashCode(),
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val text = t.reminderText(days, name)
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_stipendo)
            .setContentTitle("Stipendo")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        try {
            manager.notify("$id:$days".hashCode(), notification)
        } catch (_: SecurityException) {
            // Notifications not allowed: nothing to do.
        }
    }

    private fun pending(context: Context, s: Scholarship, offset: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(EXTRA_ID, s.id)
            .putExtra(EXTRA_NAME, s.university)
            .putExtra(EXTRA_DAYS, offset)
        return PendingIntent.getBroadcast(
            context, "${s.id}:$offset".hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    /** The deadline day at 09:00 local time, or null if there is no usable date. */
    private fun deadlineDay(iso: String?): Calendar? {
        if (iso == null) return null
        val date = try { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso) } catch (_: Exception) { null } ?: return null
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, HOUR); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
    }
}
