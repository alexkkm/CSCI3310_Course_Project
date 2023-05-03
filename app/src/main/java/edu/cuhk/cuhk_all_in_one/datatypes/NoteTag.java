package edu.cuhk.cuhk_all_in_one.datatypes;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "note_tag", indices = {@Index(value = {"name"})})
public class NoteTag {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "name")
    public String tagName;
}
