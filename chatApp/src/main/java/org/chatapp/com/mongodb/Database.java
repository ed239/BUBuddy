package org.chatapp.com.mongodb;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.chatapp.ChatUser;

import static com.mongodb.client.model.Filters.eq;
public class Database {

    private static Database instance = null;
    private final MongoCollection<Document> userCollection;

    private Database() {
        // Replace string with our db connection
        String uri = "";

        MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("sample_chat");
        userCollection = database.getCollection("users");

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



    public Boolean createUser(String fullname, String username, String password){
        boolean exists = userExists(username);
        String hashedPassword = hashPassword(password);
        if(!exists){
            Document newDoc = new Document("username", username).append("password", hashedPassword).append("fullname", fullname);
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
            if (!user.equals(currentUser)) {
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
                System.out.println(name);
                String username = userDoc.getString("username");
                ChatUser user = new ChatUser(id, name, username);
                allChatUsers.add(user);
            }
        } finally {
            cursor.close();
        }
        return allChatUsers;
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


}
