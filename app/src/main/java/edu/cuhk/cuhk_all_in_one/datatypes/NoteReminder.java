package edu.cuhk.cuhk_all_in_one.datatypes;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "note_reminder",
        foreignKeys = {@ForeignKey(
                entity = NoteEntry.class,
                parentColumns = "uid",
                childColumns = "note_uid",
                onDelete = CASCADE
        )}, indices = {
        @Index(value = {"note_uid"})})
public class NoteReminder {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "note_uid")
    public int noteUid;

    @ColumnInfo(name = "timestamp")
    public long reminderTimestamp;

    @ColumnInfo(name = "text")
    public String reminderText;

}
