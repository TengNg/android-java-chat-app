package com.example.myapplication.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemContainerRecentConversationBinding;
import com.example.myapplication.listeners.ConversationsListener;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.User;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversationViewHolder> {
    private final List<ChatMessage> chatMessages;
    private ConversationsListener conversationsListener;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages, ConversationsListener conversationsListener) {
        this.chatMessages = chatMessages;
        this.conversationsListener = conversationsListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerRecentConversationBinding binding = ItemContainerRecentConversationBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ConversationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setData(this.chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return this.chatMessages.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversationBinding binding;

        public ConversationViewHolder(ItemContainerRecentConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage chatMessage) {
            this.binding.profileImageView.setImageBitmap(getConversationImage(chatMessage.conversationImage));
            this.binding.conversationNameTextView.setText(chatMessage.conversationName);
            this.binding.messageTextView.setText(chatMessage.message);
            this.binding.dateTimeTextView.setText(chatMessage.dateTime);
            this.binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.name = chatMessage.conversationName;
                user.image = chatMessage.conversationImage;
                user.id = chatMessage.conversationId;
                conversationsListener.onConversationClicked(user);
            });
        }
    }

    private Bitmap getConversationImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
