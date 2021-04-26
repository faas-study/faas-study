package fudan.se.service;

import com.amazonaws.services.lambda.runtime.Context;
import fudan.se.entity.OrderTicketsInfo;
import fudan.se.util.Response;


public interface PreserveService {
    Response preserve(OrderTicketsInfo oti, Context context);
}
