package fudan.se;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
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

        logger.log("[getTripsLeft] - Request Body: " + input.getBody());

        TripInfo info = JsonUtils.json2Object(input.getBody(), TripInfo.class);

        // pre handle
        if (info.getStartingPlace() == null || info.getStartingPlace().length() == 0 ||
                info.getEndPlace() == null || info.getEndPlace().length() == 0 ||
                info.getDepartureTime() == null) {
            logger.log("[[Travel Query] Fail.Something null.");

            return new Response(0, fail, info);
        }

        logger.log("Query Trips");

        try {
            return service.query(info, context);
        } catch (Exception e) {
            return new Response(0, fail, e.toString());
        }
    }
}
