package com.example.myapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.adapters.UsersAdapter;
import com.example.myapplication.databinding.ActivityUsersBinding;
import com.example.myapplication.listeners.UserListener;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.example.myapplication.utilities.Validator;
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

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void handleBackPressed() {
        this.binding.backButton.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers() {
        this.binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    usersAdapter.updateList(new ArrayList<>());
                    return;
                }

                binding.progressCircular.setVisibility(View.VISIBLE);

                CollectionReference usersRef = db.collection(Constant.KEY_COLLECTION_USERS);
                usersRef.orderBy(Constant.KEY_NAME).startAt(s.toString()).endAt(s.toString() + "\uf8ff")
                        .get()
                        .addOnCompleteListener(task -> {
                            String currentUserId = preferenceManager.getString(Constant.KEY_USER_ID);
                            if (task.isSuccessful() && task.getResult() != null) {
                                List<User> result = new ArrayList<>();
                                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                    if (currentUserId.equals(queryDocumentSnapshot.getId()))
                                        continue;
                                    User u = new User();
                                    u.email = queryDocumentSnapshot.getString(Constant.KEY_EMAIL);
                                    u.name = queryDocumentSnapshot.getString(Constant.KEY_NAME);
                                    u.token = queryDocumentSnapshot.getString(Constant.KEY_FCM_TOKEN);
                                    u.image = queryDocumentSnapshot.getString(Constant.KEY_IMAGE);
                                    u.id = queryDocumentSnapshot.getId();
                                    result.add(u);
                                }

                                if (result.size() > 0) {
                                    usersAdapter.updateList(result);
                                    binding.usersRecyclerView.setVisibility(View.VISIBLE);
                                    binding.progressCircular.setVisibility(View.INVISIBLE);
                                } else {
                                    Log.d("NoUsersFound", "No users found");
                                }
                            } else {
                                showErrorMsg();
                            }
                        });
            }

        });
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
        intent.putExtra(Constant.KEY_USER, user);
        startActivity(intent);
    }

    @Override
    public void onMessageButtonClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constant.KEY_USER, user);
        startActivity(intent);
    }
}