package fudan.se.repository;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import fudan.se.entity.FoodOrder;
import fudan.se.util.JsonUtils;
import org.bson.Document;

import java.util.Collections;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;


public class FoodOrderRepository {

    PreserveRepository preserveRepository=PreserveRepository.getInstance();

    private final MongoDatabase database = preserveRepository.mongoClient.getDatabase("food");
    private final MongoCollection<Document> collection = database.getCollection("food");

    public void save(FoodOrder foodOrder) {
        Document doc = new Document(JsonUtils.object2Map(foodOrder));
        collection.insertOne(doc);
    }

    public FoodOrder findByOrderId(UUID orderId) {
        Document resDoc = collection.find(eq("orderId", orderId.toString())).first();
        if (resDoc == null)
            return null;
        resDoc.remove("_id");
        return JsonUtils.json2Object(resDoc.toJson(), FoodOrder.class);
    }

    public static void main(String[] args) {
        FoodOrderRepository foodOrderRepository = new FoodOrderRepository();
        FoodOrder foodOrder = foodOrderRepository.findByOrderId(UUID.fromString("ab4d7c83-9ca5-41b2-ae1b-422e2554f5c8"));
        System.out.println(foodOrder);
    }
}
