package edu.cuhk.cuhk_all_in_one.datatypes;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NoteTaggingInfoDao {
    // todo add more queries as needed

    @Query("SELECT * FROM note_tagging_info")
    List<NoteTaggingInfo> getAllNoteTaggingInfo();

    @Query("SELECT * FROM note_tagging_info WHERE note_entry_uid = :noteEntryUid")
    List<NoteTaggingInfo> getAllNoteTaggingInfo(int noteEntryUid);

    @Query("SELECT * FROM note_tagging_info WHERE tag_uid = :tagUid")
    List<NoteTaggingInfo> getAllTaggingInfoWithTag(int tagUid);

    @Query("SELECT * FROM note_tagging_info WHERE note_entry_uid = :noteEntryUid AND tag_uid = :tagUid")
    NoteTaggingInfo getSpecificNoteTaggingInfo(int noteEntryUid, int tagUid);

    // for general usage, please use helper functions instead.
    @Insert
    void insertTaggingInformation(NoteTaggingInfo taggingInfo);

    // for general usage, please use helper functions instead.
    @Delete
    void removeTaggingInformation(NoteTaggingInfo taggingInfo);

    @Query("SELECT * FROM note_tag WHERE uid IN (SELECT tag_uid FROM note_tagging_info WHERE note_entry_uid = :noteEntryUid)")
    List<NoteTag> getTagsEnabledByNoteEntry(int noteEntryUid);

    @Query("SELECT * FROM note_tag WHERE uid NOT IN (SELECT tag_uid FROM note_tagging_info WHERE note_entry_uid = :noteEntryUid)")
    List<NoteTag> getTagsUnusedByNoteEntry(int noteEntryUid);

    @Query("DELETE FROM note_tagging_info WHERE note_entry_uid NOT IN (SELECT uid FROM note_entry) OR tag_uid NOT IN (SELECT uid FROM note_tag)")
    void deleteAllInvalidTaggingInfo();
}
