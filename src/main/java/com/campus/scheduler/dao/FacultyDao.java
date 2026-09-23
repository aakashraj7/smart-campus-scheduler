package com.campus.scheduler.dao;

import com.campus.scheduler.config.DatabaseConfig;
import com.campus.scheduler.model.Faculty;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class FacultyDao {
    private final MongoCollection<Document> collection;

    public FacultyDao() {
        MongoDatabase db = DatabaseConfig.getDatabase();
        this.collection = db.getCollection("faculties");
    }

    public void saveOrUpdate(Faculty faculty) {
        Document doc = faculty.toDocument();
        collection.replaceOne(
            Filters.eq("facultyId", faculty.getFacultyId()),
            doc,
            new ReplaceOptions().upsert(true)
        );
    }

    public Faculty findByFacultyId(String facultyId) {
        Document doc = collection.find(Filters.eq("facultyId", facultyId)).first();
        return Faculty.fromDocument(doc);
    }

    public List<Faculty> getAllFaculties() {
        List<Faculty> faculties = new ArrayList<>();
        for (Document doc : collection.find()) {
            faculties.add(Faculty.fromDocument(doc));
        }
        return faculties;
    }

    public boolean deleteByFacultyId(String facultyId) {
        return collection.deleteOne(Filters.eq("facultyId", facultyId)).getDeletedCount() > 0;
    }
}
