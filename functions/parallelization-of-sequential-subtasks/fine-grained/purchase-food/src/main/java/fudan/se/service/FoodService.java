package fudan.se.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import fudan.se.entity.FoodOrder;
import fudan.se.repository.FoodOrderRepository;
import fudan.se.util.Response;

import java.util.Random;
import java.util.UUID;


public class FoodService {
    private final FoodOrderRepository foodOrderRepository = new FoodOrderRepository();

    private final static int NUMBEROFLOOPS = Integer.parseInt(System.getenv().getOrDefault("NUMBEROFLOOPS", "1"));
    private final static int NUMBEROFCOMPUTATION = Integer.parseInt(System.getenv().getOrDefault("NUMBEROFCOMPUTATION", "1000000"));

    public Response createFoodOrder(FoodOrder addFoodOrder, Context context) {
        LambdaLogger logger = context.getLogger();
        FoodOrder fo = foodOrderRepository.findByOrderId(addFoodOrder.getOrderId());
        if (fo != null) {
            context.getLogger().log("[Food Service][CreateFoodOrder] Order Id: " + addFoodOrder.getOrderId() + " has existed");
            return new Response(0, "Order Id has existed", null);
        }
        fo = new FoodOrder();
        fo.setId(UUID.randomUUID());
        fo.setOrderId(addFoodOrder.getOrderId());
        fo.setFoodType(addFoodOrder.getFoodType());
        if (addFoodOrder.getFoodType() == 2) {
            fo.setStationName(addFoodOrder.getStationName());
            fo.setStoreName(addFoodOrder.getStoreName());
        }
        fo.setFoodName(addFoodOrder.getFoodName());
        fo.setPrice(addFoodOrder.getPrice());
        foodOrderRepository.save(fo);
        context.getLogger().log("[Food-Service][AddFoodOrder] Success.");

        logger.log("[Food-Service] " + Thread.currentThread().getName() + " Compute start");
        for (int i = 0; i < NUMBEROFLOOPS; i++) {
            computeTask();
        }
        logger.log("[Food-Service] " + Thread.currentThread().getName() + " Compute finish");

        return new Response<>(1, "Success", null);
    }

    private static void computeTask() {
        Random random = new Random();
        int num = NUMBEROFCOMPUTATION;
        double rd;
        while (num-- > 0) {
            rd = random.nextDouble();
            Math.tan(rd);
        }
    }
}
