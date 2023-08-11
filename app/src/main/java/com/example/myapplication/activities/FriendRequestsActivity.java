package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.adapters.FriendRequestsAdapter;
import com.example.myapplication.databinding.ActivityFriendRequestsBinding;
import com.example.myapplication.listeners.FriendRequestListener;
import com.example.myapplication.models.FriendRequest;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FriendRequestsActivity extends AppCompatActivity implements FriendRequestListener {
    private ActivityFriendRequestsBinding binding;
    private List<FriendRequest> friendRequests;
    private FriendRequestsAdapter friendRequestsAdapter;
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityFriendRequestsBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        this.handleBackPressed();
        this.initialize();
        this.listenFriendRequests();
    }

    private void initialize() {
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.db = FirebaseFirestore.getInstance();
        this.friendRequests = new ArrayList<>();
        this.friendRequestsAdapter = new FriendRequestsAdapter(friendRequests, this);
        this.binding.friendRequestsRecyclerView.setAdapter(this.friendRequestsAdapter);
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void handleBackPressed() {
        this.binding.backButton.setOnClickListener(v -> onBackPressed());
    }

    private void listenFriendRequests() {
        db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS)
                .whereEqualTo(Constant.KEY_RECEIVER_ID, this.preferenceManager.getString(Constant.KEY_USER_ID))
                .addSnapshotListener(friendRequestsEventListener);
    }

    private final EventListener<QuerySnapshot> friendRequestsEventListener = (value, error) -> {
        if (error != null) {
            return;
        }

        if (value != null) {
            int idx = 0;
            for (DocumentChange document : value.getDocumentChanges()) {
                if (document.getType() == DocumentChange.Type.ADDED) {
                    FriendRequest friendRequest = new FriendRequest();
                    friendRequest.id = document.getDocument().getId();
                    friendRequest.status = document.getDocument().getString(Constant.KEY_FRIEND_REQUEST_STATUS);
                    friendRequest.receiverId = document.getDocument().getString(Constant.KEY_RECEIVER_ID);
                    friendRequest.receiverName = document.getDocument().getString(Constant.KEY_RECEIVER_NAME);
                    friendRequest.senderId = document.getDocument().getString(Constant.KEY_SENDER_ID);
                    friendRequest.senderName = document.getDocument().getString(Constant.KEY_SENDER_NAME);
                    friendRequest.senderImage = document.getDocument().getString(Constant.KEY_SENDER_IMAGE);
                    friendRequest.dateObject = document.getDocument().getDate(Constant.KEY_TIMESTAMP);
                    friendRequest.dateTime = getSimpleMessageDateTime(friendRequest.dateObject);
                    this.friendRequests.add(friendRequest);
                    this.friendRequestsAdapter.notifyDataSetChanged();
                } else if (document.getType() == DocumentChange.Type.REMOVED) {
                    this.friendRequests.remove(idx);
                    this.friendRequestsAdapter.notifyDataSetChanged();
                }
                idx += 1;
            }
            this.binding.progressCircular.setVisibility(View.GONE);
        }
    };

    private String getSimpleMessageDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    @Override
    public void onAcceptButtonClicked(FriendRequest friendRequest) {
        String senderId = friendRequest.senderId;
        String receiverId = friendRequest.receiverId;

        HashMap<String, Boolean> friendId1 = new HashMap<>();
        friendId1.put(receiverId, true);
        this.db.collection(Constant.KEY_COLLECTION_USERS)
                .document(senderId)
                .collection(Constant.KEY_COLLECTION_USER_FRIENDS)
                .document(receiverId)
                .set(friendId1);

        HashMap<String, Boolean> friendId2 = new HashMap<>();
        friendId2.put(senderId, true);
        this.db.collection(Constant.KEY_COLLECTION_USERS)
                .document(receiverId)
                .collection(Constant.KEY_COLLECTION_USER_FRIENDS)
                .document(senderId)
                .set(friendId2);

        DocumentReference docRef = this.db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS).document(friendRequest.id);
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(Constant.KEY_FRIEND_REQUEST_STATUS, "accepted");
        docRef.update(updateData);

        this.showToast("Friend request accepted");
    }

    @Override
    public void onDeclineButtonClicked(FriendRequest friendRequest) {
        DocumentReference docRef = this.db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS).document(friendRequest.id);
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(Constant.KEY_FRIEND_REQUEST_STATUS, "declined");
        docRef.update(updateData);

        this.showToast("Friend request declined");
    }

    @Override
    public void onUserImageClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
        intent.putExtra(Constant.KEY_USER, user);
        startActivity(intent);
    }
}