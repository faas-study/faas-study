package faas.study.service;

import faas.study.entity.*;
import faas.study.util.DateUtils;
import faas.study.util.JsonUtils;
import faas.study.util.Response;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import java.util.*;

public class TravelServiceImpl implements TravelService {

    private OkHttpClient client = new OkHttpClient();

    String success = "Success";

    private static final String queryForTravel = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/query-for-travel";
    private static final String getRouteByRouteId = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-route-by-routeid";
    private static final String getTrainTypeByTrainTypeId = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-traintype-by-traintypeId";
    private static final String queryAlreadySoldOrders = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/query-already-sold-orders";
    private static final String queryForStationIdByStationName = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/query-for-station-id-by-station-name";

    @Override
    public Response getTickets(TicketRequest ticketRequest) {
        Trip trip = ticketRequest.getTrip();
        String routeId = ticketRequest.getRouteId();
        String startingPlaceId = ticketRequest.getStartingPlaceId();
        String endPlaceId = ticketRequest.getEndPlaceId();
        String startingPlaceName = ticketRequest.getStartingPlaceName();
        String endPlaceName = ticketRequest.getEndPlaceName();
        Date departureTime = ticketRequest.getDepartureTime();

        Route route = getRouteByRouteId(routeId);
        if (route == null)
            return new Response(0, "Route does not exsit.", routeId);

        if (!route.getStations().contains(startingPlaceId) ||
                !route.getStations().contains(endPlaceId) ||
                route.getStations().indexOf(startingPlaceId) >= route.getStations().indexOf(endPlaceId))
            return new Response(0, "Station info error.", null);

        //Determine if the date checked is the same day and after
        if (!afterToday(departureTime)) {
            System.out.println("departureTime:" + departureTime.toString());
            return new Response(0, "The date checked isn't the same day or after.", null);
        }
        Travel query = new Travel();
        query.setTrip(trip);
        query.setStartingPlace(startingPlaceName);
        query.setEndPlace(endPlaceName);
        query.setDepartureTime(departureTime);


        String ret = "";
        String json = JsonUtils.object2Json(query);
        System.out.println("json for queryForTravel：" + json);
        try {
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), json);

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(queryForTravel)
                    .post(body)
                    .build();

            okhttp3.Response response = client.newCall(request).execute();
            ret = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Response mRes = JsonUtils.json2Object(ret, Response.class);
        TravelResult resultForTravel = JsonUtils.conveterObject(mRes.getData(), TravelResult.class);


        System.out.println("url for queryAlreadySoldOrders：" + queryAlreadySoldOrders + "?travelDate=" + DateUtils.dateToMillisecond(departureTime) + "&trainNumber=" + trip.getTripId().toString());
        try {
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(queryAlreadySoldOrders + "?travelDate=" + DateUtils.dateToMillisecond(departureTime) + "&trainNumber=" + trip.getTripId().toString())
                    .get()
                    .build();

            okhttp3.Response response = client.newCall(request).execute();
            ret = response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Response<SoldTicket> result = JsonUtils.json2Object(ret, Response.class);
        System.out.println("result of queryAlreadySoldOrders： " + ret);
        TripResponse response = new TripResponse();
        response.setConfortClass(50);
        response.setEconomyClass(50);

        response.setStartingStation(startingPlaceName);
        response.setTerminalStation(endPlaceName);

        //Calculate the distance from the starting point
        int indexStart = route.getStations().indexOf(startingPlaceId);
        int indexEnd = route.getStations().indexOf(endPlaceId);
        int distanceStart = route.getDistances().get(indexStart) - route.getDistances().get(0);
        int distanceEnd = route.getDistances().get(indexEnd) - route.getDistances().get(0);
        TrainType trainType = getTrainType(trip.getTrainTypeId());
        //Train running time is calculated according to the average running speed of the train
        int minutesStart = 60 * distanceStart / trainType.getAverageSpeed();
        int minutesEnd = 60 * distanceEnd / trainType.getAverageSpeed();

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(trip.getStartingTime());
        calendarStart.add(Calendar.MINUTE, minutesStart);
        response.setStartingTime(calendarStart.getTime());
        // TravelServiceImpl.LOGGER.info("[Train Service] calculate time：{}  time: {}", minutesStart, calendarStart.getTime());

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(trip.getStartingTime());
        calendarEnd.add(Calendar.MINUTE, minutesEnd);
        response.setEndTime(calendarEnd.getTime());
        // TravelServiceImpl.LOGGER.info("[Train Service] calculate time：{}  time: {}", minutesEnd, calendarEnd.getTime());

        response.setTripId(new TripId(JsonUtils.conveterObject(result.getData(), SoldTicket.class).getTrainNumber()
        ));
        response.setTrainTypeId(trip.getTrainTypeId());
        response.setPriceForConfortClass(resultForTravel.getPrices().get("confortClass"));
        response.setPriceForEconomyClass(resultForTravel.getPrices().get("economyClass"));

        return new Response<TripResponse>(1, success, response);
    }

    private static boolean afterToday(Date date) {
        Calendar calDateA = Calendar.getInstance();
        Date today = new Date();
        calDateA.setTime(today);

        Calendar calDateB = Calendar.getInstance();
        calDateB.setTime(date);

        if (calDateA.get(Calendar.YEAR) > calDateB.get(Calendar.YEAR)) {
            return false;
        } else if (calDateA.get(Calendar.YEAR) == calDateB.get(Calendar.YEAR)) {
            if (calDateA.get(Calendar.MONTH) > calDateB.get(Calendar.MONTH)) {
                return false;
            } else if (calDateA.get(Calendar.MONTH) == calDateB.get(Calendar.MONTH)) {
                return calDateA.get(Calendar.DAY_OF_MONTH) <= calDateB.get(Calendar.DAY_OF_MONTH);
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private TrainType getTrainType(String trainTypeId) {
        String ret = "";
        try {
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(getTrainTypeByTrainTypeId + "?trainTypeId=" + trainTypeId)
                    .get()
                    .build();

            okhttp3.Response response = client.newCall(request).execute();
            ret = response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Response TrainTypeRes = JsonUtils.json2Object(ret, Response.class);

        TrainType trainType = null;
        if (TrainTypeRes.getStatus() == 1) {
            trainType = JsonUtils.conveterObject(TrainTypeRes.getData(), TrainType.class);

        }
        return trainType;
    }

    private String queryForStationId(String stationName) {
        String ret = "";
        try {
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(queryForStationIdByStationName + "?stationName=" + stationName)
                    .get()
                    .build();

            okhttp3.Response response = client.newCall(request).execute();
            ret = response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Response stationIDRes = JsonUtils.json2Object(ret, Response.class);

        String stationID = "";
        if (stationIDRes.getStatus() == 1) {
            stationID = (String) stationIDRes.getData();
        }
        return stationID;
    }

    private Route getRouteByRouteId(String routeId) {
        String ret = "";
        try {
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(getRouteByRouteId + "?routeId=" + routeId)
                    .get()
                    .build();

            okhttp3.Response response = client.newCall(request).execute();
            ret = response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Response routeRes = JsonUtils.json2Object(ret, Response.class);

        Route route1 = new Route();
        if (routeRes.getStatus() == 1) {
            route1 = JsonUtils.conveterObject(routeRes.getData(), Route.class);
        }
        return route1;
    }
}
