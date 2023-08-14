package com.example.myapplication.activities.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.adapters.FriendsAdapter;
import com.example.myapplication.databinding.ActivityUserFriendListBinding;
import com.example.myapplication.listeners.admin.AdminRoleFriendListener;
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

public class UserFriendListActivity extends AppCompatActivity implements AdminRoleFriendListener {
    private ActivityUserFriendListBinding binding;
    private PreferenceManager preferenceManager;
    private List<User> friends;
    private FriendsAdapter friendsAdapter;
    private FirebaseFirestore db;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityUserFriendListBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        this.handleBackPressed();
        this.initialize();
        this.listenFriends();
        this.binding.usernameTextView.setText(((User) getIntent().getSerializableExtra(Constant.KEY_USER)).name);
        this.handleOnSearchFieldChanged();
    }

    private void initialize() {
        this.currentUser = (User) getIntent().getSerializableExtra(Constant.KEY_USER);
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.friends = new ArrayList<>();
        this.friendsAdapter = new FriendsAdapter(this.friends, this, true);
        this.db = FirebaseFirestore.getInstance();
        this.binding.friendsRecyclerView.setAdapter(friendsAdapter);
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void handleBackPressed() {
        this.binding.backButton.setOnClickListener(v -> onBackPressed());
    }

    private void listenFriends() {
        db.collection(Constant.KEY_COLLECTION_USERS)
                .document(this.currentUser.id)
                .collection(Constant.KEY_COLLECTION_USER_FRIENDS)
                .addSnapshotListener(friendsEventListener);
    }

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
                                String image = friendDocument.getString(Constant.KEY_IMAGE);
                                String gender = friendDocument.getString(Constant.KEY_GENDER);
                                User user = new User();
                                user.id = friendTask.getResult().getId();
                                user.name = friendUsername;
                                user.email = friendEmail;
                                user.image = image;
                                user.gender = gender;
                                this.friends.add(user);
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
            Log.e("Error", "Failed to get friend list");
        }
    };

    private void handleOnSearchFieldChanged() {
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
                    friendsAdapter.updateList(friends);
                    return;
                }

                List<User> filteredList = new ArrayList<>();
                for (User user : friends) {
                    if (user.name.contains(editable.toString())) {
                        filteredList.add(user);
                    }
                }

                friendsAdapter.updateList(filteredList);
            }
        });
    }


    @Override
    public void onRemoveButtonClicked(User userFriend) {
        String id1 = userFriend.id;
        String id2 = this.currentUser.id;

        DocumentReference user1Ref = this.db
                .collection(Constant.KEY_COLLECTION_USERS)
                .document(id1)
                .collection(Constant.KEY_COLLECTION_USER_FRIENDS)
                .document(id2);

        DocumentReference user2Ref = this.db
                .collection(Constant.KEY_COLLECTION_USERS)
                .document(id2)
                .collection(Constant.KEY_COLLECTION_USER_FRIENDS)
                .document(id1);

        user1Ref.get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            if (task.isSuccessful()) {
                if (document.exists()) {
                    user1Ref.delete()
                            .addOnSuccessListener(deleteTask -> {
                                user2Ref.delete()
                                        .addOnSuccessListener(task2 -> {
                                            this.showToast("Unfriend " + userFriend.name);
                                        })
                                        .addOnFailureListener(e -> {
                                            this.showToast(userFriend.name + " is not friend with " + currentUser.name);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                this.showToast("Error");
                            });
                } else {
                    this.showToast("Not friend with " + currentUser.name);
                }
            } else {
                this.showToast("Error");
            }
        });
    }
}