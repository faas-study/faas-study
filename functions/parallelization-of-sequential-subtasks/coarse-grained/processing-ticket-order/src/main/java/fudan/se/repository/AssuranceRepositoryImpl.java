package fudan.se.repository;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import fudan.se.entity.Assurance;
import fudan.se.util.JsonUtils;
import org.bson.Document;

import java.util.Collections;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class AssuranceRepositoryImpl implements AssuranceRepository {
    PreserveRepository preserveRepository=PreserveRepository.getInstance();

    private final MongoDatabase database = preserveRepository.mongoClient.getDatabase("assurance");
    private final MongoCollection<Document> collection = database.getCollection("assurance");

    @Override
    public Assurance findByOrderId(UUID orderId){
        Document resDoc = collection.find(eq("orderId", orderId.toString())).first();
        if (resDoc == null)
            return null;

        resDoc.remove("_id");
        Assurance resAssurance = JsonUtils.json2Object(resDoc.toJson(), Assurance.class);
        return resAssurance;
    }

    @Override
    public void save(Assurance assurance) {
        Document doc = new Document(JsonUtils.object2Map(assurance));
        collection.insertOne(doc);
    }
}
