package faas.study;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import faas.study.util.Response;
import faas.study.entity.Travel;
import faas.study.service.BasicService;
import faas.study.service.BasicServiceImpl;
import faas.study.util.JsonUtils;

public class Handler implements RequestHandler<APIGatewayV2ProxyRequestEvent, Response> {
    private BasicService service = new BasicServiceImpl();

    @Override
    public Response handleRequest(APIGatewayV2ProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();

        logger.log("[queryForTravel] - input tavel info: " + input.getBody());
        Travel info = JsonUtils.json2Object(input.getBody(), Travel.class);

        Response mRes = service.queryForTravel(info);
        return mRes;
    }

}
