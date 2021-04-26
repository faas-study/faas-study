package faas.study.service;

import faas.study.entity.Order;
import faas.study.entity.OrderStatus;
import faas.study.entity.SeatClass;
import faas.study.entity.SoldTicket;
import faas.study.repository.OrderRepositoryImpl;
import faas.study.repository.OrderRepository;
import faas.study.util.Response;

import java.util.*;

public class OrderServiceImpl implements OrderService {

    private OrderRepository orderRepository=new OrderRepositoryImpl();

    String success = "Success";

    @Override
    public Response queryAlreadySoldOrders(Date travelDate, String trainNumber) {
        ArrayList<Order> orders = orderRepository.findByTravelDateAndTrainNumber(travelDate, trainNumber);
        SoldTicket cstr = new SoldTicket();
        cstr.setTravelDate(travelDate);
        cstr.setTrainNumber(trainNumber);
        for (Order order : orders) {
            if (order.getStatus() >= OrderStatus.CHANGE.getCode()) {
                continue;
            }
            if (order.getSeatClass() == SeatClass.NONE.getCode()) {
                cstr.setNoSeat(cstr.getNoSeat() + 1);
            } else if (order.getSeatClass() == SeatClass.BUSINESS.getCode()) {
                cstr.setBusinessSeat(cstr.getBusinessSeat() + 1);
            } else if (order.getSeatClass() == SeatClass.FIRSTCLASS.getCode()) {
                cstr.setFirstClassSeat(cstr.getFirstClassSeat() + 1);
            } else if (order.getSeatClass() == SeatClass.SECONDCLASS.getCode()) {
                cstr.setSecondClassSeat(cstr.getSecondClassSeat() + 1);
            } else if (order.getSeatClass() == SeatClass.HARDSEAT.getCode()) {
                cstr.setHardSeat(cstr.getHardSeat() + 1);
            } else if (order.getSeatClass() == SeatClass.SOFTSEAT.getCode()) {
                cstr.setSoftSeat(cstr.getSoftSeat() + 1);
            } else if (order.getSeatClass() == SeatClass.HARDBED.getCode()) {
                cstr.setHardBed(cstr.getHardBed() + 1);
            } else if (order.getSeatClass() == SeatClass.SOFTBED.getCode()) {
                cstr.setSoftBed(cstr.getSoftBed() + 1);
            } else if (order.getSeatClass() == SeatClass.HIGHSOFTBED.getCode()) {
                cstr.setHighSoftBed(cstr.getHighSoftBed() + 1);
            } else {
                //OrderServiceImpl.LOGGER.info("[Order Service][Calculate Sold Tickets] Seat class not exists. Order ID: {}", order.getId());
            }
        }
        return new Response<>(1, success, cstr);
    }


}

