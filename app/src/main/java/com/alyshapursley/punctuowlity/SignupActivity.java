package com.alyshapursley.punctuowlity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public final class SignupActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private CheckBox termsCheckbox;
    private CheckBox emailUpdatesCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        databaseHelper = new DatabaseHelper(this);
        emailInput = findViewById(R.id.edit_email);
        passwordInput = findViewById(R.id.edit_password);
        confirmPasswordInput = findViewById(R.id.edit_confirm_password);
        termsCheckbox = findViewById(R.id.checkbox_terms_privacy);
        emailUpdatesCheckbox = findViewById(R.id.checkbox_email_updates);

        Button createAccountButton = findViewById(R.id.button_create_account);
        createAccountButton.setOnClickListener(view -> createAccount());

        findViewById(R.id.button_back).setOnClickListener(view -> finish());
    }

    private void createAccount() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmation = confirmPasswordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty() || confirmation.isEmpty()) {
            Toast.makeText(this, "All account fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmation)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!termsCheckbox.isChecked()) {
            Toast.makeText(
                    this,
                    "Accept the Privacy Policy and Terms and Conditions to continue",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!databaseHelper.insertUser(email, password)) {
            Toast.makeText(this, "An account with that email already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        getSharedPreferences(SmsReminderReceiver.PREFERENCES_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean("email_updates_enabled", emailUpdatesCheckbox.isChecked())
                .apply();

        Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
