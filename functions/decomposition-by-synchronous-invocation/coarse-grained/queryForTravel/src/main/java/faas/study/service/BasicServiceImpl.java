package faas.study.service;

import java.util.HashMap;

import faas.study.entity.*;
import faas.study.util.Response;
import faas.study.util.JsonUtils;
import okhttp3.OkHttpClient;

import java.util.concurrent.*;


public class BasicServiceImpl implements BasicService {
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS).build();

    private static final String getRouteByRouteId = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-route-by-routeid";
    private static final String getTrainTypeByTrainTypeId = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-traintype-by-traintypeId";
    private static final String getPriceByRouteIdAndTrainType = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/get-price-by-routeid-and-traintype";
    private static final String queryForStationIdByStationName = "https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/api/v1/query-for-station-id-by-station-name";

    @Override
    public Response queryForTravel(Travel info) {

        Response response = new Response<>();
        TravelResult result = new TravelResult();
        result.setStatus(true);
        response.setStatus(1);
        response.setMsg("Success");
        boolean startingPlaceExist = checkStationExists(info.getStartingPlace());
        boolean endPlaceExist = checkStationExists(info.getEndPlace());

        if (!startingPlaceExist || !endPlaceExist) {
            result.setStatus(false);
            response.setStatus(0);
            response.setMsg("Start place or end place not exist!");
        }

        TrainType trainType = queryTrainType(info.getTrip().getTrainTypeId());
        if (trainType == null) {
            // BasicServiceImpl.LOGGER.info("traintype doesn't exist");
            result.setStatus(false);
            response.setStatus(0);
            response.setMsg("Train type doesn't exist");
        } else {
            result.setTrainType(trainType);
        }

        String routeId = info.getTrip().getRouteId();
        String trainTypeString = null;
        if (trainType != null) {
            trainTypeString = trainType.getId();
        }
        Route route = getRouteByRouteId(routeId);
        PriceConfig priceConfig = queryPriceConfigByRouteIdAndTrainType(routeId, trainTypeString);

        String startingPlaceId = queryForStationId(info.getStartingPlace());
        String endPlaceId = queryForStationId(info.getEndPlace());

        //  log.info("startingPlaceId : " + startingPlaceId + "endPlaceId : " + endPlaceId);

        int indexStart = 0;
        int indexEnd = 0;
        if (route != null) {
            indexStart = route.getStations().indexOf(startingPlaceId);
            indexEnd = route.getStations().indexOf(endPlaceId);
        }

        // log.info("indexStart : " + indexStart + " __ " + "indexEnd : " + indexEnd);
        if (route != null) {
            //     log.info("route.getDistances().size : " + route.getDistances().size());
        }
        HashMap<String, String> prices = new HashMap<>();
        try {
            int distance = 0;
            if (route != null) {
                distance = route.getDistances().get(indexEnd) - route.getDistances().get(indexStart);
            }
            System.out.println("[queryForTravel] distance: " + distance);
            /**
             * We need the price Rate and distance (starting station).
             */
            double priceForEconomyClass = distance * priceConfig.getBasicPriceRate();
            double priceForConfortClass = distance * priceConfig.getFirstClassPriceRate();
            prices.put("economyClass", "" + priceForEconomyClass);
            prices.put("confortClass", "" + priceForConfortClass);
        } catch (Exception e) {
            prices.put("economyClass", "95.0");
            prices.put("confortClass", "120.0");
        }
        result.setPrices(prices);
        result.setPercent(1.0);
        response.setData(result);
        return response;
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

    private boolean checkStationExists(String stationName) {
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
        return stationIDRes.getStatus() == 1;
    }

    private TrainType queryTrainType(String trainTypeId) {
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

        Route route = null;
        if (routeRes.getStatus() == 1) {
            route = JsonUtils.conveterObject(routeRes.getData(), Route.class);

        }
        return route;
    }

    private PriceConfig queryPriceConfigByRouteIdAndTrainType(String routeId, String trainType) {
        String ret = "";
        try {
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(getPriceByRouteIdAndTrainType + "?routeId=" + routeId + "&trainType=" + trainType)
                    .get()
                    .build();

            okhttp3.Response response = client.newCall(request).execute();
            ret = response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Response priceConfigRes = JsonUtils.json2Object(ret, Response.class);

        PriceConfig priceConfig = null;
        if (priceConfigRes.getStatus() == 1) {
            priceConfig = JsonUtils.conveterObject(priceConfigRes.getData(), PriceConfig.class);
        }
        System.out.println("[queryForTravel] priceConfig: " + priceConfig.getBasicPriceRate() + " & " + priceConfig.getFirstClassPriceRate());
        return priceConfig;
    }

}
