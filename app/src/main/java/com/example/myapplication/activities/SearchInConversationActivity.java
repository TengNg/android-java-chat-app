package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.example.myapplication.adapters.FounderMessagesAdapter;
import com.example.myapplication.databinding.ActivitySearchInConversationBinding;
import com.example.myapplication.listeners.FoundMessageListener;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchInConversationActivity extends AppCompatActivity implements FoundMessageListener {
    private ActivitySearchInConversationBinding binding;
    private List<ChatMessage> chatMessages;
    private List<ChatMessage> foundMessages;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private User receiver;
    private FounderMessagesAdapter founderMessagesAdapter;

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
        this.founderMessagesAdapter = new FounderMessagesAdapter(this.foundMessages, this, this.receiver);
        this.binding.foundMessagesRecyclerView.setAdapter(founderMessagesAdapter);
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
        String searchStr = this.binding.searchInput.getText().toString();

        if (searchStr.isEmpty()) {
            return;
        }

        int idx = 0;
        for (ChatMessage chatMessage : this.chatMessages) {
            if(chatMessage.message.contains(searchStr)) {
                chatMessage.searchIndex = idx;
                this.foundMessages.add(chatMessage);
            }
            idx += 1;
        }

        this.founderMessagesAdapter.notifyDataSetChanged();

        Log.d("SIC_Testing", foundMessages.toString());
    }

    private void handleGetFilteredMessages() {
        this.binding.imageSearch.setOnClickListener(v -> {
            this.getFilteredMessages();
        });
    }

    private void handleBackPressed() {
        this.binding.backButton.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onFoundMessageClicked(int searchIndex) {
        Intent intent = new Intent();
        intent.putExtra("comingFromActivity", "SIC_Activity");
        intent.putExtra("scrollToPosition", searchIndex);
        setResult(RESULT_OK, intent);
        finish();
    }
}