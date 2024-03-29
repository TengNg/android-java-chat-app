package com.example.myapplication.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class User implements Serializable {
    public String email;
    public String name;
    public String token;
    public String id; // could be senderId or receiverId
    public String image;
    public String gender;
    public boolean isAvailable;
    public boolean isAdminRole;
    public boolean isDeleted;

    @NonNull
    @Override
    public String toString() {
        return this.email + " - " + this.name + " - " + this.gender;
    }
}
