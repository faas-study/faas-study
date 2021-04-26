package faas.study;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import faas.study.entity.TicketRequest;
import faas.study.service.TravelService;
import faas.study.service.TravelServiceImpl;
import faas.study.util.JsonUtils;
import faas.study.util.Response;

public class Handler implements RequestHandler<APIGatewayV2ProxyRequestEvent, Response> {
    private TravelService service = new TravelServiceImpl();

    @Override
    public Response handleRequest(APIGatewayV2ProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("[getTickets] - Request Body: " + input.getBody());

        TicketRequest ticketRequest = JsonUtils.json2Object(input.getBody(), TicketRequest.class);

        return service.getTickets(ticketRequest);
    }

}
