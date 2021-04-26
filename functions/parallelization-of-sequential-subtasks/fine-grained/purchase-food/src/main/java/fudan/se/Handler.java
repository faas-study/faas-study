package fudan.se;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import fudan.se.entity.FoodOrder;
import fudan.se.service.FoodService;
import fudan.se.util.JsonUtils;
import fudan.se.util.Response;


public class Handler implements RequestHandler<APIGatewayV2ProxyRequestEvent, Response> {
    private final FoodService foodService = new FoodService();

    @Override
    public Response handleRequest(APIGatewayV2ProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("[Food Service] Try to create a food order");
        FoodOrder addFoodOrder = JsonUtils.json2Object(input.getBody(), FoodOrder.class);
        logger.log("[Food Service] addFoodOrder: " + addFoodOrder.toString());
        return foodService.createFoodOrder(addFoodOrder, context);
    }
}
