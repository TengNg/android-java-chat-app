package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.myapplication.databinding.ActivityUserInfoBinding;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;

public class UserInfoActivity extends AppCompatActivity {
    private ActivityUserInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        this.getUserInfo();
        this.handleBackPressed();
    }

    private void getUserInfo() {
        User currentUser = ((User) getIntent().getSerializableExtra(Constant.KEY_USER));
        this.binding.usernameTextView.setText(currentUser.name);
        this.binding.userEmailTextView.setText(currentUser.email);
    }

    private void handleBackPressed() {
        this.binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

}