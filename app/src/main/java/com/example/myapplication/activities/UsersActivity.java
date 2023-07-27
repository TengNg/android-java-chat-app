package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.adapters.UsersAdapter;
import com.example.myapplication.databinding.ActivityUsersBinding;
import com.example.myapplication.listeners.UserListener;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UsersActivity extends AppCompatActivity implements UserListener {
    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    private List<User> users;
    private Set<String> friendIds;
    private UsersAdapter usersAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.binding.progressCircular.setVisibility(View.VISIBLE);
        this.handleBackPressed();
        initialize();
        listenUsers();
    }

    private void initialize() {
        this.users = new ArrayList<>();
        this.friendIds = new HashSet<>();
        this.usersAdapter = new UsersAdapter(this.users, this);
        this.db = FirebaseFirestore.getInstance();
        this.binding.usersRecyclerView.setAdapter(usersAdapter);
    }

    private void showErrorMsg() {
        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
    }

    private void handleBackPressed() {
        this.binding.backButton.setOnClickListener(v -> onBackPressed());
    }

    private void listenUsers() {
        db.collection(Constant.KEY_COLLECTION_USERS)
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }

        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getDocument().getId().equals(this.preferenceManager.getString(Constant.KEY_USER_ID)))
                    continue;

                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    User u = new User();
                    u.email = documentChange.getDocument().getString(Constant.KEY_EMAIL);
                    u.name = documentChange.getDocument().getString(Constant.KEY_NAME);
                    u.token = documentChange.getDocument().getString(Constant.KEY_FCM_TOKEN);
                    u.id = documentChange.getDocument().getId();
                    this.users.add(u);
                    this.usersAdapter.notifyDataSetChanged();
                }
//                else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
//                    String changedModelEmail = documentChange.getDocument().getString(Constant.KEY_EMAIL);
//                    for (int i = 0; i < this.users.size(); i++) {
//                        if (this.users.get(i).email.equals(changedModelEmail)) {
//                            this.users.get(i).token = documentChange.getDocument().getString(Constant.KEY_FCM_TOKEN);
//                            this.usersAdapter.notifyItemChanged(i);
//                        }
//                    }
//                }
            }
        } else {
            this.showErrorMsg();
        }

        this.binding.progressCircular.setVisibility(View.GONE);
    };

    private void getFriendIds() {
        String specificUserID = this.preferenceManager.getString(Constant.KEY_USER_ID);
        CollectionReference specificUserFriendsRef = db.collection(Constant.KEY_COLLECTION_USERS).document(specificUserID).collection(Constant.KEY_COLLECTION_USER_FRIENDS);

        specificUserFriendsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Map<String, Object> map = document.getData();

                    if (map == null) continue;

                    // get all friend ids
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        this.friendIds.add(entry.getKey());
                    }
                }
            }
        });
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
        intent.putExtra(Constant.KEY_USER, user);
        startActivity(intent);
    }

    @Override
    public void onAddFriendButtonClicked(User user) {
//        HashMap<String, Boolean> friendId1 = new HashMap<>();
//        friendId1.put(user.id, true);
//        this.db.collection(Constant.KEY_COLLECTION_USERS)
//                .document(this.preferenceManager.getString(Constant.KEY_USER_ID))
//                .collection(Constant.KEY_COLLECTION_USER_FRIENDS)
//                .add(friendId1);
//
//        HashMap<String, Boolean> friendId2 = new HashMap<>();
//        friendId2.put(this.preferenceManager.getString(Constant.KEY_USER_ID), true);
//        this.db.collection(Constant.KEY_COLLECTION_USERS)
//                .document(user.id)
//                .collection(Constant.KEY_COLLECTION_USER_FRIENDS)
//                .add(friendId2);
//
//        Toast.makeText(getApplicationContext(), "Friend added", Toast.LENGTH_SHORT).show();

        // TODO: send notification to NotificationActivity & FriendsActivity
    }

    @Override
    public void onMessageButtonClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constant.KEY_USER, user);
        startActivity(intent);
    }
}