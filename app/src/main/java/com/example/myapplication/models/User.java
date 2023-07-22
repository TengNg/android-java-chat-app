package com.example.myapplication.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class User implements Serializable {
    public String email;
    public String name;
    public String token;
    public String id; // could be senderId or receiverId
    public boolean isActive;

    public User withDocId(String docID) {
        if (this.id.equals(docID))
            return this;
        return null;
    }
}
