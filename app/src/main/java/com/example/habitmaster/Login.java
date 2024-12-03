package com.example.habitmaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Login extends AppCompatActivity {

    EditText etLoginName, etLoginPassword;
    Button btnLogin;
    TextView tvGoToRegister;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {

            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etLoginName = findViewById(R.id.editTextUsername);
        etLoginPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.Login_btn);
        tvGoToRegister = findViewById(R.id.GoToRegister_Tv);
        dbHelper = new DBHelper(this);



        btnLogin.setOnClickListener(view -> {
            String name = etLoginName.getText().toString();
            String password = etLoginPassword.getText().toString();


            if (dbHelper.checkUser(name, password)) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();


                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.apply();


                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }
        });


        tvGoToRegister.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });
    }
}