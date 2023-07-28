package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemContainerFriendRequestPendingBinding;
import com.example.myapplication.listeners.FriendRequestListener;
import com.example.myapplication.models.FriendRequest;

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
            this.binding.notificationTextView.setText(friendRequest.senderName + "✌️");
            this.binding.dateTextView.setText(friendRequest.dateTime);
            this.binding.acceptButton.setOnClickListener(v -> friendRequestListener.onAcceptButtonClicked());
            this.binding.declineButton.setOnClickListener(v -> friendRequestListener.onDeclineButtonClicked());
        }
    }
}
