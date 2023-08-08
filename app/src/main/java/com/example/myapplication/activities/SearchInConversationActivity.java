package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivitySearchInConversationBinding;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchInConversationActivity extends AppCompatActivity {
    private ActivitySearchInConversationBinding binding;
    private List<ChatMessage> chatMessages;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private User receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivitySearchInConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.initialize();
        this.handleBackPressed();
    }

    private void handleBackPressed() {
        this.binding.backButton.setOnClickListener(v -> onBackPressed());
    }


    private void initialize() {
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.chatMessages = new ArrayList<>();
        this.receiver = (User) getIntent().getSerializableExtra(Constant.KEY_USER);
        this.db = FirebaseFirestore.getInstance();
    }

}