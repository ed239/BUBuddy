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
public class Database {
    private static Database instance = null;
    private final MongoCollection<Document> userCollection;
    private final MongoCollection<Document> messagesCollection;

    private Database() {
        // Replace string with our db connection
        String uri = "mongodb+srv://amiyev:BuBuddy2024@bubuddyv1.kups6t4.mongodb.net/?retryWrites=true&w=majority&appName=BuBuddyV1";
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

    public String getName(String username){
        Document userDoc = userCollection.find(new Document("username", username)).first();
        if (userDoc != null) {
            return userDoc.getString("fullname");
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

    public Boolean createUser(String fullname, String username, String password, String dateOfBirth, byte[] profileImageData, String email){
        boolean exists = userExists(username);
        String hashedPassword = hashPassword(password);

        Document newDoc = new Document("username", username)
                .append("password", hashedPassword)
                .append("fullname", fullname)
                .append("dob", dateOfBirth)
                .append("email",email);

        if(profileImageData != null && profileImageData.length > 0){
            newDoc.append("profileImage",new Binary(profileImageData));
        }
        if(!exists){
            userCollection.insertOne(newDoc);
            System.out.println("\nCREATED NEW USER\n");
            return true;
        } else {
            System.out.println("\nUSER ALREADY EXISTS!\n");
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
                ChatUser user = new ChatUser(id, name, username,dob);
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
        return new ChatUser(null,null,null,null);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
//                String hex = Integer.toHexString(0xff & b);
//                if (hex.length() == 1) hexString.append('0');
//                hexString.append(hex);
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

    //UPDATED PASSWORD AND HASHING IT
    public boolean updatePassword(String username, String newPassword){
        try{
            // Hash the new password before updating
            String hashedPassword = hashPassword(newPassword);
            userCollection.updateOne(eq("username", username), set("password", hashedPassword ));
            System.out.println("\nPassword updated in the database!");
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateProfileImages(String username, byte[] imageData){
        try{
            userCollection.updateOne(eq("username", username), set("profileImage", new Binary(imageData)));
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
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
    public boolean updatedUserEmail(String username, String email){
        try{
            userCollection.updateOne(eq("username", username), set("email", email));
            System.out.println("\nEMAIL UPDATED SUCCESSFULLY!\n");
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public String getEmail(String username){
        Document userDoc = userCollection.find(new Document("username", username)).first();
        if(userDoc != null){
            return userDoc.getString("email");
        }else {
            return null;
        }
    }

    public boolean updateProfileDetails(String username, String fullName, String dateOfBirth, String email){
        try{
            userCollection.updateOne(eq("username", username),
                    Updates.combine(
                            Updates.set("fullname", fullName),
                            Updates.set("dob", dateOfBirth),
                            Updates.set("email", email)
                            )
            );
            System.out.println("\nPROFILE DETAILS UPDATED SUCCESSFULLY!\n");
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
