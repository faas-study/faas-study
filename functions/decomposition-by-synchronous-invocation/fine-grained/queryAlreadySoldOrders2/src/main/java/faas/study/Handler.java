package faas.study;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import faas.study.entity.TicketRequest;
import faas.study.util.Response;
import faas.study.entity.TripResponse;
import faas.study.service.OrderService;
import faas.study.service.OrderServiceImpl;
import faas.study.util.JsonUtils;

import java.util.Map;

public class Handler implements RequestHandler<Response, Response> {
    private OrderService service = new OrderServiceImpl();

    @Override
    public Response handleRequest(Response input, Context context) {
        LambdaLogger logger = context.getLogger();

        Map map= JsonUtils.conveterObject(input.getData(), Map.class);
        TicketRequest ticketRequest=JsonUtils.conveterObject(map.get("ticketRequest"),TicketRequest.class);
        TripResponse tripResponse=JsonUtils.conveterObject(map.get("tripResponse"),TripResponse.class);

        logger.log("[queryAlreadySoldOrders2] - ticketRequest: " + ticketRequest);
        logger.log("[queryAlreadySoldOrders2] - tripResponse: " + tripResponse);

        Response mRes = service.queryAlreadySoldOrders(ticketRequest,tripResponse);

        return mRes;
    }
}
