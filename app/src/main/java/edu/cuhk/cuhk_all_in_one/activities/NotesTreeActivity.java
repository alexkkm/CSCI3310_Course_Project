package edu.cuhk.cuhk_all_in_one.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SearchView;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewAdapter;
import com.amrdeveloper.treeview.TreeViewHolderFactory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.cuhk.cuhk_all_in_one.R;
import edu.cuhk.cuhk_all_in_one.adapters.NotesTreeViewHolder;
import edu.cuhk.cuhk_all_in_one.databinding.ActivityNotesTreeBinding;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteEntry;
import edu.cuhk.cuhk_all_in_one.datatypes.NotePin;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteTag;

public class NotesTreeActivity extends AppCompatActivity {

    private ActivityNotesTreeBinding binding;

    private List<NotePin> notePins = new ArrayList<>();

    RecyclerView notesTreeRecyclerView;
    TreeViewAdapter treeViewAdapter;

    private boolean isSearchingByName = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNotesTreeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load list of notes tree view
        notesTreeRecyclerView = findViewById(R.id.notes_tree_rv);
        notesTreeRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        notesTreeRecyclerView.setNestedScrollingEnabled(false);

        TreeViewHolderFactory factory = (v, layout) -> new NotesTreeViewHolder(v);

        treeViewAdapter = new TreeViewAdapter(factory);
        notesTreeRecyclerView.setAdapter(treeViewAdapter);

        setupToolBar();
        setUpSearch();

        loadAllNotesByName();
    }

    private void setupToolBar() {
        Toolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Notes Explorer");

        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tree, menu);
        MenuItem itemSwitch = menu.findItem(R.id.switch_action_bar);
        itemSwitch.setActionView(R.layout.switch_tree_content);

        final SwitchCompat sw = menu.findItem(R.id.switch_action_bar).getActionView().findViewById(R.id.treeSwitch);

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SearchView search = findViewById(R.id.notestree_search_widget);
                search.setQuery("", false);

                ImageView switchIcon = findViewById(R.id.tree_switch_icon);

                if (isSearchingByName) {
                    switchIcon.setImageResource(R.drawable.ic_baseline_local_offer_24);
                    loadAllNotesByTag();
                } else {
                    switchIcon.setImageResource(R.drawable.ic_baseline_location_on_24);
                    loadAllNotesByName();
                }
                isSearchingByName = !isSearchingByName;
            }
        });
        return true;
    }

    private void setUpSearch() {
        SearchView searchView = findViewById(R.id.notestree_search_widget);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 0) {
                    if (isSearchingByName) {
                        filterNotesByName(newText);
                    } else {
                        filterNotesByTag(newText);
                    }

                    treeViewAdapter.expandAll();
                } else {
                    if (isSearchingByName) {
                        loadAllNotesByName();
                    } else {
                        loadAllNotesByTag();
                    }
                }
                return false;
            }
        });
    }

    private void loadAllNotesByName() {
        List<TreeNode> roots = new ArrayList<>();

        notePins = MapsActivity.noteDatabase.notePinDao().getAllPins();
        for (NotePin notePin : notePins) {
            List<NoteEntry> noteEntries = MapsActivity.noteDatabase.noteEntryDao().getAllNoteEntries(notePin.uid);
            TreeNode root = new TreeNode(notePin, R.layout.list_item_file);
            for (NoteEntry noteEntry : noteEntries) {
                root.addChild(new TreeNode(noteEntry, R.layout.list_item_file));
            }
            roots.add(root);
        }

        treeViewAdapter.notifyDataSetChanged();
        treeViewAdapter.updateTreeNodes(roots);
    }

    private void filterNotesByName(String keyword) {
        List<TreeNode> roots = new ArrayList<>();

        for (NotePin notePin : notePins) {
            List<NoteEntry> noteEntries = MapsActivity.noteDatabase.noteEntryDao().getAllNoteEntries(notePin.uid);
            TreeNode root = new TreeNode(notePin, R.layout.list_item_file);

            // Filter here
            if (notePin.pinName.toLowerCase().contains(keyword.toLowerCase())) {
                // Pin contains keyword, add all notes of this pin
                for (NoteEntry noteEntry : noteEntries) {
                    root.addChild(new TreeNode(noteEntry, R.layout.list_item_file));
                }
                roots.add(root);
            } else {
                // Pin doesn't contain keyword, check notes inside
                for (NoteEntry noteEntry : noteEntries) {
                    if (noteEntry.noteTitle.toLowerCase().contains(keyword.toLowerCase())) {
                        root.addChild(new TreeNode(noteEntry, R.layout.list_item_file));
                    }
                }
                // Only add pin if there's at least one note contains keyword
                if (!root.getChildren().isEmpty()) {
                    roots.add(root);
                }
            }
        }

        treeViewAdapter.notifyDataSetChanged();
        treeViewAdapter.updateTreeNodes(roots);
    }

    private void loadAllNotesByTag() {
        List<TreeNode> roots = new ArrayList<>();

        List<NoteTag> allTags = MapsActivity.noteDatabase.noteTagDao().getAllPossibleTags();
        for (NoteTag tag : allTags) {
            List<NoteEntry> taggedNotes = MapsActivity.noteDatabase.noteTagDao().getAllNoteEntriesUsingThisTag(tag.uid);
            TreeNode root = new TreeNode(tag, R.layout.list_item_file);
            for (NoteEntry taggedNote : taggedNotes) {
                root.addChild(new TreeNode(taggedNote, R.layout.list_item_file));
            }
            roots.add(root);
        }

        treeViewAdapter.notifyDataSetChanged();
        treeViewAdapter.updateTreeNodes(roots);
    }

    private void filterNotesByTag(String keyword) {
        List<TreeNode> roots = new ArrayList<>();
        List<NoteTag> allTags = MapsActivity.noteDatabase.noteTagDao().getAllPossibleTags();

        for (NoteTag noteTag : allTags) {
            List<NoteEntry> noteEntries = MapsActivity.noteDatabase.noteTagDao().getAllNoteEntriesUsingThisTag(noteTag.uid);
            TreeNode root = new TreeNode(noteTag, R.layout.list_item_file);

            // Filter here
            if (noteTag.tagName.toLowerCase().contains(keyword.toLowerCase())) {
                // Tags contains keyword, add all notes of this tag
                for (NoteEntry noteEntry : noteEntries) {
                    root.addChild(new TreeNode(noteEntry, R.layout.list_item_file));
                }
                roots.add(root);
            } else {
                // Tag doesn't contain keyword, check notes inside
                for (NoteEntry noteEntry : noteEntries) {
                    if (noteEntry.noteTitle.toLowerCase().contains(keyword.toLowerCase())) {
                        root.addChild(new TreeNode(noteEntry, R.layout.list_item_file));
                    }
                }
                // Only add pin if there's at least one note contains keyword
                if (!root.getChildren().isEmpty()) {
                    roots.add(root);
                }
            }
        }

        treeViewAdapter.notifyDataSetChanged();
        treeViewAdapter.updateTreeNodes(roots);
    }
}