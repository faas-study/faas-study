package faas.study.entity;

import java.util.Date;

public class TicketRequest {

    private Trip trip;
    private String routeId;
    private String startingPlaceId;
    private String endPlaceId;
    private String startingPlaceName;
    private String endPlaceName;
    private Date departureTime;

    public TicketRequest() {
        //Default Constructor
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
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

    public String getStartingPlaceName() {
        return startingPlaceName;
    }

    public void setStartingPlaceName(String startingPlaceName) {
        this.startingPlaceName = startingPlaceName;
    }

    public String getEndPlaceName() {
        return endPlaceName;
    }

    public void setEndPlaceName(String endPlaceName) {
        this.endPlaceName = endPlaceName;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
}
