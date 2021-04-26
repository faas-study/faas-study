package fudan.se.repository;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import fudan.se.entity.Trip;
import fudan.se.entity.TripId;
import fudan.se.util.JsonUtils;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class TripRepositoryImpl implements TripRepository {

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
    private final MongoDatabase database = mongoClient.getDatabase("travel");
    private final MongoCollection<Document> collection = database.getCollection("travel");

    @Override
    public ArrayList<Trip> findAll(Context context) {
        ArrayList<Trip> trips = new ArrayList<>();
        LambdaLogger logger = context.getLogger();

        logger.log("trips find all");
        MongoCursor<Document> cursor = collection.find().iterator();

        try {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                document.remove("_id");

                String json = document.toJson(JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build());
                logger.log("[getTripsLeft] get trip documents from mongo:" + json);

                trips.add(JsonUtils.json2Object(json, Trip.class));
            }
        } finally {
            cursor.close();
        }

        return trips;
    }
}
