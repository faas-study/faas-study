package faas.study.service;

import faas.study.util.Response;

public interface PriceService {

    Response findByRouteIdAndTrainType(String routeId, String trainType);

}
