package edu.cuhk.cuhk_all_in_one.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import edu.cuhk.cuhk_all_in_one.adapters.PinNotesAdapter;
import edu.cuhk.cuhk_all_in_one.databinding.ActivityPinsBinding;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteEntry;
import edu.cuhk.cuhk_all_in_one.datatypes.NotePin;
import edu.cuhk.cuhk_all_in_one.fragments.NotesRecyclerViewFragment;
import edu.cuhk.cuhk_all_in_one.R;
import edu.cuhk.cuhk_all_in_one.fragments.PhotosRecyclerViewFragment;

public class PinsActivity extends AppCompatActivity {

    private ActivityPinsBinding binding;
    private AlertDialog.Builder builder;
    private NotesRecyclerViewFragment notesRecyclerViewFragment;
    private PhotosRecyclerViewFragment photosRecyclerViewFragment;

    private boolean isAddFABOpen = false;
    private boolean showingPhotos = false;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private int pinUid;
    private String pinName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPinsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // load the pin uid
        Intent invokerIntent = getIntent();
        if (invokerIntent != null) {
            // has invoker
            this.pinUid = invokerIntent.getIntExtra("pinUid", -1);
            if (this.pinUid < 0) {
                // well this cannot be good
                throw new RuntimeException("pinUid is invalid");
            }
            Log.d("TAG", "Pin notes from intent: UID " + this.pinUid);
        }

        this.refreshPinLocationDisplay();

        // Tool bar

        setupToolBar();


        setupAddFAB();

        // Show list of notes by default
        showPinNotes();

        // help button
        builder = new AlertDialog.Builder(this);

