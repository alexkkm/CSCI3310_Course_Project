package edu.cuhk.cuhk_all_in_one.datatypes;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {
        NoteEntry.class,
        NoteMedia.class,
        NotePin.class,
        NoteReminder.class,
        NoteTag.class,
        NoteTaggingInfo.class,
}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract NotePinDao notePinDao();

    public abstract NoteEntryDao noteEntryDao();

    public abstract NoteReminderDao noteReminderDao();

    public abstract NoteTagDao noteTagDao();

    public abstract NoteTaggingInfoDao noteTaggingInfoDao();
}
