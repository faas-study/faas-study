package fudan.se.entity;

public class RouteRequest {
    private String routeId;
    private String startingPlaceId;
    private String endPlaceId;

    public RouteRequest() {

    }

    public RouteRequest(String routeId, String startingPlaceId, String endPlaceId) {
        this.routeId = routeId;
        this.startingPlaceId = startingPlaceId;
        this.endPlaceId = endPlaceId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getStartingPlaceId() {
        return startingPlaceId;
    }

    public void setStartingPlaceId(String startingPlaceId) {
        this.startingPlaceId = startingPlaceId;
    }

    public String getEndPlaceId() {
        return endPlaceId;
    }

    public void setEndPlaceId(String endPlaceId) {
        this.endPlaceId = endPlaceId;
    }
}
