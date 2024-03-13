package org.chatapp.com.mongodb;

import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClientURI;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;


public class QuickStart {
    public static void main(String[] args) {
        String uri = "mongodb+srv://amiyev:BuBuddy2024@bubuddyv1.kups6t4.mongodb.net/?retryWrites=true&w=majority&appName=BuBuddyV1";

        try(MongoClient mongoClient = MongoClients.create(uri)) {

            // Get the database
            MongoDatabase database = mongoClient.getDatabase("sample_chat");
            MongoCollection<Document> collection = database.getCollection("messages");

            Document doc = collection.find(eq("username", "eden")).first();
            if (doc != null) {
                System.out.println(doc.toJson());
            } else {
                System.out.println("No matching documents found.");
            }

            Document query = new Document("username", "Jake");
            doc = collection.find(query).first();

            if (doc != null) {
                // Document for "Jake" exists, update it
                collection.updateOne(eq("_id", doc.get("_id")), new Document("$set", new Document("text", "hello back")));
                System.out.println("Text updated for username Jake.");
            } else {
                // Document for "Jake" doesn't exist, insert a new one
                Document newDoc = new Document("username", "Jake").append("text", "hello back");
                collection.insertOne(newDoc);
                System.out.println("New document inserted for username Jake.");
            }
            // Print confirmation message
            System.out.println("Connected to MongoDB successfully!");

            // Close the MongoClient
            mongoClient.close();
        } catch (Exception e) {
            // Print error message if connection fails
            System.err.println("Failed to connect to MongoDB: " + e.getMessage());
        }
    }
}

