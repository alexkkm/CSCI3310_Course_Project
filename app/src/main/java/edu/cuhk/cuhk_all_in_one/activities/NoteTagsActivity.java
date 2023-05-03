package edu.cuhk.cuhk_all_in_one.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;

import edu.cuhk.cuhk_all_in_one.R;
import edu.cuhk.cuhk_all_in_one.adapters.NoteTagsAdapter;
import edu.cuhk.cuhk_all_in_one.databinding.ActivityNoteEntryTagsBinding;
import edu.cuhk.cuhk_all_in_one.util.NoteEntryUtil;

public class NoteTagsActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener {

    private ActivityNoteEntryTagsBinding binding;

    private int noteEntryUid;

    // Declare Variables
    RecyclerView recycler;
    NoteTagsAdapter tagsAdapter;
    SearchView tagEditSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNoteEntryTagsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
//        setSupportActionBar(toolbar);
//        MaterialToolbar toolBarLayout = binding.toolbarLayout;
//        toolBarLayout.setTitle("Manage tags");


        Intent invokerIntent = getIntent();
        if (invokerIntent != null) {
            this.noteEntryUid = invokerIntent.getIntExtra("noteUid", -1);
            if (this.noteEntryUid < 0) {
                Log.e("TAG", "Bad: received an invalid note UID");
            }
        }

        setUpFinishButton();

        setupSearchControl();
        NoteEntryUtil.cleanupInvalidData();
    }

    private void setUpFinishButton() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String text = newText;
        Log.d("SEARCH", text);
//        adapter.filter(text);
        tagsAdapter.refreshTagsWithThisSearchString(text);
        return false;
    }

    private void setupSearchControl() {
        tagEditSearch = (SearchView) findViewById(R.id.tag_search_widget);
        tagEditSearch.setOnQueryTextListener(this);

        // reference: https://localcoder.org/how-to-modify-searchview-close-clear-button-to-always-close-the-searchbox-no-mat
        // force clear -> set text("")
        int clearButtonId = tagEditSearch.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView clearButton = tagEditSearch.findViewById(clearButtonId);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagEditSearch.setQuery("", true);
            }
        });

        recycler = (RecyclerView) findViewById(R.id.recyclerView);
        tagsAdapter = new NoteTagsAdapter(noteEntryUid);
        recycler.setAdapter(tagsAdapter);
    }
}