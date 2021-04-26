package faas.study.service;

import faas.study.util.Response;

import java.util.Date;

public interface OrderService {

    Response queryAlreadySoldOrders(Date travelDate, String trainNumber);


}
