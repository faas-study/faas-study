package faas.study.service;

import faas.study.entity.TicketRequest;
import faas.study.entity.TripResponse;
import faas.study.util.Response;

public interface OrderService {

    //Response queryAlreadySoldOrders(Date travelDate, String trainNumber);
    Response queryAlreadySoldOrders(TicketRequest ticketRequest, TripResponse tripResponse);

}
