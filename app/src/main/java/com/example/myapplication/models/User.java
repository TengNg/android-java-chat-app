package com.example.myapplication.models;

import java.io.Serializable;

public class User implements Serializable {
    public String email;
    public String name;
    public String token;
    public String id; // could be senderId or receiverId
    public String image;

    public boolean isAvailable;
}
