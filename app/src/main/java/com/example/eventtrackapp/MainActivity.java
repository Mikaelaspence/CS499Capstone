package com.example.eventtrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);

        // Initialize the database helper
        databaseHelper = new DatabaseHelper(this);

        // Apply window insets listener to the root layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up button listeners
        loginButton.setOnClickListener(v -> login());
        registerButton.setOnClickListener(v -> register());
    }

    private void login() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use the database helper to check if the user exists
        if (databaseHelper.checkUser(username, password)) {

            // If login is successful, navigate to the Event Display Screen
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            Log.d("LOGIN", "User credentials are valid. Redirecting to EventDisplayActivity.");
            Intent intent = new Intent(MainActivity.this, EventDisplayActivity.class);
            startActivity(intent);

            //finish();  // Close the MainActivity to prevent returning to the login screen
        } else {
            Log.d("LOGIN", "Invalid login credentials.");
            Toast.makeText(this, "Invalid login credentials", Toast.LENGTH_SHORT).show();
        }
    }

    private void register() {
        // Navigate to the Register Activity
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
