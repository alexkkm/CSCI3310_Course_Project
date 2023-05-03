package edu.cuhk.cuhk_all_in_one.datatypes;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class NoteMedia {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    // todo media details, media types, etc
    public String todo;
}
