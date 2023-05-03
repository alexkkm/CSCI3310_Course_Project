package edu.cuhk.cuhk_all_in_one.datatypes;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteEntryDao {
    // todo add more queries as needed

    @Query("SELECT * FROM note_entry")
    List<NoteEntry> getAllNoteEntries();

    @Query("SELECT * FROM note_entry WHERE pin_uid = :pinUid")
    List<NoteEntry> getAllNoteEntries(int pinUid);

    @Query("SELECT * FROM note_entry WHERE uid = :noteEntryUid")
    NoteEntry getNoteEntry(int noteEntryUid);

    @Insert
    List<Long> insertNoteEntries(NoteEntry... entries);

    @Update
    void updateNoteEntry(NoteEntry entry);

    @Delete
    void deleteNoteEntry(NoteEntry entry);

    @Query("DELETE FROM note_entry WHERE pin_uid NOT IN (SELECT uid FROM note_pin)")
    void deleteInvalidNoteEntries();
}
