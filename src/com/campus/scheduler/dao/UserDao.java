package com.campus.scheduler.dao;

import com.campus.scheduler.config.DatabaseConfig;
import com.campus.scheduler.model.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private final MongoCollection<Document> collection;

    public UserDao() {
        MongoDatabase db = DatabaseConfig.getDatabase();
        this.collection = db.getCollection("users");
    }

    public void saveUser(User user) {
        Document doc = user.toDocument();
        collection.insertOne(doc);
        user.setId(doc.getObjectId("_id"));
    }

    public User findByUsername(String username) {
        Document doc = collection.find(Filters.eq("username", username)).first();
        return User.fromDocument(doc);
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        for (Document doc : collection.find()) {
            users.add(User.fromDocument(doc));
        }
        return users;
    }

    public long countUsers() {
        return collection.countDocuments();
    }
}
