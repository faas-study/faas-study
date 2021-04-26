package fudan.se;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import fudan.se.entity.Seat;
import fudan.se.service.SeatService;
import fudan.se.service.SeatServiceImpl;
import fudan.se.util.JsonUtils;
import fudan.se.util.Response;


public class Handler implements RequestHandler<APIGatewayV2ProxyRequestEvent, Response> {
    private SeatService service = new SeatServiceImpl();

    @Override
    public Response handleRequest(APIGatewayV2ProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("[dispatchSeat] - Request Body: " + input.getBody());
        Seat seatRequest = JsonUtils.json2Object(input.getBody(), Seat.class);

        return service.distributeSeat(seatRequest,context);
    }
}
