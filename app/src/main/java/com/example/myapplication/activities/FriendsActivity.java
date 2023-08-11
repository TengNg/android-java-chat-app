package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.adapters.FriendsAdapter;
import com.example.myapplication.databinding.ActivityFriendsBinding;
import com.example.myapplication.listeners.FriendListener;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends BaseActivity implements FriendListener {
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
        this.handleShowFriendRequests();
        this.initialize();
        this.listenFriends();
        this.listenFriendRequestsCount();
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

    private void handleShowFriendRequests() {
        this.binding.pendingButton.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), FriendRequestsActivity.class));
        });
    }

    private void listenFriends() {
        db.collection(Constant.KEY_COLLECTION_USERS)
                .document(this.preferenceManager.getString(Constant.KEY_USER_ID))
                .collection(Constant.KEY_COLLECTION_USER_FRIENDS)
                .addSnapshotListener(friendsEventListener);
    }

    private void listenFriendRequestsCount() {
        db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS)
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
                this.binding.friendRequestCountTextView.setText(String.valueOf(requestCount));
            }
        }
    };

    private final EventListener<QuerySnapshot> friendsEventListener = (value, error) -> {
        if (error != null) {
            return;
        }

        if (value != null) {
            CollectionReference usersRef = db.collection(Constant.KEY_COLLECTION_USERS);

            for (DocumentChange documentChange : value.getDocumentChanges()) {
                String friendId = documentChange.getDocument().getId();
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    DocumentReference friendUserRef = usersRef.document(friendId);
                    friendUserRef.get().addOnCompleteListener(friendTask -> {
                        if (friendTask.isSuccessful()) {
                            DocumentSnapshot friendDocument = friendTask.getResult();
                            if (friendDocument.exists()) {
                                String friendUsername = friendDocument.getString(Constant.KEY_NAME);
                                String friendEmail = friendDocument.getString(Constant.KEY_EMAIL);
                                String friendToken = friendDocument.getString(Constant.KEY_FCM_TOKEN);
                                String image = friendDocument.getString(Constant.KEY_IMAGE);
                                String gender = friendDocument.getString(Constant.KEY_GENDER);
                                boolean friendAvailability = Boolean.TRUE.equals(friendDocument.getBoolean(Constant.KEY_IS_AVAILABLE));
                                User user = new User();
                                user.name = friendUsername;
                                user.email = friendEmail;
                                user.token = friendToken;
                                user.id = friendTask.getResult().getId();
                                user.image = image;
                                user.gender = gender;
                                user.isAvailable = friendAvailability;
                                this.users.add(user);
                                this.friendsAdapter.notifyDataSetChanged();
                            } else {
                                Log.d("FriendInfo", "Friend document does not exist");
                            }
                        } else {
                            Log.e("Error", "Error getting friend document: ", friendTask.getException());
                        }
                    });

                }
            }

            this.binding.progressCircular.setVisibility(View.GONE);

            if (users.size() > 0) {
                this.binding.searchInput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (editable.length() == 0) {
                            friendsAdapter.updateList(users);
                            return;
                        }

                        List<User> filteredList = new ArrayList<>();
                        for (User user : users) {
                            if (user.name.contains(editable.toString())) {
                                filteredList.add(user);
                            }
                        }

                        friendsAdapter.updateList(filteredList);
                    }
                });
            }
        } else {
            this.showErrorMsg();
        }
    };

    @Override
    public void onFriendClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constant.KEY_USER, user);
        startActivity(intent);
    }

    @Override
    public void onShowInfoButtonClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
        intent.putExtra(Constant.KEY_USER, user);
        startActivity(intent);
    }
}
