package org.chatapp.com.mongodb;

import com.mongodb.client.*;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.chatapp.ChatUser;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import ch.qos.logback.classic.Level;  //disables logs from mongoDB
import ch.qos.logback.classic.LoggerContext;  //disables logs from mongoDB
import org.slf4j.LoggerFactory; //disables logs from mongoDB

//
// Class: Database
//
// Description:
//     This is a Database class which manages the connection to
//     our MongoDB database.
//
public class Database {
    private static Database instance = null;
    private final MongoCollection<Document> userCollection;
    private final MongoCollection<Document> messagesCollection;

    ///////////////////////////////////////////////////////////////////
    /// Database() creates connection to db                         ///
    /// Input : None                                                ///
    /// Output: None                                                ///
    /// Connects to the database and defines the collections        ///
    /// Note: change URI to match database link                     ///
    ///////////////////////////////////////////////////////////////////
    private Database() {
        // Replace string with our db connection
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.mongodb.driver").setLevel(Level.ERROR); //disables logs from mongoDB
        //Change uri here
        String uri = "";
        MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("sample_chat");
        userCollection = database.getCollection("users");
        messagesCollection = database.getCollection("messages");
    }

    ///////////////////////////////////////////////////////////////////
    /// getInstance() pass instance of db to different controllers  ///
    /// Input : None                                                ///
    /// Output: instance of the database connection                 ///
    /// allows access to db from different scene controllers        ///
    ///////////////////////////////////////////////////////////////////
    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    ///////////////////////////////////////////////////////////////////
    /// userExists() check if user exists                           ///
    /// Input : String Username                                     ///
    /// Output: Boolean                                             ///
    /// Checks if the Username exists in db                         ///
    /// Used for Login and SignUp                                   ///
    ///////////////////////////////////////////////////////////////////
    public boolean userExists(String username) {
        Document doc = userCollection.find(new Document("username", username)).first();
        if(doc != null) {
            return true;
        }
        return false;
    }

