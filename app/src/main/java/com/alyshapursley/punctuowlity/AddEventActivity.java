package com.alyshapursley.punctuowlity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public final class AddEventActivity extends AppCompatActivity {
    static final String EXTRA_EVENT_ID = "event_id";

    private DatabaseHelper databaseHelper;
    private EditText titleInput;
    private EditText dateInput;
    private EditText timeInput;
    private Switch reminderSwitch;
    private Spinner categorySpinner;
    private int eventId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        databaseHelper = new DatabaseHelper(this);
        titleInput = findViewById(R.id.edit_event_title);
        dateInput = findViewById(R.id.edit_event_date);
        timeInput = findViewById(R.id.edit_event_time);
        reminderSwitch = findViewById(R.id.switch_alert);
        categorySpinner = findViewById(R.id.spinner_category);
        Button saveButton = findViewById(R.id.button_save);

        findViewById(R.id.button_back).setOnClickListener(view -> finish());
        saveButton.setOnClickListener(view -> saveEvent());

        loadEventForEditing();
    }

    private void loadEventForEditing() {
        eventId = getIntent().getIntExtra(EXTRA_EVENT_ID, -1);
        if (eventId == -1) {
            return;
        }

        Event event = databaseHelper.getEventById(eventId);
        if (event == null) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        titleInput.setText(event.getTitle());
        dateInput.setText(event.getDate());
        timeInput.setText(event.getTime());
        reminderSwitch.setChecked(event.isReminderEnabled());
        String[] categories = getResources().getStringArray(R.array.event_categories_values);
        for (int index = 0; index < categories.length; index++) {
            if (categories[index].equals(event.getCategory())) {
                categorySpinner.setSelection(index);
                break;
            }
        }
    }

    private void saveEvent() {
        String title = titleInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        boolean reminderEnabled = reminderSwitch.isChecked();
        String[] categoryValues = getResources().getStringArray(R.array.event_categories_values);
        String category = categoryValues[categorySpinner.getSelectedItemPosition()];

        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        long eventTime = ReminderScheduler.parseTriggerTime(date, time);
        if (eventTime < 0) {
            Toast.makeText(
                    this,
                    "Use MM/DD/YYYY for the date and a time such as 1:30 PM",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (reminderEnabled && eventTime <= System.currentTimeMillis()) {
            Toast.makeText(this, "Reminder time must be in the future", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean saved;
        if (eventId == -1) {
            long insertedId = databaseHelper.insertEvent(title, date, time, reminderEnabled, category);
            saved = insertedId != -1;
            if (saved) {
                eventId = (int) insertedId;
            }
        } else {
            ReminderScheduler.cancel(this, eventId);
            saved = databaseHelper.updateEvent(eventId, title, date, time, reminderEnabled, category);
        }

        if (!saved) {
            Toast.makeText(this, "Unable to save event", Toast.LENGTH_SHORT).show();
            return;
        }

        Event savedEvent = databaseHelper.getEventById(eventId);
        if (savedEvent != null && reminderEnabled && !ReminderScheduler.schedule(this, savedEvent)) {
            Toast.makeText(
                    this,
                    "Event saved, but the reminder could not be scheduled",
                    Toast.LENGTH_LONG
            ).show();
        } else {
            Toast.makeText(
                    this,
                    getIntent().hasExtra(EXTRA_EVENT_ID) ? "Event Updated" : "Event Added",
                    Toast.LENGTH_SHORT
            ).show();
        }

        finish();
    }
}
