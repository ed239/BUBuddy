package org.chatapp;


import org.bson.types.ObjectId;


//
// Class: ChatUser
//
// Description:
//     TThis is a class to create chatUser objects
//     It allows to get information about the chatUser
//
public class ChatUser {
    private ObjectId id;
    private String name;
    private String username;
    private String dob;

    ////////////////////////////////////////////////////////////////////////////
    /// ChatUser() constructor for chatUser object                           ///
    /// Input : ObjectId id, String name, String username, String dateOfBirth///
    /// Output: None                                                         ///
    /// creates a chatUser Obj with id,name,username, and dob                ///
    ////////////////////////////////////////////////////////////////////////////
    public ChatUser(ObjectId id, String name, String username, String dateOfBirth) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.dob = dateOfBirth;
    }

    //////////////////////////////////////////////////
    /// getId() returns chatUsers id               ///
    /// Input : None                               ///
    /// Output: ObjectId id                        ///
    //////////////////////////////////////////////////
    public ObjectId getId() {
        return id;
    }

    //////////////////////////////////////////////////
    /// getName() returns chatUsers name           ///
    /// Input : None                               ///
    /// Output: String name                        ///
    //////////////////////////////////////////////////
    public String getName() {
        return name;
    }

    //////////////////////////////////////////////////
    /// getUsername() returns chatUsers username   ///
    /// Input : None                               ///
    /// Output: String username                    ///
    //////////////////////////////////////////////////
    public String getUsername() {
        return username;
    }

    //////////////////////////////////////////////////
    /// getDOB() returns chatUsers username        ///
    /// Input : None                               ///
    /// Output: String DOB                         ///
    //////////////////////////////////////////////////
    public String getDOB() { return dob;}

}

