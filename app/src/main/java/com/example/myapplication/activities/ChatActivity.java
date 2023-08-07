package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
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
import com.google.firebase.firestore.DocumentSnapshot;
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
    private String conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        initialize();
        getReceiverInfo();
        handleBackPressed();
        handleSendMessage();
        handleShowUserInfo();
        listenMessages();
        listenFriendActiveStatus();
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void initialize() {
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.chatMessages = new ArrayList<>();
        this.receiver = (User) getIntent().getSerializableExtra(Constant.KEY_USER);
        this.chatAdapter = new ChatAdapter(
                this.chatMessages,
                this.preferenceManager.getString(Constant.KEY_USER_ID),
                this.getUserImage(this.preferenceManager.getString(Constant.KEY_IMAGE)),
                this.getUserImage(this.receiver.image)
        );
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

        if (this.conversationId != null) {
            this.updateConversation(this.binding.messageInput.getText().toString());
        } else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constant.KEY_SENDER_ID, this.preferenceManager.getString(Constant.KEY_USER_ID));
            conversation.put(Constant.KEY_SENDER_NAME, this.preferenceManager.getString(Constant.KEY_NAME));
            conversation.put(Constant.KEY_SENDER_IMAGE, this.preferenceManager.getString(Constant.KEY_IMAGE));
            conversation.put(Constant.KEY_RECEIVER_ID, this.receiver.id);
            conversation.put(Constant.KEY_RECEIVER_NAME, this.receiver.name);
            conversation.put(Constant.KEY_RECEIVER_IMAGE, this.receiver.image);
            conversation.put(Constant.KEY_LAST_MESSAGE, this.binding.messageInput.getText().toString());
            conversation.put(Constant.KEY_TIMESTAMP, new Date());
            this.addConversation(conversation);
        }

        this.binding.messageInput.setText(null);
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

    private void listenFriendActiveStatus() {
        User currentUser = (User) getIntent().getSerializableExtra(Constant.KEY_USER);

        DocumentReference docRef = db.collection(Constant.KEY_COLLECTION_USERS).document(currentUser.id);

        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e("Firestore", "Error listening for document changes.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                boolean isAvail = snapshot.getBoolean(Constant.KEY_IS_AVAILABLE);

                if (isAvail == true) {
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

        if (this.conversationId == null) {
            this.checkConversation();
        }
    };

    private void getReceiverInfo() {
        this.binding.usernameTextView.setText(this.receiver.name);

        this.binding.imageInfo.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
            intent.putExtra(Constant.KEY_USER, this.receiver);
            startActivity(intent);
        });
    }

    private void handleShowUserInfo() {
        this.binding.imageInfo.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
            intent.putExtra(Constant.KEY_USER, this.receiver);
            startActivity(intent);
        });
    }

    private void handleBackPressed() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private String getSimpleMessageDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void updateConversation(String message) {
        DocumentReference docRef = this.db.collection(Constant.KEY_COLLECTION_CONVERSATIONS).document(this.conversationId);
        docRef.update(
                Constant.KEY_LAST_MESSAGE, message,
                Constant.KEY_TIMESTAMP, new Date()
        );
    }

    private void addConversation(HashMap<String, Object> conversation) {
        this.db.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(docRef -> conversationId = docRef.getId());
    }

    private void checkConversation() {
        if (this.chatMessages.size() > 0) {
            checkConversationById(this.preferenceManager.getString(Constant.KEY_USER_ID), receiver.id);
            checkConversationById(receiver.id, this.preferenceManager.getString(Constant.KEY_USER_ID));
        }
    }

    private void checkConversationById(String senderId, String receiverId) {
        this.db.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constant.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constant.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        this.conversationId = documentSnapshot.getId();
                    }
                });
    }
}