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

    private final OrderRepository orderRepository = new OrderRepositoryImpl();
    private final Order2Repository order2Repository = new Order2RepositoryImpl();
    private final TripRepository tripRepository = new TripRepositoryImpl();
    private final Trip2Repository trip2Repository = new Trip2RepositoryImpl();
    private final RouteRepository routeRepository = new RouteRepositoryImpl();
    private final TrainTypeRepository trainTypeRepository = new TrainTypeRepositoryImpl();

    private static final String getRouteByTripId = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-route-by-tripid";
    private static final String getSoldTickets = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-sold-tickets";
    private static final String getTrainTypeByTripId = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-traintype-by-tripid";
    private static final String getRouteByTripId2 = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-route-by-tripid-2";
    private static final String getSoldTickets2 = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-sold-tickets-2";
    private static final String getTrainTypeByTripId2 = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-traintype-by-tripid-2";


    String success = "Success";
    String noContent = "No Content";
    Response<Route> routeResult;
    LeftTicketInfo leftTicketInfo;
    TrainType trainTypeResult;

    @Override
    public Response<Ticket> distributeSeat(Seat seatRequest, Context context) {
        LambdaLogger logger = context.getLogger();
        //Distinguish G\D from other trains
        String trainNumber = seatRequest.getTrainNumber();

        if (trainNumber.startsWith("G") || trainNumber.startsWith("D")) {
            logger.log("[SeatService distributeSeat] TrainNumber start with G|D");

            //Call the microservice to query all the station information for the train
//            routeResult = getRouteByTripId(trainNumber);
//            logger.log("[SeatService distributeSeat] The result of getRouteResult is {" + routeResult.getMsg() + "}");
            String ret = "";
            try {
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(getRouteByTripId+ "?tripId=" + trainNumber)
                        .get()
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                ret = response.body().string();

            } catch (Exception e) {
                e.printStackTrace();
            }
            routeResult = JsonUtils.json2Object(ret, Response.class);
            logger.log("[SeatService distributeSeat] The result of getRouteResult is {"+routeResult.getMsg()+"}");


            //Call the microservice to query for residual Ticket information: the set of the Ticket sold for the specified seat type
//            Response<LeftTicketInfo> mRes = getSoldTickets(seatRequest, context);
//            logger.log("Left ticket info is : {" + mRes.toString() + "}");
//            leftTicketInfo = JsonUtils.conveterObject(mRes.getData(), LeftTicketInfo.class);
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
            logger.log("Left ticket info is : {"+mRes.toString()+"}");
            leftTicketInfo = JsonUtils.conveterObject(mRes.getData(), LeftTicketInfo.class);


            //Calls the microservice to query the total number of seats specified for that vehicle

//            Response<TrainType> trainTypeResponse = getTrainTypeByTripId(seatRequest.getTrainNumber());
//            trainTypeResult = JsonUtils.conveterObject(trainTypeResponse.getData(), TrainType.class);
//
//            logger.log("[SeatService distributeSeat 1] The result of getTrainTypeResult is {" + mRes.getData() + "}");
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

            logger.log("[SeatService distributeSeat 1] The result of getTrainTypeResult is {"+mRes.toString()+"}");
        } else {

            logger.log("[SeatService] TrainNumber start with other capital");

            //Call the micro service to query all the station information for the trains
//            routeResult = getRouteByTripId2(trainNumber, context);
//            logger.log("[SeatService distributeSeat] The result of getRouteResult is {" + routeResult.getMsg() + "}");
            String ret = "";
            try {
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(getRouteByTripId2+ "?tripId=" + trainNumber)
                        .get()
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                ret = response.body().string();

            } catch (Exception e) {
                e.printStackTrace();
            }
            routeResult = JsonUtils.json2Object(ret, Response.class);
            logger.log("[SeatService distributeSeat] The result of getRouteResult is {"+routeResult.getMsg()+"}");


            //Call the microservice to query for residual Ticket information: the set of the Ticket sold for the specified seat type
//            Response<LeftTicketInfo> mRes = getSoldTickets2(seatRequest, context);
//            logger.log("Left ticket info is : {" + mRes.toString() + "}");
//            leftTicketInfo = JsonUtils.conveterObject(mRes.getData(), LeftTicketInfo.class);
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
            logger.log("Left ticket info is : {"+mRes.toString()+"}");
            leftTicketInfo = JsonUtils.conveterObject(mRes.getData(), LeftTicketInfo.class);


            //Calls the microservice to query the total number of seats specified for that vehicle
//            Response<TrainType> trainTypeResponse = getTrainTypeByTripId2(seatRequest.getTrainNumber());
//            trainTypeResult = JsonUtils.conveterObject(trainTypeResponse.getData(), TrainType.class);
//
//            logger.log("[SeatService distributeSeat 2] The result of getTrainTypeResult is {" + mRes.getData() + "}");

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

            logger.log("[SeatService distributeSeat 2] The result of getTrainTypeResult is {"+mRes.toString()+"}");
        }

        //Assign seats
        List<String> stationList = JsonUtils.conveterObject(routeResult.getData(), Route.class).getStations();
        int seatTotalNum;
        if (seatRequest.getSeatType() == SeatClass.FIRSTCLASS.getCode()) {
            seatTotalNum = trainTypeResult.getConfortClass();
            logger.log("[SeatService distributeSeat] The request seat type is confortClass and the total num is {" + seatTotalNum + "}");
        } else {
            seatTotalNum = trainTypeResult.getEconomyClass();
            logger.log("[SeatService distributeSeat] The request seat type is economyClass and the total num is {" + seatTotalNum + "}");
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
                    logger.log("[SeatService distributeSeat] Use the previous distributed seat number! {" + soldTicket.getSeatNo() + "}");
                    return new Response<>(1, "Use the previous distributed seat number!", ticket);
                }
            }
            while (isContained(soldTickets, seat)) {
                seat = rand.nextInt(range) + 1;
            }
        }
        ticket.setSeatNo(seat + "");
        logger.log("[SeatService distributeSeat] Use a new seat number! {" + seat + "}");
        return new Response<>(1, "Use a new seat number!", ticket);
    }

    private boolean isContained(Set<Ticket> soldTickets, int seat) {
        //Check that the seat number has been used
        for (Ticket soldTicket : soldTickets) {
            if (soldTicket.getSeatNo().equals(seat+"")) {
                System.out.println("soldTicket.getSeatNo():" + soldTicket.getSeatNo() + "& seat: " + seat);
                return true;
            }
        }
        return false;
    }

    public Response<LeftTicketInfo> getSoldTickets(Seat seatRequest, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("[getSoldTicket]: seatRequest.getTravelDate:" + seatRequest.getTravelDate());
        ArrayList<Order> list = orderRepository.findByTravelDateAndTrainNumber(seatRequest.getTravelDate(),
                seatRequest.getTrainNumber(), context);
        logger.log("[getSoldTicket]: size:" + list.size());
        if (!list.isEmpty()) {
            Set<Ticket> ticketSet = new HashSet<>();
            for (Order tempOrder : list) {
                System.out.println("seat num of order: " + tempOrder.getSeatNumber());
                ticketSet.add(new Ticket(tempOrder.getSeatNumber(),
                        tempOrder.getFrom(), tempOrder.getTo()));
            }
            LeftTicketInfo leftTicketInfo = new LeftTicketInfo();
            leftTicketInfo.setSoldTickets(ticketSet);
            logger.log("[getSoldTicket]: Left ticket info is: {" + leftTicketInfo.toString() + "}");
            return new Response<>(1, success, leftTicketInfo);
        } else {
            logger.log("[getSoldTicket]: Left ticket info is empty");
            return new Response<>(0, "Order is Null.", null);
        }
    }

    public Response<Route> getRouteByTripId(String tripId) {
        Route route = null;
        if (null != tripId && tripId.length() >= 2) {
            TripId tripId1 = new TripId(tripId);
            Trip trip = tripRepository.findByTripId(tripId1);
            if (trip != null) {
                route = getRouteByRouteId(trip.getRouteId());
            }
        }
        if (route != null) {
            return new Response<>(1, success, route);
        } else {
            return new Response<>(0, noContent, null);
        }
    }

    private Route getRouteByRouteId(String routeId) {
        return routeRepository.findById(routeId);
    }

    public Response<TrainType> getTrainTypeByTripId(String tripId) {
        TripId tripId1 = new TripId(tripId);
        TrainType trainType = null;

        System.out.println("tripId1:" + tripId1.toString());
        Trip trip = tripRepository.findByTripId(tripId1);
        System.out.println("trip=null?:" + (trip == null));

        if (trip != null) {
            trainType = getTrainType(trip.getTrainTypeId());
        }
        if (trainType != null) {
            return new Response<>(1, success, trainType);
        } else {
            return new Response<>(0, noContent, null);
        }
    }

    private TrainType getTrainType(String trainTypeId) {
        if (trainTypeRepository.findById(trainTypeId) == null) {
            return null;
        } else {
            return trainTypeRepository.findById(trainTypeId);
        }
    }

    public Response<LeftTicketInfo> getSoldTickets2(Seat seatRequest, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("[getSoldTicket2]: seatRequest.getTravelDate:" + seatRequest.getTravelDate());
        ArrayList<Order> list = order2Repository.findByTravelDateAndTrainNumber(seatRequest.getTravelDate(),
                seatRequest.getTrainNumber(), context);
        logger.log("size:" + list.size());
        if (!list.isEmpty()) {
            Set<Ticket> ticketSet = new HashSet<>();
            for (Order tempOrder : list) {
                Ticket ticket = new Ticket();
                ticket.setSeatNo(tempOrder.getSeatNumber());
                ticket.setStartStation(tempOrder.getFrom());
                ticket.setDestStation(tempOrder.getTo());
                ticketSet.add(ticket);
            }
            LeftTicketInfo leftTicketInfo = new LeftTicketInfo();
            leftTicketInfo.setSoldTickets(ticketSet);
            logger.log("[getSoldTicket2]: Left ticket info is: {" + leftTicketInfo.toString() + "}");
            return new Response<>(1, success, leftTicketInfo);
        } else {
            logger.log("[getSoldTicket2]: Left ticket info is empty");
            return new Response<>(0, "Seat is Null.", null);
        }
    }

    public Response<Route> getRouteByTripId2(String tripId, Context context) {
        LambdaLogger logger = context.getLogger();

        TripId tripId1 = new TripId(tripId);

        Trip trip = trip2Repository.findByTripId(tripId1);
        if (trip == null) {
            logger.log("[Get Route By Trip ID 2] Trip Not Found: {" + tripId + "}");
            return new Response<>(0, "\"[Get Route By Trip ID 2] Trip Not Found:\" + tripId", null);
        } else {
            Route route = getRouteByRouteId(trip.getRouteId());
            if (route == null) {
                return new Response<>(0, "\"[Get Route By Trip ID 2] Route Not Found:\" + trip.getRouteId()", null);
            } else {
                logger.log("[Get Route By Trip ID 2] Success");
                return new Response<>(1, "[Get Route By Trip ID 2] Success", route);
            }
        }
    }

    public Response<TrainType> getTrainTypeByTripId2(String tripId) {
        TripId tripId1 = new TripId(tripId);
        TrainType trainType = null;

        Trip trip = trip2Repository.findByTripId(tripId1);

        if (trip != null) {
            trainType = getTrainType(trip.getTrainTypeId());
        }
        if (trainType != null) {
            return new Response<>(1, success, trainType);
        } else {
            return new Response<>(0, noContent, null);
        }
    }

}