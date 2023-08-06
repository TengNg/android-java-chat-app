package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.adapters.RecentConversationsAdapter;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.listeners.ConversationsListener;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ConversationsListener {
    List<ChatMessage> recentConversations;
    ActivityMainBinding binding;
    PreferenceManager preferenceManager;
    RecentConversationsAdapter recentConversationsAdapter;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        this.getToken();
        this.initialize();
        this.loadUserImage();
        this.listenConversations();
        this.handleOpenMenu();
    }

    private void initialize() {
        this.recentConversations = new ArrayList<>();
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.recentConversationsAdapter = new RecentConversationsAdapter(this.recentConversations, this);
        this.binding.conversationsRecyclerView.setAdapter(this.recentConversationsAdapter);
        this.db = FirebaseFirestore.getInstance();
    }

    private void handleOpenMenu() {
        this.binding.menuImage.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MenuActivity.class)));
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void loadUserImage() {
        byte[] bytes = Base64.decode(preferenceManager.getString(Constant.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        this.binding.profileImage.setImageBitmap(bitmap);
        this.binding.profileImage.setImageBitmap(bitmap);
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference dr = db.collection(Constant.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constant.KEY_USER_ID));
        dr.update(Constant.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused -> showToast("Token updated successfully"))
                .addOnFailureListener(ex -> showToast("Unable to update token"));
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
                Log.d("TestingResult", "before added");
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Log.d("TestingResult", "added");
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

    @Override
    public void onConversationClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constant.KEY_USER, user);
        startActivity(intent);
    }
}