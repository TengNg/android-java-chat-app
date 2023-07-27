package com.example.myapplication.activities;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class UsersActivity extends AppCompatActivity implements UserListener {
    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    private List<User> users;
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
        getUsers();
    }

    private void initialize() {
        this.users = new ArrayList<>();
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

    public void getUsers() {
        CollectionReference usersCollection = this.db.collection(Constant.KEY_COLLECTION_USERS);
        String specificUserID = this.preferenceManager.getString(Constant.KEY_USER_ID);
        CollectionReference specificUserFriendsRef = usersCollection.document(specificUserID).collection(Constant.KEY_COLLECTION_USER_FRIENDS);

        specificUserFriendsRef.get().addOnSuccessListener(friendsQuerySnapshot -> {
            List<String> friendIDs = new ArrayList<>();
            for (DocumentSnapshot documentSnapshot : friendsQuerySnapshot.getDocuments()) {
                String friendID = documentSnapshot.getData().keySet().iterator().next(); // get only first field in a document
                friendIDs.add(friendID);
            }

            usersCollection.get().addOnSuccessListener(usersQuerySnapshot -> {
                List<DocumentSnapshot> allUsers = usersQuerySnapshot.getDocuments();
                List<DocumentSnapshot> nonFriendUsers = new ArrayList<>();

                for (DocumentSnapshot user : allUsers) {
                    String userID = user.getId();
                    if (!friendIDs.contains(userID) && !userID.equals(specificUserID)) {
                        nonFriendUsers.add(user);
                    }
                }

                for (DocumentSnapshot document : nonFriendUsers) {
                    User u = new User();
                    u.email = document.getString(Constant.KEY_EMAIL);
                    u.name = document.getString(Constant.KEY_NAME);
                    u.token = document.getString(Constant.KEY_FCM_TOKEN);
                    u.id = document.getId();
                    this.users.add(u);
                    this.usersAdapter.notifyDataSetChanged();
                }

                this.binding.progressCircular.setVisibility(View.GONE);
            }).addOnFailureListener(e -> {
                this.showErrorMsg();
            });
        }).addOnFailureListener(e -> {
            this.showErrorMsg();
        });

//        Task<QuerySnapshot> usersTask = usersCollection.get();
//        usersTask.continueWithTask(task -> {
//            CollectionReference specificUserFriendsRef = usersCollection.document(specificUserID).collection(Constant.KEY_COLLECTION_USER_FRIENDS);
//            return specificUserFriendsRef.get();
//        }).addOnSuccessListener(friendsQuerySnapshot -> {
//            List<DocumentSnapshot> allUsers = usersTask.getResult().getDocuments();
//            List<DocumentSnapshot> nonFriendUsers = new ArrayList<>();
//            List<String> friendIDs = new ArrayList<>();
//
//            for (DocumentSnapshot friend : friendsQuerySnapshot.getDocuments()) {
//                friendIDs.add(friend.getId());
//                String id = friend.getData().keySet().iterator().next(); // get only first field in a document
//                friendIDs.add(id);
//            }
//
//            for (DocumentSnapshot user : allUsers) {
//                String userID = user.getId();
//
//                if (userID.equals(specificUserID))
//                    continue;
//
//                if (friendIDs.contains(userID))
//                    continue;
//
//                nonFriendUsers.add(user);
//            }
//
//            for (DocumentSnapshot document : nonFriendUsers) {
//                User u = new User();
//                u.email = document.getString(Constant.KEY_EMAIL);
//                u.name = document.getString(Constant.KEY_NAME);
//                u.token = document.getString(Constant.KEY_FCM_TOKEN);
//                u.id = document.getId();
//                this.users.add(u);
//                this.usersAdapter.notifyDataSetChanged();
//            }
//
//            this.binding.progressCircular.setVisibility(View.GONE);
//
//        }).addOnFailureListener(e -> {
//            this.showErrorMsg();
//        });

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
        String senderId = this.preferenceManager.getString(Constant.KEY_USER_ID);
        String receiverId = user.id;
        String friendRequestID = this.db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS).document().getId();
        HashMap<String, Object> friendRequestData = new HashMap<>();
        friendRequestData.put("senderID", senderId);
        friendRequestData.put("receiverID", receiverId);
        friendRequestData.put("status", "pending");
        friendRequestData.put("timestamp", new Date());

        this.db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS)
                .document(friendRequestID)
                .set(friendRequestData)
                .addOnSuccessListener(aVoid -> {
                    // Friend request sent successfully
                    Toast.makeText(getApplicationContext(), "Friend request sent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Error occurred while sending friend request
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onMessageButtonClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constant.KEY_USER, user);
        startActivity(intent);
    }

    private String getSimpleMessageDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
}