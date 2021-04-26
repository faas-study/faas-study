package faas.study.repository;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import faas.study.entity.Order;
import faas.study.util.DateUtils;
import faas.study.util.JsonUtils;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static com.mongodb.client.model.Filters.and;
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
    public ArrayList<Order> findByTravelDateAndTrainNumber(Date travelDate, String trainNumber) {
        ArrayList<Order> orders = new ArrayList<>();
        Document tempDoc;
        long travelDateL = DateUtils.dateToMillisecond(travelDate);
        MongoCursor<Document> cursor = collection.find(and(eq("travelDate", travelDateL), eq("trainNumber", trainNumber))).iterator();
        try {
            while (cursor.hasNext()) {
                tempDoc = cursor.next();
                tempDoc.remove("_id");
                String json = tempDoc.toJson(JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build());
                orders.add(JsonUtils.json2Object(json, Order.class));
            }
        } finally {
            cursor.close();
        }

        return orders;
    }
}
