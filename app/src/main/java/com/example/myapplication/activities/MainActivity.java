package com.example.myapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.admin.AdminControllerActivity;
import com.example.myapplication.adapters.RecentConversationsAdapter;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.listeners.ConversationsListener;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ConversationsListener {
    List<ChatMessage> recentConversations;
    ActivityMainBinding binding;
    PreferenceManager preferenceManager;
    RecentConversationsAdapter recentConversationsAdapter;
    FirebaseFirestore db;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        this.getToken();
        this.initialize();
        this.loadUserImage();

        this.setupNavDrawerView();
        this.setupNavBottomView();

        this.listenConversations();
        this.listenFriendRequestsCount();

        this.handleOpenMenu();
        this.handleOpenAdminController();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initialize() {
        this.recentConversations = new ArrayList<>();
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.recentConversationsAdapter = new RecentConversationsAdapter(this.recentConversations, this);
        this.binding.conversationsRecyclerView.setAdapter(this.recentConversationsAdapter);
        this.db = FirebaseFirestore.getInstance();
    }

    private void handleOpenMenu() {
        this.binding.profileImage.setOnClickListener(v -> {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.openDrawer(GravityCompat.START);
        });
    }

    private void handleOpenAdminController() {
        if (this.preferenceManager.getBoolean(Constant.KEY_IS_ADMIN_ROLE)) {
            this.binding.menuImage.setVisibility(View.VISIBLE);
            this.binding.menuImage.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), AdminControllerActivity.class)));
        } else {
            this.binding.menuImage.setVisibility(View.GONE);
        }
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void loadUserImage() {
        byte[] bytes = Base64.decode(preferenceManager.getString(Constant.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        this.binding.profileImage.setImageBitmap(bitmap);

        String username = preferenceManager.getString(Constant.KEY_NAME);
        String email = preferenceManager.getString(Constant.KEY_EMAIL);

        View headerView = binding.navView.getHeaderView(0);
        TextView usernameTextView = headerView.findViewById(R.id.nav_header_name);
        TextView emailTextView = headerView.findViewById(R.id.nav_header_email);
        ImageView userImage = headerView.findViewById(R.id.nav_header_image);

        usernameTextView.setText(username);
        emailTextView.setText(email);
        userImage.setImageBitmap(bitmap);
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference dr = db.collection(Constant.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constant.KEY_USER_ID));
        dr.update(Constant.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused -> Log.d("UpdateToken", "Token updated successfully"))
                .addOnFailureListener(ex -> Log.d("UpdateToken", "Unable to update token"));
    }

    private void listenConversations() {
        this.db.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constant.KEY_SENDER_ID, this.preferenceManager.getString(Constant.KEY_USER_ID))
                .addSnapshotListener(eventListener);

        this.db.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constant.KEY_RECEIVER_ID, this.preferenceManager.getString(Constant.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }

        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constant.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constant.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (senderId.equals(this.preferenceManager.getString(Constant.KEY_USER_ID))) {
                        chatMessage.conversationImage = documentChange.getDocument().getString(Constant.KEY_RECEIVER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constant.KEY_RECEIVER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(Constant.KEY_RECEIVER_ID);
                    } else {
                        chatMessage.conversationImage = documentChange.getDocument().getString(Constant.KEY_SENDER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constant.KEY_SENDER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(Constant.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constant.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constant.KEY_TIMESTAMP);
                    chatMessage.dateTime = getSimpleMessageDateTime(chatMessage.dateObject, new Date());

                    this.recentConversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    String senderId = documentChange.getDocument().getString(Constant.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constant.KEY_RECEIVER_ID);
                    for (int i = 0; i < this.recentConversations.size(); i++) {
                        if (this.recentConversations.get(i).senderId.equals(senderId) && this.recentConversations.get(i).receiverId.equals(receiverId)) {
                            this.recentConversations.get(i).message = documentChange.getDocument().getString(Constant.KEY_LAST_MESSAGE);
                            this.recentConversations.get(i).dateObject = documentChange.getDocument().getDate(Constant.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }

            Collections.sort(this.recentConversations, (o1, o2) -> o2.dateObject.compareTo(o1.dateObject));
            this.recentConversationsAdapter.notifyDataSetChanged();
            this.binding.conversationsRecyclerView.smoothScrollToPosition(0);
            this.binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
        }
    };

    private String getSimpleMessageDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private String getSimpleMessageDateTime(Date date1, Date date2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        int year1 = calendar1.get(Calendar.YEAR);
        int month1 = calendar1.get(Calendar.MONTH);
        int day1 = calendar1.get(Calendar.DAY_OF_MONTH);

        int year2 = calendar2.get(Calendar.YEAR);
        int month2 = calendar2.get(Calendar.MONTH);
        int day2 = calendar2.get(Calendar.DAY_OF_MONTH);

        if (year1 == year2 && month1 == month2 && day1 == day2) {
            return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date1);
        }

        if (day2 - day1 == 1){
            return "Yesterday" + ", " + new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date1);
        }

        if (day2 - day1 < 7) {
            return new SimpleDateFormat("EEEE", Locale.getDefault()).format(date1);
        }

        if (day2 - day1 > 7) {
            return new SimpleDateFormat("MMMM dd", Locale.getDefault()).format(date1);
        }

        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date1);
    }

    private void listenFriendRequestsCount() {
        this.db.collection(Constant.KEY_COLLECTION_FRIEND_REQUESTS)
                .whereEqualTo(Constant.KEY_RECEIVER_ID, this.preferenceManager.getString(Constant.KEY_USER_ID))
                .whereEqualTo(Constant.KEY_FRIEND_REQUEST_STATUS, "pending")
                .addSnapshotListener(friendRequestsCountEventListener);
    }

    private final EventListener<QuerySnapshot> friendRequestsCountEventListener = (value, error) -> {
        if (error != null) {
            return;
        }

        if (value != null) {
            BadgeDrawable badgeDrawable = this.bottomNavigationView.getOrCreateBadge(R.id.nav_friend_requests);
            long requestCount = value.size();
            if (requestCount == 0) {
                this.bottomNavigationView.removeBadge(R.id.nav_friend_requests);
                badgeDrawable.clearNumber();
            } else {
                badgeDrawable.setNumber((int) requestCount);
            }
        }
    };

    private void setupNavBottomView() {
        bottomNavigationView = findViewById(R.id.nav_bottom);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_contacts) {
                if (!isUsersActivityVisible()) {
                    startActivity(new Intent(this, FriendsActivity.class));
                    return true;
                }
            } else if (id == R.id.nav_friend_requests) {
                startActivity(new Intent(this, NotificationActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupNavDrawerView() {
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            // Handle item clicks in the Navigation Drawer here
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_contacts) {
                drawerLayout.closeDrawers();
                startActivity(new Intent(this, FriendsActivity.class));
            } else if (id == R.id.nav_find_friends) {
                drawerLayout.closeDrawers();
                if (!isUsersActivityVisible()) {
                    startActivity(new Intent(this, UsersActivity.class));
                }
            } else if (id == R.id.nav_settings) {
                showToast("Settings clicked");
            } else if (id == R.id.nav_logout) {
                drawerLayout.closeDrawers();
                signOut();
                return true;
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private boolean isUsersActivityVisible() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo runningTaskInfo : runningTasks) {
            ComponentName topActivity = runningTaskInfo.topActivity;
            if (topActivity.getClassName().equals(UsersActivity.class.getName())) {
                return true;
            }
        }

        return false;
    }

    private void signOut() {
        showToast("Signing out...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference dr = db.collection(Constant.KEY_COLLECTION_USERS).document(this.preferenceManager.getString(Constant.KEY_USER_ID));

        HashMap<String, Object> newData = new HashMap<>();
        newData.put(Constant.KEY_FCM_TOKEN, FieldValue.delete());
        newData.put(Constant.KEY_IS_AVAILABLE, false);
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

    @Override
    public void onConversationClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constant.KEY_USER, user);
        startActivity(intent);
    }
}
