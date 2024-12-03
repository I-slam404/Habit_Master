package com.example.habitmaster;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class MyNotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "HabitReminderChannel";
    private static final int NOTIFICATION_ID = 1001; // Use a constant for the ID

    @Override
    public void onReceive(Context context, Intent intent) {
        String habitName = intent.getStringExtra("habit_name");
        int habitId = intent.getIntExtra("habit_id", 0); // Use a unique habit ID

        if (habitName == null) {
            habitName = "Unknown"; // Default if habit name is missing
        }

        createNotificationChannel(context);

        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher) // Ensure this resource exists
                    .setContentTitle("Habit Reminder")
                    .setContentText("Your habit \"" + habitName + "\" is due in 5 minutes!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(habitId, builder.build()); // Use habitId as the notification ID
            }
        } catch (Exception e) {
            Log.e("NotificationReceiver", "Error while sending notification", e);
        }
    }


    private void createNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Habit Reminder", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for Habit Reminders");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
        // For pre-O devices, we don't need to do anything for the channel
    }
}