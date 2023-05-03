package edu.cuhk.cuhk_all_in_one.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import edu.cuhk.cuhk_all_in_one.activities.MapsActivity;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteEntry;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteReminder;

public class ReminderBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent)
    {
        NotificationUtil util = new NotificationUtil(context);
        // check the details!
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            // check
            int noteEntryUid = bundle.getInt("noteEntryUid", -1);
            if (noteEntryUid >= 0) {
                // ok
                NoteReminder reminder = NoteEntryUtil.getValidReminderOfNoteEntry(noteEntryUid, 1000);
                if (reminder != null) {
                    // give the system a bit of buffer time to show the notif
                    // is valid
                    NoteEntry noteEntry = MapsActivity.noteDatabase.noteEntryDao().getNoteEntry(noteEntryUid);
                    String notifTitle = "Reminder: " + noteEntry.noteTitle;
                    String notifText = reminder.reminderText;
                    if (notifText.equals("")) {
                        notifText = "You are now reminded of this place.";
                    }
                    NotificationCompat.Builder builder = util.setNotification(notifTitle, notifText);
                    util.getManager().notify(101, builder.build());
                }
            }
        }
//        NotificationCompat.Builder builder = util.setNotification("Testing", "Testing notification system");
//        util.getManager().notify(101, builder.build());
    }
}