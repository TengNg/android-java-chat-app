package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.myapplication.adapters.ChatAdapter;
import com.example.myapplication.databinding.ActivityChatBinding;
import com.example.myapplication.models.ChatMessage;
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private User receiver;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        getReceiverInfo();
        handleBackPressed();
        initialize();
        handleSendMessage();
        listenMessages();
        listenFriendActiveStatus();
    }

    private void initialize() {
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.chatMessages = new ArrayList<>();
        this.chatAdapter = new ChatAdapter(this.chatMessages, preferenceManager.getString(Constant.KEY_USER_ID));
        this.binding.chatRecyclerView.setAdapter(chatAdapter);
        this.db = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        if (this.binding.messageInput.getText().toString().isEmpty()) return;

        HashMap<String, Object> data = new HashMap<>();
        data.put(Constant.KEY_SENDER_ID, preferenceManager.getString(Constant.KEY_USER_ID));
        data.put(Constant.KEY_RECEIVER_ID, receiver.id);
        data.put(Constant.KEY_TIMESTAMP, new Date());
        data.put(Constant.KEY_MESSAGE, this.binding.messageInput.getText().toString());
        db.collection(Constant.KEY_COLLECTION_CHAT).add(data);

        this.binding.messageInput.setText("");
    }

    private void handleSendMessage() {
        this.binding.sendButton.setOnClickListener(v -> this.sendMessage());
    }

    private void listenMessages() {
        db.collection(Constant.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constant.KEY_SENDER_ID, preferenceManager.getString(Constant.KEY_USER_ID))
                .whereEqualTo(Constant.KEY_RECEIVER_ID, receiver.id)
                .addSnapshotListener(eventListener);

        db.collection(Constant.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constant.KEY_SENDER_ID, receiver.id)
                .whereEqualTo(Constant.KEY_RECEIVER_ID, preferenceManager.getString(Constant.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }

        if (value != null) {
            int nMessages = this.chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage c = new ChatMessage();
                    c.receiverName = ((User) getIntent().getSerializableExtra(Constant.KEY_USER)).name;
                    c.senderName = this.preferenceManager.getString(Constant.KEY_NAME);
                    c.senderId = documentChange.getDocument().getString(Constant.KEY_SENDER_ID);
                    c.receiverId = documentChange.getDocument().getString(Constant.KEY_RECEIVER_ID);
                    c.message = documentChange.getDocument().getString(Constant.KEY_MESSAGE);
                    c.dateTime = getSimpleMessageDateTime(documentChange.getDocument().getDate(Constant.KEY_TIMESTAMP));
                    c.dateObject = documentChange.getDocument().getDate(Constant.KEY_TIMESTAMP);
                    this.chatMessages.add(c);
                }
            }
            this.chatMessages.sort(Comparator.comparing(o -> o.dateObject));

            if (nMessages == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(this.chatMessages.size(), this.chatMessages.size());
                this.binding.chatRecyclerView.smoothScrollToPosition(this.chatMessages.size() - 1);
            }
            this.binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }

        this.binding.progressCircular.setVisibility(View.GONE);
    };


    private void listenFriendActiveStatus() {
        DocumentReference docRef = db.collection(Constant.KEY_COLLECTION_USERS)
                .document(((User) getIntent().getSerializableExtra(Constant.KEY_USER)).id);

        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e("Firestore", "Error listening for document changes.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                String token = snapshot.getString(Constant.KEY_FCM_TOKEN);

                if (token != null) {
                    this.binding.activeStatusImage.setVisibility(View.VISIBLE);
                } else {
                    this.binding.activeStatusImage.setVisibility(View.INVISIBLE);
                }

            } else {
                // The document has been deleted.
                Log.d("Firestore", "Document does not exist.");
            }
        });
    }


    private void getReceiverInfo() {
        this.receiver = (User) getIntent().getSerializableExtra(Constant.KEY_USER);

        // Set receiver name
        this.binding.usernameTextView.setText(this.receiver.name);

        // Set receiver active status
        if (this.receiver.token != null) {
            this.binding.activeStatusImage.setVisibility(View.VISIBLE);
        } else {
            this.binding.activeStatusImage.setVisibility(View.GONE);
        }
    }

    private void handleBackPressed() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private String getSimpleMessageDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
}