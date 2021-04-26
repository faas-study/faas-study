package fudan.se.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import fudan.se.entity.TrainType;
import fudan.se.util.JsonUtils;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class TrainTypeRepositoryImpl implements TrainTypeRepository {
    DBconnecter connecter = DBconnecter.getInstance();
    private final MongoDatabase database = connecter.mongoClient.getDatabase("train");
    private final MongoCollection<Document> collection = database.getCollection("train");


    public TrainType findById(String id) {
        Document resDoc = collection.find(eq("id", id)).first();
        if (resDoc == null)
            return null;
        resDoc.remove("_id");
        TrainType resTrainType = JsonUtils.json2Object(resDoc.toJson(), TrainType.class);
        return resTrainType;
    }
}
