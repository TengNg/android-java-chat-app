package com.example.myapplication.activities.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import com.example.myapplication.databinding.ActivityUserAccountControllerBinding;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserAccountControllerActivity extends AppCompatActivity {
    ActivityUserAccountControllerBinding binding;
    PreferenceManager preferenceManager;
    FirebaseFirestore db;
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityUserAccountControllerBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        this.handleBackPressed();
        this.initialize();
        this.getUserData();
        this.handleOpenUserProfileEditor();
    }

    private void handleBackPressed() {
        this.binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void initialize() {
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.db = FirebaseFirestore.getInstance();
        this.currentUser = (User) getIntent().getSerializableExtra(Constant.KEY_USER);
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void getUserData() {
        User currentUser = ((User) getIntent().getSerializableExtra(Constant.KEY_USER));
        this.binding.profileImageReceiverView.setImageBitmap(this.getUserImage(currentUser.image));
        this.binding.usernameTextView.setText(currentUser.name);
        this.binding.userEmailTextView.setText(currentUser.email);
        this.binding.userGenderTextView.setText(currentUser.gender);
    }

    private void handleOpenUserProfileEditor() {
        this.binding.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditUserInformationActivity.class);
            intent.putExtra(Constant.KEY_USER, this.currentUser);
            startActivity(intent);
        });
    }
}