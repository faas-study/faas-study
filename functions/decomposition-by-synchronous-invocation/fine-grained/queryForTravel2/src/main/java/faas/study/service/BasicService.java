package faas.study.service;

import faas.study.entity.TicketRequest;
import faas.study.entity.TripResponse;
import faas.study.util.Response;

public interface BasicService {

    Response queryForTravel(TicketRequest request, TripResponse tripResponse);

}
