package org.chatapp;

import org.bson.types.ObjectId;
public class ChatUser {
    private ObjectId id;
    private String name;
    private String username;
    private String dob;
    private String profileImagePath;

    public ChatUser(ObjectId id, String name, String username, String dateOfBirth, String profileImagePath) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.dob = dateOfBirth;
        this.profileImagePath = profileImagePath;
    }

    @Override
    public String toString(){
        return "CURRENT CHAT USER: -> {" + "USERNAME = '" + username + '\'' + '}';
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


    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }
}

