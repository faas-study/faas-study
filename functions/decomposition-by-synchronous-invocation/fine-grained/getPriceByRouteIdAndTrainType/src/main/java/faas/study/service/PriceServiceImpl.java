package faas.study.service;

import faas.study.repository.PriceConfigRepository;
import faas.study.repository.PriceConfigRepositoryImpl;
import faas.study.entity.PriceConfig;
import faas.study.util.Response;

public class PriceServiceImpl implements PriceService {

    private PriceConfigRepository priceConfigRepository = new PriceConfigRepositoryImpl();

    String noThatConfig = "No that config";

    @Override
    public Response findByRouteIdAndTrainType(String routeId, String trainType) {
        PriceConfig priceConfig = priceConfigRepository.findByRouteIdAndTrainType(routeId, trainType);
        if (priceConfig == null) {
            return new Response<>(0, noThatConfig, routeId + trainType);
        } else {
            return new Response<>(1, "Success", priceConfig);
        }
    }
}
