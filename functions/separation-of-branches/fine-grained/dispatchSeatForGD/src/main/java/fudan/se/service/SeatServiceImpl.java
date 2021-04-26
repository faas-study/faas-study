package fudan.se.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import fudan.se.entity.*;
import fudan.se.repository.*;
import fudan.se.util.JsonUtils;
import fudan.se.util.Response;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;


import java.util.*;
import java.util.concurrent.*;


public class SeatServiceImpl implements SeatService {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS).build();


    private static final String getRouteByTripId = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-route-by-tripid";
    private static final String getSoldTickets = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-sold-tickets";
    private static final String getTrainTypeByTripId = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-traintype-by-tripid";


    String success = "Success";
    String noContent = "No Content";


    @Override
    public Response distributeSeatForGD(Seat seatRequest, Context context) {

        LambdaLogger logger = context.getLogger();

        Response<Route> routeResult;

        LeftTicketInfo leftTicketInfo;
        TrainType trainTypeResult;

        //Distinguish G\D from other trains
        String trainNumber = seatRequest.getTrainNumber();
        logger.log("[SeatService distributeSeat] TrainNumber:" + trainNumber);

        if (!trainNumber.startsWith("G") && !trainNumber.startsWith("D")) {
            logger.log("[SeatService] TrainNumber start with other capital");
            return new Response(0, "Input trainNumber is not G\\D", seatRequest.getTrainNumber());
        }

        logger.log("[SeatService distributeSeat] TrainNumber start with G|D");

        //Call the microservice to query all the station information for the train
//        routeResult = getRouteByTripId(trainNumber);
//        logger.log("[SeatService distributeSeat] The result of getRouteResult is {" + routeResult.getMsg() + "}");
//
//
//        //Call the microservice to query for residual Ticket information: the set of the Ticket sold for the specified seat type
//        Response<LeftTicketInfo> mRes = getSoldTickets(seatRequest, context);
//        logger.log("Left ticket info is : {" + mRes.toString() + "}");
//        leftTicketInfo = JsonUtils.conveterObject(mRes.getData(), LeftTicketInfo.class);
//
//
//        //Calls the microservice to query the total number of seats specified for that vehicle
//        Response<TrainType> trainTypeResponse = getTrainTypeByTripId(seatRequest.getTrainNumber());
//        trainTypeResult = JsonUtils.conveterObject(trainTypeResponse.getData(), TrainType.class);
//
//        logger.log("[SeatService distributeSeat 1] The result of getTrainTypeResult is {" + mRes.getData() + "}");

        //Call the microservice to query all the station information for the train
        String ret = "";
        try {
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(getRouteByTripId + "?tripId=" + trainNumber)
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
                    .url(getSoldTickets)
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
                    .url(getTrainTypeByTripId + "?tripId=" + seatRequest.getTrainNumber())
                    .get()
                    .build();

            okhttp3.Response response = client.newCall(request).execute();
            ret = response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
        }
        mRes = JsonUtils.json2Object(ret, Response.class);
        trainTypeResult = JsonUtils.conveterObject(mRes.getData(), TrainType.class);

        logger.log("[SeatService distributeSeat 1] The result of getTrainTypeResult is {" + mRes.toString() + "}");


        //Assign seats
        List<String> stationList = JsonUtils.conveterObject(routeResult.getData(), Route.class).getStations();
        int seatTotalNum = 0;
        if (seatRequest.getSeatType() == SeatClass.FIRSTCLASS.getCode()) {
            seatTotalNum = trainTypeResult.getConfortClass();
            //SeatServiceImpl.LOGGER.info("[SeatService distributeSeat] The request seat type is confortClass and the total num is {}", seatTotalNum);
        } else {
            seatTotalNum = trainTypeResult.getEconomyClass();
            //SeatServiceImpl.LOGGER.info("[SeatService distributeSeat] The request seat type is economyClass and the total num is {}", seatTotalNum);
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
                    //SeatServiceImpl.LOGGER.info("[SeatService distributeSeat] Use the previous distributed seat number! {}", soldTicket.getSeatNo());
                    return new Response<>(1, "Use the previous distributed seat number!", ticket);
                }
            }
            while (isContained(soldTickets, seat)) {
                seat = rand.nextInt(range) + 1;
            }
        }
        ticket.setSeatNo(seat + "");
        //SeatServiceImpl.LOGGER.info("[SeatService distributeSeat] Use a new seat number! {}", seat);
        return new Response<>(1, "Use a new seat number!", ticket);
    }

    private boolean isContained(Set<Ticket> soldTickets, int seat) {
        //Check that the seat number has been used
        boolean result = false;
        for (Ticket soldTicket : soldTickets) {
            if (soldTicket.getSeatNo().equals(seat + "")) {
                return true;
            }
        }
        return result;
    }

//    public Response<LeftTicketInfo> getSoldTickets(Seat seatRequest, Context context) {
//        LambdaLogger logger = context.getLogger();
//        logger.log("[getSoldTicket]: seatRequest.getTravelDate:" + seatRequest.getTravelDate());
//        ArrayList<Order> list = orderRepository.findByTravelDateAndTrainNumber(seatRequest.getTravelDate(),
//                seatRequest.getTrainNumber(), context);
//        logger.log("[getSoldTicket]: size:" + list.size());
//        if (!list.isEmpty()) {
//            Set<Ticket> ticketSet = new HashSet<>();
//            for (Order tempOrder : list) {
//                System.out.println("seat num of order: " + tempOrder.getSeatNumber());
//                ticketSet.add(new Ticket(tempOrder.getSeatNumber(),
//                        tempOrder.getFrom(), tempOrder.getTo()));
//            }
//            LeftTicketInfo leftTicketInfo = new LeftTicketInfo();
//            leftTicketInfo.setSoldTickets(ticketSet);
//            logger.log("[getSoldTicket]: Left ticket info is: {" + leftTicketInfo.toString() + "}");
//            return new Response<>(1, success, leftTicketInfo);
//        } else {
//            logger.log("[getSoldTicket]: Left ticket info is empty");
//            return new Response<>(0, "Order is Null.", null);
//        }
//    }
//
//    public Response<Route> getRouteByTripId(String tripId) {
//        Route route = null;
//        if (null != tripId && tripId.length() >= 2) {
//            TripId tripId1 = new TripId(tripId);
//            Trip trip = tripRepository.findByTripId(tripId1);
//            if (trip != null) {
//                route = getRouteByRouteId(trip.getRouteId());
//            }
//        }
//        if (route != null) {
//            return new Response<>(1, success, route);
//        } else {
//            return new Response<>(0, noContent, null);
//        }
//    }
//
//    private Route getRouteByRouteId(String routeId) {
//        return routeRepository.findById(routeId);
//    }
//
//    public Response<TrainType> getTrainTypeByTripId(String tripId) {
//        TripId tripId1 = new TripId(tripId);
//        TrainType trainType = null;
//
//        System.out.println("tripId1:" + tripId1.toString());
//        Trip trip = tripRepository.findByTripId(tripId1);
//        System.out.println("trip=null?:" + (trip == null));
//
//        if (trip != null) {
//            trainType = getTrainType(trip.getTrainTypeId());
//        }
//        if (trainType != null) {
//            return new Response<>(1, success, trainType);
//        } else {
//            return new Response<>(0, noContent, null);
//        }
//    }
//
//    private TrainType getTrainType(String trainTypeId) {
//        if (trainTypeRepository.findById(trainTypeId) == null) {
//            return null;
//        } else {
//            return trainTypeRepository.findById(trainTypeId);
//        }
//    }

}