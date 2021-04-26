package fudan.se;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import fudan.se.entity.Order;
import fudan.se.service.OrderService;
import fudan.se.service.OrderServiceImpl;
import fudan.se.util.JsonUtils;
import fudan.se.util.Response;


public class Handler implements RequestHandler<APIGatewayV2ProxyRequestEvent, Response> {
    private OrderService service = new OrderServiceImpl();

    @Override
    public Response handleRequest(APIGatewayV2ProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("[createOrder] - Start to create order.");

        Order order = JsonUtils.json2Object(input.getBody(), Order.class);
        logger.log("[Order Service][Create Order] Create Order form {" + order.getFrom() + "} ---> {" + order.getTo() + "} at {" + order.getTravelDate() + "}");
        Response mRes = service.create(order, logger);
        logger.log("[Order Service][Create Order] Success");
        return mRes;
    }
}
