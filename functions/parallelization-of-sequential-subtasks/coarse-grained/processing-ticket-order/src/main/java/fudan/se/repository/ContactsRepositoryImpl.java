package fudan.se.repository;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import fudan.se.entity.Contacts;
import fudan.se.util.JsonUtils;
import org.bson.Document;

import java.util.Collections;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class ContactsRepositoryImpl implements ContactsRepository {
    PreserveRepository preserveRepository=PreserveRepository.getInstance();
    private final MongoDatabase database = preserveRepository.mongoClient.getDatabase("contacts");
    private final MongoCollection<Document> collection = database.getCollection("contacts");


    @Override
    public Contacts findById(UUID id) {
        Document resDoc = collection.find(eq("id", id.toString())).first();
        if (resDoc == null)
            return null;
        resDoc.remove("_id");
        Contacts resContact = JsonUtils.json2Object(resDoc.toJson(), Contacts.class);
        return resContact;
    }
}
