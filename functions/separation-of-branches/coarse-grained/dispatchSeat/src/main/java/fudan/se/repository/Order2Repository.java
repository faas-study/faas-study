package fudan.se.repository;

import com.amazonaws.services.lambda.runtime.Context;
import fudan.se.entity.Order;

import java.util.ArrayList;
import java.util.Date;


public interface Order2Repository {

    ArrayList<Order> findByTravelDateAndTrainNumber(Date travelDate, String trainNumber, Context context);

}
