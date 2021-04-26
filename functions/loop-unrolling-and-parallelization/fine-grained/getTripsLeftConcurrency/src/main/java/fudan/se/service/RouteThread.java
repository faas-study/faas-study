package fudan.se.service;

import fudan.se.entity.Route;
import fudan.se.repository.RouteRepository;

public class RouteThread implements Runnable {
    private String routeId;
    private Route route;
    private String startingPlaceId;
    private String endPlaceId;
    private String returnValRouteId;

    RouteThread(String routeId, String startingPlaceId, String endPlaceId, Route route) {
        this.routeId = routeId;
        this.startingPlaceId = startingPlaceId;
        this.endPlaceId = endPlaceId;
        this.route = route;
    }

    public void run() {
        Route route = this.route;
        if (route.getStations().contains(this.startingPlaceId) &&
                route.getStations().contains(this.endPlaceId) &&
                route.getStations().indexOf(startingPlaceId) < route.getStations().indexOf(endPlaceId)) {
            this.returnValRouteId = route.getId();
        } else {
            this.returnValRouteId = "";
        }
    }

    public String getReturnValRouteId() {
        return this.returnValRouteId;
    }
}
