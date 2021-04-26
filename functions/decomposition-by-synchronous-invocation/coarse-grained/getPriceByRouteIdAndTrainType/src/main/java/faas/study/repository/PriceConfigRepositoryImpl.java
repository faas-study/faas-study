package faas.study.repository;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import faas.study.entity.PriceConfig;
import faas.study.util.JsonUtils;
import org.bson.Document;

import java.util.Collections;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class PriceConfigRepositoryImpl implements PriceConfigRepository {
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
    private final MongoDatabase database = mongoClient.getDatabase("price");
    private final MongoCollection<Document> collection = database.getCollection("price");


    @Override
    public PriceConfig findByRouteIdAndTrainType(String routeId, String trainType) {
        Document resDoc = collection.find(and(eq("routeId", routeId), eq("trainType", trainType))).first();
        if (resDoc == null)
            return null;
        resDoc.remove("_id");
        PriceConfig resPriceConfig = JsonUtils.json2Object(resDoc.toJson(), PriceConfig.class);
        return resPriceConfig;
    }
}
