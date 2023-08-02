package com.example.myapplication.listeners;

import com.example.myapplication.models.User;

public interface FriendListener {
    void onFriendClicked(User user);
    void onShowInfoButtonClicked(User user);
}
