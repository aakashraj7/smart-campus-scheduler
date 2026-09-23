package com.campus.scheduler.dao;

import com.campus.scheduler.config.DatabaseConfig;
import com.campus.scheduler.model.AcademicConfig;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.springframework.stereotype.Repository;

@Repository
public class AcademicConfigDao {
    private final MongoCollection<Document> collection;

    public AcademicConfigDao() {
        MongoDatabase db = DatabaseConfig.getDatabase();
        this.collection = db.getCollection("academic_configs");
    }

    public AcademicConfig getConfig() {
        Document doc = collection.find(Filters.eq("configName", "DEFAULT_CONFIG")).first();
        if (doc == null) {
            AcademicConfig defaultConfig = new AcademicConfig();
            saveConfig(defaultConfig);
            return defaultConfig;
        }
        return AcademicConfig.fromDocument(doc);
    }

    public void saveConfig(AcademicConfig config) {
        config.setConfigName("DEFAULT_CONFIG");
        Document doc = config.toDocument();
        collection.replaceOne(
            Filters.eq("configName", "DEFAULT_CONFIG"),
            doc,
            new ReplaceOptions().upsert(true)
        );
    }
}
