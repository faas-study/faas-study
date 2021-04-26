package fudan.se;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.LambdaRuntime;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import fudan.se.entity.OrderTicketsInfo;
import fudan.se.service.PreserveService;
import fudan.se.service.PreserveServiceImpl;
import fudan.se.util.JsonUtils;
import fudan.se.util.Response;


public class Handler implements RequestHandler<APIGatewayV2ProxyRequestEvent, Response> {
    private PreserveService preserveService = new PreserveServiceImpl();
    @Override
    public Response handleRequest(APIGatewayV2ProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("[preserveTickets v2] - Request Body: " + input.getBody());
        OrderTicketsInfo oti = JsonUtils.json2Object(input.getBody(), OrderTicketsInfo.class);
        return preserveService.preserve(oti, context);
    }

}
