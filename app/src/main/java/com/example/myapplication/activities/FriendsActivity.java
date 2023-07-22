package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.adapters.FriendsAdapter;
import com.example.myapplication.databinding.ActivityFriendsBinding;
import com.example.myapplication.databinding.ActivityUsersBinding;
import com.example.myapplication.listeners.FriendListener;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity implements FriendListener {
    private ActivityFriendsBinding binding;
    private PreferenceManager preferenceManager;
    private List<User> users;
    private FriendsAdapter friendsAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityFriendsBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.binding.progressCircular.setVisibility(View.VISIBLE);
        this.handleBackPressed();
        initialize();
        listenUsers();
    }

    private void initialize() {
        this.users = new ArrayList<>();
        this.friendsAdapter = new FriendsAdapter(this.users, this);
        this.db = FirebaseFirestore.getInstance();
        this.binding.friendsRecyclerView.setAdapter(friendsAdapter);
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
                    this.friendsAdapter.notifyDataSetChanged();
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    String changedModelEmail = documentChange.getDocument().getString(Constant.KEY_EMAIL);
                    for (int i = 0; i < this.users.size(); i++) {
                        if (this.users.get(i).email.equals(changedModelEmail)) {
                            this.users.get(i).token = documentChange.getDocument().getString(Constant.KEY_FCM_TOKEN);
                            this.friendsAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }
        } else {
            this.showErrorMsg();
        }
        this.binding.progressCircular.setVisibility(View.GONE);
    };

    @Override
    public void onFriendClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constant.KEY_USER, user);
        startActivity(intent);
    }
}