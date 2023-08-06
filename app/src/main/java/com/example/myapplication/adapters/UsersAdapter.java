package com.example.myapplication.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemContainerUserBinding;
import com.example.myapplication.listeners.UserListener;
import com.example.myapplication.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    private List<User> users;
    private final UserListener userListener;

    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
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
            this.binding.profileImageView.setImageBitmap(getUserImage(user.image));
            this.binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
            this.binding.messageImage.setOnClickListener(v -> userListener.onMessageButtonClicked(user));
        }
    }
}