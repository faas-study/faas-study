package faas.study;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import faas.study.service.PriceService;
import faas.study.service.PriceServiceImpl;
import faas.study.util.Response;

public class Handler implements RequestHandler<APIGatewayV2ProxyRequestEvent, Response> {
    private PriceService service = new PriceServiceImpl();

    @Override
    public Response handleRequest(APIGatewayV2ProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();

        String routeId = input.getQueryStringParameters().get("routeId");
        String trainType = input.getQueryStringParameters().get("trainType");
        logger.log("[getPriceByRouteIdAndTrainType] - routeId: " + routeId + " & trainType: " + trainType);

        Response mRes = service.findByRouteIdAndTrainType(routeId, trainType);

        return mRes;
    }
}
