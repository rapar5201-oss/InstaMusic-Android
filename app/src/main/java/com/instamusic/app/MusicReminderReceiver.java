package com.instamusic.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MusicReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && MusicReminderScheduler.ACTION_CHECK_REMINDER.equals(intent.getAction())) {
            MusicReminderScheduler.triggerReminder(context);
        }
    }
}
