package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemContainerFriendBinding;
import com.example.myapplication.listeners.FriendListener;
import com.example.myapplication.models.User;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
    private List<User> users;
    private final FriendListener friendListener;

    public FriendsAdapter(List<User> users, FriendListener friendListener) {
        this.users = users;
        this.friendListener = friendListener;
    }

    public void updateList(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerFriendBinding binding = ItemContainerFriendBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FriendViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        holder.setUserData(this.users.get(position));
    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        ItemContainerFriendBinding binding;

        public FriendViewHolder(ItemContainerFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setUserData(User user) {
            this.binding.emailTextView.setText(user.email);
            this.binding.nameTextView.setText(user.name);

            if (user.isAvailable) {
                this.binding.activeStatusImage.setVisibility(View.VISIBLE);
            } else {
                this.binding.activeStatusImage.setVisibility(View.INVISIBLE);
            }

            binding.getRoot().setOnClickListener(v -> friendListener.onFriendClicked(user));
            binding.infoImage.setOnClickListener(v -> friendListener.onShowInfoButtonClicked(user));
        }
    }
}
