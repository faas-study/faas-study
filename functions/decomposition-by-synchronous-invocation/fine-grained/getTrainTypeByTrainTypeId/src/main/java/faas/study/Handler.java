package faas.study;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import faas.study.entity.TrainType;
import faas.study.service.TrainService;
import faas.study.service.TrainServiceImpl;
import faas.study.util.Response;

public class Handler implements RequestHandler<APIGatewayV2ProxyRequestEvent, Response> {
    private TrainService trainService = new TrainServiceImpl();

    @Override
    public Response handleRequest(APIGatewayV2ProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();

        String trainTypeId = input.getQueryStringParameters().get("trainTypeId");
        logger.log("[getTrainTypeByTrainTypeId] - TrainTypeId: " + trainTypeId);

        TrainType trainType = trainService.retrieve(trainTypeId);
        Response mRes;

        if (trainType == null) {
            mRes = new Response(0, "here is no TrainType with the trainType id", trainTypeId);
            logger.log("[getTrainTypeByTrainTypeId] - here is no TrainType with the trainType id: " + trainTypeId);
        } else {
            mRes = new Response(1, "success", trainType);
            logger.log("[getTrainTypeByTrainTypeId] - Succeed finding TrainType:: " + trainTypeId);

        }

        return mRes;
    }
}
