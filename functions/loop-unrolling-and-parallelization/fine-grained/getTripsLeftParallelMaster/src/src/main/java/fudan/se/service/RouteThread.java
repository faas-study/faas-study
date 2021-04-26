package fudan.se.service;

import fudan.se.entity.Route;
import fudan.se.repository.RouteRepository;

import java.util.List;

public class RouteThread implements Runnable {
    private String routeId;
    private String startingPlaceId;
    private String endPlaceId;
    private RouteRepository routeRepository;
    private Route returnValRoute;

    RouteThread(String routeId, String startingPlaceId, String endPlaceId, RouteRepository routeRepository) {
        this.routeId = routeId;
        this.startingPlaceId = startingPlaceId;
        this.endPlaceId = endPlaceId;
        this.routeRepository = routeRepository;
    }

    public void run() {
        Route route = this.routeRepository.findById(this.routeId);
        if (route.getStations().contains(this.startingPlaceId) &&
                route.getStations().contains(this.endPlaceId) &&
                route.getStations().indexOf(startingPlaceId) < route.getStations().indexOf(endPlaceId)) {
            this.returnValRoute = route;
        } else {
            this.returnValRoute = null;
        }
    }

    public Route getReturnValRoute() {
        return this.returnValRoute;
    }
}
