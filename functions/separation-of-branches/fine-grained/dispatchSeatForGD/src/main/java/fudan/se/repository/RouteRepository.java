package fudan.se.repository;

import fudan.se.entity.Route;

public interface RouteRepository {
    Route findById(String id);

}
