package com.example.myapplication.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.AdminRoleItemContainerFriendBinding;
import com.example.myapplication.databinding.ItemContainerFriendBinding;
import com.example.myapplication.listeners.FriendListener;
import com.example.myapplication.listeners.admin.AdminRoleFriendListener;
import com.example.myapplication.models.User;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<User> users;
    private FriendListener friendListener;
    private AdminRoleFriendListener adminRoleFriendListener;
    private boolean isAdmin;

    public FriendsAdapter(List<User> users, FriendListener friendListener) {
        this.users = users;
        this.friendListener = friendListener;
        this.isAdmin = false;
    }

    public FriendsAdapter(List<User> users, AdminRoleFriendListener friendListener, boolean isAdmin) {
        this.users = users;
        this.adminRoleFriendListener = friendListener;
        this.isAdmin = isAdmin;
    }

    public void updateList(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (isAdmin) {
            AdminRoleItemContainerFriendBinding binding = AdminRoleItemContainerFriendBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new AdminRoleFriendViewHolder(binding);
        }

        ItemContainerFriendBinding binding = ItemContainerFriendBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FriendViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (isAdmin) {
            ((AdminRoleFriendViewHolder) holder).setUserData(this.users.get(position));
        } else {
            ((FriendViewHolder) holder).setUserData(this.users.get(position));
        }
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
            this.binding.profileImageView.setImageBitmap(getUserImage(user.image));

            if (user.isAvailable) {
                this.binding.activeStatusImage.setVisibility(View.VISIBLE);
            } else {
                this.binding.activeStatusImage.setVisibility(View.INVISIBLE);
            }

            binding.getRoot().setOnClickListener(v -> friendListener.onFriendClicked(user));
            binding.infoImage.setOnClickListener(v -> friendListener.onShowInfoButtonClicked(user));
        }
    }


    class AdminRoleFriendViewHolder extends RecyclerView.ViewHolder {
        private AdminRoleItemContainerFriendBinding binding;

        public AdminRoleFriendViewHolder(AdminRoleItemContainerFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setUserData(User user) {
            this.binding.emailTextView.setText(user.email);
            this.binding.nameTextView.setText(user.name);
            this.binding.profileImageView.setImageBitmap(getUserImage(user.image));
            this.binding.getRoot().setOnClickListener(v -> adminRoleFriendListener.onRemoveButtonClicked(user));
        }
    }

}
