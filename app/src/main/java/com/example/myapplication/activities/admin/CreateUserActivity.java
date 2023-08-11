package com.example.myapplication.activities.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityCreateUserBinding;

public class CreateUserActivity extends AppCompatActivity {
    ActivityCreateUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityCreateUserBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        this.handleBackPressed();
    }

    private void handleBackPressed() {
        this.binding.backButton.setOnClickListener(v -> onBackPressed());
    }
}