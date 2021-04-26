package fudan.se.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import fudan.se.entity.Route;
import fudan.se.entity.Trip;
import fudan.se.entity.TripInfo;
import fudan.se.repository.RouteRepository;
import fudan.se.repository.TripRepository;
import fudan.se.repository.TripRepositoryImpl;
import fudan.se.util.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/
public class TravelServiceImpl implements TravelService {
    String success = "Success";
    String fail = "Fail";

    private final TripRepository tripRepository = new TripRepositoryImpl();
    private final RouteRepository routeRepository = new RouteRepository();

    public Map<String, String> stationMap = new HashMap<String, String>() {{
        put("Shang Hai", "shanghai");
        put("Hang Zhou", "hangzhou");
        put("Su Zhou", "suzhou");
        put("Wu Xi", "wuxi");
        put("Tai Yuan", "taiyuan");
        put("Bei Jing", "beijing");
        put("Nan Jing", "nanjing");
        put("Shi Jia Zhuang", "shijiazhuang");
        put("Xu Zhou", "xuzhou");
        put("Ji Nan", "jinan");
    }};

    public Response query(TripInfo info, Context context) throws InterruptedException {
        LambdaLogger logger = context.getLogger();

        String startingPlaceName = info.getStartingPlace();
        String endPlaceName = info.getEndPlace();

        String startingPlaceId = stationMap.getOrDefault(startingPlaceName, "");
        String endPlaceId = stationMap.getOrDefault(endPlaceName, "");
        if(startingPlaceId.equals("") || endPlaceId.equals("")) {
            return new Response(0, fail, "startingPlaceId or endPlaceId not found:" + startingPlaceName + " " + endPlaceName);
        }

        //This is the final result
        List<String> routeList = new ArrayList<>();

        List<Thread> threadList = new ArrayList<>();
        List<RouteRequestThread> routeRequestThreadList = new ArrayList<>();

        List<Trip> allTripList = tripRepository.findAll(context);
        for(Trip trip: allTripList) {
            logger.log("[getTripsLeftParallelMaster] trip: " + trip.getTripId() + "starting...\n");
            RouteRequestThread rt = new RouteRequestThread(trip.getRouteId(), startingPlaceId, endPlaceId);
            Thread t = new Thread(rt);
            t.start();
            threadList.add(t);
            routeRequestThreadList.add(rt);
        }

        logger.log("[getTripsLeftParallelMaster] Thread joining...\n");
        for(Thread t: threadList) {
            t.join();
        }
        logger.log("[getTripsLeftParallelMaster] Thread join finished\n");

        for( RouteRequestThread rt: routeRequestThreadList ) {
            if(rt.getReturnValRoute() != "" && rt.getReturnValRoute() != null) {
                routeList.add(rt.getReturnValRoute());
            }
        }

        return new Response(0, success, routeList);
    }
}
