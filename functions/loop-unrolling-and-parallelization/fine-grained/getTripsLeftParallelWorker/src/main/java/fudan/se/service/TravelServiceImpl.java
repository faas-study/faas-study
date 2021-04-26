package fudan.se.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import fudan.se.entity.Route;
import fudan.se.entity.RouteRequest;
import fudan.se.entity.Trip;
import fudan.se.entity.TripInfo;
import fudan.se.entity.TripResponse;
import fudan.se.repository.RouteRepository;
import fudan.se.repository.TripRepository;
import fudan.se.repository.TripRepositoryImpl;
import fudan.se.util.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TravelServiceImpl implements TravelService {
    String success = "Success";
    String fail = "Fail";

    private final RouteRepository routeRepository = new RouteRepository();

    public Response query(RouteRequest request, Context context) {
        LambdaLogger logger = context.getLogger();

        String startingPlaceId = request.getStartingPlaceId();
        String endPlaceId = request.getEndPlaceId();
        String routeId = request.getRouteId();


        Route route = routeRepository.findById(routeId);
        logger.log("Route:" + route.toString());
        if (route.getStations().contains(startingPlaceId) &&
            route.getStations().contains(endPlaceId) &&
            route.getStations().indexOf(startingPlaceId) < route.getStations().indexOf(endPlaceId)) {
            route = route;
        } else {
            route = null;
        }

        String result = "";
        if (route != null) {
            result = route.getId();
        }

        return new Response(0, success, result);
    }
}
