package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemContainerUserBinding;
import com.example.myapplication.listeners.UserListener;
import com.example.myapplication.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    private final List<User> users;
    private final UserListener userListener;

    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(this.users.get(position));
    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemContainerUserBinding binding;

        public UserViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            this.binding = itemContainerUserBinding;
        }

        void setUserData(User user) {
            this.binding.emailTextView.setText(user.email);
            this.binding.nameTextView.setText(user.name);
            this.binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
            this.binding.addFriendImage.setOnClickListener(v -> userListener.onAddFriendButtonClicked(user));
            this.binding.messageImage.setOnClickListener(v -> userListener.onMessageButtonClicked(user));
        }
    }
}