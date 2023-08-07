package com.example.myapplication.activities;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityMainBinding.inflate(getLayoutInflater());
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());

//        if (savedInstanceState == null) {
//            startActivity(new Intent(this, UsersActivity.class));
//            finish();
//        }

        if (preferenceManager.getBoolean(Constant.KEY_IS_SIGNED_IN)) {
            setupNavDrawerView();
//            navigationToUsersActivity();// Mặc định chuyển hướng đến UsersActivity khi chạy chương trình
        } else {
            showToast("Moi ban dang nhap vao da");
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
        }
        setupNavBottomView();
//        binding.showFriendsButton.setVisibility(View.GONE);
//        binding.findFriendsButton.setVisibility(View.GONE);
//        binding.notificationsButton.setVisibility(View.GONE);
        loadUserData();
        getToken();
//        handleSignOut();
//        handleShowFriends();


    }

    private void setupNavBottomView() {
        bottomNavigationView = findViewById(R.id.nav_bottom);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (id == R.id.nav_friends) {
                if (!isUsersActivityVisible()) {
                    startActivity(new Intent(this, UsersActivity.class));
                    finish();
                }
            } else if (id == R.id.nav_settings) {
                // Handle Settings click
                showToast("Settings clicked");
            }
            return true;
        });
    }

    private void setupNavDrawerView() {
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            // Handle item clicks in the Navigation Drawer here
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (id == R.id.nav_friends) {
                drawerLayout.closeDrawers();
                if (!isUsersActivityVisible()) {
                    startActivity(new Intent(this, UsersActivity.class));
                    finish();
                }
            } else if (id == R.id.nav_settings) {
                // Handle Settings click
                showToast("Settings clicked");
            } else if (id == R.id.nav_logout) {
                // Handle Logout click
                signOut();

            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void loadUserData() {
//        this.binding.usernameTextView.setText(preferenceManager.getString(Constant.KEY_NAME));
        // Lấy thông tin người dùng từ SharedPreferences hoặc nguồn dữ liệu khác (đã lưu khi đăng nhập).
        String username = preferenceManager.getString(Constant.KEY_NAME);
        String email = preferenceManager.getString(Constant.KEY_EMAIL);

        View headerView = binding.navView.getHeaderView(0);
        TextView usernameTextView = headerView.findViewById(R.id.nav_header_name);
        TextView emailTextView = headerView.findViewById(R.id.nav_header_email);

        usernameTextView.setText(username);
        emailTextView.setText(email);
    }

    private void updateToken(String token) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference dr = db.collection(Constant.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constant.KEY_USER_ID));
//        dr.update(Constant.KEY_FCM_TOKEN, token)
//                .addOnSuccessListener(unused -> showToast("Token updated successfully"))
//                .addOnFailureListener(ex -> showToast("Unable to update token"));
        String userId = preferenceManager.getString(Constant.KEY_USER_ID);
        if (userId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference dr = db.collection(Constant.KEY_COLLECTION_USERS).document(userId);
            // Update the token in Firestore (you can also handle success/failure accordingly)
            dr.update(Constant.KEY_FCM_TOKEN, token)
                    .addOnSuccessListener(unused -> showToast("Token updated successfully"))
                    .addOnFailureListener(ex -> showToast("Unable to update token."));
        } else {
            showToast("User ID is null. Unable to update token.");
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Đóng navigation drawer sau khi chọn mục
        return super.onOptionsItemSelected(item);
    }

//    private void handleSignOut() {
//        this.binding.signOutButton.setOnClickListener(v -> signOut());
//    }

    private void signOut() {
        showToast("Signing out...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference dr = db.collection(Constant.KEY_COLLECTION_USERS).document(this.preferenceManager.getString(Constant.KEY_USER_ID));
        HashMap<String, Object> newData = new HashMap<>();
        newData.put(Constant.KEY_FCM_TOKEN, FieldValue.delete());
        dr.update(newData)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(ex -> {
                    showToast("Unable to sign out");
                });
    }

//    private void handleShowFriends() {
//        this.binding.showFriendsButton.setOnClickListener(v -> {
//            startActivity(new Intent(getApplicationContext(), UsersActivity.class));
//        });
//    }

//    private boolean isUsersActivityVisible() {
//        String className = UsersActivity.class.getSimpleName();
//        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        if (activityManager != null) {
//            List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(1);
//            if (!taskInfoList.isEmpty()) {
//                ComponentName topActivity = taskInfoList.get(0).topActivity;
//                return topActivity.getClassName().equals(className);
//            }
//        }
//        return false;
//    }

    private void navigationToUsersActivity() {
        // Check if UsersActivity is not already visible
        if (!isUsersActivityVisible()) {
            startActivity(new Intent(this, UsersActivity.class));
//        }
        }
    }

    private boolean isUsersActivityVisible() {
        // Lấy danh sách tất cả các Activity đang hiển thị
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        // Kiểm tra xem UsersActivity có trong danh sách các Activity đang hiển thị không
        for (ActivityManager.RunningTaskInfo runningTaskInfo : runningTasks) {
            ComponentName topActivity = runningTaskInfo.topActivity;
            if (topActivity.getClassName().equals(UsersActivity.class.getName())) {
                return true; // UsersActivity đang hiển thị
            }
        }

        return false; // UsersActivity không hiển thị
    }
}
