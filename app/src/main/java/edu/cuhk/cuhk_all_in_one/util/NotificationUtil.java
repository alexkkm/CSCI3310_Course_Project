package edu.cuhk.cuhk_all_in_one.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import edu.cuhk.cuhk_all_in_one.R;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteReminder;

public class NotificationUtil extends ContextWrapper {

    private NotificationManager manager;
    private Context context;

    public static final String CHANNEL_ID = "MapNotes_Channel";
    public static final String CHANNEL_NAME = "MapNotes Channel";

    public NotificationUtil(Context base) {
        super(base);
        context = base;
        createChannel();
    }

    public NotificationCompat.Builder setNotification(String title, String body) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.common_full_open_on_phone)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);
    }

    private void createChannel()
    {
        // todo why wont the notification become a heads up notification?
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager()
    {
        if(manager == null)
        {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return manager;
    }

    public void setReminder(long timeInMillis)
    {
        Intent intent = new Intent(context, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        manager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
    }

    public void setNoteEntryReminder(NoteReminder reminder) {
        Intent intent = new Intent(context, ReminderBroadcast.class);
        intent.putExtra("noteEntryUid", reminder.noteUid);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        manager.set(AlarmManager.RTC_WAKEUP, reminder.reminderTimestamp, pendingIntent);
    }

}
