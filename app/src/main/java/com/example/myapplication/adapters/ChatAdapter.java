package com.example.myapplication.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemContainerReceivedMessageBinding;
import com.example.myapplication.databinding.ItemContainerSentMessageBinding;
import com.example.myapplication.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessages;
    private final String senderId;
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;
    private final Bitmap senderImage;
    private final Bitmap receiverImage;

    public ChatAdapter(List<ChatMessage> chatMessages, String senderId, Bitmap senderImage, Bitmap receiverImage) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.senderImage = senderImage;
        this.receiverImage = receiverImage;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
        return new ReceivedMessageViewHolder(
                ItemContainerReceivedMessageBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position), senderImage);
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)) {
            return TYPE_SENT;
        }
        return TYPE_RECEIVED;
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private ItemContainerSentMessageBinding binding;

        public SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            this.binding = itemContainerSentMessageBinding;
        }

        public void setData(ChatMessage chatMessage, Bitmap image) {
            this.binding.usernameTextView.setText(chatMessage.senderName);

            if (chatMessage.isHighlighted) {
                Spannable wordToSpan = new SpannableString(chatMessage.message);
                wordToSpan.setSpan(
                        new BackgroundColorSpan(Color.YELLOW),
                        chatMessage.highlightStartIndex,
                        chatMessage.highlightEndIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                this.binding.messageTextView.setText(wordToSpan);
            }
            else {
                this.binding.messageTextView.setText(chatMessage.message);
            }

            this.binding.dateTimeTextView.setText(chatMessage.dateTime);
            this.binding.profileImageView.setImageBitmap(image);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private ItemContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            this.binding = itemContainerReceivedMessageBinding;
        }

        public void setData(ChatMessage chatMessage, Bitmap image) {
            this.binding.usernameTextView.setText(chatMessage.receiverName);

            if (chatMessage.isHighlighted) {
                Spannable wordToSpan = new SpannableString(chatMessage.message);
                wordToSpan.setSpan(
                        new BackgroundColorSpan(Color.YELLOW),
                        chatMessage.highlightStartIndex,
                        chatMessage.highlightEndIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                this.binding.messageTextView.setText(wordToSpan);
            } else {
                this.binding.messageTextView.setText(chatMessage.message);
            }

            this.binding.dateTimeTextView.setText(chatMessage.dateTime);
            this.binding.profileImageView.setImageBitmap(image);
        }
    }
}
