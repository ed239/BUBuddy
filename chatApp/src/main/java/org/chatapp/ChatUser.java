package org.chatapp;

import org.bson.types.ObjectId;
public class ChatUser {
    private ObjectId id;
    private String name;
    private String username;

    public ChatUser(ObjectId id, String name, String username) {
        this.id = id;
        this.name = name;
        this.username = username;
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
}

