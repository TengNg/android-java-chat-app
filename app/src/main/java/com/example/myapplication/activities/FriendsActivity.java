package com.example.myapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.security.identity.DocTypeNotSupportedException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.adapters.FriendsAdapter;
import com.example.myapplication.databinding.ActivityFriendsBinding;
import com.example.myapplication.databinding.ActivityUsersBinding;
import com.example.myapplication.listeners.FriendListener;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.components.Qualified;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
//        listenFriends();
        getFriends();
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

//    private void listenFriends() {
//        db.collection(Constant.KEY_COLLECTION_USERS)
//                .addSnapshotListener(eventListener);
//    }

//    public void getFriends() {
//        this.db.collection(Constant.KEY_COLLECTION_USERS)
//                .document(this.preferenceManager.getString(Constant.KEY_USER_ID))
//                .collection(Constant.KEY_FRIENDS)
//                .get()
//                .addOnCompleteListener(task -> {
//                    String currentUserId = this.preferenceManager.getString(Constant.KEY_USER_ID);
//                    if (task.isSuccessful() && task.getResult() != null) {
//                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
//                            this.db.collection(Constant.KEY_COLLECTION_USERS)
//                                    .get()
//                                    .addOnCompleteListener(t -> {
//                                        for (QueryDocumentSnapshot q : t.getResult()) {
//                                            if (currentUserId.equals(q.getId()))
//                                                continue;
//
//                                            if (queryDocumentSnapshot.getBoolean(q.getId()) == true) {
//                                                User u = new User();
//                                                u.email = q.getString(Constant.KEY_EMAIL);
//                                                u.name = q.getString(Constant.KEY_NAME);
//                                                u.token = q.getString(Constant.KEY_FCM_TOKEN);
//                                                u.id = q.getId();
//                                                this.users.add(u);
//                                            }
//
//                                        }
//                                    });
//
//                        }
//                        if (users.size() > 0) {
//                            FriendsAdapter friendsAdapter = new FriendsAdapter(users, this);
//                            this.binding.friendsRecyclerView.setAdapter(friendsAdapter);
//                            this.binding.friendsRecyclerView.setVisibility(View.VISIBLE);
//                            this.binding.progressCircular.setVisibility(View.INVISIBLE);
//                        } else {
//                            Log.d("NoUsersFound", "No users found");
//                        }
//                    } else {
//                        showErrorMsg();
//                    }
//                });
//    }

    private void getFriends() {
        String specificUserID = this.preferenceManager.getString(Constant.KEY_USER_ID);
        CollectionReference usersRef = db.collection(Constant.KEY_COLLECTION_USERS);
        CollectionReference specificUserFriendsRef = db.collection(Constant.KEY_COLLECTION_USERS).document(specificUserID).collection(Constant.KEY_FRIENDS);

        specificUserFriendsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    List<String> friendIDs = new ArrayList<>();
                    Map<String, Object> map = document.getData();

                    if (map == null) continue;

                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        friendIDs.add(entry.getKey());
                    }

                    for (String friendID : friendIDs) {
                        DocumentReference friendUserRef = usersRef.document(friendID);
                        friendUserRef.get().addOnCompleteListener(friendTask -> {
                            if (friendTask.isSuccessful()) {
                                DocumentSnapshot friendDocument = friendTask.getResult();
                                if (friendDocument.exists()) {
                                    String friendUsername = friendDocument.getString(Constant.KEY_NAME);
                                    String friendEmail = friendDocument.getString(Constant.KEY_EMAIL);
                                    String friendToken = friendDocument.getString(Constant.KEY_FCM_TOKEN);

                                    // TODO: ada user to adapter
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
                this.binding.progressCircular.setVisibility(View.GONE);
            } else {
                Log.e("Error", "Error getting friends for specific user: " + specificUserID, task.getException());
            }
        });

    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }

        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getDocument().getId().equals(this.preferenceManager.getString(Constant.KEY_USER_ID)))
                    continue;

                switch (documentChange.getType()) {
                    case ADDED:
                        QueryDocumentSnapshot document = documentChange.getDocument();
                        Log.d("DC", "New document added: " + document.getId() + " Data: " + document.getData());
                        break;
                    case MODIFIED:
                        QueryDocumentSnapshot modifiedDocument = documentChange.getDocument();
                        Log.d("DC", "Modified document: " + modifiedDocument.getId() + " Data: " + modifiedDocument.getData());
                        break;
                    case REMOVED:
                        QueryDocumentSnapshot removedDocument = documentChange.getDocument();
                        Log.d("DC", "Removed document: " + removedDocument.getId() + " Data: " + removedDocument.getData());
                        break;
                }

//                DocumentReference documentReference = this.db.collection(Constant.KEY_COLLECTION_USERS)
//                        .document(this.preferenceManager.getString(Constant.KEY_USER_ID))
//                        .collection(Constant.KEY_FRIENDS)
//                        .getParent();
//
//                documentReference.get().addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot document = task.getResult();
//                        if (!document.exists()) {
//                            finish();
//                        }
//                    }
//                });
//
//                if (documentChange.getType() == DocumentChange.Type.ADDED) {
//                    User u = new User();
//                    u.email = documentChange.getDocument().getString(Constant.KEY_EMAIL);
//                    u.name = documentChange.getDocument().getString(Constant.KEY_NAME);
//                    u.token = documentChange.getDocument().getString(Constant.KEY_FCM_TOKEN);
//                    u.id = documentChange.getDocument().getId();
//                    this.users.add(u);
//                    this.friendsAdapter.notifyDataSetChanged();
//                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
//                    String changedModelEmail = documentChange.getDocument().getString(Constant.KEY_EMAIL);
//                    for (int i = 0; i < this.users.size(); i++) {
//                        if (this.users.get(i).email.equals(changedModelEmail)) {
//                            this.users.get(i).token = documentChange.getDocument().getString(Constant.KEY_FCM_TOKEN);
//                            this.friendsAdapter.notifyItemChanged(i);
//                        }
//                    }
//                }
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
        String result = user.id + " - " + user.name + " - " + user.email;
        Log.d("UserInfo", result);
        startActivity(intent);
    }
}