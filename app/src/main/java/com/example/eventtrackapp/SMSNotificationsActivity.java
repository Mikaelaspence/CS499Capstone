package com.example.eventtrackapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SMSNotificationsActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;
    private Button requestPermissionButton;
    private TextView permissionStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_notifications);

        requestPermissionButton = findViewById(R.id.request_sms_permission_button);
        permissionStatusTextView = findViewById(R.id.sms_permission_status);

        // Check and update the initial permission status
        if (checkSmsPermission()) {
            updatePermissionStatus("SMS permission already granted");

            // Automatically return to the previous screen if permission is already granted
            Toast.makeText(this, "SMS permission already granted", Toast.LENGTH_SHORT).show();
            finish(); // Close this activity and return to the previous one
        } else {
            updatePermissionStatus("SMS permission not granted");
            requestPermissionButton.setOnClickListener(v -> requestSmsPermission());
        }
    }

    private boolean checkSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
    }

    private void updatePermissionStatus(String status) {
        permissionStatusTextView.setText("Permission Status: " + status);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updatePermissionStatus("SMS permission granted");
                Log.d("SMSNotificationsActivity", "SMS permission granted");
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
                finish(); // Close this activity and return to the previous one
            } else {
                updatePermissionStatus("SMS permission denied");
                Log.d("SMSNotificationsActivity", "SMS permission denied");
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
