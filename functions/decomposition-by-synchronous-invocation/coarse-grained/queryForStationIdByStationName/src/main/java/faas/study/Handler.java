package faas.study;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import faas.study.util.Response;
import faas.study.service.StationService;
import faas.study.service.StationServiceImpl;

public class Handler implements RequestHandler<APIGatewayV2ProxyRequestEvent, Response> {
    private StationService service = new StationServiceImpl();

    @Override
    public Response handleRequest(APIGatewayV2ProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();

        String stationName = input.getQueryStringParameters().get("stationName");
        logger.log("[queryForStationIdByStationName] - stationName: " + stationName);

        Response mRes = service.queryForId(stationName);
        return mRes;
    }
}
