package org.chatapp;

import org.bson.types.ObjectId;
public class ChatUser {
    private ObjectId id;
    private String name;
    private String username;
    private String dob;

    public ChatUser(ObjectId id, String name, String username, String dateOfBirth) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.dob = dateOfBirth;
    }

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getDOB() { return dob;}
}

