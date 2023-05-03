package edu.cuhk.cuhk_all_in_one.datatypes;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NoteReminderDao {
    // todo add more queries as needed

    @Query("SELECT * FROM note_reminder")
    List<NoteReminder> getAllNoteReminders();

    @Query("SELECT * FROM note_reminder WHERE note_uid = :noteUid ORDER BY timestamp DESC")
    List<NoteReminder> getAllNoteReminders(int noteUid);

    @Query("SELECT * FROM note_reminder WHERE uid = :noteReminderUid")
    NoteReminder getNoteReminder(int noteReminderUid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertNoteReminders(NoteReminder... reminders);

    @Query("DELETE FROM note_reminder WHERE note_uid = :noteUid")
    void clearAllRemindersOfNote(int noteUid);

    @Delete
    void deleteNoteReminder(NoteReminder entry);
}
