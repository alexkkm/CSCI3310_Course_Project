package edu.cuhk.cuhk_all_in_one.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import edu.cuhk.cuhk_all_in_one.R;
import edu.cuhk.cuhk_all_in_one.activities.MapsActivity;
import edu.cuhk.cuhk_all_in_one.activities.PinsActivity;
import edu.cuhk.cuhk_all_in_one.adapters.PinNotesAdapter;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteEntry;

public class NotesRecyclerViewFragment extends Fragment {
    private static final String TAG = "NotesRecyclerViewFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 60;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected RecyclerView mRecyclerView;
    protected PinNotesAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected String[] mDataset;

    private int pinUid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize dataset, this data would usually come from a local content provider or
        // remote server.
//        initDataset();

        // load uid
        Bundle bundleArguments = getArguments();
        if (bundleArguments != null) {
            this.pinUid = bundleArguments.getInt("pinUid");
            Log.d("TAG", "Recycler view: pin UID " + this.pinUid);
        }
        if (savedInstanceState != null) {
            // todo
            this.pinUid = savedInstanceState.getInt("pinUid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.notes_rv_frag, container, false);
        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.notes_rv);

        DividerItemDecoration decoration = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.rv_divider));
        mRecyclerView.addItemDecoration(decoration);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

//        mAdapter = new PinNotesAdapter(mDataset);
        mAdapter = new PinNotesAdapter(this.pinUid);
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

        useSwipeToDelete(mRecyclerView, mAdapter);

        PinsActivity parentActivity = (PinsActivity) getActivity();
        assert parentActivity != null;
        parentActivity.notifyRefreshActivityUi();

        return rootView;
    }

    void useSwipeToDelete(RecyclerView recyclerView, PinNotesAdapter adapter) {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                // This method is called when the item is moved.
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Remove note when swiping right
                int position = viewHolder.getAdapterPosition();
                NoteEntry deletedNote = adapter.getNoteEntryAt(position);
                // TODO: Should make db related as singleton outside of MapsActivity
                MapsActivity.noteDatabase.noteEntryDao().deleteNoteEntry(deletedNote);

                adapter.refreshNotePins();
                adapter.notifyItemRemoved(position);
                PinsActivity parentActivity = (PinsActivity) getActivity();
                assert parentActivity != null;
                parentActivity.notifyRefreshActivityUi();

                // Display snack bar for undo
                Snackbar.make(recyclerView, deletedNote.noteTitle + "has been deleted", Snackbar.LENGTH_LONG).setAction("", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: Add note back to db and ui
//                        recyclerDataArrayList.add(position, deletedCourse);
//                        adapter.notifyItemInserted(position);
                    }
                }).show();
            }
        }).attachToRecyclerView(recyclerView);

    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initDataset() {
        mDataset = new String[DATASET_COUNT];
        for (int i = 0; i < DATASET_COUNT; i++) {
            mDataset[i] = "This is element #" + i;
        }
    }

    public PinNotesAdapter getPinNotesAdapter() {
        return mAdapter;
    }
}
