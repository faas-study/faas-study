package fudan.se.repository;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import fudan.se.entity.Trip;
import fudan.se.entity.TripId;
import fudan.se.util.JsonUtils;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.util.Collections;

public class Trip2RepositoryImpl implements Trip2Repository {
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
    private final MongoDatabase database = mongoClient.getDatabase("travel2");
    private final MongoCollection<Document> collection = database.getCollection("travel2");

    @Override
    public Trip findByTripId(TripId tripId) {
        MongoCursor<Document> cursor = collection.find().iterator();
        Document tempDoc;
        Trip resTrip = null;
        Document tempTripIdDoc;

        try {
            while (cursor.hasNext()) {
                tempDoc = cursor.next();
                tempTripIdDoc = (Document) tempDoc.get("tripId");
                if (tempTripIdDoc.get("type").equals(tripId.getType().getName()) && tempTripIdDoc.get("number").equals(tripId.getNumber())) {
                    tempDoc.remove("_id");
                    String json = tempDoc.toJson(JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build());
                    resTrip = JsonUtils.json2Object(json, Trip.class);
                    break;
                }

            }
        } finally {
            cursor.close();
        }

        if (resTrip == null)
            return null;
        return resTrip;
    }
}

