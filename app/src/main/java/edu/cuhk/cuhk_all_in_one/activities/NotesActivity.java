package edu.cuhk.cuhk_all_in_one.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import edu.cuhk.cuhk_all_in_one.databinding.ActivityNotesBinding;
import edu.cuhk.cuhk_all_in_one.R;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteEntry;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteReminder;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteTag;
import edu.cuhk.cuhk_all_in_one.util.NoteEntryUtil;
import edu.cuhk.cuhk_all_in_one.util.NotificationUtil;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;

public class NotesActivity extends AppCompatActivity {

    private ActivityNotesBinding binding;

    private boolean isEditing = false;

    private int noteEntryUid;

    private EditText inputRenameTitle;

    private Markwon markwon;
    private MarkwonEditor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent invokerIntent = getIntent();
        if (invokerIntent != null) {
            // has invoker
            this.noteEntryUid = invokerIntent.getIntExtra("noteUid", -1);
            if (this.noteEntryUid < 0) {
                // well this cannot be good
                throw new RuntimeException("noteUid is invalid");
            }
            Log.d("TAG", "Pin notes from intent: UID " + this.noteEntryUid);
        }

        binding = ActivityNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initially set to render the text
        this.initializeMarkdownRenderingMechanism();

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        this.properlySetToolbarTitle(getTitle());
        NoteEntry noteEntry = MapsActivity.noteDatabase.noteEntryDao().getNoteEntry(this.noteEntryUid);
        if (noteEntry != null) {
            this.properlySetToolbarTitle(noteEntry.noteTitle);
        }

        stopEditText(true);

        FloatingActionButton fabEditText = binding.fabEditText;
        fabEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEditing) {
                    stopEditText(false);
                    Toast.makeText(getApplicationContext(), "New changes saved", Toast.LENGTH_LONG).show();
                } else {
                    startEditText();
                }
            }
        });

        // edit title
        this.setupNoteTitleRenaming();

        // edit tags
        this.setupTagManagement();

        // edit reminder
        this.setupReminderManagement();

        // back to pin button
        this.setupGoToPinButton();

        // back button
        this.setupBackButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.noteEntryUid >= 0) {
            // put the note content into the UI
            EditText noteTextContentEditText = findViewById(R.id.note_edittext);
            // load the notes
            NoteEntry noteEntry = MapsActivity.noteDatabase.noteEntryDao().getNoteEntry(this.noteEntryUid);
            // todo check is text or audio
            noteTextContentEditText.setText(noteEntry.noteText);

            // check the tags
            this.updateTagsDisplayText();

            // check the reminder
            this.updateReminderDisplayText();
        }
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

    private void setupGoToPinButton() {
        Toolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Intent intent = getIntent();
        boolean fromTreeActivity = intent.getBooleanExtra("fromTreeActivity", false);
        if (!fromTreeActivity) {
            return false;
        }

        getMenuInflater().inflate(R.menu.menu_notes, menu);
        MenuItem itemSwitch = menu.findItem(R.id.go_to_pin);
        itemSwitch.setActionView(R.layout.button_to_pin);

        final ImageButton button = menu.findItem(R.id.go_to_pin).getActionView().findViewById(R.id.goToPinButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NoteEntry entry = MapsActivity.noteDatabase.noteEntryDao().getNoteEntry(noteEntryUid);
                Intent intent = new Intent(v.getContext(), PinsActivity.class);
                intent.putExtra("pinUid", entry.pinUid);
                startActivity(intent);
            }
        });
        return true;
    }

    private void initializeMarkdownRenderingMechanism() {
        // markdown-enabled editor with style
        markwon = Markwon.builder(NotesActivity.this)
                .usePlugin(new AbstractMarkwonPlugin() {
            @Override
            public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                super.configureTheme(builder);

                float textSizes[] = new float[] {1.2F, 1.F, .9F, .74F, .67F, .5F};
                builder.headingTextSizeMultipliers(textSizes);
                builder.headingBreakHeight(0);

                builder.bulletWidth(15);
                builder.listItemColor(Color.argb(255,180,180,180));
            }
        }).build();


        editor = MarkwonEditor.create(markwon);
        EditText editTextNoteContent = findViewById(R.id.note_edittext);
        editTextNoteContent.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor));

        // markdown-based text rendering
        TextView textViewNoteContent = findViewById(R.id.note_display_text);
        if (this.noteEntryUid >= 0) {
            NoteEntry entry = MapsActivity.noteDatabase.noteEntryDao().getNoteEntry(this.noteEntryUid);
            markwon.setMarkdown(textViewNoteContent, entry.noteText);
        }

        // initially set to render the text
