package com.example.myapplication.activities.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.myapplication.adapters.UsersAdapter;
import com.example.myapplication.databinding.ActivityAdminControllerBinding;
import com.example.myapplication.listeners.admin.AdminRoleUserListener;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminControllerActivity extends AppCompatActivity implements AdminRoleUserListener {
    private ActivityAdminControllerBinding binding;
    private FirebaseFirestore db;
    private List<User> users;
    private UsersAdapter usersAdapter;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityAdminControllerBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        this.initialize();
        this.handleBackPressed();
        this.handleOpenCreateUserActivity();
        this.listenUsers();
    }

    private void initialize() {
        this.users = new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.usersAdapter = new UsersAdapter(this.users, this, true);
        this.binding.usersRecyclerView.setAdapter(this.usersAdapter);
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
                    u.image = documentChange.getDocument().getString(Constant.KEY_IMAGE);
                    u.gender = documentChange.getDocument().getString(Constant.KEY_GENDER);
                    u.id = documentChange.getDocument().getId();
                    u.isAdminRole = Boolean.TRUE.equals(documentChange.getDocument().getBoolean(Constant.KEY_IS_ADMIN_ROLE));
                    this.users.add(u);
                    this.usersAdapter.notifyDataSetChanged();
                }
                else if (documentChange.getType() == DocumentChange.Type.REMOVED) {

                }
            }
        } else {
            Log.d("AdminController > Error", "Failed when get users");
        }

        this.binding.progressCircular.setVisibility(View.GONE);
    };

//    private void getUsers() {
//        this.db.collection(Constant.KEY_COLLECTION_USERS)
//                .get()
//                .addOnCompleteListener(task -> {
//                    String currentUserId = this.preferenceManager.getString(Constant.KEY_USER_ID);
//                    if (task.isSuccessful() && task.getResult() != null) {
//                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
//                            if (currentUserId.equals(queryDocumentSnapshot.getId()))
//                                continue;
//                            User u = new User();
//                            u.email = queryDocumentSnapshot.getString(Constant.KEY_EMAIL);
//                            u.name = queryDocumentSnapshot.getString(Constant.KEY_NAME);
//                            u.token = queryDocumentSnapshot.getString(Constant.KEY_FCM_TOKEN);
//                            u.image = queryDocumentSnapshot.getString(Constant.KEY_IMAGE);
//                            u.id = queryDocumentSnapshot.getId();
//                            u.gender = queryDocumentSnapshot.getString(Constant.KEY_GENDER);
//                            this.users.add(u);
//                            this.usersAdapter.notifyDataSetChanged();
//                        }
//                        if (this.users.size() > 0) {
//                            Log.d("AdminController > GetUsers", String.valueOf(this.users.size()));
//                        } else {
//                            Log.d("AdminController > GetUsers", "No users found");
//                        }
//                    } else {
//                        Log.d("AdminController > GetUsers", "Error from getting users");
//                    }
//                });
//    }

    private void handleBackPressed() {
        this.binding.backButtonAd.setOnClickListener(v -> onBackPressed());
    }

    private void handleOpenCreateUserActivity() {
        this.binding.addBtnAd.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CreateUserActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), UserAccountControllerActivity.class);
        intent.putExtra(Constant.KEY_USER, user);
        startActivity(intent);
    }
}