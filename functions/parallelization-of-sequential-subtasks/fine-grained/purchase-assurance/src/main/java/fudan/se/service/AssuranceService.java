package fudan.se.service;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import fudan.se.util.Response;


public interface AssuranceService {

    /**
     * find assurance by type index, order id
     *
     * @param typeIndex type index
     * @param orderId order id
     * @return Response
     */
    Response create(int typeIndex, String orderId, LambdaLogger logger);
}
