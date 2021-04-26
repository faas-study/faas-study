package fudan.se.service;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import fudan.se.entity.Order;
import fudan.se.util.Response;

s
public interface OrderService {

    Response create(Order newOrder, LambdaLogger logger);

}
