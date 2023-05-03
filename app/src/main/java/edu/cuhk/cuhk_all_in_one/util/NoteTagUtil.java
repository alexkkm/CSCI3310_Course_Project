package edu.cuhk.cuhk_all_in_one.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.cuhk.cuhk_all_in_one.datatypes.NoteTag;

public class NoteTagUtil {

    public static List<NoteTag> sortNoteTagsByNameAsc(List<NoteTag> noteTagList) {
        List<NoteTag> sortedList = new ArrayList<>(noteTagList);
        sortedList.sort(Comparator.comparing(a -> a.tagName));
        return sortedList;
    }
}
