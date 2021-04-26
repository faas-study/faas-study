package fudan.se.repository;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import fudan.se.entity.Order;
import fudan.se.util.JsonUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class OrderRepositoryImpl implements OrderRepository {

    private final String databaseURL = System.getenv("DATABASE_HOST");
    private final String sourceDatabase = System.getenv("SOURCE_DATABASE");
    private final String username = System.getenv("USERNAME");
    private final String password = System.getenv("PASSWORD");


    private final MongoCredential mongoCredential = MongoCredential.createCredential(username, sourceDatabase, password.toCharArray());
    private final MongoClient mongoClient = MongoClients.create(
            MongoClientSettings.builder()
                    .applyToClusterSettings(builder ->
                            builder.hosts(Collections.singletonList(new ServerAddress(databaseURL, 27017))))
                    .credential(mongoCredential)
                    .build());
    private final MongoDatabase database = mongoClient.getDatabase("order");
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
