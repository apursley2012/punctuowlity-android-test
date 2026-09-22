package com.alyshapursley.punctuowlity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public final class LoginActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private EditText usernameInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);
        usernameInput = findViewById(R.id.edit_username);
        passwordInput = findViewById(R.id.edit_password);
        Button loginButton = findViewById(R.id.button_login);
        Button signUpButton = findViewById(R.id.button_sign_up);

        loginButton.setOnClickListener(view -> logIn());
        findViewById(R.id.button_back).setOnClickListener(view -> finish());
        signUpButton.setOnClickListener(
                view -> startActivity(new Intent(this, SignupActivity.class))
        );
    }

    private void logIn() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!databaseHelper.checkUser(username, password)) {
            Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences preferences = getSharedPreferences(
                SmsReminderReceiver.PREFERENCES_NAME,
                MODE_PRIVATE
        );
        Class<?> destination = preferences.getBoolean(
                SmsReminderReceiver.SMS_PREFERENCE_SET,
                false
        ) ? MainActivity.class : SmsPermissionActivity.class;

        startActivity(new Intent(this, destination));
        finish();
    }
}
