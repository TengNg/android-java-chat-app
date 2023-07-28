package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        this.handleShowFriendRequests();
        this.initialize();
//        getFriend();
        this.listenFriends();
        this.listenActiveStatus();
        this.listenFriendRequests();
//        this.getFriendRequestCount();
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

    private void listenActiveStatus() {
        db.collection(Constant.KEY_COLLECTION_USERS)
                .addSnapshotListener(friendsActiveStatusEventListener);
    }

    private void listenFriends() {
        db.collection(Constant.KEY_COLLECTION_USERS)
                .document(this.preferenceManager.getString(Constant.KEY_USER_ID))
                .collection(Constant.KEY_COLLECTION_USER_FRIENDS)
                .addSnapshotListener(friendsEventListener);
    }

    private void listenFriendRequests() {
        db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS)
                .whereEqualTo(Constant.KEY_RECEIVER_ID, this.preferenceManager.getString(Constant.KEY_USER_ID))
                .whereEqualTo(Constant.KEY_FRIEND_REQUEST_STATUS, "pending")
                .addSnapshotListener(friendRequestsEventListener);
    }

    private final EventListener<QuerySnapshot> friendRequestsEventListener = (value, error) -> {
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
                String friendId = documentChange.getDocument().getData().keySet().iterator().next(); // get only first field in a document
                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                    DocumentReference friendUserRef = usersRef.document(friendId);

                    friendUserRef.get().addOnCompleteListener(friendTask -> {
                        if (friendTask.isSuccessful()) {
                            DocumentSnapshot friendDocument = friendTask.getResult();
                            if (friendDocument.exists()) {
                                String friendUsername = friendDocument.getString(Constant.KEY_NAME);
                                String friendEmail = friendDocument.getString(Constant.KEY_EMAIL);
                                String friendToken = friendDocument.getString(Constant.KEY_FCM_TOKEN);

                                User user = new User();
                                user.name = friendUsername;
                                user.email = friendEmail;
                                user.token = friendToken;
                                user.id = friendTask.getResult().getId();

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

        } else {
            this.showErrorMsg();
        }
    };

    private final EventListener<QuerySnapshot> friendsActiveStatusEventListener = (value, error) -> {
        if (error != null) {
            return;
        }

        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getDocument().getId().equals(this.preferenceManager.getString(Constant.KEY_USER_ID)))
                    continue;

                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    String changedModelEmail = documentChange.getDocument().getString(Constant.KEY_EMAIL);
                    for (int i = 0; i < this.users.size(); i++) {
                        if (this.users.get(i).email.equals(changedModelEmail)) {
                            this.users.get(i).token = documentChange.getDocument().getString(Constant.KEY_FCM_TOKEN);
                            this.friendsAdapter.notifyItemChanged(i);
                        }
                    }
                }

            }
        } else {
            this.showErrorMsg();
        }

        this.binding.progressCircular.setVisibility(View.GONE);
    };

//    private void getFriends() {
//        String specificUserID = this.preferenceManager.getString(Constant.KEY_USER_ID);
//        CollectionReference usersRef = db.collection(Constant.KEY_COLLECTION_USERS);
//        CollectionReference specificUserFriendsRef = db.collection(Constant.KEY_COLLECTION_USERS).document(specificUserID).collection(Constant.KEY_COLLECTION_USER_FRIENDS);
//
//        specificUserFriendsRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                for (QueryDocumentSnapshot document : task.getResult()) {
//                    Map<String, Object> map = document.getData();
//
//                    if (map == null) continue;
//
//                    // get all friend ids
//                    for (Map.Entry<String, Object> entry : map.entrySet()) {
//                        this.friendIDs.add(entry.getKey());
//                    }
//                }
//
//                // > iterative through friend ids
//                // > get friend info (user details)
//                // > add to friendsAdapter
//                for (String friendID : this.friendIDs) {
//                    DocumentReference friendUserRef = usersRef.document(friendID);
//                    friendUserRef.get().addOnCompleteListener(friendTask -> {
//                        if (friendTask.isSuccessful()) {
//                            DocumentSnapshot friendDocument = friendTask.getResult();
//                            if (friendDocument.exists()) {
//                                String friendUsername = friendDocument.getString(Constant.KEY_NAME);
//                                String friendEmail = friendDocument.getString(Constant.KEY_EMAIL);
//                                String friendToken = friendDocument.getString(Constant.KEY_FCM_TOKEN);
//
//                                User user = new User();
//                                user.name = friendUsername;
//                                user.email = friendEmail;
//                                user.token = friendToken;
//                                user.id = friendTask.getResult().getId();
//
//                                this.users.add(user);
//                                this.friendsAdapter.notifyDataSetChanged();
//                            } else {
//                                Log.d("FriendInfo", "Friend document does not exist");
//                            }
//                        } else {
//                            Log.e("Error", "Error getting friend document: ", friendTask.getException());
//                        }
//                    });
//                }
//
//                this.binding.progressCircular.setVisibility(View.GONE);
//            } else {
//                Log.e("Error", "Error getting friends for specific user: " + specificUserID, task.getException());
//            }
//        });
//
//    }

    @Override
    public void onFriendClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constant.KEY_USER, user);
        startActivity(intent);
    }
}
