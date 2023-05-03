package edu.cuhk.cuhk_all_in_one.util;

import androidx.annotation.NonNull;

import java.util.List;

import edu.cuhk.cuhk_all_in_one.activities.MapsActivity;
import edu.cuhk.cuhk_all_in_one.datatypes.AppDatabase;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteEntry;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteReminder;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteTag;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteTagDao;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteTaggingInfo;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteTaggingInfoDao;

public class NoteEntryUtil {
    public static NoteReminder getValidReminderOfNoteEntry(int noteEntryUid, long bufferTimeMs) {
        List<NoteReminder> reminderList = MapsActivity.noteDatabase.noteReminderDao().getAllNoteReminders(noteEntryUid);
        if (reminderList.isEmpty()) {
            return null;
        }
        NoteReminder reminder = reminderList.get(0);
        if (reminder.reminderTimestamp < System.currentTimeMillis() - bufferTimeMs) {
            return null;
        }
        return reminder;
    }

    public static List<NoteTag> getEnabledTagsForNoteEntry(int noteEntryUid) {
        return MapsActivity.noteDatabase.noteTaggingInfoDao().getTagsEnabledByNoteEntry(noteEntryUid);
    }

    public static List<NoteTag> getTagsNotUsedByNoteEntry(int noteEntryUid) {
        return MapsActivity.noteDatabase.noteTaggingInfoDao().getTagsUnusedByNoteEntry(noteEntryUid);
    }

    public static void addTagForNoteEntry(int noteEntryUid, String tagName) {
        // ensure that the internal tag exists; and then associate it with the note entry
        NoteTagDao tagDao = MapsActivity.noteDatabase.noteTagDao();
        NoteTag tag = tagDao.getTag(tagName);
        if (tag == null) {
            tag = new NoteTag();
            tag.tagName = tagName;
            tag.uid = (int) tagDao.registerTag(tag);
        }

        NoteTaggingInfoDao taggingInfoDao = MapsActivity.noteDatabase.noteTaggingInfoDao();
        NoteTaggingInfo info = taggingInfoDao.getSpecificNoteTaggingInfo(noteEntryUid, tag.uid);
        if (info == null) {
            // not yet registered
            info = new NoteTaggingInfo();
            info.noteEntryUid = noteEntryUid;
            info.tagUid = tag.uid;
            taggingInfoDao.insertTaggingInformation(info);
        }
        // else: already registered... normally should not happen
    }

    public static void removeTagForNoteEntry(int noteEntryUid, String tagName) {
        // ensure that the internal tag exists; and then associate it with the note entry
        NoteTagDao tagDao = MapsActivity.noteDatabase.noteTagDao();
        NoteTag tag = tagDao.getTag(tagName);
        if (tag == null) {
            // nothing to remove
            return;
        }

        NoteTaggingInfoDao taggingInfoDao = MapsActivity.noteDatabase.noteTaggingInfoDao();
        NoteTaggingInfo info = taggingInfoDao.getSpecificNoteTaggingInfo(noteEntryUid, tag.uid);
        if (info == null) {
            // nothing to remove
            return;
        }
        taggingInfoDao.removeTaggingInformation(info);

        // check again: do we have no correlation left?
        List<NoteTaggingInfo> tagInfoWithTag = taggingInfoDao.getAllTaggingInfoWithTag(tag.uid);
        if (tagInfoWithTag.isEmpty()) {
            // no more remains; remove it
            tagDao.deleteTag(tag);
        }
    }

    public static void cleanupInvalidData() {
        AppDatabase database = MapsActivity.noteDatabase;
        database.noteEntryDao().deleteInvalidNoteEntries();
        database.noteTagDao().deleteAllUnusedTags();
        database.noteTaggingInfoDao().deleteAllInvalidTaggingInfo();
    }

    public static List<NoteEntry> getAllNoteEntriesUsingThisTag(int tagUid) {
        return MapsActivity.noteDatabase.noteTagDao().getAllNoteEntriesUsingThisTag(tagUid);
    }

    public static List<NoteEntry> getAllNoteEntriesUsingThisTag(@NonNull NoteTag tag) {
        return getAllNoteEntriesUsingThisTag(tag.uid);
    }
}
