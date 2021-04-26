package fudan.se.service;


import com.amazonaws.services.lambda.runtime.LambdaLogger;
import fudan.se.entity.Assurance;
import fudan.se.entity.AssuranceType;
import fudan.se.repository.AssuranceRepository;
import fudan.se.repository.AssuranceRepositoryImpl;
import fudan.se.util.Response;

import java.util.Random;
import java.util.UUID;


public class AssuranceServiceImpl implements AssuranceService {

    private AssuranceRepository assuranceRepository=new AssuranceRepositoryImpl();

    private final static int NUMBEROFLOOPS = Integer.parseInt(System.getenv().getOrDefault("NUMBEROFLOOPS", "1"));
    private final static int NUMBEROFCOMPUTATION = Integer.parseInt(System.getenv().getOrDefault("NUMBEROFCOMPUTATION", "1000000"));
    //private static final Logger LOGGER = LoggerFactory.getLogger(AssuranceServiceImpl.class);

    @Override
    public Response create(int typeIndex, String orderId, LambdaLogger logger) {
        logger.log("[Assurance Service] " + Thread.currentThread().getName() + " Compute start");
        for (int i = 0; i < NUMBEROFLOOPS; i++) {
            computeTask();
        }
        logger.log("[Assurance Service] " + Thread.currentThread().getName() + " Compute finish");
        Assurance a = new Assurance();
        AssuranceType at = AssuranceType.getTypeByIndex(typeIndex);
        if (a != null) {
           // AssuranceServiceImpl.LOGGER.info("[Assurance-Add&Delete-Service][AddAssurance] Fail.Assurance already exists");
            return new Response<>(0, "Fail.Assurance already exists", null);
        } else if (at == null) {
           // AssuranceServiceImpl.LOGGER.info("[Assurance-Add&Delete-Service][AddAssurance] Fail.Assurance type doesn't exist");
            return new Response<>(0, "Fail.Assurance type doesn't exist", null);
        } else {
            Assurance assurance = new Assurance(UUID.randomUUID(), UUID.fromString(orderId), at);
            assuranceRepository.save(assurance);
           // AssuranceServiceImpl.LOGGER.info("[Assurance-Add&Delete-Service][AddAssurance] Success.");
        }
        return new Response<>(1, "Success", a);

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
