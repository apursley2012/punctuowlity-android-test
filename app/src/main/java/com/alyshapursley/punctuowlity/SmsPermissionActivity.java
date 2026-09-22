package com.alyshapursley.punctuowlity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public final class SmsPermissionActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_REQUEST = 1001;

    private EditText phoneNumberInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        phoneNumberInput = findViewById(R.id.edit_phone_number);

        findViewById(R.id.button_allow).setOnClickListener(view -> requestSmsPermission());
        findViewById(R.id.button_deny).setOnClickListener(view -> savePreferenceAndContinue(false));
        findViewById(R.id.button_back).setOnClickListener(view -> finish());
    }

    private void requestSmsPermission() {
        String phoneNumber = phoneNumberInput.getText().toString().trim();
        if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
            Toast.makeText(
                    this,
                    "Enter the phone number that should receive event reminders",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            savePreferenceAndContinue(true);
            return;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_REQUEST
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != SMS_PERMISSION_REQUEST) {
            return;
        }

        boolean granted = grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (!granted) {
            Toast.makeText(
                    this,
                    "SMS reminders are off. Event tracking will continue normally.",
                    Toast.LENGTH_LONG
            ).show();
        }

        savePreferenceAndContinue(granted);
    }

    private void savePreferenceAndContinue(boolean enabled) {
        String phoneNumber = enabled ? phoneNumberInput.getText().toString().trim() : "";

        getSharedPreferences(SmsReminderReceiver.PREFERENCES_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(SmsReminderReceiver.SMS_PREFERENCE_SET, true)
                .putBoolean(SmsReminderReceiver.SMS_ENABLED, enabled)
                .putString(SmsReminderReceiver.SMS_PHONE_NUMBER, phoneNumber)
                .apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
