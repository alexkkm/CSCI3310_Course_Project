package edu.cuhk.cuhk_all_in_one.datatypes;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "note_tagging_info",
        foreignKeys = {@ForeignKey(
                entity = NoteEntry.class,
                parentColumns = "uid",
                childColumns = "note_entry_uid",
                onDelete = CASCADE
        ), @ForeignKey(
                entity = NoteTag.class,
                parentColumns = "uid",
                childColumns = "tag_uid",
                onDelete = CASCADE
        )}, indices = {
        @Index(value = {"note_entry_uid"}),
        @Index(value = {"tag_uid"})})
public class NoteTaggingInfo {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "note_entry_uid")
    public int noteEntryUid;

    @ColumnInfo(name = "tag_uid")
    public int tagUid;

}
