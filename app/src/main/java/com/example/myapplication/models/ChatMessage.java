package com.example.myapplication.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

public class ChatMessage implements Serializable {
    public String senderName;
    public String receiverName;
    public String senderId;
    public String receiverId;
    public String message;
    public String dateTime;
    public Date dateObject;

    public String conversationId;
    public String conversationName;
    public String conversationImage;
    public int searchIndex;

    @NonNull
    @Override
    public String toString() {
        return this.message + " - " + this.searchIndex;
    }
}
