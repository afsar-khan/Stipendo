package com.stipendo.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Fires when an alarm goes off and shows the notification. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(Reminders.EXTRA_ID) ?: return
        val name = intent.getStringExtra(Reminders.EXTRA_NAME) ?: return
        val days = intent.getIntExtra(Reminders.EXTRA_DAYS, 0)
        Reminders.show(context, id, name, days)
    }
}
