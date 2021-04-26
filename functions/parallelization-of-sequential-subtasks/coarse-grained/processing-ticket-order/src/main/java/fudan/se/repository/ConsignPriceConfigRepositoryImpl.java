package fudan.se.repository;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import fudan.se.entity.ConsignPrice;
import fudan.se.util.JsonUtils;
import org.bson.Document;

import java.util.Collections;

import static com.mongodb.client.model.Filters.eq;

public class ConsignPriceConfigRepositoryImpl implements ConsignPriceConfigRepository{
    PreserveRepository preserveRepository=PreserveRepository.getInstance();

    private final MongoDatabase database = preserveRepository.mongoClient.getDatabase("consign-price");
    private final MongoCollection<Document> collection = database.getCollection("consign-price");

    @Override
    public ConsignPrice findByIndex(int index){
        Document resDoc = collection.find(eq("index", index)).first();
        if (resDoc == null)
            return null;
        resDoc.remove("_id");
        ConsignPrice resConfig = JsonUtils.json2Object(resDoc.toJson(), ConsignPrice.class);
        return resConfig;
    }

}
