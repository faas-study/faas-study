package faas.study;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import faas.study.service.OrderServiceImpl;
import faas.study.util.Response;
import faas.study.service.OrderService;

import java.util.Date;

public class Handler implements RequestHandler<APIGatewayV2ProxyRequestEvent, Response> {
    private OrderService service = new OrderServiceImpl();

    @Override
    public Response handleRequest(APIGatewayV2ProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();

        String travelDateStr = input.getQueryStringParameters().get("travelDate");
        String trainNumber = input.getQueryStringParameters().get("trainNumber");
        Date travelDate = new Date(Long.parseLong(travelDateStr));
        logger.log("[queryAlreadySoldOrders] - routeId: " + travelDate + " & trainNumber: " + trainNumber);

        Response mRes = service.queryAlreadySoldOrders(travelDate, trainNumber);

        return mRes;
    }
}