    //////////////////////////////////////////////////////////////////
    /// verifyPassword() compares inputted password to stored      ///
    /// Input : String Username, password                          ///
    /// Output: Boolean                                            ///
    /// Checks if login credentials are valid - authentication     ///
    /// Have to unhash password from db and verify with user input ///
    public boolean verifyPassword(String username, String password) {
        Document userDoc = userCollection.find(new Document("username", username)).first();
        if (userDoc != null) {
            String storedPassword = userDoc.getString("password");
            String hashedPassword = hashPassword(password);
            return storedPassword.equals(hashedPassword);
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    /// getName() get full name of a user                           ///
    /// Input : String Username                                     ///
    /// Output: String fullname                                     ///
    /// used to populate left chat panel and profile page           ///
    ///////////////////////////////////////////////////////////////////
    public String getName(String username){
        Document userDoc = userCollection.find(new Document("username", username)).first();
        if (userDoc != null) {
            return userDoc.getString("fullname");
        }
        return "";
    }

    //////////////////////////////////////////////////////////////////
    /// getDOB() get DOB of a user                                 ///
    /// Input : String Username                                    ///
    /// Output: String DOB                                         ///
    /// displayed in profile page                                  ///
    //////////////////////////////////////////////////////////////////
    public String getDOB(String username){
        Document userDoc = userCollection.find(new Document("username", username)).first();
        if (userDoc != null) {
            return userDoc.getString("dob");
        } else {
            return null;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    /// createUser() new user sign up                                     ///
    /// Input : String fullanme, username, password, DOB, profileimage    ///
    /// Output: True if new user created, otherwise false                 ///
    /// Creates a new User in the db                                      ///
    /////////////////////////////////////////////////////////////////////////
    public Boolean createUser(String fullname, String username, String password, String dateOfBirth, byte[] profileImageData){
        boolean exists = userExists(username);
        String hashedPassword = hashPassword(password);

        Document newDoc = new Document("username", username)
                .append("password", hashedPassword)
                .append("fullname", fullname)
                .append("dob", dateOfBirth);

        if(profileImageData != null && profileImageData.length > 0){
            newDoc.append("profileImage",new Binary(profileImageData));
        }
        if(!exists){
            userCollection.insertOne(newDoc);
            return true;
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    /// getAllChatUsersExceptCurrent()  List of names excluding cur user        ///
    /// Input : ChatUser currentUser                                            ///
    /// Output: String array of names of all users not including current user   ///
    /// String array of names used to populate left chat panel                  ///
    ///////////////////////////////////////////////////////////////////////////////
    public String[] getAllChatUsersExceptCurrent(ChatUser currentUser) {
        List<String> usersList = new ArrayList<>();
        List<ChatUser> allChatUsers = getAllChatUsersFromDatabase();
        for (ChatUser user : allChatUsers) {
            if (!user.getId().equals(currentUser.getId())) {
                usersList.add(user.getName());
            }
        }
        return usersList.toArray(new String[0]);
    }

    ///////////////////////////////////////////////////////////////////////////////
    /// getAllChatUsersFromDatabase()  List of all ChatUser obj                 ///
    /// Input : None                                                            ///
    /// Output: List of ChatUser objects                                        ///
    /// Gives all users in one list -- used in  getAllChatUsersExceptCurrent    ///
    ///////////////////////////////////////////////////////////////////////////////
    public List<ChatUser> getAllChatUsersFromDatabase() {
        List<ChatUser> allChatUsers = new ArrayList<>();

        MongoCursor<Document> cursor = null;
        try {
            cursor = userCollection.find().iterator();
            while (cursor.hasNext()) {
                Document userDoc = cursor.next();
                ObjectId id = userDoc.getObjectId("_id");
                String name = userDoc.getString("fullname");
                String username = userDoc.getString("username");
                String dob = userDoc.getString("dob");
                ChatUser user = new ChatUser(id, name, username,dob);
                allChatUsers.add(user);
            }
        } finally {
            cursor.close();
        }
        return allChatUsers;
    }

    ///////////////////////////////////////////////////////////////////////////////
    /// addNewmessage()  adds new message to db                                 ///
    /// Input : ObjectId toId, fromId, String text, Date timestamp              ///
    /// Output: True if added, otherwise false                                  ///
    /// All messages are stores in db in main chat... this is how               ///
    ///////////////////////////////////////////////////////////////////////////////
    public Boolean addNewmessage(ObjectId toId, ObjectId fromId, String text, Date timestamp){
        try {
            Document message = new Document("toId", toId).append("fromId", fromId).append("text", text).append("timestamp", timestamp);
            messagesCollection.insertOne(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    /// getChatUser() get  current chat user                                    ///
    /// Input : String name (fullname)                                          ///
    /// Output: ChatUser obj, if not found then null                            ///
    /// chatUser obj of the currently logged in user                            ///
    ///////////////////////////////////////////////////////////////////////////////
    public ChatUser getChatUser(String name){
        List<ChatUser> allChatUsers = getAllChatUsersFromDatabase();
        for (ChatUser user : allChatUsers) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return new ChatUser(null,null,null,null);
    }

    ///////////////////////////////////////////////////////////////////////////////
    /// hashPassword() encrypt the password                                     ///
    /// Input : String password                                                 ///
    /// Output: None                                                            ///
    /// Encrypt password for storage in db using SHA-256                        ///
    ///////////////////////////////////////////////////////////////////////////////
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    /// getMessagesBetweenUsers() get all messages between 2 users              ///
    /// Input : ObjectId toId, ObjectId fromId                                  ///
    /// Output: FindIterable<Document> of sorted messages                       ///
    /// Used to populate chat page                                              ///
    ///////////////////////////////////////////////////////////////////////////////
    public FindIterable<Document> getMessagesBetweenUsers(ObjectId toId, ObjectId fromId){
        Document query = new Document("$or",
                List.of(
                    new Document("fromId", fromId).append("toId", toId),
                    new Document("fromId", toId).append("toId", fromId)
                ));
        return messagesCollection.find(query).sort(new Document("timestamp", 1));
    }

    ///////////////////////////////////////////////////////////////////////////////
    /// getMessagesBetweenUsers() get all messages between 2 users              ///
    /// Input : ObjectId toId, ObjectId fromId                                  ///
    /// Output: FindIterable<Document> of sorted messages                       ///
    /// Used to populate chat page                                              ///
    ///////////////////////////////////////////////////////////////////////////////
    public FindIterable<Document> getNewMessagesBetweenUsers(ObjectId toId, ObjectId fromId, Date lastDisplayedTimestamp) {
        Document query = new Document("$or",
                List.of(
                    new Document("fromId", fromId).append("toId", toId),
                    new Document("fromId", toId).append("toId", fromId)
                )).append("timestamp", new Document("$gt", lastDisplayedTimestamp));
        return messagesCollection.find(query).sort(new Document("timestamp", 1));
    }

    ///////////////////////////////////////////////////////////////////////////////
    /// updatePassword() updates user's password in Database                    ///
    /// Input : String password, newPassword                                    ///
    /// Output: None                                                            ///
    /// If the user updates their password, it is updated in the Database       ///
    ///////////////////////////////////////////////////////////////////////////////
    public boolean updatePassword(String username, String newPassword){
        try{
            // Hash the new password before updating
            String hashedPassword = hashPassword(newPassword);
            userCollection.updateOne(eq("username", username), set("password", hashedPassword ));
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    /// validEmailDob() verifies username and dob match a user                  ///
    /// Input : String username, dob                                            ///
    /// Output: Boolean - true if match, otherwise false                        ///
    /// Used to check user info for forgot password                             ///
    ///////////////////////////////////////////////////////////////////////////////
    public boolean validEmailDob(String username, String dob) {
        Document doc = userCollection.find(new Document("username", username)).first();
        if (doc != null) {
            return dob.equals(doc.getString("dob"));
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////////
    /// updateProfileImages() update Profile image in Database                  ///
    /// Input : String username, byte imageDate                                 ///
    /// Output: Boolean - true if updated, otherwise false                      ///
    /// Returns updated image in Profile page                                   ///
    ///////////////////////////////////////////////////////////////////////////////
    public boolean updateProfileImages(String username, byte[] imageData){
        try{
            userCollection.updateOne(eq("username", username), set("profileImage", new Binary(imageData)));
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    /// getProfileImage() get Profile image from Database                        ///
    /// Input : String username                                                  ///
    /// Output: Returns image if not null, otherwise null                        ///
    /// If the user have Profile image in Database, then get image from Database ///
    ////////////////////////////////////////////////////////////////////////////////
    public byte[] getProfileImage(String username){
        Document userDoc = userCollection.find(eq("username", username)).first();
        if(userDoc != null){
            Binary imageDate = userDoc.get("profileImage", Binary.class);
            if(imageDate != null){
                return imageDate.getData();
            }
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////
    /// updateProfileDetails() update Profile details in Database                ///
    /// Input : String username, fullName, dateOfBirth                           ///
    /// Output: Boolean - true if updated, otherwise false                       ///
    /// If the user updates the profile data, it is updated in the database      ///
    ////////////////////////////////////////////////////////////////////////////////
    public boolean updateProfileDetails(String username, String fullName, String dateOfBirth){
        try{
            userCollection.updateOne(eq("username", username),
                    Updates.combine(
                            Updates.set("fullname", fullName),
                            Updates.set("dob", dateOfBirth)));
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
