package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivityUserInfoBinding;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

enum FriendRequestStatus {
    PENDING_SENT,
    PENDING_RECEIVED,
    FRIENDS,
    NOT_FOUND
}

public class UserInfoActivity extends AppCompatActivity {
    private ActivityUserInfoBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        this.initialize();
        this.getUserInfo();
        this.handleBackPressed();
        this.handleOpenConversation();
        this.handleAddFriend();
        this.handleUnfriend();
        this.handleUpdateFriendRequestStatus(this.binding.acceptFriendRequestFromUserButton, "accepted");
        this.handleUpdateFriendRequestStatus(this.binding.declineFriendRequestFromUserButton, "declined");
        this.handleRemovePendingSentFriendRequest();
    }

    private void initialize() {
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.db = FirebaseFirestore.getInstance();
    }

    private void getUserInfo() {
        User currentUser = ((User) getIntent().getSerializableExtra(Constant.KEY_USER));
        this.binding.usernameTextView.setText(currentUser.name);
        this.binding.userEmailTextView.setText(currentUser.email);
        checkFriendRequestStatus(currentUser.id);
    }

    public void checkFriendRequestStatus(String specificUserId) {
        CollectionReference friendRequestsRef = db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS);
        String currentUserId = this.preferenceManager.getString(Constant.KEY_USER_ID);

        CollectionReference currentUserFriendsRef = db.collection(Constant.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .collection(Constant.KEY_COLLECTION_USER_FRIENDS);

        friendRequestsRef.whereEqualTo("senderId", currentUserId)
                .whereEqualTo("receiverId", specificUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(sentRequestSnapshot -> {
                    boolean sentRequestExists = !sentRequestSnapshot.isEmpty();
                    List<String> friendIds = new ArrayList<>();
                    currentUserFriendsRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                String id = documentSnapshot.getData().keySet().iterator().next();
                                friendIds.add(id);
                            }

                            friendRequestsRef.whereEqualTo("senderId", specificUserId)
                                    .whereEqualTo("receiverId", currentUserId)
                                    .get()
                                    .addOnSuccessListener(receivedRequestSnapshot -> {
                                        boolean receivedRequestExists = !receivedRequestSnapshot.isEmpty();
                                        if (friendIds.contains(specificUserId)) {
                                            this.onFriendRequestStatus(FriendRequestStatus.FRIENDS);
                                        } else if (sentRequestExists) {
                                            this.onFriendRequestStatus(FriendRequestStatus.PENDING_SENT);
                                        } else if (receivedRequestExists) {
                                            this.onFriendRequestStatus(FriendRequestStatus.PENDING_RECEIVED);
                                        } else {
                                            this.onFriendRequestStatus(FriendRequestStatus.NOT_FOUND);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d("Error", "Error checking friend request");
                                    });
                        }
                    });
               })
                .addOnFailureListener(e -> {
                    Log.d("Error", "Error checking friend request");
                });
    }

    private void onFriendRequestStatus(FriendRequestStatus status) {
        switch (status) {
            case PENDING_SENT:
                // The current user sent a friend request to the specific user, and it's pending
                this.binding.friendRequestStatusTextView.setText("Awaiting friend request confirmation");
                this.binding.cancelFriendRequestButton.setVisibility(View.VISIBLE);
                break;
            case PENDING_RECEIVED:
                // The current user received a friend request from the specific user, and it's pending
                this.binding.friendRequestStatusTextView.setText("This user sends you a friend request");
                this.binding.receivedFriendRequestButtonLayout.setVisibility(View.VISIBLE);
                break;
            case FRIENDS:
                // The current user and the specific user are already friends
                this.binding.friendRequestStatusTextView.setText("Friend✅");
                this.binding.unfriendButton.setVisibility(View.VISIBLE);
                break;
            case NOT_FOUND:
                // There is no friend request between the users or the request was already accepted/declined
                this.binding.friendRequestStatusTextView.setText("");
                this.binding.addFriendButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void handleUpdateFriendRequestStatus(Button button, String status) {
        button.setOnClickListener(v -> {
            String senderId = this.preferenceManager.getString(Constant.KEY_USER_ID);
            String receiverId = ((User) getIntent().getSerializableExtra(Constant.KEY_USER)).id;

            CollectionReference friendRequestsRef = db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS);

            friendRequestsRef
                    .whereEqualTo(Constant.KEY_SENDER_ID, receiverId)
                    .whereEqualTo(Constant.KEY_RECEIVER_ID, senderId)
                    .whereEqualTo(Constant.KEY_FRIEND_REQUEST_STATUS, "pending")
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        this.binding.acceptFriendRequestFromUserButton.setVisibility(View.GONE);
                        this.binding.declineFriendRequestFromUserButton.setVisibility(View.GONE);

                        if (!querySnapshot.isEmpty()) {
                            String documentId = querySnapshot.getDocuments().get(0).getId();
                            friendRequestsRef.document(documentId).update("status", status)
                                    .addOnSuccessListener(aVoid -> {
                                        if (status.equals("accepted")) {
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

                                            this.binding.friendRequestStatusTextView.setText("Friend✅");
                                            this.onFriendRequestStatus(FriendRequestStatus.FRIENDS);
                                        } else if (status.equals("declined")){
                                            this.onFriendRequestStatus(FriendRequestStatus.NOT_FOUND);
                                        }
                                        this.showToast("Friend request " + status);
                                    })
                                    .addOnFailureListener(e -> {
                                        this.onFriendRequestStatus(FriendRequestStatus.NOT_FOUND);
                                        this.showToast("Friend request not found");
                                    });

                        } else {
                            this.onFriendRequestStatus(FriendRequestStatus.NOT_FOUND);
                            this.showToast("Friend request not found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d("Error", "Error when retrieving friend request");
                    });
        });
    }

    private void handleRemovePendingSentFriendRequest() {
        this.binding.cancelFriendRequestButton.setOnClickListener(v -> {
            User receiver = ((User) getIntent().getSerializableExtra(Constant.KEY_USER));
            String receiverId = receiver.id;
            String senderId = this.preferenceManager.getString(Constant.KEY_USER_ID);

            this.db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS)
                    .whereEqualTo(Constant.KEY_SENDER_ID, senderId)
                    .whereEqualTo(Constant.KEY_RECEIVER_ID, receiverId)
                    .whereEqualTo(Constant.KEY_FRIEND_REQUEST_STATUS, "pending")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String documentId = querySnapshot.getDocuments().get(0).getId();
                            this.db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS).document(documentId).delete()
                                    .addOnSuccessListener(aVoid -> this.showToast("Friend request removed"))
                                    .addOnFailureListener(e -> this.showToast("Cannot remove friend request"));

                            this.binding.cancelFriendRequestButton.setVisibility(View.GONE);
                            this.onFriendRequestStatus(FriendRequestStatus.NOT_FOUND);

                        } else {
                            this.showToast("Cannot remove friend request");
                        }
                    })
                    .addOnFailureListener(e -> {
                        this.showToast("Cannot remove friend request");
                    });
        });
    }

    private void handleUnfriend() {
        User receiver = ((User) getIntent().getSerializableExtra(Constant.KEY_USER));
        String user1Id = receiver.id;
        String user2Id = this.preferenceManager.getString(Constant.KEY_USER_ID);

        DocumentReference user1Ref = db.collection(Constant.KEY_COLLECTION_USERS).document(user1Id);
        DocumentReference user2Ref = db.collection(Constant.KEY_COLLECTION_USERS).document(user2Id);

        this.binding.unfriendButton.setOnClickListener(v -> {

        });
    }

    private void handleAddFriend() {
        this.binding.addFriendButton.setOnClickListener(v -> {
            User currentUser = ((User) getIntent().getSerializableExtra(Constant.KEY_USER));
            String receiverId = currentUser.id;
            String receiverName = currentUser.name;

            String senderId = this.preferenceManager.getString(Constant.KEY_USER_ID);
            String senderName = this.preferenceManager.getString(Constant.KEY_NAME);

            HashMap<String, Object> friendRequestData = new HashMap<>();
            friendRequestData.put("senderId", senderId);
            friendRequestData.put("senderName", senderName);
            friendRequestData.put("receiverId", receiverId);
            friendRequestData.put("receiverName", receiverName);
            friendRequestData.put("status", "pending");
            friendRequestData.put("timestamp", new Date());

            this.db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS)
                    .whereEqualTo(Constant.KEY_SENDER_ID, senderId)
                    .whereEqualTo(Constant.KEY_RECEIVER_ID, receiverId)
                    .whereIn(Constant.KEY_FRIEND_REQUEST_STATUS, Arrays.asList("accepted", "pending")) // only check for pending and accepted status
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean isSent = !task.getResult().isEmpty();
                            if (isSent) {
                                this.showToast("You have already sent friend request to this user");
                            } else {
                                DocumentReference newNotificationRef = db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS).document();
                                newNotificationRef.set(friendRequestData);
                                this.showToast("Friend request sent");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        this.showToast("Error");
                    });


            this.binding.addFriendButton.setVisibility(View.GONE);
            this.onFriendRequestStatus(FriendRequestStatus.PENDING_SENT);
        });
    }

    private void handleOpenConversation() {
        this.binding.messageImage.setOnClickListener(v -> {
            User currentUser = ((User) getIntent().getSerializableExtra(Constant.KEY_USER));
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra(Constant.KEY_USER, currentUser);
            startActivity(intent);
        });
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void handleBackPressed() {
        this.binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

}
