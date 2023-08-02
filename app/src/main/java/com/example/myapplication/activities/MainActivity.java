package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityMainBinding.inflate(getLayoutInflater());
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        loadUserData();
        getToken();
        handleSignOut();
        handleShowFriends();
        handleFindFriends();
        handleShowNotifications();
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void loadUserData() {
        this.binding.usernameTextView.setText(preferenceManager.getString(Constant.KEY_NAME));
    }

    private void updateToken(String token) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference dr = db.collection(Constant.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constant.KEY_USER_ID));
        dr.update(Constant.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused -> showToast("Token updated successfully"))
                .addOnFailureListener(ex -> showToast("Unable to update token"));
    }

    private void handleSignOut() {
        this.binding.signOutButton.setOnClickListener(v -> signOut());
    }

    private void signOut() {
        showToast("Signing out...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference dr = db.collection(Constant.KEY_COLLECTION_USERS).document(this.preferenceManager.getString(Constant.KEY_USER_ID));

        HashMap<String, Object> newData = new HashMap<>();
        newData.put(Constant.KEY_FCM_TOKEN, FieldValue.delete());
        dr.update(newData)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(ex -> {
                    showToast("Unable to sign out");
                });
    }

    private void handleShowFriends() {
        this.binding.showFriendsButton.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
        });
    }

    private void handleFindFriends() {
        this.binding.findFriendsButton.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), UsersActivity.class));
        });
    }

    private void handleShowNotifications() {
        this.binding.notificationsButton.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
        });
    }

    private void getNotificationsCount() {

    }
}