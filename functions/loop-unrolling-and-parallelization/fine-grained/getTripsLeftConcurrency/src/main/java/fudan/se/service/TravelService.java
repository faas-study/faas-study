package fudan.se.service;

import com.amazonaws.services.lambda.runtime.Context;
import fudan.se.entity.TripInfo;
import fudan.se.util.Response;

public interface TravelService {
    public Response query(TripInfo info, Context context) throws InterruptedException;
}
