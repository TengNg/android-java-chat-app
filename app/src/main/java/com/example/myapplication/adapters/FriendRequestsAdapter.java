package com.example.myapplication.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemContainerFriendRequestPendingBinding;
import com.example.myapplication.listeners.FriendRequestListener;
import com.example.myapplication.models.FriendRequest;
import com.example.myapplication.models.User;

import java.util.List;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.FriendRequestViewHolder> {
    private final List<FriendRequest> friendRequests;
    private final FriendRequestListener friendRequestListener;
    private static final int TYPE_PENDING = 1;
    private static final int TYPE_ACCEPTED = 2;
    private static final int TYPE_DECLINED = 3;

    public FriendRequestsAdapter(List<FriendRequest> friendRequests, FriendRequestListener friendRequestListener) {
        this.friendRequests = friendRequests;
        this.friendRequestListener = friendRequestListener;
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerFriendRequestPendingBinding binding = ItemContainerFriendRequestPendingBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FriendRequestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {
        holder.setFriendRequestData(this.friendRequests.get(position));
    }

    @Override
    public int getItemCount() {
        return this.friendRequests.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (this.friendRequests.get(position).status.equals("pending")) {
            return TYPE_PENDING;
        }

        if (this.friendRequests.get(position).status.equals("accepted")) {
            return TYPE_ACCEPTED;
        }

        return TYPE_DECLINED;
    }

    class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        ItemContainerFriendRequestPendingBinding binding;

        public FriendRequestViewHolder(ItemContainerFriendRequestPendingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setFriendRequestData(FriendRequest friendRequest) {
            String status = friendRequest.status;

            this.binding.notificationTextView.setText(friendRequest.senderName + "✌️");
            this.binding.dateTextView.setText(friendRequest.dateTime);
            this.binding.profileImageView.setImageBitmap(getUserImage(friendRequest.senderImage));
            this.binding.profileImageView.setOnClickListener(v -> {
                User user = new User();
                user.id = friendRequest.senderId;
                user.name = friendRequest.senderName;
                friendRequestListener.onUserImageClicked(user);
            });

            switch (status) {
                case "pending":
                    this.binding.acceptButton.setOnClickListener(v -> this.handleAcceptFriendRequest(friendRequest));
                    this.binding.declineButton.setOnClickListener(v -> this.handleDeclineFriendRequest(friendRequest));
                    break;
                case "accepted":
                    this.setAcceptedFriendRequest();
                    break;
                case "declined":
                    this.setDeclinedFriendRequest();
                    break;
            }

        }

        void setAcceptedFriendRequest() {
            this.binding.getRoot().setBackgroundColor(Color.parseColor("#aff7b6"));
//            this.binding.profileImageView.setImageBitmap();
            this.binding.acceptButton.setVisibility(View.GONE);
            this.binding.declineButton.setVisibility(View.GONE);
        }

        void setDeclinedFriendRequest() {
            this.binding.getRoot().setBackgroundColor(Color.parseColor("f48e77"));
            this.binding.acceptButton.setVisibility(View.GONE);
            this.binding.declineButton.setVisibility(View.GONE);
        }

        void handleAcceptFriendRequest(FriendRequest friendRequest) {
            friendRequestListener.onAcceptButtonClicked(friendRequest);
            this.setAcceptedFriendRequest();
        }

        void handleDeclineFriendRequest(FriendRequest friendRequest) {
            friendRequestListener.onDeclineButtonClicked(friendRequest);
            this.setDeclinedFriendRequest();
        }

    }
}
