package fudan.se.service;

import com.amazonaws.services.lambda.runtime.Context;
import fudan.se.entity.Seat;
import fudan.se.util.Response;


public interface SeatService {

    Response distributeSeat(Seat seatRequest, Context context);
}
