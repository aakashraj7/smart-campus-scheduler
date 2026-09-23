package com.campus.scheduler.dao;

import com.campus.scheduler.config.DatabaseConfig;
import com.campus.scheduler.model.Subject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class SubjectDao {
    private final MongoCollection<Document> collection;

    public SubjectDao() {
        MongoDatabase db = DatabaseConfig.getDatabase();
        this.collection = db.getCollection("subjects");
    }

    public void saveOrUpdate(Subject subject) {
        Document doc = subject.toDocument();
        collection.replaceOne(
            Filters.eq("subjectCode", subject.getSubjectCode()),
            doc,
            new ReplaceOptions().upsert(true)
        );
    }

    public Subject findByCode(String subjectCode) {
        Document doc = collection.find(Filters.eq("subjectCode", subjectCode)).first();
        return Subject.fromDocument(doc);
    }

    public List<Subject> getAllSubjects() {
        List<Subject> list = new ArrayList<>();
        for (Document doc : collection.find()) {
            list.add(Subject.fromDocument(doc));
        }
        return list;
    }

    public boolean deleteByCode(String subjectCode) {
        return collection.deleteOne(Filters.eq("subjectCode", subjectCode)).getDeletedCount() > 0;
    }
}
