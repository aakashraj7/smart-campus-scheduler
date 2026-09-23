package com.campus.scheduler.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Database configuration & singleton MongoClient connection manager.
 * Connects to local MongoDB instance at mongodb://localhost:27017/smart_campus_scheduler
 */
public class DatabaseConfig {

    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DB_NAME = "smart_campus_scheduler";

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    public static synchronized MongoDatabase getDatabase() {
        if (database == null) {
            try {
                // Disable verbose MongoDB driver logging for clean CLI experience
                java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(java.util.logging.Level.SEVERE);
                
                mongoClient = MongoClients.create(CONNECTION_STRING);
                database = mongoClient.getDatabase(DB_NAME);
                
                // Ping database to verify connection
                database.runCommand(new Document("ping", 1));
            } catch (Exception e) {
                System.err.println("❌ ERROR: Failed to connect to local MongoDB database at " + CONNECTION_STRING);
                System.err.println("Please make sure your MongoDB service is running.");
                throw new RuntimeException("MongoDB connection failed", e);
            }
        }
        return database;
    }

    public static synchronized void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
        }
    }
}
