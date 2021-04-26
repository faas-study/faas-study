package fudan.se.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import fudan.se.entity.*;
import fudan.se.util.JsonUtils;
import fudan.se.util.Response;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;


public class SeatServiceImpl implements SeatService {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS).build();

    private static final String getRouteByTripId2 = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-route-by-tripid-2";
    private static final String getSoldTickets2 = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-sold-tickets-2";
    private static final String getTrainTypeByTripId2 = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-traintype-by-tripid-2";

    @Override
    public Response distributeSeat(Seat seatRequest, Context context) {
        LambdaLogger logger = context.getLogger();

        Response<Route> routeResult;

        LeftTicketInfo leftTicketInfo;
        TrainType trainTypeResult;

        //Distinguish G\D from other trains
        String trainNumber = seatRequest.getTrainNumber();

        if (trainNumber.startsWith("G") || trainNumber.startsWith("D")) {
            logger.log("[distributeSeatForOthers] TrainNumber start with G|D");
            return new Response(0, "Input trainNumber is G\\D", seatRequest.getTrainNumber());
        }


        logger.log("[distributeSeatForOthers] TrainNumber start with other capital");
        //Call the micro service to query all the station information for the trains
        String ret = "";
        try {
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(getRouteByTripId2 + "?tripId=" + trainNumber)
                    .get()
                    .build();

            okhttp3.Response response = client.newCall(request).execute();
            ret = response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
        }
        routeResult = JsonUtils.json2Object(ret, Response.class);
        logger.log("[SeatService distributeSeat] The result of getRouteResult is {" + routeResult.getMsg() + "}");


        //Call the microservice to query for residual Ticket information: the set of the Ticket sold for the specified seat type
        String json = JsonUtils.object2Json(seatRequest);
        try {
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), json);

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(getSoldTickets2)
                    .post(body)
                    .build();

            okhttp3.Response response = client.newCall(request).execute();
            ret = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Response mRes = JsonUtils.json2Object(ret, Response.class);
        logger.log("Left ticket info is : {" + mRes.toString() + "}");
        leftTicketInfo = JsonUtils.conveterObject(mRes.getData(), LeftTicketInfo.class);


        //Calls the microservice to query the total number of seats specified for that vehicle


        try {
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(getTrainTypeByTripId2 + "?tripId=" + seatRequest.getTrainNumber())
                    .get()
                    .build();

            okhttp3.Response response = client.newCall(request).execute();
            ret = response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
        }
        mRes = JsonUtils.json2Object(ret, Response.class);
        trainTypeResult = JsonUtils.conveterObject(mRes.getData(), TrainType.class);

        logger.log("[SeatService distributeSeat 2] The result of getTrainTypeResult is {" + mRes.toString() + "}");


        //Assign seats
        List<String> stationList = JsonUtils.conveterObject(routeResult.getData(), Route.class).getStations();
        int seatTotalNum = 0;
        if (seatRequest.getSeatType() == SeatClass.FIRSTCLASS.getCode()) {
            seatTotalNum = trainTypeResult.getConfortClass();
            logger.log("[distributeSeatForOthers] The request seat type is confortClass and the total num is {"+seatTotalNum+"}");
        } else {
            seatTotalNum = trainTypeResult.getEconomyClass();
            logger.log("[distributeSeatForOthers] The request seat type is economyClass and the total num is {"+seatTotalNum+"}");

        }
        String startStation = seatRequest.getStartStation();
        Ticket ticket = new Ticket();
        ticket.setStartStation(startStation);
        ticket.setDestStation(seatRequest.getDestStation());

        //Assign new tickets
        Random rand = new Random();
        int range = seatTotalNum;
        int seat = rand.nextInt(range) + 1;

        if (leftTicketInfo != null) {
            Set<Ticket> soldTickets = leftTicketInfo.getSoldTickets();
            //Give priority to tickets already sold
            for (Ticket soldTicket : soldTickets) {
                String soldTicketDestStation = soldTicket.getDestStation();
                //Tickets can be allocated if the sold ticket's end station before the start station of the request
                if (stationList.indexOf(soldTicketDestStation) < stationList.indexOf(startStation)) {
                    ticket.setSeatNo(soldTicket.getSeatNo());
                    logger.log("[distributeSeatForOthers] Use the previous distributed seat number! {"+soldTicket.getSeatNo()+"}");
                    return new Response<>(1, "Use the previous distributed seat number!", ticket);
                }
            }
            while (isContained(soldTickets, seat)) {
                seat = rand.nextInt(range) + 1;
            }
        }
        ticket.setSeatNo(seat);
        //SeatServiceImpl.LOGGER.info("[distributeSeatForOthers] Use a new seat number! {}", seat);
        logger.log("[distributeSeatForOthers] Use a new seat number! {"+seat+"}");
        return new Response<>(1, "Use a new seat number!", ticket);
    }

    private boolean isContained(Set<Ticket> soldTickets, int seat) {
        //Check that the seat number has been used
        boolean result = false;
        for (Ticket soldTicket : soldTickets) {
            if (soldTicket.getSeatNo() == seat) {
                return true;
            }
        }
        return result;
    }

}