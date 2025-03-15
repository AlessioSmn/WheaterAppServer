package it.unipi.lsmsd;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Date;

public class mongodb_connection_test {
    static public void main(String[] args) {

        // IMPORTANT: you must activate mongoDB before
        // After having it installed
        // Run 'mongod' on the terminal, should start

        try {
            // Connection to MongoDB
            // Note: without authentication
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");

            // Access database 'test'
            // Note: should be present by default on MongoDB
            MongoDatabase database = mongoClient.getDatabase("test");

            // Open if exists (otherwise it creates) a collection in that database
            MongoCollection<Document> collection = database.getCollection("coll_test");

            // Create a new document with some test fields
            Document doc = new Document("int_field", 1)
                    .append("string_field", "test string")
                    .append("timestamp", new Date());

            // Add the document to the collection
            collection.insertOne(doc);

            // Print information
            System.out.println("Connection OK, Document added");

            // Close the MongoDB connection
            mongoClient.close();

            // NOTE:
            // you can test if the addition is made by typing:
            //      mongosh "mongodb://localhost:27017"
            //      use test
            // (this should display coll_test:)
            //      show tables
            //      db.coll_test.find({})

        } catch (MongoException e) {
            // Error message
            System.err.println("Connection error to MongoDB: " + e.getMessage());
        }
    }
}
