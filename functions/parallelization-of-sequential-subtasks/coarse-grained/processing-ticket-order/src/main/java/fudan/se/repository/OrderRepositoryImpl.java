package fudan.se.repository;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import fudan.se.entity.Order;
import fudan.se.service.PreserveService;
import fudan.se.util.JsonUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class OrderRepositoryImpl implements OrderRepository {
    PreserveRepository preserveRepository = PreserveRepository.getInstance();
    private final MongoDatabase database = preserveRepository.mongoClient.getDatabase("order");
    private final MongoCollection<Document> collection = database.getCollection("order");


    @Override
    public ArrayList<Order> findByAccountId(UUID accountId) {
        ArrayList<Order> orders = new ArrayList<>();
        Document tempDoc;
        MongoCursor<Document> cursor = collection.find(eq("accountId", accountId.toString())).iterator();
        try {
            while (cursor.hasNext()) {
                tempDoc = cursor.next();
                tempDoc.remove("_id");
                orders.add(JsonUtils.json2Object(tempDoc.toJson(), Order.class));
            }
        } finally {
            cursor.close();
        }
        return orders;
    }

    @Override
    public void save(Order order) {
        Document doc = new Document(JsonUtils.object2Map(order));
        collection.insertOne(doc);
    }
}
