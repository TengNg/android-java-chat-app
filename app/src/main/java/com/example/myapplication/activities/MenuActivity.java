package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivityMenuBinding;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class MenuActivity extends BaseActivity {
    private ActivityMenuBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityMenuBinding.inflate(getLayoutInflater());
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.db = FirebaseFirestore.getInstance();
        setContentView(binding.getRoot());
        loadUserData();
        handleSignOut();
        handleShowFriends();
        handleFindFriends();
        handleShowNotifications();
        handleBackPressed();
        this.listenFriendRequestsCount();
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void loadUserData() {
        this.binding.usernameTextView.setText(preferenceManager.getString(Constant.KEY_NAME));
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
        newData.put(Constant.KEY_IS_AVAILABLE, false);
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

    private void listenFriendRequestsCount() {
        this.db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS)
                .whereEqualTo(Constant.KEY_RECEIVER_ID, this.preferenceManager.getString(Constant.KEY_USER_ID))
                .whereEqualTo(Constant.KEY_FRIEND_REQUEST_STATUS, "pending")
                .addSnapshotListener(friendRequestsCountEventListener);
    }

    private final EventListener<QuerySnapshot> friendRequestsCountEventListener = (value, error) -> {
        if (error != null) {
            return;
        }

        if (value != null) {
            long requestCount = value.size();
            if (requestCount == 0) {
                this.binding.friendRequestCountTextView.setVisibility(View.GONE);
            } else {
                this.binding.friendRequestCountTextView.setVisibility(View.VISIBLE);
                this.binding.friendRequestCountTextView.setText(String.valueOf(requestCount));
            }
        }
    };

    private void handleBackPressed() {
        this.binding.usernameTextView.setOnClickListener(v -> onBackPressed());
    }
}