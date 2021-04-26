package fudan.se.repository;

import fudan.se.entity.Order;

import java.util.ArrayList;
import java.util.UUID;


public interface OrderRepository {

    ArrayList<Order> findByAccountId(UUID accountId);

    void save(Order order);
}
