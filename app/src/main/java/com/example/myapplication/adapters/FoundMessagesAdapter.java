package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemContainerFoundMessageBinding;
import com.example.myapplication.listeners.FoundMessageListener;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.User;

import java.util.List;

public class FoundMessagesAdapter extends RecyclerView.Adapter<FoundMessagesAdapter.FoundMessageViewHolder> {
    private List<ChatMessage> chatMessages;
    private final FoundMessageListener foundMessageListener;
    private final User receiver;
    private int highlightIndex = -1;

    public FoundMessagesAdapter(List<ChatMessage> chatMessages, FoundMessageListener foundMessageListener, User receiver) {
        this.chatMessages = chatMessages;
        this.foundMessageListener = foundMessageListener;
        this.receiver = receiver;
    }

    public void updateList(List<ChatMessage> chatMessages){
        this.chatMessages = chatMessages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoundMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerFoundMessageBinding binding = ItemContainerFoundMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FoundMessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoundMessageViewHolder holder, int position) {
        holder.setData(this.chatMessages.get(position), this.receiver);
    }

    @Override
    public int getItemCount() {
        return this.chatMessages.size();
    }

    class FoundMessageViewHolder extends RecyclerView.ViewHolder {
        ItemContainerFoundMessageBinding binding;

        public FoundMessageViewHolder(ItemContainerFoundMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        public void setData(ChatMessage chatMessage, User user) {
            this.binding.profileImageView.setImageBitmap(getUserImage(user.image));
            this.binding.conversationNameTextView.setText(user.name);

            SpannableString spannableString = new SpannableString(chatMessage.message);
            spannableString.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    chatMessage.highlightStartIndex,
                    chatMessage.highlightEndIndex,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            this.binding.messageTextView.setText(spannableString);

            this.binding.dateTimeTextView.setText(chatMessage.dateTime);

            this.binding.getRoot().setOnClickListener(v -> {
                foundMessageListener.onFoundMessageClicked(chatMessage);
            });
        }
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
