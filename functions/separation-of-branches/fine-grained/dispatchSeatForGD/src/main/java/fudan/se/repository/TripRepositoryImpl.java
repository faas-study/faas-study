package fudan.se.repository;

import com.mongodb.client.*;
import fudan.se.entity.Trip;
import fudan.se.entity.TripId;
import fudan.se.util.JsonUtils;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

public class TripRepositoryImpl implements TripRepository {
    DBconnecter connecter = DBconnecter.getInstance();
    private final MongoDatabase database = connecter.mongoClient.getDatabase("travel");
    private final MongoCollection<Document> collection = database.getCollection("travel");

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

