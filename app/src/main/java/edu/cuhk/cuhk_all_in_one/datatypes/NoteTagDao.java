package edu.cuhk.cuhk_all_in_one.datatypes;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NoteTagDao {
    // todo add more queries as needed

    @Query("SELECT * FROM note_tag")
    List<NoteTag> getAllPossibleTags();

    @Query("SELECT * FROM note_tag WHERE uid = :tagUid")
    NoteTag getTag(int tagUid);

    @Query("SELECT * FROM note_tag WHERE name = :tagName")
    NoteTag getTag(String tagName);

    // for general usage, please use helper functions instead.
    @Insert
    long registerTag(NoteTag noteTag);

    // for general usage, please use helper functions instead.
    @Delete
    void deleteTag(NoteTag tag);

    // for general usage, please use helper functions instead.
    @Query("DELETE FROM note_tag WHERE name = :tagName")
    void deleteTag(String tagName);

    @Query("DELETE FROM note_tag WHERE uid NOT IN (SELECT tag_uid FROM note_tagging_info)")
    void deleteAllUnusedTags();

    @Query("SELECT entry.* FROM note_entry entry INNER JOIN note_tagging_info info ON (entry.uid = info.note_entry_uid) WHERE info.tag_uid = :tagUid")
    List<NoteEntry> getAllNoteEntriesUsingThisTag(int tagUid);
}
