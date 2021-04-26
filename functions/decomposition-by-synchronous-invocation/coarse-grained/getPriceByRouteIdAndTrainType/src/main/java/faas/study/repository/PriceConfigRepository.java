package faas.study.repository;

import faas.study.entity.PriceConfig;

public interface PriceConfigRepository  {

    PriceConfig findByRouteIdAndTrainType(String routeId, String trainType);

}
