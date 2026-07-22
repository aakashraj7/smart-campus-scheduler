package com.campus.scheduler.dao;

import com.campus.scheduler.config.DatabaseConfig;
import com.campus.scheduler.model.ClassGroup;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class ClassDao {
    private final MongoCollection<Document> collection;

    public ClassDao() {
        MongoDatabase db = DatabaseConfig.getDatabase();
        this.collection = db.getCollection("classes");
    }

    public void saveOrUpdate(ClassGroup classGroup) {
        Document doc = classGroup.toDocument();
        collection.replaceOne(
            Filters.eq("classId", classGroup.getClassId()),
            doc,
            new ReplaceOptions().upsert(true)
        );
    }

    public ClassGroup findByClassId(String classId) {
        Document doc = collection.find(Filters.eq("classId", classId)).first();
        return ClassGroup.fromDocument(doc);
    }

    public List<ClassGroup> getAllClasses() {
        List<ClassGroup> list = new ArrayList<>();
        for (Document doc : collection.find()) {
            list.add(ClassGroup.fromDocument(doc));
        }
        return list;
    }

    public boolean deleteByClassId(String classId) {
        return collection.deleteOne(Filters.eq("classId", classId)).getDeletedCount() > 0;
    }
}
