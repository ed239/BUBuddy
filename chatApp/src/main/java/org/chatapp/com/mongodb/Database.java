package org.chatapp.com.mongodb;

import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.chatapp.ChatUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
public class Database {

    private static Database instance = null;
    private final MongoCollection<Document> userCollection;
    private final MongoCollection<Document> messagesCollection;

    private Database() {
        // Replace string with our db connection
        String uri = "";
        MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("sample_chat");
        userCollection = database.getCollection("users");
        messagesCollection = database.getCollection("messages");

    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public boolean userExists(String username) {
        Document doc = userCollection.find(new Document("username", username)).first();
        if(doc != null) {
            return true;
        }
        return false;
    }

    public boolean verifyPassword(String username, String password) {
        Document userDoc = userCollection.find(new Document("username", username)).first();
        if (userDoc != null) {
            String storedPassword = userDoc.getString("password");
            String hashedPassword = hashPassword(password);
            return storedPassword.equals(hashedPassword);
        }
        return false;
    }
    // VERIFY WHETHER THE DATE OF BIRTH IS MATCH?
    public boolean verifyDateOfBirth(String username,String dateOfBirth){
        Document userDoc = userCollection.find(new Document("username", username)).first();
        if(userDoc!= null){
            String storeDateOfBirth = userDoc.getString("dob");
            return storeDateOfBirth.equals(dateOfBirth);
        }
        return false;
    }

    public String getName(String username, String password){
        Document userDoc = userCollection.find(new Document("username", username)).first();
        if (userDoc != null) {
            String name = userDoc.getString("fullname");
            return name;
        }
        return "";
    }

    public ObjectId getUserObjectId(String username) {
        Document userDoc = userCollection.find(new Document("username", username)).first();
        if (userDoc != null) {
            return userDoc.getObjectId("_id");
        } else {
            return null; 
        }
    }
    public String getDOB(String username){
        Document userDoc = userCollection.find(new Document("username", username)).first();
        if (userDoc != null) {
            return userDoc.getString("dob");
        } else {
            return null;
        }
    }



    public Boolean createUser(String fullname, String username, String password, String dateOfBirth, String profileImagePath){
        boolean exists = userExists(username);
        String hashedPassword = hashPassword(password);
        if(!exists){
            Document newDoc = new Document("username", username)
                    .append("password", hashedPassword)
                    .append("fullname", fullname)
                    .append("dob", dateOfBirth)
                    .append("profileImagePath",profileImagePath);
            userCollection.insertOne(newDoc);
            System.out.println("Created New User");
            return true;
        } else {
            System.out.println("User already exists");
            return false;
        }
    }

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
    public List<ChatUser> getAllChatUsersFromDatabase() {
        List<ChatUser> allChatUsers = new ArrayList<>();

        MongoCursor<Document> cursor = null;
        try {
            cursor = userCollection.find().iterator();
            while (cursor.hasNext()) {
                Document userDoc = cursor.next();
                ObjectId id = userDoc.getObjectId("_id");
                String name = userDoc.getString("fullname");
                //System.out.println(name);
                String username = userDoc.getString("username");
                String dob = userDoc.getString("dob");
                String profileImagePath = userDoc.getString("profileImagePath");
                ChatUser user = new ChatUser(id, name, username,dob,profileImagePath);
                allChatUsers.add(user);
            }
        } finally {
            cursor.close();
        }
        return allChatUsers;
    }

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

    public ChatUser getChatUser(String name){
//        Document doc = userCollection.find(new Document("fullname", name)).first();
//        if (doc != null) {
//            ObjectId id = doc.getObjectId("_id");
//            String fullname = doc.getString("fullname");
//            String username = doc.getString("username");
//            String dob = doc.getString("dob");
//            return new ChatUser(id, fullname, username, dob);
//
//         }
        List<ChatUser> allChatUsers = getAllChatUsersFromDatabase();
        for (ChatUser user : allChatUsers) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return new ChatUser(null,null,null,null, null);

    }

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

    public FindIterable<Document> getMessagesBetweenUsers(ObjectId toId, ObjectId fromId, String text){
        Document query = new Document("$or",
                List.of(
                    new Document("fromId", fromId).append("toId", toId),
                    new Document("fromId", toId).append("toId", fromId)
                ));
        return messagesCollection.find(query).sort(new Document("timestamp", 1));
    }

    public FindIterable<Document> getNewMessagesBetweenUsers(ObjectId toId, ObjectId fromId, Date lastDisplayedTimestamp) {
        Document query = new Document("$or",
                List.of(
                    new Document("fromId", fromId).append("toId", toId),
                    new Document("fromId", toId).append("toId", fromId)
                )).append("timestamp", new Document("$gt", lastDisplayedTimestamp));
        return messagesCollection.find(query).sort(new Document("timestamp", 1));
    }

    public boolean updatePassword(String username, String newPassword){
        try{
            userCollection.updateOne(eq("username", username), set("password", hashPassword(newPassword)));
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateProfileImages(String username, String profileImagesPath){
        try{
            userCollection.updateOne(eq("username", username), set("profileImagesPath", profileImagesPath));
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }



}
