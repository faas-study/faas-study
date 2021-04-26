package faas.study.service;

import faas.study.entity.TicketRequest;
import faas.study.util.Response;

public interface TravelService {
    //mResponse query(TripInfo info);

    Response getTickets(TicketRequest ticketRequest);

}
