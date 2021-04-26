package fudan.se;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import fudan.se.entity.RouteRequest;
import fudan.se.entity.TripInfo;
import fudan.se.service.TravelService;
import fudan.se.service.TravelServiceImpl;
import fudan.se.util.JsonUtils;
import fudan.se.util.Response;

import java.util.ArrayList;


public class Handler implements RequestHandler<APIGatewayV2ProxyRequestEvent, Response> {
    String success = "Success";
    String fail = "Fail";

    private final TravelService service = new TravelServiceImpl();

    @Override
    public Response handleRequest(APIGatewayV2ProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();

        logger.log("[getTripsLeftParallelWorker] - Request Body: " + input.getBody());

        RouteRequest request = JsonUtils.json2Object(input.getBody(), RouteRequest.class);

        logger.log("[getTripsLeftParallelWorker] Route Request:" + request.toString());

        logger.log("Query Routes");

        return service.query(request, context);
    }
}
