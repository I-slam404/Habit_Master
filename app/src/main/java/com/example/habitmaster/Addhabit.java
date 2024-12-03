package com.example.habitmaster;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Addhabit extends AppCompatActivity {

    private EditText HabitName_edt;
    private Spinner spinnerFrequency;
    private TextView SelectDate_tv, SelectTime_tv;
    private Button SaveHabit_btn;
    private DBHelper dbHelper;
    private Calendar selectedDateTime = Calendar.getInstance();
    private boolean isDateSet = false;
    private boolean isTimeSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_addhabit);

        HabitName_edt = findViewById(R.id.editTextHabitName);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        SelectDate_tv = findViewById(R.id.SelectDate_tv);
        SelectTime_tv = findViewById(R.id.SelectTime_tv);
        SaveHabit_btn = findViewById(R.id.SaveHabit_btn);
        dbHelper = new DBHelper(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.frequency_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapter);

        // Set up date selection
        SelectDate_tv.setOnClickListener(v -> showDatePicker());

        // Set up time selection
        SelectTime_tv.setOnClickListener(v -> showTimePicker());

        // Save habit with selected date and time
        SaveHabit_btn.setOnClickListener(view -> saveHabit());

    }

    private void showDatePicker() {
        int year = selectedDateTime.get(Calendar.YEAR);
        int month = selectedDateTime.get(Calendar.MONTH);
        int day = selectedDateTime.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            selectedDateTime.set(year1, month1, dayOfMonth);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SelectDate_tv.setText(dateFormat.format(selectedDateTime.getTime()));
            isDateSet = true; // Mark date as set
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        int hour = selectedDateTime.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDateTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute1);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SelectTime_tv.setText(timeFormat.format(selectedDateTime.getTime()));
            isTimeSet = true; // Mark time as set
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private void saveHabit() {

        String name = HabitName_edt.getText().toString().trim();
        String frequency = (spinnerFrequency.getSelectedItem() != null) ? spinnerFrequency.getSelectedItem().toString() : "";

        // Format the selected date and time
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String dateTime = dateTimeFormat.format(selectedDateTime.getTime());

        // region Validate input fields
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter habit name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (frequency.isEmpty()) {
            Toast.makeText(this, "Please select a frequency", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isDateSet || !isTimeSet) { // Check if both date and time are set
            Toast.makeText(this, "Please select both date and time", Toast.LENGTH_SHORT).show();
            return;
        }
        // endregion

        // Get user_id from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return; // Stop execution if the user ID is invalid.
        }

        // Save the habit with the selected date and time, including user_id
        boolean isAdded = dbHelper.addHabit(name, frequency, dateTime, userId);

        if (isAdded) {
            Toast.makeText(this, "Habit added successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Addhabit.this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to add habit", Toast.LENGTH_SHORT).show();
        }
    }
}

