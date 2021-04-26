package fudan.se;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import fudan.se.service.AssuranceService;
import fudan.se.service.AssuranceServiceImpl;
import fudan.se.util.Response;


public class Handler implements RequestHandler<APIGatewayV2ProxyRequestEvent, Response> {
    private AssuranceService service = new AssuranceServiceImpl();

    @Override
    public Response handleRequest(APIGatewayV2ProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();

        String typeIndex = input.getQueryStringParameters().get("typeIndex");
        String orderId = input.getQueryStringParameters().get("orderId");
        logger.log("[insertConsignRecord] - typeIndex: " + typeIndex + ", orderId: " + orderId);

        Response mRes = service.create(Integer.parseInt(typeIndex), orderId, logger);

        return mRes;
    }
}


