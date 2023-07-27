package com.example.myapplication.adapters;

import android.app.Notification;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemContainerNotificationBinding;
import com.example.myapplication.models.FriendRequest;

import java.util.List;

public class NotificationsAdapter {
    List<Notification> notifications;

    class FriendRequestNotificationViewHolder extends RecyclerView.ViewHolder {
        ItemContainerNotificationBinding binding;

        public FriendRequestNotificationViewHolder(ItemContainerNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setNotificationData(FriendRequest friendRequest) {
            String msg = friendRequest.senderName + " send you a friend request";

            this.binding.notificationTextView.setText(msg);
            this.binding.dateTextView.setText(friendRequest.dateTime);
        }
    }
}
