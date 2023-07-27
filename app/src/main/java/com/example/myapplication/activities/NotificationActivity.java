package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.myapplication.databinding.ActivityNotificationBinding;

public class NotificationActivity extends AppCompatActivity {
    private ActivityNotificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        handleBackPressed();
    }

    private void handleBackPressed() {

    }
}