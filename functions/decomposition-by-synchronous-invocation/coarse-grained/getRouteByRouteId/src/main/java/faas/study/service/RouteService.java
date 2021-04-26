package faas.study.service;

import faas.study.entity.Route;
import faas.study.repository.RouteRepository;
import faas.study.util.Response;

public class RouteService {
    private RouteRepository routeRepository = new RouteRepository();

    //    public Route getRouteByRouteId(String routeId) {
//        return routeRepository.findById(routeId);
//    }
    String success = "Success";


    public Response getRouteById(String routeId) {
        Route route = routeRepository.findById(routeId);
        if (route == null) {
            return new Response<>(0, "No content with the routeId", routeId);
        } else {
            return new Response<>(1, success, route);
        }

    }
}
