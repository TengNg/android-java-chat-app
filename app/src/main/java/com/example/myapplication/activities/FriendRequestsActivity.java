package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.myapplication.adapters.FriendRequestsAdapter;
import com.example.myapplication.databinding.ActivityFriendRequestsBinding;
import com.example.myapplication.listeners.FriendRequestListener;
import com.example.myapplication.models.FriendRequest;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        this.getFriendRequests();
    }


//    private void initialize() {
//        this.notifications = new ArrayList<>();
//        this.preferenceManager = new PreferenceManager(getApplicationContext());
//        this.notificationsAdapter = new NotificationsAdapter(notifications, this);
//        this.db = FirebaseFirestore.getInstance();
//        this.binding.notificationsRecyclerView.setAdapter(notificationsAdapter);
//    }

    private void initialize() {
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.db = FirebaseFirestore.getInstance();
        this.friendRequests = new ArrayList<>();
        this.friendRequestsAdapter = new FriendRequestsAdapter(friendRequests, this);
        this.binding.friendRequestsRecyclerView.setAdapter(this.friendRequestsAdapter);
    }

    private void handleBackPressed() {
        this.binding.backButton.setOnClickListener(v -> onBackPressed());
    }

    private void getFriendRequests() {
        this.db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS)
                .whereEqualTo(Constant.KEY_RECEIVER_ID, this.preferenceManager.getString(Constant.KEY_USER_ID))
                .whereEqualTo(Constant.KEY_FRIEND_REQUEST_STATUS, "pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                FriendRequest friendRequest = new FriendRequest();
                                friendRequest.status = document.getString(Constant.KEY_FRIEND_REQUEST_STATUS);
                                friendRequest.receiverId = document.getString(Constant.KEY_RECEIVER_ID);
                                friendRequest.receiverName = document.getString(Constant.KEY_RECEIVER_NAME);
                                friendRequest.senderId = document.getString(Constant.KEY_SENDER_ID);
                                friendRequest.senderName = document.getString(Constant.KEY_SENDER_NAME);
                                friendRequest.dateObject = document.getDate(Constant.KEY_TIMESTAMP);
                                friendRequest.dateTime = getSimpleMessageDateTime(friendRequest.dateObject);
                                this.friendRequests.add(friendRequest);
                                this.friendRequestsAdapter.notifyDataSetChanged();

                                Log.d("TestingResult", document.getData().toString());
                            }

//                            Log.d("TestingResult", pendingRequests.toString());
                        }
                        this.binding.progressCircular.setVisibility(View.GONE);
                    } else {
                        Log.d("Error", "Can't get any notification");
                    }
                });
    }

    private String getSimpleMessageDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    @Override
    public void onAcceptButtonClicked() {

    }

    @Override
    public void onDeclineButtonClicked() {

    }
}