package fudan.se.service;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import fudan.se.entity.Order;
import fudan.se.repository.OrderRepository;
import fudan.se.repository.OrderRepositoryImpl;
import fudan.se.util.Response;

import java.util.ArrayList;
import java.util.UUID;


public class OrderServiceImpl implements OrderService {

    private OrderRepository orderRepository = new OrderRepositoryImpl();

    String success = "Success";

    @Override
    public Response create(Order order, LambdaLogger logger) {
        ArrayList<Order> accountOrders = orderRepository.findByAccountId(order.getAccountId());
        if (accountOrders.contains(order)) {
            logger.log("[Order Service][Order Create] Fail.Order already exists.");
            return new Response<>(0, "Order already exist", null);
        } else {
            order.setId(UUID.randomUUID());
            orderRepository.save(order);
            logger.log("[Order Service][Order Create] Success.");
            logger.log("[Order Service][Order Create] Price: {" + order.getPrice() + "}");
            return new Response<>(1, success, order);
        }
    }

}

