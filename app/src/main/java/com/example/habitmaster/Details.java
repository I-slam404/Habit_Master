package com.example.habitmaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Details extends AppCompatActivity {

    private TextView tvNameDetails, tvDateDetails;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_details);



        tvNameDetails = findViewById(R.id.tv_Name_details);
        tvDateDetails = findViewById(R.id.tv_Date_details);
        btnLogout = findViewById(R.id.btnLogout);


        String habitName = getIntent().getStringExtra("habit_name");
        String habitDate = getIntent().getStringExtra("habit_date");


        if (habitName != null) {
            tvNameDetails.setText(habitName);
        }
        if (habitDate != null) {
            tvDateDetails.setText(habitDate);
        }


        btnLogout.setOnClickListener(view -> {

            Intent intent = new Intent(Details.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