//        textViewNoteContent.setVisibility(View.VISIBLE);
        editTextNoteContent.setVisibility(View.INVISIBLE);
        textViewNoteContent.setVisibility(View.VISIBLE);
//        editTextNoteContent.setVisibility(View.INVISIBLE);
//        editTextNoteContent.addTextChangedListener(MarkwonEditorTextWatcher.withPreRender(editor, Executors.newCachedThreadPool(), editTextNoteContent));
    }

    void startEditText() {
        isEditing = true;
        EditText editText = findViewById(R.id.note_edittext);
        editText.setVisibility(View.VISIBLE);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setEnabled(true);
        editText.requestFocus();

        // Show keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

        FloatingActionButton editTextFab = findViewById(R.id.fab_edit_text);
        editTextFab.setImageResource(R.drawable.ic_baseline_check_24);

        // markdown rendering mechanism (hack)
        TextView textViewNoteContent = findViewById(R.id.note_display_text);
        textViewNoteContent.setVisibility(View.INVISIBLE);
    }

    void stopEditText(boolean doNotSave) {
        isEditing = false;
        EditText editText = findViewById(R.id.note_edittext);
        editText.setVisibility(View.INVISIBLE);
        editText.setFocusable(false);

        // Collapse keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        FloatingActionButton editTextFab = findViewById(R.id.fab_edit_text);
        editTextFab.setImageResource(R.drawable.ic_baseline_edit_24);

        // save it to the db
        if (!doNotSave) {
            NoteEntry entry = MapsActivity.noteDatabase.noteEntryDao().getNoteEntry(this.noteEntryUid);
            entry.noteText = editText.getText().toString();
            MapsActivity.noteDatabase.noteEntryDao().updateNoteEntry(entry);
        }

        // markdown rendering mechanism (hack)
        TextView textViewNoteContent = findViewById(R.id.note_display_text);
        if (this.noteEntryUid >= 0) {
            NoteEntry entry = MapsActivity.noteDatabase.noteEntryDao().getNoteEntry(this.noteEntryUid);
            markwon.setMarkdown(textViewNoteContent, entry.noteText);
        }
        textViewNoteContent.setVisibility(View.VISIBLE);
    }

    private void properlySetToolbarTitle(CharSequence charSequence) {
        CollapsingToolbarLayout ctl = findViewById(R.id.toolbar_layout);
        ctl.setTitle(charSequence);
    }

    private void setupNoteTitleRenaming() {
        // first prepare the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.renaming_note_title);
        EditText inputRenameTitle = new EditText(this);
        inputRenameTitle.setInputType(InputType.TYPE_CLASS_TEXT);
        // should have already loaded a valid note
        if (noteEntryUid >= 0) {
            NoteEntry entry = MapsActivity.noteDatabase.noteEntryDao().getNoteEntry(noteEntryUid);
            inputRenameTitle.setText(entry.noteTitle);
        }
        builder.setView(inputRenameTitle);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (noteEntryUid >= 0) {
                    // put the updated title back to the DB
                    NoteEntry noteEntry = MapsActivity.noteDatabase.noteEntryDao().getNoteEntry(noteEntryUid);
                    noteEntry.noteTitle = inputRenameTitle.getText().toString();
                    MapsActivity.noteDatabase.noteEntryDao().updateNoteEntry(noteEntry);
                    properlySetToolbarTitle(noteEntry.noteTitle);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog properDialog = builder.create();

        // then link the dialog to the UI element
        // all toolbars must get the onCLick
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                properDialog.show();
            }
        });
        CollapsingToolbarLayout ctl = findViewById(R.id.toolbar_layout);
        ctl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                properDialog.show();
            }
        });
    }

    private void setupTagManagement() {
        RelativeLayout tagRelativeLayout = findViewById(R.id.tag_layout);
        tagRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // goto tabs activity
                Intent intent = new Intent(view.getContext(), NoteTagsActivity.class);
                intent.putExtra("noteUid", noteEntryUid);
                startActivity(intent);
            }
        });
    }

    private void setupReminderManagement() {
        // enable/disable reminder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.config_reminder_title);
        // todo: issue: reminder UI does not update when "reminder is triggered when inside the NoteEntry UI" (extreme edge case)
        View dialogView = this.inflateAndInitReminderDialog();
        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                testNotification();
                // did the user say to enable reminders?
                SwitchCompat reminderSwitch = dialogView.findViewById(R.id.switchEnableReminder);
                if (!reminderSwitch.isChecked()) {
                    // no. remove any reminders
                    if (noteEntryUid >= 0) {
                        MapsActivity.noteDatabase.noteReminderDao().clearAllRemindersOfNote(noteEntryUid);
                    }
                    updateReminderDisplayText();
                    return;
                }
                // build a timestamp string and then convert it into a Date object
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
                TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
                String timestampString = "" + datePicker.getYear() + "-" + String.format("%02d", datePicker.getMonth() + 1) + "-" + String.format("%02d", datePicker.getDayOfMonth());
                timestampString += " " + String.format("%02d", timePicker.getHour()) + ":" + String.format("%02d", timePicker.getMinute());

                try {
                    Date reminderDate = formatter.parse(timestampString);
                    assert reminderDate != null;
                    long timestampMs = reminderDate.getTime();

                    // try to upsert
                    List<NoteReminder> reminderList = MapsActivity.noteDatabase.noteReminderDao().getAllNoteReminders(noteEntryUid);
                    NoteReminder reminder;
                    if (reminderList.size() > 0) {
                        reminder = reminderList.get(0);
                    } else {
                        reminder = new NoteReminder();
                        reminder.noteUid = noteEntryUid;
                    }
                    EditText reminderTextEditText = dialogView.findViewById(R.id.editTextReminderText);
                    reminder.reminderText = reminderTextEditText.getText().toString();
                    reminder.reminderTimestamp = timestampMs;
                    MapsActivity.noteDatabase.noteReminderDao().upsertNoteReminders(reminder);
                    updateReminderDisplayText();
                    scheduleNoteEntryReminder();
                } catch (ParseException x) {
                    Log.e("TAG", "Failed to parse date! I got: " + timestampString);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                updateReminderDisplayText();
            }
        });
        AlertDialog properDialog = builder.create();

        RelativeLayout reminderRelativeLayout = findViewById(R.id.reminder_layout);
        reminderRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                properDialog.show();
            }
        });
    }

    private View inflateAndInitReminderDialog() {
        // inflates and configures the dialog layout etc
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_reminder, null);
        EditText reminderTextEdit = dialogView.findViewById(R.id.editTextReminderText);
        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
        datePicker.setMinDate(System.currentTimeMillis() - 1000);
        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hours, int minutes) {
                // we cannot allow the date+time to be set to "before now"

                // check against system time
                LocalDateTime ldt = LocalDateTime.now().plusMinutes(1);
                DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String nowDateTimeString = ldt.format(myFormatObj);

                String dateTimePickerString = "" + datePicker.getYear() + "-" + String.format("%02d", datePicker.getMonth() + 1) + "-" + String.format("%02d", datePicker.getDayOfMonth());
                dateTimePickerString += " " + String.format("%02d", timePicker.getHour()) + ":" + String.format("%02d", timePicker.getMinute());

                if (dateTimePickerString.compareTo(nowDateTimeString) < 0) {
                    // not OK!
                    datePicker.updateDate(ldt.getYear(), ldt.getMonthValue() - 1, ldt.getDayOfMonth());
                    timePicker.setHour(ldt.getHour());
                    timePicker.setMinute(ldt.getMinute());
                }
            }
        });
        SwitchCompat reminderSwitch = dialogView.findViewById(R.id.switchEnableReminder);
        reminderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                reminderTextEdit.setEnabled(isChecked);
                datePicker.setEnabled(isChecked);
                timePicker.setEnabled(isChecked);
            }
        });
        boolean isChecked = false;
        NoteReminder reminder = NoteEntryUtil.getValidReminderOfNoteEntry(this.noteEntryUid, 0);
        if (reminder != null) {
            // valid time
            isChecked = true;
            // update the various values here
            LocalDateTime ldt = LocalDateTime.ofEpochSecond(reminder.reminderTimestamp / 1000, (int) (reminder.reminderTimestamp % 1000), OffsetDateTime.now().getOffset());
            datePicker.updateDate(ldt.getYear(), ldt.getMonthValue() - 1, ldt.getDayOfMonth());
            timePicker.setHour(ldt.getHour());
            timePicker.setMinute(ldt.getMinute());
        } else {
            // no valid reminder time; set it to now + 1 minute to force future
            // todo it doesnt work?
            LocalDateTime ldt = LocalDateTime.now().plusMinutes(1);
            datePicker.updateDate(ldt.getYear(), ldt.getMonthValue() - 1, ldt.getDayOfMonth());
            timePicker.setHour(ldt.getHour());
            timePicker.setMinute(ldt.getMinute());
        }
        reminderSwitch.setChecked(isChecked);

        return dialogView;
    }

    private void updateTagsDisplayText() {
        // display max 3 tags per note
        TextView tagsText = findViewById(R.id.tags_text);
        List<NoteTag> enabledTags = NoteEntryUtil.getEnabledTagsForNoteEntry(noteEntryUid);
        if (enabledTags.isEmpty()) {
            tagsText.setText(R.string.note_tags_default);
            return;
        }
        String displayText = "";
        displayText += enabledTags.get(0).tagName;
        if (enabledTags.size() >= 2) {
            displayText += ", " + enabledTags.get(1).tagName;
            if (enabledTags.size() >= 3) {
                displayText += ", " + enabledTags.get(2).tagName;
                if (enabledTags.size() >= 4) {
                    displayText += ", ...";
                }
            }
        }
        tagsText.setText(displayText);
    }

    private void updateReminderDisplayText() {
        // check reminder
        // this does not use the util method because we need to detect expiration
        List<NoteReminder> reminderList = MapsActivity.noteDatabase.noteReminderDao().getAllNoteReminders(this.noteEntryUid);
        TextView reminderText = findViewById(R.id.reminder_text);
        if (reminderList.isEmpty()) {
            // no reminder; set early
            reminderText.setText(R.string.note_reminders_default);
            return;
        }
        // has reminder
        NoteReminder reminder = reminderList.get(0);
        // format: Reminder set: [date]
        Date reminderDate = new Date(reminder.reminderTimestamp);
        if (System.currentTimeMillis() > reminderDate.getTime()) {
            // expired!
            reminderText.setText("Reminder expired");
            return;
        }
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timestampString = format.format(reminderDate);
        reminderText.setText("Reminder set: " + timestampString);
    }

    private void testNotification() {
        NotificationUtil util = new NotificationUtil(this);
        long currentTimeMs = System.currentTimeMillis();
        long tenSeconds = 1000 * 10;
        long triggerTimeMs = currentTimeMs + tenSeconds; //triggers a reminder after 10 seconds.
        util.setReminder(triggerTimeMs);
    }

    private void scheduleNoteEntryReminder() {
        if (this.noteEntryUid < 0) {
            return;
        }
        List<NoteReminder> reminderList = MapsActivity.noteDatabase.noteReminderDao().getAllNoteReminders(this.noteEntryUid);
        if (reminderList.isEmpty()) {
            // should have already updated the records in the db
            return;
        }
        NoteReminder reminder = reminderList.get(0);
        if (reminder.reminderTimestamp < System.currentTimeMillis()) {
            // invalid
            return;
        }
        //
        NotificationUtil util = new NotificationUtil(this);
        util.setNoteEntryReminder(reminder);
    }
}
