package com.example.myapplication.adapters;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemContainerFriendRequestPendingBinding;
import com.example.myapplication.databinding.ItemContainerNotificationBinding;
import com.example.myapplication.listeners.NotificationListener;
import com.example.myapplication.models.FriendRequest;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.FriendRequestNotificationViewHolder> {
    List<FriendRequest> notifications;
    NotificationListener notificationListener;

    public NotificationsAdapter(List<FriendRequest> notifications, NotificationListener notificationListener) {
        this.notifications = notifications;
        this.notificationListener = notificationListener;
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @NonNull
    @Override
    public FriendRequestNotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerNotificationBinding binding = ItemContainerNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FriendRequestNotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestNotificationViewHolder holder, int position) {
        holder.setNotificationData(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return this.notifications.size();
    }


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
            this.binding.profileImageView.setImageBitmap(getUserImage(friendRequest.senderImage));
            this.binding.getRoot().setOnClickListener(v -> notificationListener.onNotificationClicked());
        }
    }

}
