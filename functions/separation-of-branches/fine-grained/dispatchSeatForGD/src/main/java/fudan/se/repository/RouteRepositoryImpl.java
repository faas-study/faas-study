package fudan.se.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import fudan.se.entity.Route;
import fudan.se.util.JsonUtils;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class RouteRepositoryImpl implements RouteRepository {
    DBconnecter connecter = DBconnecter.getInstance();
    private final MongoDatabase database = connecter.mongoClient.getDatabase("route");
    private final MongoCollection<Document> collection = database.getCollection("route");


    public Route findById(String id) {
        Document resDoc = collection.find(eq("id", id)).first();
        if (resDoc == null) {
            return null;
        }
        resDoc.remove("_id");
        return JsonUtils.json2Object(resDoc.toJson(), Route.class);
    }
}
