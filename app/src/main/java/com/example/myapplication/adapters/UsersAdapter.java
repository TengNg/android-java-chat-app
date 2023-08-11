package com.example.myapplication.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.AdminRoleItemContainerUserBinding;
import com.example.myapplication.databinding.ItemContainerUserBinding;
import com.example.myapplication.listeners.UserListener;
import com.example.myapplication.listeners.admin.AdminRoleUserListener;
import com.example.myapplication.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<User> users;
    private UserListener userListener;
    private AdminRoleUserListener adminRoleUserListener;
    private boolean isAdmin;

    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
        this.isAdmin = false;
    }

    public UsersAdapter(List<User> users, AdminRoleUserListener adminRoleUserListener, boolean isAdmin) {
        this.users = users;
        this.adminRoleUserListener = adminRoleUserListener;
        this.isAdmin = isAdmin;
    }

    public void updateList(List<User> users){
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
        if (this.isAdmin) {
            return new AdminRoleUserViewHolder(
                    AdminRoleItemContainerUserBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
        return new UserViewHolder(
                ItemContainerUserBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (this.isAdmin) {
            ((AdminRoleUserViewHolder) holder).setUserData(this.users.get(position));
        } else {
            ((UserViewHolder) holder).setUserData(this.users.get(position));
        }
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
            this.binding.profileImageView.setImageBitmap(getUserImage(user.image));
            this.binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
            this.binding.messageImage.setOnClickListener(v -> userListener.onMessageButtonClicked(user));
        }
    }

    class AdminRoleUserViewHolder extends RecyclerView.ViewHolder {
        AdminRoleItemContainerUserBinding binding;

        public AdminRoleUserViewHolder(AdminRoleItemContainerUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setUserData(User user) {
            this.binding.emailTextView.setText(user.email);
            this.binding.nameTextView.setText(user.name);
            this.binding.profileImageView.setImageBitmap(getUserImage(user.image));
            this.binding.getRoot().setOnClickListener(v -> adminRoleUserListener.onUserClicked(user));
        }

    }
}