package com.campus.scheduler.dao;

import com.campus.scheduler.config.DatabaseConfig;
import com.campus.scheduler.model.Timetable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TimetableDao {
    private final MongoCollection<Document> collection;

    public TimetableDao() {
        MongoDatabase db = DatabaseConfig.getDatabase();
        this.collection = db.getCollection("timetables");
    }

    public void saveOrUpdate(Timetable timetable) {
        Document doc = timetable.toDocument();
        collection.replaceOne(
            Filters.eq("classId", timetable.getClassId()),
            doc,
            new ReplaceOptions().upsert(true)
        );
    }

    public Timetable findByClassId(String classId) {
        Document doc = collection.find(Filters.eq("classId", classId)).first();
        return Timetable.fromDocument(doc);
    }

    public List<Timetable> getAllTimetables() {
        List<Timetable> list = new ArrayList<>();
        for (Document doc : collection.find()) {
            list.add(Timetable.fromDocument(doc));
        }
        return list;
    }

    public boolean deleteByClassId(String classId) {
        return collection.deleteOne(Filters.eq("classId", classId)).getDeletedCount() > 0;
    }
}
