package com.example.myapplication.listeners;

import com.example.myapplication.models.FriendRequest;

public interface FriendRequestListener {
    void onAcceptButtonClicked(FriendRequest friendRequest);
    void onDeclineButtonClicked(FriendRequest friendRequest);
}
