package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
    private static final int REQUEST_EDIT = 1;

    private ChatMessage selectedMessageFromSIC;

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
        handleShowSearchInConversation();
        handleScrollToRecent();
        listenMessages();
        listenFriendActiveStatus();

        handleChatRecyclerViewOnScrolled();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            if (data != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    this.selectedMessageFromSIC = data.getSerializableExtra("selectedMessage", ChatMessage.class);
                    Log.d("SelectedMsg", selectedMessageFromSIC.toString());

                    int stp = selectedMessageFromSIC.searchIndex;
                    this.chatMessages.set(stp, selectedMessageFromSIC);
                    this.chatAdapter.notifyItemChanged(stp);

                    this.binding.chatRecyclerView.scrollToPosition(stp);
                }
            }
        }
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
        String senderId = this.preferenceManager.getString(Constant.KEY_USER_ID);
        String receiverId = this.receiver.id;
        db.collection(Constant.KEY_COLLECTION_CHAT)
                .whereIn(Constant.KEY_SENDER_ID, Arrays.asList(senderId, receiverId))
                .whereIn(Constant.KEY_RECEIVER_ID, Arrays.asList(senderId, receiverId))
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
                    c.dateObject = documentChange.getDocument().getDate(Constant.KEY_TIMESTAMP);
                    c.dateTime = getSimpleMessageDateTime(c.dateObject, new Date());
                    this.chatMessages.add(c);
                }
            }
            this.chatMessages.sort(Comparator.comparing(o -> o.dateObject));

            if (nMessages == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
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
        this.binding.profileImageView.setImageBitmap(getUserImage(this.receiver.image));
    }

    private void handleShowUserInfo() {
        this.binding.profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
            intent.putExtra(Constant.KEY_USER, this.receiver);
            startActivity(intent);
        });
    }

    private void handleChatRecyclerViewOnScrolled() {
        this.binding.chatRecyclerView.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        int totalScrollRange = recyclerView.computeVerticalScrollRange() - recyclerView.getHeight();
                        int currentScrollOffset = recyclerView.computeVerticalScrollOffset();
                        int scrollUpOffset = totalScrollRange - currentScrollOffset;
                        if (scrollUpOffset >= 250) {
                            binding.scrollToRecentButton.setVisibility(View.VISIBLE);
                        } else {
                            binding.scrollToRecentButton.setVisibility(View.INVISIBLE);
                        }
                    }
                }
        );
    }

    private void handleShowSearchInConversation() {
        this.binding.imageSearch.setOnClickListener(v -> {
            if (this.selectedMessageFromSIC != null) {
                this.selectedMessageFromSIC.isHighlighted = false;
                this.chatMessages.set(this.selectedMessageFromSIC.searchIndex, this.selectedMessageFromSIC);
                this.chatAdapter.notifyItemChanged(this.selectedMessageFromSIC.searchIndex);
            }

            Intent intent = new Intent(getApplicationContext(), SearchInConversationActivity.class);
            intent.putExtra("chatMessages", (Serializable) this.chatMessages);
            intent.putExtra(Constant.KEY_USER, this.receiver);
            startActivityForResult(intent, REQUEST_EDIT);
        });
    }

    private void handleBackPressed() {
        this.binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void handleScrollToRecent() {
        this.binding.scrollToRecentButton.setOnClickListener(v ->  {
            this.binding.chatRecyclerView.smoothScrollToPosition(this.chatMessages.size() - 1);
        });
    }

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

        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date1);
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
        String senderId = this.preferenceManager.getString(Constant.KEY_USER_ID);
        String receiverId = this.receiver.id;
        if (this.chatMessages.size() > 0) {
            this.db.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
                    .whereIn(Constant.KEY_SENDER_ID, Arrays.asList(senderId, receiverId))
                    .whereIn(Constant.KEY_RECEIVER_ID, Arrays.asList(senderId, receiverId))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            this.conversationId = documentSnapshot.getId();
                        }
                    });
        }
    }
}