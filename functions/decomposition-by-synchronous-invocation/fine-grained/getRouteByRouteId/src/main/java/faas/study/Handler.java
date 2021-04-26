package faas.study;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import faas.study.service.RouteService;
import faas.study.util.Response;

public class Handler implements RequestHandler<APIGatewayV2ProxyRequestEvent, Response> {
    private RouteService routeService = new RouteService();

    @Override
    public Response handleRequest(APIGatewayV2ProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();

        String routeId = input.getQueryStringParameters().get("routeId");
        logger.log("[getRouteByRouteId] - routeId: " + routeId);


        Response mRes = routeService.getRouteById(routeId);

        return mRes;
    }
}
