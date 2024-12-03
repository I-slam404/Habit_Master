package com.example.habitmaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private HabitAdapter habitAdapter;
    private ArrayList<Habit> habitList;
    private DBHelper dbHelper;
    private Button btnAddHabit;
    private TextView Logout_tv;

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        btnAddHabit = findViewById(R.id.AddHabit_Btn);

        Logout_tv  = findViewById(R.id.Logout_tv);

        dbHelper = new DBHelper(this);

        // Get the user_id from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        // Initialize habit list and set up RecyclerView and adapter
        habitList = new ArrayList<>();
        habitAdapter = new HabitAdapter(this, habitList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(habitAdapter);

        // Load habits when activity is created
        loadHabits(userId); // Pass userId to load habits specific to the user

        // Button to add a new habit
        btnAddHabit.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Addhabit.class);
            startActivity(intent);
        });

        Logout_tv.setOnClickListener(view -> {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.apply();
            Toast.makeText(MainActivity.this, "Logged Out Successfully", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the habit list when returning to this activity
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);
        loadHabits(userId);
    }

    @SuppressLint("Range")
    private void loadHabits(int userId) {
        Cursor cursor = null;
        try {
            // Fetch habits from the database specific to the user
            cursor = dbHelper.getUserHabits(userId);
            Log.d(TAG, "Number of habits loaded: " + (cursor != null ? cursor.getCount() : 0));

            habitList.clear(); // Clear the list to avoid duplicates

            if (cursor == null || cursor.getCount() == 0) {
                if (habitList.isEmpty()) {
                    Toast.makeText(this, "No habits saved", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME));
                String frequency = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FREQUENCY));
                String dateTime = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DATE)); // Retrieve date and time

                // Add the habit to the beginning of the list
                habitList.add(0, new Habit(id, name, frequency, dateTime));
            }

            habitAdapter.notifyDataSetChanged(); // Notify adapter of data changes
        } catch (Exception e) {
            Log.e(TAG, "Error loading habits", e);
            Toast.makeText(this, "Error loading habits", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close(); // Ensure cursor is closed after use
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    finishAffinity();
                    System.exit(0);
                })
                .setNegativeButton("No", null)
                .show();
    }
}
