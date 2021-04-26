package fudan.se.repository;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import fudan.se.entity.ConsignRecord;
import fudan.se.util.JsonUtils;
import org.bson.Document;

import java.util.Collections;

public class ConsignRepositoryImpl implements ConsignRepository {
    PreserveRepository preserveRepository=PreserveRepository.getInstance();
    private final MongoDatabase database = preserveRepository.mongoClient.getDatabase("assurance");
    private final MongoCollection<Document> collection = database.getCollection("assurance");


    @Override
    public void save(ConsignRecord consignRecord) {
        Document doc = new Document(JsonUtils.object2Map(consignRecord));
        collection.insertOne(doc);
    }
}
