package fudan.se.service;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import fudan.se.entity.*;
import fudan.se.util.JsonUtils;
import fudan.se.util.Response;
import fudan.se.util.StationInfo;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;




public class PreserveServiceImpl implements PreserveService {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS).build();

    private static final String getContactsByIdURL = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-contacts-by-contactsid";
    private static final String createOrderURL = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/create-order";
    private static final String addAssuranceForOrderURL = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/add-assurance-for-order";
    private static final String createFoodOrderURL = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/create-food-order";
    private static final String createConsignURL = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/insert-consign-record";

    @Override
    public Response preserve(OrderTicketsInfo oti, Context context) {
        LambdaLogger logger = context.getLogger();
        int seatTotal = 100;
        // 1. find contacts
        Response<Contacts> gcr = getContactsById(oti.getContactsId());
        if (gcr.getStatus() == 0) {
            logger.log("[preserveTickets v2] - Get contacts Fail: " + gcr.getMsg());
            return new Response(0, gcr.getMsg(), null);
        }
        logger.log("[preserveTickets v2] - Get contacts succeed.");
        // 2. create order
        logger.log("[preserveTickets v2] - Create order start");
        Contacts contacts=JsonUtils.conveterObject(gcr.getData(),Contacts.class);

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
        logger.log("[preserveTickets v2][Order] Order Travel Date: " + oti.getDate().toString());
        order.setTravelDate(oti.getDate());
        //order.setTravelTime(gtdr.getTripResponse().getStartingTime());

        // dispatch the seat
        order.setSeatNumber("" + new Random().nextInt(seatTotal));
        order.setSeatClass(SeatClass.SECONDCLASS.getCode());
        order.setPrice(String.valueOf(95));

        Response<Order> cor = createOrder(order);
        Order reOrder=JsonUtils.conveterObject(cor.getData(),Order.class);

        if (cor.getStatus() == 0) {
            logger.log("[preserveTickets v2] - Create order fail: " + cor.getMsg());
            return new Response(0, cor.getMsg(), null);
        }
        logger.log("[preserveTickets v2] - Create order succeed.");

        Response returnResponse  = new Response(1, "Success", cor.getMsg());
        ExecutorService exec = Executors.newCachedThreadPool();
        // 3. create assurance
        Future<String> createAssuranceResult = exec.submit(new CreateAssurance(oti, reOrder, context));
        // 4. create food
        Future<String> createFoodResult = exec.submit(new CreateFood(oti, reOrder, context));
        // 5. create consign
        Future<String> createConsignResult = exec.submit(new CreateConsign(oti, reOrder, context));
        // 6, 7, create Food.
        Future<String> createFoodResult2 = exec.submit(new CreateFood(oti, reOrder, context));
        Future<String> createFoodResult3 = exec.submit(new CreateFood(oti, reOrder, context));


        // get result.
        StringBuilder returnMsg = new StringBuilder("Success. ");
        try {
            returnMsg.append(createAssuranceResult.get());
            returnMsg.append(createFoodResult.get());
            returnMsg.append(createConsignResult.get());
            createFoodResult2.get();
            createFoodResult3.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        returnResponse.setMsg(returnMsg.toString());
        return returnResponse;
    }

    private Response<Order> createOrder(Order order) {
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

    class CreateAssurance implements Callable<String> {
        private final OrderTicketsInfo oti;
        private final Order reOrder;
        private final Context context;


        public CreateAssurance(OrderTicketsInfo oti, Order reOrder, Context context) {
            this.oti = oti;
            this.reOrder = reOrder;
            this.context = context;
        }

        @Override
        public String call() throws Exception {
            LambdaLogger logger = context.getLogger();
            String returnString;
            if (oti.getAssurance() == 0) {
                logger.log("[preserveTickets v2][Step 5] - Do not need to buy assurance. ");
                returnString = "Do not need to buy assurance. ";
            }
            else {
                logger.log("[preserveTickets v2][Step 5] - Start to buy assurance.");
                Response<Assurance> addAssuranceResult = addAssuranceForOrder(
                        oti.getAssurance(), reOrder.getId().toString());
                if (addAssuranceResult.getStatus() == 1) {
                    logger.log("[preserveTickets v2][Step 5] Buy Assurance Success");
                    returnString = "Buy Assurance Success. ";
                } else {
                    logger.log("[preserveTickets v2][Step 5] Buy Assurance Fail.");
                    returnString = "Buy Assurance Failed. ";
                }
            }
            return returnString;
        }

        private Response<Assurance> addAssuranceForOrder(int assuranceType, String orderId) {
            String ret = "";
            try {
                Request request = new Request.Builder()
                        .url(addAssuranceForOrderURL + "?typeIndex=" + assuranceType + "&orderId=" + orderId)
                        .get()
                        .build();
                okhttp3.Response response = client.newCall(request).execute();
                ret = response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
                return new Response<>(0, "Create Assurance Failed.", null);
            }
            return JsonUtils.json2Object(ret, Response.class);
        }
    }

    class CreateFood implements Callable<String> {
        private final OrderTicketsInfo oti;
        private final Order reOrder;
        private final Context context;

        public CreateFood(OrderTicketsInfo oti,Order reOrder, Context context) {
            this.oti = oti;
            this.reOrder = reOrder;
            this.context = context;
        }

        @Override
        public String call() throws Exception {
            LambdaLogger logger = context.getLogger();
            String returnString;
            if (oti.getFoodType() != 0) {
                logger.log("[preserveTickets v2][Step 6] Start to buy food. ");
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
                Response afor = createFoodOrder(foodOrder);
                if (afor.getStatus() == 1) {
                    logger.log("[preserveTickets v2][Step 6] Buy Food Success");
                    returnString = "Buy Food Success. ";
                } else {
                    logger.log("[preserveTickets v2][Step 6] Buy Food Fail.");
                    returnString = "Buy Food Fail. ";
                }
            } else {
                logger.log("[preserveTickets v2][Step 6] Do not need to buy food");
                returnString = "Do not need to buy food. ";
            }
            return returnString;
        }

        private Response createFoodOrder(FoodOrder foodOrder) {
            String ret = "";
            String json = JsonUtils.object2Json(foodOrder);
            try {
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"), json);

                Request request = new Request.Builder()
                        .url(createFoodOrderURL)
                        .post(body)
                        .build();
                okhttp3.Response response = client.newCall(request).execute();
                ret = response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return JsonUtils.json2Object(ret, Response.class);
        }
    }

    class CreateConsign implements Callable<String> {
        private final OrderTicketsInfo oti;
        private final Order reOrder;
        private final Context context;



        public CreateConsign(OrderTicketsInfo oti, Order reOrder, Context context) {
            this.oti = oti;
            this.reOrder = reOrder;
            this.context = context;
        }
        @Override
        public String call() throws Exception {
            LambdaLogger logger = context.getLogger();
            String returnString;
            if (null != oti.getConsigneeName() && !"".equals(oti.getConsigneeName())) {
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
                logger.log("[preserveTickets v2][Step 7] - CONSIGN INFO : " +consignRequest.toString());
                Response icresult = createConsign(consignRequest);
                if (icresult.getStatus() == 1) {
                    logger.log("[preserveTickets v2][Step 7] Consign Success");
                    returnString = "Create Consign Success. ";
                } else {
                    logger.log("[preserveTickets v2][Step 7] Consign Fail.");
                    returnString = "Create Consign Fail. ";
                }
            }
            else {
                logger.log("[preserveTickets v2][Step 7] Do not need to consign");
                returnString = "Do not need to consign. ";
            }
            return returnString;
        }


        private Response createConsign(Consign consignRequest) {
            String ret = "";
            String json = JsonUtils.object2Json(consignRequest);
            try {
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"), json);

                Request request = new Request.Builder()
                        .url(createConsignURL)
                        .post(body)
                        .build();
                okhttp3.Response response = client.newCall(request).execute();
                ret = response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return JsonUtils.json2Object(ret, Response.class);
        }
    }
}





