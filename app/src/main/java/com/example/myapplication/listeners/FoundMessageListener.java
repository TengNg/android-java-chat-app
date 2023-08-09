package com.example.myapplication.listeners;

import com.example.myapplication.models.ChatMessage;

public interface FoundMessageListener {
    void onFoundMessageClicked(ChatMessage chatMessage);
}
