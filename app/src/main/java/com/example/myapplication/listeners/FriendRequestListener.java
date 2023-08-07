package com.example.myapplication.listeners;

import com.example.myapplication.models.FriendRequest;
import com.example.myapplication.models.User;

public interface FriendRequestListener {
    void onAcceptButtonClicked(FriendRequest friendRequest);
    void onDeclineButtonClicked(FriendRequest friendRequest);
    void onUserImageClicked(User user);
}
