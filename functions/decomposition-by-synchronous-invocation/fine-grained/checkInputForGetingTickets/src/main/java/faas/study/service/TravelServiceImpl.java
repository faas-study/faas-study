package faas.study.service;

import faas.study.entity.*;
import faas.study.repository.RouteRepository;
import faas.study.repository.TrainTypeRepository;
import faas.study.repository.TrainTypeRepositoryImpl;
import faas.study.util.Response;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TravelServiceImpl implements TravelService {

    private RouteRepository routeRepository = new RouteRepository();
    private TrainTypeRepository trainTypeRepository = new TrainTypeRepositoryImpl();

    @Override
    public Response checkInput(TicketRequest ticketRequest) {
        Trip trip = ticketRequest.getTrip();
        String routeId = ticketRequest.getRouteId();
        String startingPlaceId = ticketRequest.getStartingPlaceId();
        String endPlaceId = ticketRequest.getEndPlaceId();
        String startingPlaceName = ticketRequest.getStartingPlaceName();
        String endPlaceName = ticketRequest.getEndPlaceName();
        Date departureTime = ticketRequest.getDepartureTime();

        if (!afterToday(departureTime)) {
            System.out.println("departureTime:" + departureTime.toString());
            return new Response(0, "The date checked isn't the same day or after.", null);
        }

        Route route = getRouteByRouteId(routeId);
        if (route == null)
            return new Response(0, "Route does not exsit.", null);

        if (!route.getStations().contains(startingPlaceId) ||
                !route.getStations().contains(endPlaceId) ||
                route.getStations().indexOf(startingPlaceId) >= route.getStations().indexOf(endPlaceId))
            return new Response(0, "Station info error.", null);

        TripResponse tripResponse = initRes(trip, route, startingPlaceId, endPlaceId, startingPlaceName, endPlaceName);

        Map<String, Object> map = new HashMap<>();
        map.put("ticketRequest", ticketRequest);
        map.put("tripResponse", tripResponse);

        return new Response(1, "Pass", map);
    }

    public TripResponse initRes(Trip trip, Route route, String startingPlaceId, String endPlaceId, String startingPlaceName, String endPlaceName) {
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

        response.setTrainTypeId(trip.getTrainTypeId());
        return response;
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
        return trainTypeRepository.findById(trainTypeId);
    }

    private Route getRouteByRouteId(String routeId) {
        return routeRepository.findById(routeId);
    }
}
