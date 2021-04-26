package faas.study.repository;

import faas.study.entity.Order;

import java.util.ArrayList;
import java.util.Date;

public interface OrderRepository{

    ArrayList<Order> findByTravelDateAndTrainNumber(Date travelDate, String trainNumber);

}
