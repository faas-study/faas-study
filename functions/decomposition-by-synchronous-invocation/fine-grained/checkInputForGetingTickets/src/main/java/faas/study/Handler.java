package faas.study;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import faas.study.entity.TicketRequest;
import faas.study.util.Response;
import faas.study.service.TravelService;
import faas.study.service.TravelServiceImpl;

public class Handler implements RequestHandler<TicketRequest, Response> {
    private TravelService service = new TravelServiceImpl();

    @Override
    public Response handleRequest(TicketRequest input, Context context) {
        LambdaLogger logger = context.getLogger();

        logger.log("[checkInputForGetingTickets] - input: " + input);

        Response mRes = service.checkInput(input);
        return mRes;

    }
}
