package com.example.myapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import com.example.myapplication.adapters.FoundMessagesAdapter;
import com.example.myapplication.databinding.ActivitySearchInConversationBinding;
import com.example.myapplication.listeners.FoundMessageListener;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SearchInConversationActivity extends AppCompatActivity implements FoundMessageListener {
    private ActivitySearchInConversationBinding binding;
    private List<ChatMessage> chatMessages;
    private List<ChatMessage> foundMessages;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private User receiver;
    private FoundMessagesAdapter foundMessagesAdapter;

    private static class MatchRange {
        int startIndex;
        int endIndex;

        MatchRange(int startIndex, int endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        @NonNull
        @Override
        public String toString() {
            return this.startIndex + " - " + this.endIndex + "\n";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivitySearchInConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.initialize();
        this.loadUserData();
        this.handleBackPressed();
        this.handleGetFilteredMessages();
    }

    private void initialize() {
        this.receiver = (User) getIntent().getSerializableExtra(Constant.KEY_USER);
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.chatMessages = (List<ChatMessage>) getIntent().getSerializableExtra("chatMessages");
        this.foundMessages = new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();
        this.foundMessagesAdapter = new FoundMessagesAdapter(this.foundMessages, this, this.receiver);
        this.binding.foundMessagesRecyclerView.setAdapter(foundMessagesAdapter);
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void loadUserData() {
        this.binding.profileImageView.setImageBitmap(this.getUserImage(this.receiver.image));
        this.binding.usernameTextView.setText(this.receiver.name);
    }

    private void getFilteredMessages() {
        if (this.foundMessages.size() > 0)
            this.foundMessages.clear();

        String searchStr = this.binding.searchInput.getText().toString();

        if (searchStr.isEmpty()) {
            return;
        }

        int idx = 0;
        for (ChatMessage chatMessage : this.chatMessages) {
            String msg = chatMessage.message;
            MatchRange matchRange = containsWord(searchStr, msg);
            if (matchRange != null) {
                chatMessage.searchIndex = idx;
                chatMessage.highlightStartIndex = matchRange.startIndex;
                chatMessage.highlightEndIndex = matchRange.endIndex;
                this.foundMessages.add(chatMessage);
            }
            idx += 1;
        }

        this.chatMessages.sort(Comparator.comparing(o -> o.dateObject, Comparator.reverseOrder()));

        this.foundMessagesAdapter.notifyDataSetChanged();
    }

    private MatchRange containsWord(String target, String message) {
        if (message.contains(target)) {
            return new MatchRange(message.indexOf(target), message.indexOf(target) + target.length());
        }

        String[] wordsTarget = target.split("\\s+");
        String[] wordsMsg = message.split("\\s+");
        for (String wt : wordsTarget) {
            for (String wm : wordsMsg) {
                if (wt.equals(wm)) {
                    int startIndex = message.indexOf(wt);
                    int endIndex = startIndex + wt.length();
                    return new MatchRange(startIndex, endIndex);
                }
            }
        }

        return null;
    }

    private void handleGetFilteredMessages() {
        this.binding.imageSearch.setOnClickListener(v -> {
            this.getFilteredMessages();
            this.binding.searchInput.setText(null);
        });
    }

    private void handleBackPressed() {
        this.binding.backButton.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    @Override
    public void onFoundMessageClicked(ChatMessage chatMessage) {
        Intent intent = new Intent();
        intent.putExtra("comingFromActivity", "SIC_Activity");
        chatMessage.isHighlighted = true;
        intent.putExtra("selectedMessage", chatMessage);
        setResult(RESULT_OK, intent);
        finish();
    }
}