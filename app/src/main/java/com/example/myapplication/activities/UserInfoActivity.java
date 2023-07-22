package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.myapplication.databinding.ActivityUserInfoBinding;

public class UserInfoActivity extends AppCompatActivity {
    private ActivityUserInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
    }
}