        setupBackButton();
    }

    @Override
    protected void onPause() {
        super.onPause();
        renamePin(pinName);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pins, menu);
        MenuItem itemSwitch = menu.findItem(R.id.switch_action_bar);
        itemSwitch.setActionView(R.layout.switch_pin_content);

        final SwitchCompat sw = menu.findItem(R.id.switch_action_bar).getActionView().findViewById(R.id.pinSwitch);

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleBetweenNotesAndPhotos();
            }
        });
        return true;
    }

    private void setupToolBar() {
        Toolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        EditText editPinTitle = findViewById(R.id.pin_title);
        editPinTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                pinName = s.toString();
            }
        });
    }

    private void setupAddFAB() {
        binding.fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isAddFABOpen){
                    showAddFABMenu();
                }else{
                    closeAddFABMenu();
                }
            }
        });

        // notes
        binding.fabAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add a new note entry to this pin
                // TODO: Delete db then refresh may be slow as UI has to wait db finished.
                NoteEntry noteEntry = new NoteEntry();
                noteEntry.noteTitle = "Untitled Note";
                noteEntry.noteText = "";
                noteEntry.pinUid = pinUid;
                // TODO: Should make db related as singleton outside of MapsActivity
                List<Long> newNoteIds = MapsActivity.noteDatabase.noteEntryDao().insertNoteEntries(noteEntry);

                PinNotesAdapter adapter = notesRecyclerViewFragment.getPinNotesAdapter();
                adapter.refreshNotePins();
                adapter.notifyItemInserted(adapter.getItemCount() - 1);

                notifyRefreshActivityUi();

                Toast.makeText(getApplicationContext(), "A new note has been created", Toast.LENGTH_LONG).show();

                if (showingPhotos) {
                    SwitchCompat sw = findViewById(R.id.pinSwitch);
                    sw.setChecked(false);
                }

                // Open created notes immediately
                Intent intent = new Intent(view.getContext(), NotesActivity.class);
                intent.putExtra("noteUid", newNoteIds.get(0).intValue());
                view.getContext().startActivity(intent);
            }
        });

        // Photos
        binding.fabAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
            }
        });
    }

    private void showAddFABMenu(){
        isAddFABOpen = true;
        binding.fabAddNote.animate().translationY(-getResources().getDimension(R.dimen.standard_64));
        binding.fabAddPhoto.animate().translationY(-getResources().getDimension(R.dimen.standard_128));
        binding.fabAddVideo.animate().translationY(-getResources().getDimension(R.dimen.standard_192));
    }

    private void closeAddFABMenu(){
        isAddFABOpen = false;
        binding.fabAddNote.animate().translationY(0);
        binding.fabAddPhoto.animate().translationY(0);
        binding.fabAddVideo.animate().translationY(0);
    }

    private void setupBackButton() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void toggleBetweenNotesAndPhotos() {
        ImageView switchIcon = findViewById(R.id.switch_icon);
        if (showingPhotos) {
            switchIcon.setImageResource(R.drawable.ic_baseline_library_books_24);
            showPinNotes();
        } else {
            switchIcon.setImageResource(R.drawable.ic_baseline_photo_library_24);
            showPinPhotos();
        }
        showingPhotos = !showingPhotos;
    }

    private void showPinNotes() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        NotesRecyclerViewFragment fragment = new NotesRecyclerViewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("pinUid", this.pinUid);
        fragment.setArguments(bundle);
        notesRecyclerViewFragment = fragment;
        transaction.replace(R.id.pin_content_fragment, notesRecyclerViewFragment);
        transaction.commit();
    }

    private void showPinPhotos() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        PhotosRecyclerViewFragment fragment = new PhotosRecyclerViewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("pinUid", this.pinUid);
        fragment.setArguments(bundle);
        photosRecyclerViewFragment = fragment;
        transaction.replace(R.id.pin_content_fragment, photosRecyclerViewFragment);
        transaction.commit();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void captureImage() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pictureIntent, 100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                saveImage(imageBitmap);
            }
        }

        if (!showingPhotos) {
            SwitchCompat sw = findViewById(R.id.pinSwitch);
            sw.setChecked(true);
        }
    }

    private void saveImage(Bitmap bitmap) {
        String filename;
        Date date = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        filename = sdf.format(date);

        try {
            String path = this.getApplicationContext().getExternalFilesDir(null).toString();
            OutputStream outputStream = null;
            File file = new File(path, "/images/" + String.valueOf(pinUid) + "/" + filename + ".jpg");
            File root = new File(Objects.requireNonNull(file.getParent()));
            if (file.getParent() != null && !root.isDirectory()) {
                root.mkdirs();
            }
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            outputStream.flush();
            outputStream.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch (Exception e) {
            Log.e("PinsActivity", "saveImage: " + e);
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        // force the stuff to update the UI; we know of no other way
        if (notesRecyclerViewFragment != null) {
            PinNotesAdapter adapter = notesRecyclerViewFragment.getPinNotesAdapter();
            adapter.refreshNotePins();
            adapter.notifyDataSetChanged();

            // determine what layout to display
            this.notifyRefreshActivityUi();
        }
    }

    public void notifyRefreshActivityUi() {
        // a note has been added/deleted
        // call this when the fragments initialize
        if (notesRecyclerViewFragment != null) {
            PinNotesAdapter adapter = notesRecyclerViewFragment.getPinNotesAdapter();

            View contentFragment = findViewById(R.id.pin_content_fragment);
            View nothingToDisplay = findViewById(R.id.layout_nothing_to_display);
            if (!showingPhotos) {
                // text mode
                if (adapter.getItemCount() == 0) {
                    // nothing to display
                    contentFragment.setVisibility(View.INVISIBLE);
                    nothingToDisplay.setVisibility(View.VISIBLE);
                } else {
                    // got something to display
                    contentFragment.setVisibility(View.VISIBLE);
                    nothingToDisplay.setVisibility(View.INVISIBLE);
                }
            } else {
                // photo mode
                if (!this.hasPhotos()) {
                    // nothing to display
                    contentFragment.setVisibility(View.INVISIBLE);
                    nothingToDisplay.setVisibility(View.VISIBLE);
                } else {
                    // got something to display
                    contentFragment.setVisibility(View.VISIBLE);
                    nothingToDisplay.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public Context getContext() {
        return this.getApplicationContext();
    }

    private void refreshPinLocationDisplay() {
        if (this.pinUid >= 0) {
            NotePin notePin = MapsActivity.noteDatabase.notePinDao().getPinById(pinUid);
            pinName = notePin.pinName;
            EditText editPinTitle = findViewById(R.id.pin_title);
            editPinTitle.setText(pinName);
        }
    }

    private void renamePin(String name) {
        if (pinUid >= 0) {
            NotePin notePin = MapsActivity.noteDatabase.notePinDao().getPinById(pinUid);
            notePin.pinName = name;
            MapsActivity.noteDatabase.notePinDao().updatePin(notePin);
            refreshPinLocationDisplay();
        }
    }

    private boolean hasPhotos() {
        File path = new File(getContext().getExternalFilesDir(null).toString(), "images/" + String.valueOf(pinUid));
        if (!path.exists()) {
            return false;
        }
        String[] fileNames = path.list();
        if (fileNames == null) {
            return false;
        }
        return fileNames.length > 0;
    }
}