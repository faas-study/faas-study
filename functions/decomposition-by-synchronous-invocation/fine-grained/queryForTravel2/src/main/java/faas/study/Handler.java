package faas.study;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import faas.study.entity.TicketRequest;
import faas.study.entity.TripResponse;
import faas.study.service.BasicService;
import faas.study.service.BasicServiceImpl;
import faas.study.util.Response;
import faas.study.util.JsonUtils;

import java.util.Map;

public class Handler implements RequestHandler<Response, Response> {
    private BasicService service = new BasicServiceImpl();

    @Override
    public Response handleRequest(Response input, Context context) {
        LambdaLogger logger = context.getLogger();

        Map map= JsonUtils.conveterObject(input.getData(), Map.class);
        TicketRequest ticketRequest=JsonUtils.conveterObject(map.get("ticketRequest"),TicketRequest.class);
        TripResponse tripResponse=JsonUtils.conveterObject(map.get("tripResponse"),TripResponse.class);

        logger.log("[queryForTravel2] - ticketRequest: " + ticketRequest);
        logger.log("[queryForTravel2] - tripResponse: " + tripResponse);

        Response mRes = service.queryForTravel(ticketRequest,tripResponse);

        return mRes;
    }
}
