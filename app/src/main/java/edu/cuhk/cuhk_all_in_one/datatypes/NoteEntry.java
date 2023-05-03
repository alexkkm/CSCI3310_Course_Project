package edu.cuhk.cuhk_all_in_one.datatypes;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "note_entry",
        foreignKeys = {@ForeignKey(
        entity = NotePin.class,
        parentColumns = "uid",
        childColumns = "pin_uid",
        onDelete = CASCADE
)}, indices = {
        @Index(value = {"pin_uid"})})
public class NoteEntry {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "pin_uid")
    public int pinUid;

    @ColumnInfo(name = "title")
    public String noteTitle;

    @ColumnInfo(name = "text")
    public String noteText;
}
