package fudan.se.repository;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.mongodb.client.*;
import fudan.se.entity.Order;
import fudan.se.util.DateUtils;
import fudan.se.util.JsonUtils;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.util.ArrayList;
import java.util.Date;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class OrderRepositoryImpl implements OrderRepository {
    DBconnecter connecter = DBconnecter.getInstance();
    private final MongoDatabase database = connecter.mongoClient.getDatabase("order");
    private final MongoCollection<Document> collection = database.getCollection("order");


    @Override
    public ArrayList<Order> findByTravelDateAndTrainNumber(Date travelDate, String trainNumber, Context context) {
        ArrayList<Order> result = new ArrayList<>();
        LambdaLogger logger = context.getLogger();

        Document tempDoc;
        long travelDateL = DateUtils.dateToMillisecond(travelDate);

        MongoCursor<Document> cursor = collection.find(and(eq("travelDate", travelDateL), eq("trainNumber", trainNumber))).iterator();
        try {
            while (cursor.hasNext()) {
                tempDoc = cursor.next();
                tempDoc.remove("_id");

                /*
                Mongodb读取document时，如果存在long的话，导出的json中会有"$numberLong"字段阻碍Jackson对对象进行反序列化
                因此使用RELAXED的模式调用toJson()函数，以忽略多余的字段
                 */
                String json = tempDoc.toJson(JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build());
                logger.log("[getSoldTickets] - get document from mongodb:" + json);

                result.add(JsonUtils.json2Object(json, Order.class));
            }
        } finally {
            cursor.close();
        }

        return result;
    }
}
