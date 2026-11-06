package com.stipendo.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.stipendo.app.data.ScholarshipRepository

/** Alarms are cleared when the phone restarts, so set them again for the saved scholarships. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Reminders.rescheduleAll(context, ScholarshipRepository.load(context))
        }
    }
}
