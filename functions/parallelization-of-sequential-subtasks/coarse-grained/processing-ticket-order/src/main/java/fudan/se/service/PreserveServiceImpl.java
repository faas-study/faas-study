package fudan.se.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import fudan.se.entity.*;
import fudan.se.repository.*;
import fudan.se.util.JsonUtils;
import fudan.se.util.Response;
import fudan.se.util.StationInfo;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;



public class PreserveServiceImpl implements PreserveService {

    private final AssuranceRepository assuranceRepository = new AssuranceRepositoryImpl();
    private final ConsignRepository consignRepository = new ConsignRepositoryImpl();
    private final FoodOrderRepository foodOrderRepository = new FoodOrderRepository();

    private static final String getPriceByWeightAndRegion = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-price-by-weight-and-region";
    private static final String getContactsByIdURL = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-contacts-by-contactsid";
    private static final String createOrderURL = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/create-order";

    private final static int NUMBEROFLOOPS = Integer.parseInt(System.getenv().getOrDefault("NUMBEROFLOOPS", "1"));
    private final static int NUMBEROFCOMPUTATION = Integer.parseInt(System.getenv().getOrDefault("NUMBEROFCOMPUTATION", "1000000"));

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS).build();

    String success = "Success";

    @Override
    public Response preserve(OrderTicketsInfo oti, Context context) {
        LambdaLogger logger = context.getLogger();
        int seatTotal = 100;
        // 1. find contacts
        Response<Contacts> gcr = getContactsById(oti.getContactsId());
        if (gcr.getStatus() == 0) {
            logger.log("[preserveTickets] - Get contacts Fail: " + gcr.getMsg());
            return new Response(0, gcr.getMsg(), null);
        }
        logger.log("[preserveTickets] - Get contacts succeed.");

        // 2. create order
        logger.log("[preserveTickets] - Create order start");
        Contacts contacts = JsonUtils.conveterObject(gcr.getData(), Contacts.class);

        Order order = new Order();
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        order.setId(orderId);
        order.setTrainNumber(oti.getTripId());
        order.setAccountId(accountId);

        String fromStationId = queryForStationId(oti.getFrom());
        String toStationId = queryForStationId(oti.getTo());

        order.setFrom(fromStationId);
        order.setTo(toStationId);
        order.setBoughtDate(new Date());
        order.setStatus(OrderStatus.NOTPAID.getCode());
        order.setContactsDocumentNumber(contacts.getDocumentNumber());
        order.setContactsName(contacts.getName());
        order.setDocumentType(contacts.getDocumentType());

        order.setSeatClass(oti.getSeatType());
        logger.log("[Preserve Service][Order] Order Travel Date: " + oti.getDate().toString());
        order.setTravelDate(oti.getDate());
        //order.setTravelTime(gtdr.getTripResponse().getStartingTime());

        // dispatch the seat
        order.setSeatNumber("" + new Random().nextInt(seatTotal));
        order.setSeatClass(SeatClass.SECONDCLASS.getCode());
        order.setPrice(String.valueOf(95));

        Response<Order> cor = createOrder(order, logger);
        Order reOrder = JsonUtils.conveterObject(cor.getData(), Order.class);

        if (cor.getStatus() == 0) {
            logger.log("[preserveTickets] - Create order fail: " + cor.getMsg());
            return new Response(0, cor.getMsg(), null);
        }
        logger.log("[preserveTickets] - Create order succeed.");

        Response returnResponse = new Response(1, "Success", cor.getMsg());
//        Response returnResponse = new Response(1, "Success", null);
        // 3. create assurance
        if (oti.getAssurance() == 0) {
            logger.log("[preserveTickets] - Do not need to buy assurance. ");
        } else {
            logger.log("[preserveTickets][Step 5] - Start to buy assurance.");
            Response<Assurance> addAssuranceResult = addAssuranceForOrder(
                    oti.getAssurance(), reOrder.getId().toString(), logger);
            if (addAssuranceResult.getStatus() == 1) {
                logger.log("[preserveTicket][Step 5] Buy Assurance Success");
            } else {
                logger.log("[preserveTicket][Step 5] Buy Assurance Fail.");
                returnResponse.setMsg("Success.But Buy Assurance Fail.");
            }
        }
        // 4. create food
        if (oti.getFoodType() != 0) {
            logger.log("[preserveTicket][Step 6] - Start to buy food.");
            FoodOrder foodOrder = new FoodOrder();
            foodOrder.setOrderId(reOrder.getId());
            foodOrder.setFoodType(oti.getFoodType());
            foodOrder.setFoodName(oti.getFoodName());
            foodOrder.setPrice(oti.getFoodPrice());

            if (oti.getFoodType() == 2) {
                foodOrder.setStationName(oti.getStationName());
                foodOrder.setStoreName(oti.getStoreName());
                logger.log("[Food Service]!!!!!!!!!!!!!!!foodstore= " + foodOrder.getFoodType() + " " + foodOrder.getStationName() + " " + foodOrder.getStoreName());

            }
            Response afor = createFoodOrder(foodOrder, logger);
            if (afor.getStatus() == 1) {
                logger.log("[Preserve Service][Step 6] Buy Food Success");
            } else {
                logger.log("[Preserve Service][Step 6] Buy Food Fail.");
                returnResponse.setMsg("Success.But Buy Food Fail.");
            }
        } else {
            logger.log("[Preserve Service][Step 6] Do not need to buy food");
        }

        // 5. create consign
        if (null != oti.getConsigneeName() && !"".equals(oti.getConsigneeName())) {
            logger.log("[preserveTicket][Step 7] Start to buy consign.");
            Consign consignRequest = new Consign();
            consignRequest.setOrderId(reOrder.getId());
            consignRequest.setAccountId(reOrder.getAccountId());
            consignRequest.setHandleDate(oti.getHandleDate());
            consignRequest.setTargetDate(reOrder.getTravelDate().toString());
            consignRequest.setFrom(reOrder.getFrom());
            consignRequest.setTo(reOrder.getTo());
            consignRequest.setConsignee(oti.getConsigneeName());
            consignRequest.setPhone(oti.getConsigneePhone());
            consignRequest.setWeight(oti.getConsigneeWeight());
            consignRequest.setWithin(oti.isWithin());
            logger.log("[preserveTickets][Step 7] - CONSIGN INFO : " + consignRequest.toString());
            Response icresult = createConsign(consignRequest, logger);
            if (icresult.getStatus() == 1) {
                logger.log("[preserveTickets][Step 7] Consign Success");
            } else {
                logger.log("[preserveTickets][Step 7] Consign Fail.");
                returnResponse.setMsg("Consign Fail.");
            }
        } else {
            logger.log("[Preserve Service][Step 7] Do not need to consign");
        }
        logger.log("[preserveTickets][Step 8] Computation start...");
        for (int i = 0; i < NUMBEROFLOOPS; i++) {
            computeTask();
        }
        logger.log("[preserveTickets][Step 8] Finish computation.");

        return returnResponse;
    }

    private void computeTask() {
        Random random = new Random();
        int num = NUMBEROFCOMPUTATION;
        double rd;
        while (num-- > 0) {
            rd = random.nextDouble();
            Math.tan(rd);
        }
    }

    private Response createFoodOrder(FoodOrder addFoodOrder, LambdaLogger logger) {
        FoodOrder fo = foodOrderRepository.findByOrderId(addFoodOrder.getOrderId());
        if (fo != null) {
            logger.log("[Food Service][CreateFoodOrder] Order Id: " + addFoodOrder.getOrderId() + " has existed");
            return new Response(0, "Order Id has existed", null);
        }
        fo = new FoodOrder();
        fo.setId(UUID.randomUUID());
        fo.setOrderId(addFoodOrder.getOrderId());
        fo.setFoodType(addFoodOrder.getFoodType());
        if (addFoodOrder.getFoodType() == 2) {
            fo.setStationName(addFoodOrder.getStationName());
            fo.setStoreName(addFoodOrder.getStoreName());
        }
        fo.setFoodName(addFoodOrder.getFoodName());
        fo.setPrice(addFoodOrder.getPrice());
        foodOrderRepository.save(fo);
        logger.log("[Food-Service][AddFoodOrder] Success.");
        return new Response<>(1, "Success", fo);
    }

    private Response createConsign(Consign consignRequest, LambdaLogger logger) {
        logger.log("[Consign service] [ Insert new consign record]" + consignRequest.getOrderId());

        ConsignRecord consignRecord = new ConsignRecord();
        //Set the record attribute
        consignRecord.setId(UUID.randomUUID());
        logger.log("[Consign service] Order ID is :" + consignRequest.getOrderId());
        consignRecord.setOrderId(consignRequest.getOrderId());
        consignRecord.setAccountId(consignRequest.getAccountId());

        logger.log("[Consign service] The handle date is {}" + consignRequest.getHandleDate());
        logger.log("[Consign service] The target date is {}" + consignRequest.getTargetDate());
        consignRecord.setHandleDate(consignRequest.getHandleDate());
        consignRecord.setTargetDate(consignRequest.getTargetDate());
        consignRecord.setFrom(consignRequest.getFrom());
        consignRecord.setTo(consignRequest.getTo());
        consignRecord.setConsignee(consignRequest.getConsignee());
        consignRecord.setPhone(consignRequest.getPhone());
        consignRecord.setWeight(consignRequest.getWeight());

        //get the price
        double price = getPriceByWeightAndRegion(consignRequest.getWeight(), consignRequest.isWithin());
        consignRecord.setPrice(price);

        logger.log("[Consign service][getPrice]" + price);

        logger.log("[Consign service] SAVE consign info : " + consignRecord.toString());
        consignRepository.save(consignRecord);
        logger.log("[Consign service] SAVE consign result : " + consignRecord.toString());
        return new Response<>(1, "You have consigned successfully! The price is " + consignRecord.getPrice(), consignRecord);
    }

    public double  getPriceByWeightAndRegion(double weight, boolean isWithinRegion) {
        String ret = "";
        try {
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(getPriceByWeightAndRegion + "?weight=" + weight + "&isWithinRegion=" + isWithinRegion)
                    .get()
                    .build();
            okhttp3.Response res = client.newCall(request).execute();
            ret = res.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Response<Double> result = JsonUtils.json2Object(ret, Response.class);
        return result.getData();
    }


    private Response<Assurance> addAssuranceForOrder(int assuranceType, String orderId, LambdaLogger logger) {
        Assurance a = assuranceRepository.findByOrderId(UUID.fromString(orderId));
        AssuranceType at = AssuranceType.getTypeByIndex(assuranceType);
        if (a != null) {
            logger.log("[Assurance-Add&Delete-Service][AddAssurance] Fail.Assurance already exists");
            return new Response<>(0, "Fail.Assurance already exists", null);
        } else if (at == null) {
            logger.log("[Assurance-Add&Delete-Service][AddAssurance] Fail.Assurance type doesn't exist");
            return new Response<>(0, "Fail.Assurance type doesn't exist", null);
        } else {
            Assurance assurance = new Assurance(UUID.randomUUID(), UUID.fromString(orderId), at);
            assuranceRepository.save(assurance);
            logger.log("[Assurance-Add&Delete-Service][AddAssurance] Success.");
            return new Response<>(1, "Success", assurance);
        }
    }

    private Response<Order> createOrder(Order order, LambdaLogger logger) {
        String ret = "";
        String json = JsonUtils.object2Json(order);
        try {
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
            Request request = new Request.Builder()
                    .url(createOrderURL)
                    .post(body)
                    .build();
            okhttp3.Response response = client.newCall(request).execute();
            ret = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonUtils.json2Object(ret, Response.class);
    }

    private String queryForStationId(String from) {
        return StationInfo.stationsInfo.get(from);
    }

    private Response<Contacts> getContactsById(String contactsId) {
        String ret = "";
        try {
            Request request = new Request.Builder()
                    .url(getContactsByIdURL + "?contactsId=" + contactsId)
                    .get()
                    .build();
            okhttp3.Response response = client.newCall(request).execute();
            ret = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonUtils.json2Object(ret, Response.class);
    }

}
