package edu.cuhk.cuhk_all_in_one.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.cuhk.cuhk_all_in_one.R;
import edu.cuhk.cuhk_all_in_one.activities.MapsActivity;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteTag;
import edu.cuhk.cuhk_all_in_one.util.NoteEntryUtil;
import edu.cuhk.cuhk_all_in_one.util.NoteTagUtil;

public class NoteTagsAdapter extends RecyclerView.Adapter<NoteTagsAdapter.ViewHolder> {

    private static final String TAG = "TagAdaptor";

    private int noteEntryUid;

    private List<String> mNoteTags = new ArrayList<>();
    private List<Boolean> mNoteTagsEnabled = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final CheckBox checkBoxTagToggle;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    NoteEntry entry = mNoteEntries.get(getAdapterPosition());
//                    Intent noteIntent = new Intent(v.getContext(), NotesActivity.class);
//                    noteIntent.putExtra("noteUid", entry.uid);
//                    Log.d(TAG, "Note entry " + entry.uid + " clicked.");
                    Log.d(TAG, "Somebody got clicked");
//                    v.getContext().startActivity(noteIntent);
                }
            });

            checkBoxTagToggle = (CheckBox) v.findViewById(R.id.checkBox_TagToggle);
        }

        public CheckBox getCheckBoxTagToggle() {
            return this.checkBoxTagToggle;
        }
    }

    public NoteTagsAdapter() {
        // dummy create some data
        for (int i = 0; i < 20; i++) {
            String numStr = "" + i;
            mNoteTags.add(numStr);
        }
    }

    public NoteTagsAdapter(int noteEntryUid) {
        this.noteEntryUid = noteEntryUid;
        this.resetTagDisplayToDefault();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_tag_row_item, parent, false);

        return new NoteTagsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Log.d(TAG, "Element " + position + " set.");

        String tagString = mNoteTags.get(position);
        holder.getCheckBoxTagToggle().setText(tagString);
        boolean tagEnabled = mNoteTagsEnabled.get(position);
        holder.getCheckBoxTagToggle().setChecked(tagEnabled);

        holder.getCheckBoxTagToggle().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // this avoids unintended tag changes when the recycler view refreshes
                boolean isChecked = holder.getCheckBoxTagToggle().isChecked();
                if (isChecked) {
                    Log.d(TAG, "Enable " + noteEntryUid + " " + tagString);
                    NoteEntryUtil.addTagForNoteEntry(noteEntryUid, tagString);
                } else {
                    Log.d(TAG, "Disable " + noteEntryUid + " " + tagString);
                    NoteEntryUtil.removeTagForNoteEntry(noteEntryUid, tagString);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNoteTags.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshTagsWithThisSearchString(String searchString) {
        if (searchString.length() == 0) {
            // with nothing put in: show all tags that this note has registered
            this.resetTagDisplayToDefault();
            return;
        }
        // filter the list with "string contains"
        // todo there may or may not exist a better way of doing this
        List<NoteTag> allNoteTags = MapsActivity.noteDatabase.noteTagDao().getAllPossibleTags();
        List<NoteTag> filteredTags = NoteTagUtil.sortNoteTagsByNameAsc(allNoteTags).stream().filter(new Predicate<NoteTag>() {
            @Override
            public boolean test(NoteTag noteTag) {
                return noteTag.tagName.contains(searchString);
            }
        }).collect(Collectors.toList());
        List<NoteTag> enabledNoteTags = NoteEntryUtil.getEnabledTagsForNoteEntry(this.noteEntryUid);
        Map<String, NoteTag> enabledTagMapping = new HashMap<>();
        for (NoteTag tag : enabledNoteTags) {
            enabledTagMapping.put(tag.tagName, tag);
        }
        // construct the resulting list
        boolean directSearchStringFound = MapsActivity.noteDatabase.noteTagDao().getTag(searchString) != null;
        mNoteTags.clear();
        mNoteTagsEnabled.clear();
        if (!directSearchStringFound) {
            // not found; add a fake one at the top
            mNoteTags.add(searchString);
            mNoteTagsEnabled.add(false);
        }
        for (NoteTag tag : filteredTags) {
            if (tag.tagName.equals(searchString)) {
                 continue;
            }
            mNoteTags.add(tag.tagName);
            mNoteTagsEnabled.add(enabledTagMapping.containsKey(tag.tagName));
        }
        // notify changed
        this.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void resetTagDisplayToDefault() {
        // load them from the db!
        mNoteTags.clear();
        mNoteTagsEnabled.clear();
        List<NoteTag> enabledNoteTagList = NoteEntryUtil.getEnabledTagsForNoteEntry(noteEntryUid);
        enabledNoteTagList = NoteTagUtil.sortNoteTagsByNameAsc(enabledNoteTagList);
        for (NoteTag noteTag : enabledNoteTagList) {
            mNoteTags.add(noteTag.tagName);
            mNoteTagsEnabled.add(true);
        }
        List<NoteTag> disabledNoteTagList = NoteEntryUtil.getTagsNotUsedByNoteEntry(noteEntryUid);
        disabledNoteTagList = NoteTagUtil.sortNoteTagsByNameAsc(disabledNoteTagList);
        for (NoteTag noteTag : disabledNoteTagList) {
            mNoteTags.add(noteTag.tagName);
            mNoteTagsEnabled.add(false);
        }
        this.notifyDataSetChanged();
    }

}
